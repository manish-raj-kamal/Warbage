package com.example.warbage.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.warbage.data.entity.UserEntity
import com.example.warbage.data.repository.UserRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.example.warbage.network.RetrofitClient
import com.example.warbage.network.BackendUser

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
     */
    suspend fun loginWithEmail(email: String, password: String): Result<UserEntity> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = authResult.user ?: throw Exception("User authentication failed.")

            // Check if user already exists in local DB to retain role
            val existing = userRepository.getUserSync(firebaseUser.uid)
            val role = existing?.role ?: "collector"

            val userEntity = UserEntity(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: email,
                displayName = firebaseUser.displayName ?: existing?.displayName ?: email.substringBefore("@"),
                role = role,
                phoneNumber = firebaseUser.phoneNumber ?: existing?.phoneNumber ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: existing?.photoUrl ?: "",
                authProvider = "email"
            )

            userRepository.saveUser(userEntity)
            Result.success(userEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create user with Email and Password using Firebase Auth, set display name, and store in Room DB.
     */
    suspend fun signupWithEmail(
        email: String,
        password: String,
        displayName: String,
        role: String
    ): Result<UserEntity> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = authResult.user ?: throw Exception("User registration failed.")

            // Update Firebase Profile display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            val userEntity = UserEntity(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: email,
                displayName = displayName,
                role = role,
                phoneNumber = "",
                photoUrl = "",
                authProvider = "email"
            )

            userRepository.saveUser(userEntity)
            
            // Sync to MongoDB Vercel Backend
            try {
                val backendUser = BackendUser(
                    userId = firebaseUser.uid,
                    name = displayName,
                    email = firebaseUser.email ?: email,
                    phone = "",
                    role = role.uppercase(),
                    authProvider = "email"
                )
                android.util.Log.d("AuthManager", "Syncing new email user to backend: $backendUser")
                val response = RetrofitClient.api.createUser(backendUser)
                android.util.Log.d("AuthManager", "Backend sync successful: $response")
            } catch (e: Exception) {
                android.util.Log.e("AuthManager", "Backend sync failed", e)
            }
            
            Result.success(userEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Authenticate with Google using Credential Manager API and Firebase GoogleAuthProvider.
     */
    suspend fun signInWithGoogle(
        context: Context,
        webClientId: String,
        role: String = "collector"
    ): Result<UserEntity> {
        return try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = context,
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
     * Quick Guest / Demo login option for immediate testing even when offline or before Firebase is configured.
     */
    suspend fun signInAsGuest(
        displayName: String = "Eco Collector",
        role: String = "collector"
    ): Result<UserEntity> {
        return try {
            val guestId = "guest_${UUID.randomUUID().toString().take(8)}"
            val userEntity = UserEntity(
                uid = guestId,
                email = "guest@warbage.eco",
                displayName = displayName,
                role = role,
                authProvider = "guest"
            )
            userRepository.saveUser(userEntity)
            Result.success(userEntity)
        } catch (e: Exception) {
            Result.failure(e)
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
     * Sign out user from Firebase and clear active session.
     */
    suspend fun logout() {
        try {
            auth.signOut()
        } catch (_: Exception) {}
        userRepository.clearUser()
    }
}

