package com.example.game.engine

import com.example.game.model.PropKind
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

data class DynamicProp(
  val id: Int,
  val kind: PropKind,
  var x: Float,
  var y: Float,
  var z: Float,
  var vx: Float = 0f,
  var vy: Float = 0f,
  var vz: Float = 0f,
  var rotY: Float = 0f,
  var rotX: Float = 0f,
  var angVel: Float = 0f,
  var isBroken: Boolean = false,
  var isGrabbed: Boolean = false,
  var respawnTimer: Float = 0f,
  val spawnX: Float = x,
  val spawnZ: Float = z,
)

data class DebrisParticle(
  var x: Float,
  var y: Float,
  var z: Float,
  var vx: Float,
  var vy: Float,
  var vz: Float,
  var life: Float,
  val colorArgb: Int,
  val size: Float,
)

data class PlayerPhysicsState(
  var x: Float = 0f,
  var y: Float = 0f,
  var z: Float = 0f,
  var vx: Float = 0f,
  var vy: Float = 0f,
  var vz: Float = 0f,
  var yaw: Float = 0f,
  var isGrounded: Boolean = true,
  var isCrouching: Boolean = false,
  var isSprinting: Boolean = false,
  var isSwimming: Boolean = false,
  var isRagdoll: Boolean = false,
  var ragdollTimer: Float = 0f,
  var ragdollRoll: Float = 0f,
  var ragdollPitch: Float = 0f,
  var walkPhase: Float = 0f,
  var grabbedPropId: Int? = null,
  var sittingTimer: Float = 0f,
)

class PhysicsManager(private val world: WorldManager, private val audio: AudioManager) {
  val player = PlayerPhysicsState()
  val props = mutableListOf<DynamicProp>()
  val particles = mutableListOf<DebrisParticle>()
  var brokenPropCount = 0
  var lastChaosImpulse = 0

  init {
    spawnInitialProps()
  }

  private fun spawnInitialProps() {
    var nextId = 1
    fun add(kind: PropKind, x: Float, z: Float) {
      props.add(DynamicProp(nextId++, kind, x, kind.halfSize, z))
    }
    // Old Dhaka breakables & props
    add(PropKind.PLASTIC_CHAIR, -9f, 5f)
    add(PropKind.PLASTIC_CHAIR, -11f, 7f)
    add(PropKind.WOODEN_TABLE, -8f, 6f)
    add(PropKind.CLAY_POT, -12f, 22f)
    add(PropKind.CLAY_POT, 14f, 26f)
    add(PropKind.CLAY_POT, -5f, 29f)
    add(PropKind.CLAY_POT, 18f, 18f)
    add(PropKind.GLASS_BOTTLE, 10f, 11f)
    add(PropKind.GLASS_BOTTLE, 11f, 11.5f)
    add(PropKind.CARDBOARD_BOX, 15f, -14f)
    add(PropKind.CARDBOARD_BOX, 15f, -13f)
    add(PropKind.BUCKET, -14f, 15f)
    add(PropKind.SLIPPER, -6f, -11f)
    add(PropKind.STREET_SIGN, 7.5f, -35f)
    // Residential football & cricket
    add(PropKind.FOOTBALL_PROP, -126f, 8f)
    add(PropKind.CRICKET_BALL, -115f, -20f)
    add(PropKind.PLASTIC_CHAIR, -148f, 16f)
    // Commercial boxes & bottles
    for (i in 0..5) {
      add(PropKind.CARDBOARD_BOX, 134f + (i % 3) * 1.4f, -45f + (i / 3) * 1.6f)
    }
    add(PropKind.STREET_SIGN, 128f, -30f)
    // Village pots & buckets
    add(PropKind.CLAY_POT, -138f, -130f)
    add(PropKind.BUCKET, -144f, -128f)
    // Riverside props
    add(PropKind.BUCKET, 2f, 138f)
    add(PropKind.WOODEN_TABLE, 16f, 112f)
    add(PropKind.PLASTIC_CHAIR, 17f, 113f)
  }

