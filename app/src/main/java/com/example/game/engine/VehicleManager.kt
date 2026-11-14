package com.example.game.engine

import com.example.game.model.VehicleType
import kotlin.math.abs

data class VehicleEntity(
  val id: Int,
  val type: VehicleType,
  var x: Float,
  var y: Float,
  var z: Float,
  var yaw: Float = 0f,
  var speed: Float = 0f,
  var steerAngle: Float = 0f,
  var hp: Float = 100f,
  var suspensionBounce: Float = 0f,
  var wheelSpin: Float = 0f,
  var isOccupied: Boolean = false,
)

class VehicleManager(
  private val world: WorldManager,
  private val physics: PhysicsManager,
  private val audio: AudioManager,
) {
  val vehicles = mutableListOf<VehicleEntity>()
  var activeVehicleId: Int? = null
  private var nextId = 1

  init {
    spawnDefaultFleet()
  }

  private fun spawnDefaultFleet() {
    spawn(VehicleType.RICKSHAW, 5f, 8f, 0f)
    spawn(VehicleType.BICYCLE, -5f, 12f, 1.57f)
    spawn(VehicleType.CNG, 6f, -20f, 3.14f)
    spawn(VehicleType.MOTORCYCLE, -6f, -35f, 0f)
    spawn(VehicleType.SCOOTER, -128f, 14f, 1.57f)
    spawn(VehicleType.PRIVATE_CAR, 5f, -128f, 0f)
    spawn(VehicleType.LOCAL_BUS, 138f, -45f, 0f)
    spawn(VehicleType.MINI_TRUCK, 130f, -65f, 1.57f)
    spawn(VehicleType.PICKUP, -130f, -125f, 0f)
    spawn(VehicleType.DELIVERY_VAN, 14f, -110f, -1.57f)
    spawn(VehicleType.BOAT, 0f, 150f, 1.57f)
    spawn(VehicleType.BOAT, -18f, 148f, 0.5f)
  }

  fun spawn(type: VehicleType, x: Float, z: Float, yaw: Float = 0f): VehicleEntity {
    val entity = VehicleEntity(nextId++, type, x, 0f, z, yaw)
    vehicles.add(entity)
    return entity
  }

  fun activeVehicle(): VehicleEntity? = vehicles.find { it.id == activeVehicleId }

  fun findNearestEnterable(px: Float, pz: Float, maxDist: Float = 4.5f): VehicleEntity? {
    var best: VehicleEntity? = null
    var bestD2 = maxDist * maxDist
    for (v in vehicles) {
      val dx = v.x - px
      val dz = v.z - pz
      val d2 = dx * dx + dz * dz
      if (d2 < bestD2) {
        bestD2 = d2
        best = v
      }
    }
    return best
  }

  fun toggleEnterExit(): String? {
    val active = activeVehicle()
    if (active != null) {
      active.isOccupied = false
      activeVehicleId = null
      val sideX = MathUtils.cosf(active.yaw) * 1.8f
      val sideZ = -MathUtils.sinf(active.yaw) * 1.8f
      physics.player.x = active.x + sideX
      physics.player.z = active.z + sideZ
      physics.player.y = active.y + 0.2f
      physics.player.vx = 0f; physics.player.vy = 0f; physics.player.vz = 0f
      return "EXITED:${active.type.titleEn}"
    }
    val nearest = findNearestEnterable(physics.player.x, physics.player.z) ?: return null
    nearest.isOccupied = true
    activeVehicleId = nearest.id
    physics.player.isRagdoll = false
    physics.player.sittingTimer = 0f
    physics.dropGrabbedProp(0f)
    return "ENTERED:${nearest.type.titleEn}"
  }

  fun honk() {
    val v = activeVehicle()
    if (v?.type == VehicleType.RICKSHAW || v?.type == VehicleType.BICYCLE) {
      audio.play(SoundEffect.RICKSHAW_BELL)
    } else {
      audio.play(SoundEffect.HORN)
    }
  }

  fun step(dt: Float, throttleInput: Float, steerInput: Float, isMonsoon: Boolean, maxParticles: Int) {
    for (v in vehicles) {
      if (v.isOccupied) {
        val accel = throttleInput * v.type.accel
        v.speed += accel * dt
        val drag = if (isMonsoon) 0.985f else 0.97f
        v.speed *= drag
        v.speed = v.speed.coerceIn(-v.type.maxSpeed * 0.45f, v.type.maxSpeed)

        val turnFactor = (abs(v.speed) / 4f).coerceIn(0.25f, 1.0f)
        val reverseSign = if (v.speed < -0.5f) -1f else 1f
        v.steerAngle = MathUtils.lerp(v.steerAngle, -steerInput * 0.55f, 10f * dt)
        v.yaw += -steerInput * v.type.turnSpeed * turnFactor * reverseSign * dt
      } else {
        v.speed *= 0.92f
        if (abs(v.speed) < 0.05f) v.speed = 0f
      }

      val fx = MathUtils.sinf(v.yaw)
      val fz = MathUtils.cosf(v.yaw)
      val nextX = (v.x + fx * v.speed * dt).coerceIn(-225f, 225f)
      val nextZ = (v.z + fz * v.speed * dt).coerceIn(-225f, 225f)

      val water = world.isInWater(nextX, nextZ)
      if (v.type.isBoat) {
        if (water != null) {
          v.x = nextX
          v.z = nextZ
          v.y = water.surfaceY + 0.15f + MathUtils.sinf(System.currentTimeMillis() * 0.004f + v.id) * 0.08f
        } else {
          v.speed *= -0.2f
        }
      } else {
        if (water != null) {
          // Land vehicle splashes & stalls slightly in deep water
          v.speed *= 0.85f
        }
        val (resX, resZ) = physics.resolveBuildingCollisions(nextX, v.y, nextZ, 1.25f)
        if (abs(resX - nextX) > 0.05f || abs(resZ - nextZ) > 0.05f) {
          if (abs(v.speed) > 6f) {
            v.hp = (v.hp - abs(v.speed) * 0.8f).coerceAtLeast(15f)
            audio.play(SoundEffect.BANG_RAGDOLL)
            physics.spawnParticles(resX, v.y + 0.8f, resZ, v.type.primaryColor, 6, maxParticles)
          }
          v.speed *= -0.35f
        }
        v.x = resX
        v.z = resZ
        v.y = world.groundHeightAt(v.x, v.y, v.z)
      }

      v.wheelSpin += v.speed * dt * 2.5f
      v.suspensionBounce = MathUtils.sinf(v.wheelSpin * 2f) * (abs(v.speed) / v.type.maxSpeed) * 0.06f

      // Vehicle vs Dynamic Props collision
      if (abs(v.speed) > 1.5f) {
        for (p in physics.props) {
          if (p.isBroken || p.isGrabbed) continue
          val dx = p.x - v.x
          val dz = p.z - v.z
          val d2 = dx * dx + dz * dz
          if (d2 < 2.1f * 2.1f) {
            if (p.kind.breakable && abs(v.speed) > 5f) {
              physics.shatterProp(p, maxParticles)
            } else {
              p.vx += fx * abs(v.speed) * 1.1f
              p.vz += fz * abs(v.speed) * 1.1f
              p.vy += 4.5f
            }
          }
        }
      }

      if (v.isOccupied) {
        physics.player.x = v.x
        physics.player.y = v.y + 0.4f
        physics.player.z = v.z
        physics.player.yaw = v.yaw
      }
    }
  }
}
