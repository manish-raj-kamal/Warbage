package com.warbage.recycling.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.warbage.recycling.data.entity.UserEntity
import com.warbage.recycling.data.repository.UserRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.warbage.recycling.network.RetrofitClient
import com.warbage.recycling.network.BackendUser

class EmailNotVerifiedException(
    val email: String,
    message: String = "Please verify your email address before logging in."
) : Exception(message)

class AuthManager(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val currentDbUser: Flow<UserEntity?> = userRepository.getLastActiveUser()

    val currentFirebaseUser: FirebaseUser?
        get() = auth.currentUser

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Sign in with Email and Password using Firebase Auth, then cache user in Room DB.
     * Enforces that the user's email must be verified before allowing access.
     */
    suspend fun loginWithEmail(email: String, password: String): Result<UserEntity> {
        return try {
            val trimmedEmail = email.trim()
            val existingByEmail = userRepository.getUserByEmail(trimmedEmail)

            val authResult = try {
                auth.signInWithEmailAndPassword(trimmedEmail, password).await()
            } catch (e: Exception) {
                if (existingByEmail?.authProvider == "google") {
                    throw Exception("This account was created via Google Sign-In. Please sign in with Google or use 'Forgot Password' to set an email password.")
                } else {
                    throw e
                }
            }

            val firebaseUser = authResult.user ?: throw Exception("User authentication failed.")

            // Refresh user state from Firebase to get latest verification status
            try {
                firebaseUser.reload().await()
            } catch (_: Exception) {}

            // Require email verification
            if (!firebaseUser.isEmailVerified) {
                userRepository.clearUser()
                throw EmailNotVerifiedException(
                    email = firebaseUser.email ?: trimmedEmail,
                    message = "Please verify your email address before logging in. We've sent a verification link to ${firebaseUser.email ?: trimmedEmail}."
                )
            }

            // Check if user already exists in local DB to retain role
            val existing = userRepository.getUserSync(firebaseUser.uid) ?: existingByEmail
            val role = existing?.role ?: "collector"

            val userEntity = UserEntity(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: email,
                displayName = firebaseUser.displayName ?: existing?.displayName ?: email.substringBefore("@"),
                role = role,
                phoneNumber = firebaseUser.phoneNumber ?: existing?.phoneNumber ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: existing?.photoUrl ?: "",
                authProvider = existing?.authProvider ?: "email"
            )

            userRepository.saveUser(userEntity)
            Result.success(userEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create user with Email and Password using Firebase Auth, set display name, and send verification email.
     * Does NOT save user in local DB yet; the user must verify their email first.
     */
    suspend fun signupWithEmail(
        email: String,
        password: String,
        displayName: String,
        role: String
    ): Result<UserEntity> {
        return try {
            val trimmedEmail = email.trim()
            
            // Check if account already exists locally
            val existingUser = userRepository.getUserByEmail(trimmedEmail)
            if (existingUser != null) {
                if (existingUser.authProvider == "google") {
                    throw Exception("An account with this email already exists via Google Sign-In. Please sign in with Google or reset your password.")
                } else {
                    throw Exception("An account with this email address already exists. Please log in using your password.")
                }
            }

            val authResult = try {
                auth.createUserWithEmailAndPassword(trimmedEmail, password).await()
            } catch (e: Exception) {
                if (e.message?.contains("already in use", ignoreCase = true) == true ||
                    e is com.google.firebase.auth.FirebaseAuthUserCollisionException
                ) {
                    throw Exception("An account with this email address already exists. Please log in instead.")
                } else {
                    throw e
                }
            }

            val firebaseUser = authResult.user ?: throw Exception("User registration failed.")

            // Update Firebase Profile display name
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
            } catch (_: Exception) {}

            // Send Verification Email
            firebaseUser.sendEmailVerification().await()

            // Do not save to local DB or backend yet; user is unverified
            val pendingUser = UserEntity(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: trimmedEmail,
                displayName = displayName,
                role = role,
                phoneNumber = "",
                photoUrl = "",
                authProvider = "email"
            )

            Result.success(pendingUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if the currently authenticated Firebase user has verified their email.
     * If verified, save user to local Room DB, sync to backend MongoDB, and activate session.
     */
    suspend fun verifyEmailAndActivate(displayName: String, role: String): Result<UserEntity> {
        return try {
            val fbUser = auth.currentUser ?: throw Exception("No active session found. Please sign in.")
            fbUser.reload().await()
            if (!fbUser.isEmailVerified) {
                return Result.failure(Exception("Your email has not been verified yet. Please click the verification link in your email and tap this button again."))
            }

            val userEntity = UserEntity(
                uid = fbUser.uid,
                email = fbUser.email ?: "",
                displayName = if (displayName.isNotBlank()) displayName else (fbUser.displayName ?: fbUser.email?.substringBefore("@") ?: "User"),
                role = role,
                phoneNumber = fbUser.phoneNumber ?: "",
                photoUrl = fbUser.photoUrl?.toString() ?: "",
                authProvider = "email"
            )

            userRepository.saveUser(userEntity)

            // Sync user to MongoDB Vercel Backend
            try {
                val backendUser = BackendUser(
                    userId = fbUser.uid,
                    name = userEntity.displayName,
                    email = userEntity.email,
                    phone = userEntity.phoneNumber,
                    role = role.uppercase(),
                    authProvider = "email"
                )
                RetrofitClient.api.createUser(backendUser)
            } catch (e: Exception) {
                android.util.Log.e("AuthManager", "Backend sync error during email verification", e)
            }

            Result.success(userEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Resend verification email to the current logged-in Firebase user, or by signing in temporarily.
     */
    suspend fun resendVerificationEmail(email: String = "", password: String = ""): Result<Unit> {
        return try {
            var fbUser = auth.currentUser
            if (fbUser == null && email.isNotBlank() && password.isNotBlank()) {
                val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
                fbUser = authResult.user
            }
            if (fbUser == null) {
                throw Exception("Unable to resend email. Please try signing in again.")
            }
            fbUser.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Authenticate with Google using Credential Manager API and Firebase GoogleAuthProvider.
     */
    suspend fun signInWithGoogle(
        context: Context,
        webClientId: String = "",
        role: String = "collector"
    ): Result<UserEntity> {
        return try {
            val resolvedWebClientId = if (webClientId.isNotBlank()) {
                webClientId
            } else {
                try {
                    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                    if (resId != 0) context.getString(resId) else "28003673307-gno54bgcafonddlgfot5s4kns15of4gk.apps.googleusercontent.com"
                } catch (_: Exception) {
                    "28003673307-gno54bgcafonddlgfot5s4kns15of4gk.apps.googleusercontent.com"
                }
            }

            val activity = generateSequence(context) {
                (it as? android.content.ContextWrapper)?.baseContext
            }.filterIsInstance<android.app.Activity>().firstOrNull() ?: context

            val credentialManager = CredentialManager.create(activity)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(resolvedWebClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = activity,
                request = request
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val firebaseUser = authResult.user ?: throw Exception("Google authentication failed.")

                val existing = userRepository.getUserSync(firebaseUser.uid)
                val finalRole = existing?.role ?: role

                val userEntity = UserEntity(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: googleIdTokenCredential.displayName ?: "Google User",
                    role = finalRole,
                    phoneNumber = firebaseUser.phoneNumber ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString() ?: googleIdTokenCredential.profilePictureUri?.toString() ?: "",
                    authProvider = "google"
                )

                userRepository.saveUser(userEntity)
                
                // Sync to MongoDB Vercel Backend
                try {
                    val backendUser = BackendUser(
                        userId = firebaseUser.uid,
                        name = userEntity.displayName,
                        email = userEntity.email,
                        phone = userEntity.phoneNumber,
                        role = finalRole.uppercase(),
                        authProvider = "google"
                    )
                    android.util.Log.d("AuthManager", "Syncing google user to backend: $backendUser")
                    val response = RetrofitClient.api.createUser(backendUser)
                    android.util.Log.d("AuthManager", "Backend sync successful: $response")
                } catch (e: Exception) {
                    android.util.Log.e("AuthManager", "Backend sync failed", e)
                }
                
                Result.success(userEntity)
            } else {
                Result.failure(Exception("Unsupported credential type received from Google."))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Google Sign-In was cancelled."))
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Demo accounts for Google Play testers.
     */
    suspend fun signInAsDemoUser(role: String): Result<UserEntity> {
        return try {
            val isRecycler = role.lowercase() == "recycler"
            val demoId = if (isRecycler) "demo_recycler_id" else "demo_collector_id"
            val email = if (isRecycler) "demo.recycler@warbage.eco" else "demo.collector@warbage.eco"
            val name = if (isRecycler) "Demo Recycler" else "Demo Collector"

            val userEntity = UserEntity(
                uid = demoId,
                email = email,
                displayName = name,
                role = if (isRecycler) "recycler" else "collector",
                authProvider = "demo"
            )
            RetrofitClient.demoUserId = demoId
            userRepository.saveUser(userEntity)
            
            // Sync demo user to backend
            try {
                val backendUser = BackendUser(
                    userId = demoId,
                    name = name,
                    email = email,
                    phone = "",
                    role = userEntity.role.uppercase(),
                    authProvider = "demo"
                )
                RetrofitClient.api.createUser(backendUser)
            } catch (e: Exception) {
                android.util.Log.e("AuthManager", "Backend sync failed for demo user", e)
            }
            
            Result.success(userEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Quick Guest / Demo login option for immediate testing even when offline or before Firebase is configured.
     */
    suspend fun signInAsGuest(
        displayName: String = "Eco Collector",
        role: String = "collector"
    ): Result<UserEntity> {
        return signInAsDemoUser(role)
    }

    /**
     * Request Account Deletion with a 7-day grace period.
     */
    suspend fun requestAccountDeletion(uid: String): Result<UserEntity> {
        return try {
            val existing = userRepository.getUserSync(uid) ?: throw Exception("User not found.")
            val updated = existing.copy(
                isPendingDeletion = true,
                deletionRequestedAt = System.currentTimeMillis()
            )
            userRepository.saveUser(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cancel pending Account Deletion.
     */
    suspend fun cancelAccountDeletion(uid: String): Result<UserEntity> {
        return try {
            val existing = userRepository.getUserSync(uid) ?: throw Exception("User not found.")
            val updated = existing.copy(
                isPendingDeletion = false,
                deletionRequestedAt = 0L
            )
            userRepository.saveUser(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Permanently purge user account and all associated data from MongoDB backend, Firebase, and local storage.
     */
    suspend fun permanentlyDeleteAccount(uid: String): Result<Unit> {
        return try {
            // 1. Purge user and unfulfilled lots from Backend MongoDB
            try {
                RetrofitClient.api.deleteUser(uid)
            } catch (e: Exception) {
                android.util.Log.e("AuthManager", "Backend account purge error: ${e.message}")
            }

            // 2. Delete user from Firebase Auth
            currentFirebaseUser?.let { fbUser ->
                if (fbUser.uid == uid) {
                    try {
                        fbUser.delete().await()
                    } catch (e: Exception) {
                        android.util.Log.e("AuthManager", "Firebase Auth account deletion error: ${e.message}")
                    }
                }
            }

            // 3. Clear local session & DB
            logout()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if 7 days have passed since deletion request. If so, purge user completely.
     */
    suspend fun checkAndDeleteExpiredAccount(uid: String): Boolean {
        return try {
            val user = userRepository.getUserSync(uid) ?: return false
            if (user.isPendingDeletion && user.deletionRequestedAt > 0) {
                val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
                if (System.currentTimeMillis() - user.deletionRequestedAt >= sevenDaysMs) {
                    permanentlyDeleteAccount(uid)
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update user profile information in local DB and Firebase (if connected).
     */
    suspend fun updateProfile(
        uid: String,
        displayName: String,
        phoneNumber: String,
        role: String
    ): Result<UserEntity> {
        return try {
            val existing = userRepository.getUserSync(uid) ?: throw Exception("User not found.")
            val updated = existing.copy(
                displayName = displayName,
                phoneNumber = phoneNumber,
                role = role
            )

            currentFirebaseUser?.let { fbUser ->
                if (fbUser.uid == uid) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    fbUser.updateProfile(profileUpdates).await()
                }
            }

            userRepository.saveUser(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send password reset email link via Firebase Auth.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            val trimmedEmail = email.trim()
            if (trimmedEmail.isBlank()) throw Exception("Please enter your email address.")
            auth.sendPasswordResetEmail(trimmedEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out user from Firebase and clear active session.
     */
    suspend fun logout() {
        RetrofitClient.demoUserId = null
        try {
            auth.signOut()
        } catch (_: Exception) {}
        userRepository.clearUser()
    }
}