  fun triggerPlayerRagdoll(impulseX: Float, impulseY: Float, impulseZ: Float) {
    if (!player.isRagdoll) {
      audio.play(SoundEffect.BANG_RAGDOLL)
    }
    player.isRagdoll = true
    player.ragdollTimer = 2.0f
    player.vx = impulseX
    player.vy = impulseY.coerceAtLeast(4.5f)
    player.vz = impulseZ
    player.isGrounded = false
    dropGrabbedProp(throwForce = 0f)
  }

  fun jump() {
    if (player.isRagdoll) return
    if (player.isGrounded || player.isSwimming) {
      player.vy = if (player.isSwimming) 5.5f else 8.6f
      player.isGrounded = false
      player.sittingTimer = 0f
      audio.play(SoundEffect.JUMP)
    }
  }

  fun grabOrThrowNearestProp(): String? {
    val currentId = player.grabbedPropId
    if (currentId != null) {
      dropGrabbedProp(throwForce = 16f)
      return "THREW"
    }
    var best: DynamicProp? = null
    var bestDist = 3.2f * 3.2f
    for (p in props) {
      if (p.isBroken) continue
      val dx = p.x - player.x
      val dz = p.z - player.z
      val d2 = dx * dx + dz * dz
      if (d2 < bestDist) {
        bestDist = d2
        best = p
      }
    }
    if (best != null) {
      best.isGrabbed = true
      player.grabbedPropId = best.id
      return "GRABBED:${best.kind.labelEn}"
    }
    return null
  }

  fun dropGrabbedProp(throwForce: Float) {
    val id = player.grabbedPropId ?: return
    player.grabbedPropId = null
    val prop = props.find { it.id == id } ?: return
    prop.isGrabbed = false
    if (throwForce > 0f) {
      val fx = MathUtils.sinf(player.yaw)
      val fz = MathUtils.cosf(player.yaw)
      prop.vx = fx * throwForce + player.vx * 0.4f
      prop.vy = 5.5f
      prop.vz = fz * throwForce + player.vz * 0.4f
      prop.angVel = 9f
      audio.play(SoundEffect.JUMP)
    }
  }

