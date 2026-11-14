package com.example.game

import com.example.game.engine.GameSimulation
import com.example.game.model.DistrictId
import com.example.game.model.MissionCatalog
import com.example.game.model.VehicleType
import com.example.game.model.WeatherType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeshiChaosSimulationTest {

  @Test
  fun freeRoamMovement_andDistrictDetection_worksAcrossAll6Districts() {
    val sim = GameSimulation()
    assertEquals(DistrictId.OLD_DHAKA, sim.world.detectDistrict(0f, 0f))
    assertEquals(DistrictId.MODERN_DHAKA, sim.world.detectDistrict(0f, -135f))
    assertEquals(DistrictId.RESIDENTIAL, sim.world.detectDistrict(-135f, 0f))
    assertEquals(DistrictId.VILLAGE, sim.world.detectDistrict(-135f, -135f))
    assertEquals(DistrictId.COMMERCIAL, sim.world.detectDistrict(135f, -45f))
    assertEquals(DistrictId.RIVERSIDE, sim.world.detectDistrict(0f, 135f))

    sim.moveInputY = 1f
    repeat(30) { sim.step(0.03f) }
    assertTrue("Player should move when joystick input is active", sim.physics.player.z < -0.5f)
  }

  @Test
  fun vehicleDriving_enterDriveExit_works() {
    val sim = GameSimulation()
    val rickshaw = sim.vehicles.vehicles.first { it.type == VehicleType.RICKSHAW }
    sim.physics.player.x = rickshaw.x
    sim.physics.player.z = rickshaw.z

    val enterMsg = sim.vehicles.toggleEnterExit()
    assertNotNull(enterMsg)
    assertNotNull(sim.vehicles.activeVehicle())

    sim.moveInputY = 1f
    repeat(30) { sim.step(0.03f) }
    assertTrue(rickshaw.speed > 1f)

    val exitMsg = sim.vehicles.toggleEnterExit()
    assertNotNull(exitMsg)
    assertNull(sim.vehicles.activeVehicle())
  }

  @Test
  fun ragdollPhysics_andWeatherCycle_andSaveRoundTrip_work() {
    val sim = GameSimulation()
    sim.physics.triggerPlayerRagdoll(5f, 6f, 0f)
    assertTrue(sim.physics.player.isRagdoll)
    repeat(80) { sim.step(0.03f) }
    assertFalse("Ragdoll should recover after timer", sim.physics.player.isRagdoll)

    sim.weather.weather = WeatherType.MONSOON
    val initialHour = sim.weather.timeOfDayHours
    repeat(60) { sim.step(0.05f) }
    assertTrue(sim.weather.timeOfDayHours != initialHour)

    val slot = sim.toSaveSlot("Test Slot")
    val sim2 = GameSimulation()
    sim2.loadFromSaveSlot(slot)
    assertEquals(sim.moneyTaka, sim2.moneyTaka)
    assertEquals(WeatherType.MONSOON, sim2.weather.weather)
  }

  @Test
  fun optionalMission_completionAwardsTaka() {
    val sim = GameSimulation()
    val m = MissionCatalog.missions.first()
    val beforeTaka = sim.moneyTaka
    sim.startMission(m)
    assertEquals(m.id, sim.activeMission?.id)

    // Move player to mission target
    sim.physics.player.x = m.targetX
    sim.physics.player.z = m.targetZ
    sim.step(0.03f)
    assertNull(sim.activeMission)
    assertTrue(sim.completedMissions.contains(m.id))
    assertEquals(beforeTaka + m.rewardTaka, sim.moneyTaka)
  }
}
