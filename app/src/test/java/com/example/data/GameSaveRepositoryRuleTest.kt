package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSaveRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun saveAndLoadSlot_authenticatedOwner_succeeds() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repo = GameSaveRepository(firestore, auth)
    val slotId = "slot_${UUID.randomUUID().toString().take(8)}"
    val slot =
      SaveSlot(
        slotName = "Old Dhaka Run",
        posX = 14.2,
        posZ = -18.5,
        district = "OLD_DHAKA",
        moneyTaka = 2400L,
        ownedVehicles = listOf("RICKSHAW", "CNG"),
        completedMissions = listOf("m01_ips_war"),
      )

    val saveResult = withTimeout(TIMEOUT_MS) { repo.saveGameSlot(slotId, slot) }
    assertTrue("Expected save success: ${saveResult.exceptionOrNull()}", saveResult.isSuccess)

    val loaded = withTimeout(TIMEOUT_MS) { repo.getSaveSlot(slotId) }
    assertTrue(loaded.isSuccess)
    val loadedSlot = loaded.getOrThrow()
    assertNotNull(loadedSlot)
    assertEquals("Old Dhaka Run", loadedSlot?.slotName)
    assertEquals(2400L, loadedSlot?.moneyTaka)

    val updated = slot.copy(moneyTaka = 3100L, weather = "RAIN")
    val updateResult = withTimeout(TIMEOUT_MS) { repo.saveGameSlot(slotId, updated) }
    assertTrue("Expected update success: ${updateResult.exceptionOrNull()}", updateResult.isSuccess)

    val observed =
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeSaveSlots().first { list -> list.any { it.id == slotId && it.moneyTaka == 3100L } }
      }
    assertTrue(observed.any { it.id == slotId && it.weather == "RAIN" })
  }

  @Test
  fun observeSaveSlots_unauthenticated_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repo = GameSaveRepository(firestore, auth)
    var caught: Exception? = null
    try {
      withTimeout(FLOW_TIMEOUT_MS) { repo.observeSaveSlots().first() }
    } catch (e: Exception) {
      caught = e
    }
    assertNotNull("Expected failure when unauthenticated", caught)
    assertTrue(caught is IllegalStateException || caught?.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
  }

  @Test
  fun crossUserRead_isDeniedBySecurityRules() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = GameSaveRepository(firestore, auth)
    val slotId = "slot_${UUID.randomUUID().toString().take(8)}"
    withTimeout(TIMEOUT_MS) {
      aliceRepo.saveGameSlot(slotId, SaveSlot(slotName = "Alice Private")).getOrThrow()
    }

    signInTestUser(BOB_EMAIL)
    var deniedCode: FirebaseFirestoreException.Code? = null
    try {
      withTimeout(TIMEOUT_MS) {
        firestore
          .collection("users")
          .document(aliceUid)
          .collection("save_slots")
          .document(slotId)
          .get()
          .let { task ->
            val deferred = kotlinx.coroutines.CompletableDeferred<Unit>()
            task.addOnSuccessListener { deferred.complete(Unit) }
            task.addOnFailureListener { deferred.completeExceptionally(it) }
            deferred.await()
          }
      }
    } catch (e: Exception) {
      deniedCode = e.unwrapFirestoreException()?.code
    }
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, deniedCode)
  }

  private companion object {
    const val ALICE_EMAIL = "alice_dhaka@test.com"
    const val BOB_EMAIL = "bob_sylhet@test.com"
    const val TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 3000L
  }
}