  fun step(
    dt: Float,
    moveInputX: Float,
    moveInputY: Float,
    cameraYaw: Float,
    inVehicle: Boolean,
    isMonsoon: Boolean,
    maxParticles: Int,
  ) {
    lastChaosImpulse = 0

    // 1. Hanging rope pendulum simulation
    val wind = MathUtils.sinf(System.currentTimeMillis() * 0.002f) * (if (isMonsoon) 2.2f else 0.6f)
    for (rope in world.ropes) {
      val accel = -6.5f * rope.swingAngle + wind
      rope.swingVel = (rope.swingVel + accel * dt) * 0.985f
      rope.swingAngle += rope.swingVel * dt
    }

    // 2. Player physics (when on foot)
    if (!inVehicle) {
      if (player.sittingTimer > 0f) {
        player.sittingTimer -= dt
        if (abs(moveInputX) > 0.2f || abs(moveInputY) > 0.2f) {
          player.sittingTimer = 0f
        }
      }

      val water = world.isInWater(player.x, player.z)
      player.isSwimming = water != null && player.y <= water.surfaceY + 0.35f

      if (player.isRagdoll) {
        player.ragdollTimer -= dt
        player.ragdollPitch += 8.5f * dt
        player.ragdollRoll += 6.2f * dt
        player.vy -= 20f * dt
        player.x += player.vx * dt
        player.y += player.vy * dt
        player.z += player.vz * dt
        val groundY = world.groundHeightAt(player.x, player.y, player.z)
        if (player.y <= groundY) {
          player.y = groundY
          if (player.vy < -4f) {
            player.vy = -player.vy * 0.35f
            audio.play(SoundEffect.BANG_RAGDOLL)
          } else {
            player.vy = 0f
          }
          player.vx *= 0.86f
          player.vz *= 0.86f
          player.isGrounded = true
        }
        if (player.ragdollTimer <= 0f) {
          player.isRagdoll = false
          player.ragdollPitch = 0f
          player.ragdollRoll = 0f
        }
      } else {
        // Responsive movement relative to camera yaw
        val sinC = MathUtils.sinf(cameraYaw)
        val cosC = MathUtils.cosf(cameraYaw)
        val wishX = moveInputX * cosC - moveInputY * sinC
        val wishZ = -moveInputX * sinC - moveInputY * cosC
        val mag = sqrt(wishX * wishX + wishZ * wishZ).coerceAtMost(1f)

        val speed = when {
          player.isSwimming -> 4.8f
          player.isCrouching -> 3.4f
          player.isSprinting -> 11.2f
          else -> 6.6f
        }

        val slippery = isMonsoon || world.isSlipperyAt(player.x, player.z)
        val accelRate = if (slippery) 4.2f else 14f

        if (mag > 0.05f) {
          val nx = wishX / mag
          val nz = wishZ / mag
          val targetVx = nx * speed * mag
          val targetVz = nz * speed * mag
          player.vx = MathUtils.lerp(player.vx, targetVx, accelRate * dt)
          player.vz = MathUtils.lerp(player.vz, targetVz, accelRate * dt)
          val targetYaw = kotlin.math.atan2(nx, nz)
          player.yaw = MathUtils.lerpAngle(player.yaw, targetYaw, 12f * dt)

          val prevPhase = player.walkPhase
          player.walkPhase += dt * (if (player.isSprinting) 13f else 8.5f) * mag
          if (player.isGrounded && !player.isSwimming && (prevPhase.toInt() / 3 != player.walkPhase.toInt() / 3)) {
            audio.play(SoundEffect.FOOTSTEP)
          }
        } else {
          val friction = if (slippery) 0.94f else 0.80f
          player.vx *= friction
          player.vz *= friction
        }

        if (player.isSwimming && water != null) {
          val targetWaterY = water.surfaceY - 0.15f
          player.vy = (targetWaterY - player.y) * 5f - 0.5f
        } else {
          player.vy -= 22f * dt
        }

        val nextX = (player.x + player.vx * dt).coerceIn(-225f, 225f)
        val nextZ = (player.z + player.vz * dt).coerceIn(-225f, 225f)
        val resolved = resolveBuildingCollisions(nextX, player.y, nextZ, 0.45f)
        player.x = resolved.first
        player.z = resolved.second
        player.y += player.vy * dt

        val groundY = world.groundHeightAt(player.x, player.y, player.z)
        if (player.y <= groundY) {
          if (!player.isGrounded && player.vy < -14f) {
            // Hard landing triggers funny ragdoll tumble!
            triggerPlayerRagdoll(player.vx * 0.5f, 3.5f, player.vz * 0.5f)
          } else {
            if (!player.isGrounded && water != null) {
              audio.play(SoundEffect.SPLASH)
              spawnParticles(player.x, water.surfaceY, player.z, 0xFF38BDF8.toInt(), 8, maxParticles)
            }
            player.y = groundY
            player.vy = 0f
            player.isGrounded = true
          }
        } else if (player.y > groundY + 0.25f) {
          player.isGrounded = false
        }
      }
    }

    // 3. Dynamic Props Physics, Grabbing & Destruction
    for (p in props) {
      if (p.isBroken) {
        p.respawnTimer -= dt
        if (p.respawnTimer <= 0f) {
          p.isBroken = false
          p.x = p.spawnX
          p.z = p.spawnZ
          p.y = p.kind.halfSize + 0.5f
          p.vx = 0f; p.vy = 0f; p.vz = 0f; p.rotX = 0f; p.rotY = 0f
        }
        continue
      }

      if (p.isGrabbed) {
        val fx = MathUtils.sinf(player.yaw)
        val fz = MathUtils.cosf(player.yaw)
        p.x = player.x + fx * 1.25f
        p.y = player.y + 1.15f
        p.z = player.z + fz * 1.25f
        p.rotY = player.yaw
        p.vx = 0f; p.vy = 0f; p.vz = 0f
        continue
      }

      // Push prop when player walks into it
      if (!inVehicle) {
        val dx = p.x - player.x
        val dz = p.z - player.z
        val distSq = dx * dx + dz * dz
        val minDist = p.kind.halfSize + 0.5f
        if (distSq < minDist * minDist && abs(p.y - (player.y + 0.5f)) < 1.2f && distSq > 0.0001f) {
          val dist = sqrt(distSq)
          val nx = dx / dist
          val nz = dz / dist
          val pushSpeed = if (p.kind == PropKind.FOOTBALL_PROP || p.kind == PropKind.CRICKET_BALL) 9f else 3.8f
          p.vx += nx * pushSpeed
          p.vz += nz * pushSpeed
          p.vy += 1.2f
        }
      }

      p.vy -= 18f * dt
      p.x += p.vx * dt
      p.y += p.vy * dt
      p.z += p.vz * dt
      p.rotY += p.angVel * dt

      val groundY = world.groundHeightAt(p.x, p.y, p.z) + p.kind.halfSize
      if (p.y <= groundY) {
        val impactSpeed = abs(p.vy) + sqrt(p.vx * p.vx + p.vz * p.vz)
        if (p.kind.breakable && impactSpeed > 11.5f) {
          shatterProp(p, maxParticles)
          continue
        }
        p.y = groundY
        val bounce = if (p.kind == PropKind.FOOTBALL_PROP || p.kind == PropKind.CRICKET_BALL) 0.65f else 0.25f
        if (abs(p.vy) > 1.2f) p.vy = -p.vy * bounce else p.vy = 0f
        p.vx *= 0.88f
        p.vz *= 0.88f
        p.angVel *= 0.85f
      }
    }

    // 4. Debris Particles
    val iter = particles.iterator()
    while (iter.hasNext()) {
      val pt = iter.next()
      pt.life -= dt
      if (pt.life <= 0f) {
        iter.remove()
        continue
      }
      pt.vy -= 16f * dt
      pt.x += pt.vx * dt
      pt.y += pt.vy * dt
      pt.z += pt.vz * dt
      if (pt.y < 0.05f) {
        pt.y = 0.05f
        pt.vy = -pt.vy * 0.3f
      }
    }
  }

