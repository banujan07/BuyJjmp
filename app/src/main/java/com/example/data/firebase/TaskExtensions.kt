package com.example.data.firebase

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Await extension for Google Play Services / Firebase Tasks using standard Coroutines.
 */
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            val exception = task.exception ?: RuntimeException("Firebase task failed with unknown error")
            continuation.resumeWithException(exception)
        }
    }
}
