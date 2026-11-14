package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class GameSaveRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(
    context: Context
  ) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    ),
    Firebase.auth,
  )

  private fun requireUserId(): String =
    auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

  fun observeUserProfile(): Flow<UserProfile?> = flow {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    emitAll(
      docRef
        .snapshots()
        .map { snapshot ->
          snapshot.toObject(
            UserProfile::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
          )
        }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path)
          }
          throw error
        }
    )
  }

  suspend fun ensureUserProfile(displayName: String): Result<Unit> {
    val uid =
      try {
        requireUserId()
      } catch (e: Exception) {
        return Result.failure(e)
      }
    val docRef = db.collection("users").document(uid)
    return try {
      val existing = docRef.get().await()
      if (!existing.exists()) {
        val profile = UserProfile(userId = uid, displayName = displayName)
        docRef.set(profile.toCreateMap(uid)).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun updateUserPreferences(language: String, graphicsQuality: String): Result<Unit> {
    val uid =
      try {
        requireUserId()
      } catch (e: Exception) {
        return Result.failure(e)
      }
    val docRef = db.collection("users").document(uid)
    return try {
      docRef
        .update(
          mapOf(
            "language" to language,
            "graphicsQuality" to graphicsQuality,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  fun observeSaveSlots(): Flow<List<SaveSlot>> = flow {
    val uid = requireUserId()
    val colRef = db.collection("users").document(uid).collection("save_slots")
    emitAll(
      colRef
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(
            SaveSlot::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
          )
        }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path)
          }
          throw error
        }
    )
  }

  suspend fun getSaveSlot(slotId: String): Result<SaveSlot?> {
    val uid =
      try {
        requireUserId()
      } catch (e: Exception) {
        return Result.failure(e)
      }
    val docRef = db.collection("users").document(uid).collection("save_slots").document(slotId)
    return try {
      val snap = docRef.get().await()
      val slot =
        snap.toObject(SaveSlot::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
      Result.success(slot)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun saveGameSlot(slotId: String, slot: SaveSlot): Result<Unit> {
    val uid =
      try {
        requireUserId()
      } catch (e: Exception) {
        return Result.failure(e)
      }
    val docRef = db.collection("users").document(uid).collection("save_slots").document(slotId)
    return try {
      val existing = docRef.get().await()
      if (!existing.exists()) {
        docRef.set(slot.toCreateMap(uid)).await()
      } else {
        docRef.update(slot.toUpdateMap()).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun deleteSaveSlot(slotId: String): Result<Unit> {
    val uid =
      try {
        requireUserId()
      } catch (e: Exception) {
        return Result.failure(e)
      }
    val docRef = db.collection("users").document(uid).collection("save_slots").document(slotId)
    return try {
      docRef.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      Result.failure(e)
    }
  }
}