  fun shatterProp(p: DynamicProp, maxParticles: Int) {
    if (p.isBroken) return
    p.isBroken = true
    p.respawnTimer = 25f
    brokenPropCount++
    lastChaosImpulse++
    audio.play(SoundEffect.SHATTER)
    spawnParticles(p.x, p.y, p.z, p.kind.colorArgb, 12, maxParticles)
  }

  fun spawnParticles(x: Float, y: Float, z: Float, colorArgb: Int, count: Int, maxParticles: Int) {
    for (i in 0 until count) {
      if (particles.size >= maxParticles) break
      particles.add(
        DebrisParticle(
          x = x,
          y = y,
          z = z,
          vx = (Random.nextFloat() - 0.5f) * 8f,
          vy = 2f + Random.nextFloat() * 6f,
          vz = (Random.nextFloat() - 0.5f) * 8f,
          life = 0.7f + Random.nextFloat() * 0.7f,
          colorArgb = colorArgb,
          size = 0.10f + Random.nextFloat() * 0.14f,
        )
      )
    }
  }

  fun resolveBuildingCollisions(x: Float, y: Float, z: Float, radius: Float): Pair<Float, Float> {
    var rx = x
    var rz = z
    for (c in world.colliders) {
      if (y >= c.maxY - 0.35f) continue // On top of walkable roof!
      if (rx + radius > c.minX && rx - radius < c.maxX && rz + radius > c.minZ && rz - radius < c.maxZ) {
        val penLeft = (rx + radius) - c.minX
        val penRight = c.maxX - (rx - radius)
        val penFront = (rz + radius) - c.minZ
        val penBack = c.maxZ - (rz - radius)
        val minPen = minOf(penLeft, penRight, penFront, penBack)
        when (minPen) {
          penLeft -> rx = c.minX - radius
          penRight -> rx = c.maxX + radius
          penFront -> rz = c.minZ - radius
          else -> rz = c.maxZ + radius
        }
      }
    }
    return rx to rz
  }
}
