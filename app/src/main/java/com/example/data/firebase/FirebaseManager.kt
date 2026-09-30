package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseManager {
    private const val TAG = "BuyJumpFirebase"

    @Volatile
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return

        synchronized(this) {
            if (isInitialized) return
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context)
                    Log.d(TAG, "FirebaseApp initialized successfully")
                }
                isInitialized = true
            } catch (e: Exception) {
                Log.w(TAG, "FirebaseApp not auto-initialized (google-services.json not configured): ${e.message}")
            }
        }
    }

    val isFirebaseAvailable: Boolean
        get() = try {
            FirebaseApp.getInstance() != null
        } catch (e: Exception) {
            false
        }

    val auth: FirebaseAuth?
        get() = if (isFirebaseAvailable) {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Exception) {
                Log.w(TAG, "FirebaseAuth unavailable: ${e.message}")
                null
            }
        } else null

    val firestore: FirebaseFirestore?
        get() = if (isFirebaseAvailable) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.w(TAG, "FirebaseFirestore unavailable: ${e.message}")
                null
            }
        } else null

    val storage: FirebaseStorage?
        get() = if (isFirebaseAvailable) {
            try {
                FirebaseStorage.getInstance()
            } catch (e: Exception) {
                Log.w(TAG, "FirebaseStorage unavailable: ${e.message}")
                null
            }
        } else null
}
