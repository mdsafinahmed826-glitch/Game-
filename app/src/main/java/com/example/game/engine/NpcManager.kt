package com.example.game.engine

import com.example.game.model.AppLang
import com.example.game.model.DistrictId
import com.example.game.model.Localization
import com.example.game.model.NpcRole
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.random.Random

enum class NpcMood { CALM, CHATTING, PANICKED, ANGRY_CHASE, RAGDOLL }

data class NpcEntity(
  val id: Int,
  val role: NpcRole,
  val district: DistrictId,
  var x: Float,
  var y: Float,
  var z: Float,
  var vx: Float = 0f,
  var vy: Float = 0f,
  var vz: Float = 0f,
  var yaw: Float = 0f,
  var homeX: Float = x,
  var homeZ: Float = z,
  var targetX: Float = x,
  var targetZ: Float = z,
  var mood: NpcMood = NpcMood.CALM,
  var stateTimer: Float = 0f,
  var walkPhase: Float = Random.nextFloat() * 6.28f,
  var ragdollRoll: Float = 0f,
  var ragdollPitch: Float = 0f,
  var bubbleTextEn: String = "",
  var bubbleTextBn: String = "",
  var bubbleTimer: Float = 0f,
)

class NpcManager(
  private val world: WorldManager,
  private val physics: PhysicsManager,
  private val audio: AudioManager,
) {
  val npcs = mutableListOf<NpcEntity>()
  var chaosPoints: Float = 0f
  var wantedStars: Int = 0
  var activeSubtitle: String = ""
  var subtitleTimer: Float = 0f
  private var nextId = 1

  init {
    spawnCityPopulation()
  }

  private fun spawnCityPopulation() {
    fun add(role: NpcRole, d: DistrictId, x: Float, z: Float) {
      npcs.add(NpcEntity(nextId++, role, d, x, 0f, z, targetX = x, targetZ = z))
    }
    // Old Dhaka
    add(NpcRole.TEA_OWNER, DistrictId.OLD_DHAKA, -8f, 3f)
    add(NpcRole.SHOPKEEPER, DistrictId.OLD_DHAKA, 13f, -13f)
    add(NpcRole.RICKSHAW_DRIVER, DistrictId.OLD_DHAKA, 5f, 12f)
    add(NpcRole.VENDOR, DistrictId.OLD_DHAKA, 9f, 9f)
    add(NpcRole.PEDESTRIAN, DistrictId.OLD_DHAKA, -4f, -15f)
    add(NpcRole.PEDESTRIAN, DistrictId.OLD_DHAKA, 12f, 20f)
    add(NpcRole.POLICE, DistrictId.OLD_DHAKA, 0f, -25f)
    add(NpcRole.DOG, DistrictId.OLD_DHAKA, -14f, 12f)
    add(NpcRole.GOAT, DistrictId.OLD_DHAKA, 16f, 25f)

    // Modern Dhaka
    add(NpcRole.STUDENT, DistrictId.MODERN_DHAKA, 4f, -118f)
    add(NpcRole.STUDENT, DistrictId.MODERN_DHAKA, -6f, -125f)
    add(NpcRole.TEACHER, DistrictId.MODERN_DHAKA, 12f, -115f)
    add(NpcRole.POLICE, DistrictId.MODERN_DHAKA, -10f, -138f)
    add(NpcRole.DELIVERY_WORKER, DistrictId.MODERN_DHAKA, 8f, -145f)
    add(NpcRole.PEDESTRIAN, DistrictId.MODERN_DHAKA, -16f, -130f)

    // Residential Area
    add(NpcRole.NEIGHBOR, DistrictId.RESIDENTIAL, -142f, 16f)
    add(NpcRole.CHILD, DistrictId.RESIDENTIAL, -125f, 6f)
    add(NpcRole.CHILD, DistrictId.RESIDENTIAL, -129f, 10f)
    add(NpcRole.PEDESTRIAN, DistrictId.RESIDENTIAL, -135f, -12f)
    add(NpcRole.DOG, DistrictId.RESIDENTIAL, -122f, -20f)

    // Village Area
    add(NpcRole.COW, DistrictId.VILLAGE, -135f, -132f)
    add(NpcRole.GOAT, DistrictId.VILLAGE, -148f, -140f)
    add(NpcRole.CHICKEN, DistrictId.VILLAGE, -128f, -124f)
    add(NpcRole.CHICKEN, DistrictId.VILLAGE, -131f, -120f)
    add(NpcRole.VENDOR, DistrictId.VILLAGE, -124f, -126f)
    add(NpcRole.PEDESTRIAN, DistrictId.VILLAGE, -140f, -118f)

    // Commercial Area
    add(NpcRole.BUS_DRIVER, DistrictId.COMMERCIAL, 140f, -40f)
    add(NpcRole.SHOPKEEPER, DistrictId.COMMERCIAL, 125f, -55f)
    add(NpcRole.DELIVERY_WORKER, DistrictId.COMMERCIAL, 136f, -25f)
    add(NpcRole.CHICKEN, DistrictId.COMMERCIAL, 138f, -35f)
    add(NpcRole.POLICE, DistrictId.COMMERCIAL, 125f, -30f)

    // Riverside
    add(NpcRole.VENDOR, DistrictId.RIVERSIDE, 16f, 110f)
    add(NpcRole.PEDESTRIAN, DistrictId.RIVERSIDE, -10f, 122f)
    add(NpcRole.RICKSHAW_DRIVER, DistrictId.RIVERSIDE, 6f, 118f)
  }

  fun addChaos(points: Float, reasonEn: String, reasonBn: String, lang: AppLang) {
    chaosPoints = (chaosPoints + points).coerceAtMost(100f)
    wantedStars = when {
      chaosPoints >= 80f -> 5
      chaosPoints >= 60f -> 4
      chaosPoints >= 40f -> 3
      chaosPoints >= 20f -> 2
      chaosPoints >= 8f -> 1
      else -> 0
    }
    if (wantedStars > 0) {
      audio.play(SoundEffect.POLICE_WHISTLE)
    }
    activeSubtitle = Localization.pick(lang, reasonEn, reasonBn)
    subtitleTimer = 3.2f
  }

  fun reactToHorn(px: Float, pz: Float, lang: AppLang) {
    for (n in npcs) {
      val dx = n.x - px
      val dz = n.z - pz
      if (dx * dx + dz * dz < 16f * 16f) {
        if (n.role == NpcRole.DOG) audio.play(SoundEffect.BARK)
        if (n.role == NpcRole.GOAT) audio.play(SoundEffect.GOAT_BLEAT)
        if (n.mood == NpcMood.CALM) {
          n.mood = NpcMood.PANICKED
          n.stateTimer = 2.5f
          val idx = Random.nextInt(Localization.npcLinesEn.size)
          n.bubbleTextEn = Localization.npcLinesEn[idx]
          n.bubbleTextBn = Localization.npcLinesBn[idx]
          n.bubbleTimer = 3f
          activeSubtitle = Localization.pick(lang, n.bubbleTextEn, n.bubbleTextBn)
          subtitleTimer = 2.5f
        }
      }
    }
  }

  fun findNearestNpc(px: Float, pz: Float, maxDist: Float = 3.8f): NpcEntity? {
    var best: NpcEntity? = null
    var bestD2 = maxDist * maxDist
    for (n in npcs) {
      val dx = n.x - px
      val dz = n.z - pz
      val d2 = dx * dx + dz * dz
      if (d2 < bestD2) {
        bestD2 = d2
        best = n
      }
    }
    return best
  }

  fun talkToNpc(n: NpcEntity, lang: AppLang): String {
    val line = if (n.role == NpcRole.POLICE && wantedStars > 0) {
      val i = Random.nextInt(Localization.policeLinesEn.size)
      Localization.pick(lang, Localization.policeLinesEn[i], Localization.policeLinesBn[i])
    } else {
      val i = Random.nextInt(Localization.npcLinesEn.size)
      Localization.pick(lang, Localization.npcLinesEn[i], Localization.npcLinesBn[i])
    }
    n.bubbleTextEn = line
    n.bubbleTextBn = line
    n.bubbleTimer = 3.5f
    activeSubtitle = "${Localization.pick(lang, n.role.titleEn, n.role.titleBn)}: \"$line\""
    subtitleTimer = 3.5f
    if (n.role == NpcRole.DOG) audio.play(SoundEffect.BARK)
    if (n.role == NpcRole.GOAT) audio.play(SoundEffect.GOAT_BLEAT)
    return activeSubtitle
  }

  fun step(dt: Float, hourOfDay: Float, vehicles: List<VehicleEntity>, lang: AppLang) {
    if (subtitleTimer > 0f) subtitleTimer -= dt

    // Wanted level slowly cools down if player avoids chaos
    if (chaosPoints > 0f) {
      chaosPoints = (chaosPoints - dt * 1.4f).coerceAtLeast(0f)
      wantedStars = when {
        chaosPoints >= 80f -> 5
        chaosPoints >= 60f -> 4
        chaosPoints >= 40f -> 3
        chaosPoints >= 20f -> 2
        chaosPoints >= 8f -> 1
        else -> 0
      }
    }

    val isNight = hourOfDay < 5.5f || hourOfDay >= 21.5f
    val px = physics.player.x
    val pz = physics.player.z

    for (n in npcs) {
      if (n.bubbleTimer > 0f) n.bubbleTimer -= dt

      if (n.mood == NpcMood.RAGDOLL) {
        n.stateTimer -= dt
        n.ragdollPitch += 7.5f * dt
        n.ragdollRoll += 5.5f * dt
        n.vy -= 18f * dt
        n.x += n.vx * dt
        n.y += n.vy * dt
        n.z += n.vz * dt
        val groundY = world.groundHeightAt(n.x, n.y, n.z)
        if (n.y <= groundY) {
          n.y = groundY
          n.vy = if (n.vy < -3f) -n.vy * 0.3f else 0f
          n.vx *= 0.84f
          n.vz *= 0.84f
        }
        if (n.stateTimer <= 0f) {
          n.mood = if (n.role == NpcRole.POLICE || n.role == NpcRole.NEIGHBOR) NpcMood.ANGRY_CHASE else NpcMood.CALM
          n.ragdollPitch = 0f
          n.ragdollRoll = 0f
          n.stateTimer = 6f
        }
        continue
      }

      // Check collision with moving vehicles (hilarious ragdoll knockback!)
      for (v in vehicles) {
        if (kotlin.math.abs(v.speed) > 3.5f) {
          val dx = n.x - v.x
          val dz = n.z - v.z
          if (dx * dx + dz * dz < 2.2f * 2.2f) {
            n.mood = NpcMood.RAGDOLL
            n.stateTimer = 2.4f
            val pushX = if (dx == 0f && dz == 0f) 1f else dx
            val pushZ = dz
            val len = sqrt(pushX * pushX + pushZ * pushZ).coerceAtLeast(0.1f)
            n.vx = (pushX / len) * kotlin.math.abs(v.speed) * 0.9f
            n.vz = (pushZ / len) * kotlin.math.abs(v.speed) * 0.9f
            n.vy = 6.2f
            audio.play(SoundEffect.BANG_RAGDOLL)
            addChaos(10f, "Whoa! Watch out for pedestrians!", "ওরে! সাবধানে চালাও, মানুষ উড়ে গেল!", lang)
            break
          }
        }
      }
      if (n.mood == NpcMood.RAGDOLL) continue

      // Check collision with flying thrown props
      for (p in physics.props) {
        if (!p.isBroken && !p.isGrabbed && (p.vx * p.vx + p.vz * p.vz) > 25f) {
          val dx = n.x - p.x
          val dz = n.z - p.z
          if (dx * dx + dz * dz < 1.2f * 1.2f) {
            n.mood = NpcMood.RAGDOLL
            n.stateTimer = 2.0f
            n.vx = p.vx * 0.6f
            n.vz = p.vz * 0.6f
            n.vy = 4.5f
            addChaos(7f, "Direct slipper/prop hit!", "সরাসরি নিশানা! আঙ্কেল ধরাশায়ী!", lang)
            break
          }
        }
      }
      if (n.mood == NpcMood.RAGDOLL) continue

      // Police chase logic when wanted
      val dxP = px - n.x
      val dzP = pz - n.z
      val distP = sqrt(dxP * dxP + dzP * dzP)

      if (n.role == NpcRole.POLICE && wantedStars > 0 && distP < 70f) {
        n.mood = NpcMood.ANGRY_CHASE
        n.targetX = px
        n.targetZ = pz
        if (distP < 1.9f && !physics.player.isRagdoll) {
          // Comedic non-violent whistle + fine / ragdoll bonk
          audio.play(SoundEffect.POLICE_WHISTLE)
          physics.triggerPlayerRagdoll(-dxP * 2f, 4f, -dzP * 2f)
          chaosPoints = 0f
          wantedStars = 0
          activeSubtitle = Localization.pick(
            lang,
            "Police: 'Caught you! Fine paid in embarrassment!'",
            "পুলিশ: 'ধরা খেয়েছো! জরিমানা হিসেবে কান ধরে ওঠবস করো!'",
          )
          subtitleTimer = 3.5f
        }
      } else if (n.mood == NpcMood.ANGRY_CHASE) {
        n.stateTimer -= dt
        n.targetX = px
        n.targetZ = pz
        if (n.stateTimer <= 0f || distP > 45f) {
          n.mood = NpcMood.CALM
          n.targetX = n.homeX
          n.targetZ = n.homeZ
        }
      } else if (n.mood == NpcMood.PANICKED) {
        n.stateTimer -= dt
        n.targetX = n.x - dxP
        n.targetZ = n.z - dzP
        if (n.stateTimer <= 0f) n.mood = NpcMood.CALM
      } else {
        // Time-of-day natural wandering around district home
        n.stateTimer -= dt
        if (n.stateTimer <= 0f) {
          val radius = when {
            isNight && n.district == DistrictId.RESIDENTIAL -> 4f
            n.role == NpcRole.TEA_OWNER || n.role == NpcRole.SHOPKEEPER -> 3f
            else -> 18f
          }
          n.targetX = n.homeX + (Random.nextFloat() - 0.5f) * 2f * radius
          n.targetZ = n.homeZ + (Random.nextFloat() - 0.5f) * 2f * radius
          n.stateTimer = 3f + Random.nextFloat() * 5f
        }
      }

      val tx = n.targetX - n.x
      val tz = n.targetZ - n.z
      val distT = sqrt(tx * tx + tz * tz)
      if (distT > 0.8f) {
        val spd = when (n.mood) {
          NpcMood.ANGRY_CHASE -> 5.8f
          NpcMood.PANICKED -> 6.2f
          else -> if (n.role == NpcRole.CHICKEN || n.role == NpcRole.DOG) 3.4f else 2.2f
        }
        val nx = (tx / distT) * spd
        val nz = (tz / distT) * spd
        val candX = (n.x + nx * dt).coerceIn(-220f, 220f)
        val candZ = (n.z + nz * dt).coerceIn(-220f, 220f)
        val (resX, resZ) = physics.resolveBuildingCollisions(candX, n.y, candZ, 0.4f)
        n.x = resX
        n.z = resZ
        n.yaw = MathUtils.lerpAngle(n.yaw, atan2(nx, nz), 8f * dt)
        n.walkPhase += dt * spd * 3f
      }
      n.y = world.groundHeightAt(n.x, n.y, n.z).coerceAtLeast(0f)
    }
  }
}
