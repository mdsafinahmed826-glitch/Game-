package com.example.game.engine

import com.example.data.SaveSlot
import com.example.game.model.AppLang
import com.example.game.model.CameraAngleMode
import com.example.game.model.CharacterAppearance
import com.example.game.model.DistrictId
import com.example.game.model.ExpressionType
import com.example.game.model.GlassesStyle
import com.example.game.model.GraphicsQuality
import com.example.game.model.HairStyle
import com.example.game.model.HatStyle
import com.example.game.model.InteractableKind
import com.example.game.model.Localization
import com.example.game.model.MissionCatalog
import com.example.game.model.MissionDef
import com.example.game.model.MissionGoalType
import com.example.game.model.PantsColor
import com.example.game.model.ShirtColor
import com.example.game.model.ShopCatalog
import com.example.game.model.ShopItem
import com.example.game.model.ShopItemCategory
import com.example.game.model.SkinTone
import com.example.game.model.VehicleType
import com.example.game.model.WeatherType
import kotlin.math.sqrt

data class HudSnapshot(
  val district: DistrictId = DistrictId.OLD_DHAKA,
  val moneyTaka: Long = 1200L,
  val wantedStars: Int = 0,
  val clockText: String = "10:30 AM",
  val weather: WeatherType = WeatherType.CLEAR,
  val powerOutage: Boolean = false,
  val activeMission: MissionDef? = null,
  val missionTimeLeftSec: Int = 0,
  val missionProgressText: String = "",
  val missionResultBanner: String? = null,
  val interactionPrompt: String? = null,
  val interactionKind: InteractableKind? = null,
  val nearestVehiclePrompt: String? = null,
  val inVehicleType: VehicleType? = null,
  val vehicleSpeedKmh: Int = 0,
  val grabbedPropLabel: String? = null,
  val subtitleText: String? = null,
  val eventBannerText: String? = null,
  val playerX: Float = 0f,
  val playerY: Float = 0f,
  val playerZ: Float = 0f,
  val playerYaw: Float = 0f,
  val isRagdoll: Boolean = false,
  val isSwimming: Boolean = false,
  val isCrouching: Boolean = false,
  val slowMotionActive: Boolean = false,
  val cameraMode: CameraAngleMode = CameraAngleMode.THIRD_PERSON,
  val hideUiViralMode: Boolean = false,
  val unlockedDistricts: Set<DistrictId> = DistrictId.entries.toSet(),
  val completedMissions: Set<String> = emptySet(),
  val ownedVehicles: Set<VehicleType> = setOf(VehicleType.RICKSHAW, VehicleType.BICYCLE),
  val inventoryItems: List<String> = listOf("hot_cha", "shingara"),
  val homeFurniture: List<String> = listOf("ceiling_fan", "charpai_bed", "crt_tv"),
  val homeLevel: Int = 1,
  val appearance: CharacterAppearance = CharacterAppearance(),
  val lang: AppLang = AppLang.EN,
  val graphicsQuality: GraphicsQuality = GraphicsQuality.HIGH,
  val isMuted: Boolean = false,
  val brokenPropCount: Int = 0,
)

class GameSimulation {
  val world = WorldManager()
  val audio = AudioManager()
  val physics = PhysicsManager(world, audio)
  val vehicles = VehicleManager(world, physics, audio)
  val npcs = NpcManager(world, physics, audio)
  val weather = WeatherManager(audio)
  val events = RandomEventManager(weather, npcs, physics, audio)

  // Input state updated from touch/keyboard
  @Volatile var moveInputX: Float = 0f
  @Volatile var moveInputY: Float = 0f
  @Volatile var cameraYaw: Float = 0f
  @Volatile var cameraPitch: Float = 0.42f
  @Volatile var cameraDist: Float = 9.5f
  @Volatile var isSprinting: Boolean = false
  @Volatile var slowMotionActive: Boolean = false
  @Volatile var cameraMode: CameraAngleMode = CameraAngleMode.THIRD_PERSON
  @Volatile var hideUiViralMode: Boolean = false

  // Player persistent progression
  var moneyTaka: Long = 1200L
  val ownedVehicles = linkedSetOf(VehicleType.RICKSHAW, VehicleType.BICYCLE)
  val completedMissions = linkedSetOf<String>()
  val unlockedDistricts = linkedSetOf(*DistrictId.entries.toTypedArray())
  val inventoryItems = mutableListOf("hot_cha", "shingara")
  val homeFurniture = mutableListOf("ceiling_fan", "charpai_bed", "crt_tv")
  var homeLevel: Int = 1
  var appearance = CharacterAppearance()
  var lang: AppLang = AppLang.EN
  var graphicsQuality: GraphicsQuality = GraphicsQuality.HIGH

  // Optional active mission state
  var activeMission: MissionDef? = null
  var missionTimerSec: Float = 0f
  var missionStartBrokenCount: Int = 0
  var missionResultBanner: String? = null
  var missionResultTimer: Float = 0f

  // UI modal callbacks requested from in-world interactables
  var pendingShopModal: InteractableKind? = null

  private var syncCounter = 0f
  @Volatile var latestHud = HudSnapshot()

  fun startAudio() = audio.start()
  fun stopAudio() = audio.stop()

  fun step(rawDt: Float) {
    val clampedRaw = rawDt.coerceIn(0.001f, 0.05f)
    val dt = if (slowMotionActive) clampedRaw * 0.35f else clampedRaw

    val activeVeh = vehicles.activeVehicle()
    physics.player.isSprinting = isSprinting

    physics.step(
      dt = dt,
      moveInputX = moveInputX,
      moveInputY = moveInputY,
      cameraYaw = cameraYaw,
      inVehicle = activeVeh != null,
      isMonsoon = weather.weather == WeatherType.MONSOON,
      maxParticles = graphicsQuality.maxParticles,
    )

    vehicles.step(
      dt = dt,
      throttleInput = moveInputY,
      steerInput = moveInputX,
      isMonsoon = weather.weather == WeatherType.MONSOON,
      maxParticles = graphicsQuality.maxParticles,
    )

    if (physics.lastChaosImpulse > 0) {
      npcs.addChaos(
        points = physics.lastChaosImpulse * 6f,
        reasonEn = "Prop smashed in public!",
        reasonBn = "রাস্তার জিনিসপত্র ভাঙচুর হয়েছে!",
        lang = lang,
      )
    }

    weather.step(dt)
    val district = world.detectDistrict(physics.player.x, physics.player.z)
    audio.isBusyMarket = district == DistrictId.OLD_DHAKA || district == DistrictId.COMMERCIAL
    npcs.step(dt, weather.timeOfDayHours, vehicles.vehicles, lang)
    events.step(dt, lang)
    stepMission(dt)

    if (missionResultTimer > 0f) {
      missionResultTimer -= clampedRaw
      if (missionResultTimer <= 0f) missionResultBanner = null
    }

    syncCounter += clampedRaw
    if (syncCounter >= 0.08f) {
      syncCounter = 0f
      latestHud = buildHudSnapshot(district)
    }
  }

  private fun stepMission(dt: Float) {
    val m = activeMission ?: return
    missionTimerSec -= dt
    if (missionTimerSec <= 0f) {
      activeMission = null
      missionResultBanner = Localization.pick(
        lang,
        "⏰ Mission Failed: ${m.titleEn} — Try again anytime!",
        "⏰ মিশন ব্যর্থ: ${m.titleBn} — আবার চেষ্টা করো!",
      )
      missionResultTimer = 4.5f
      return
    }

    val dx = physics.player.x - m.targetX
    val dz = physics.player.z - m.targetZ
    val dist = sqrt(dx * dx + dz * dz)

    val completed = when (m.goalType) {
      MissionGoalType.PROP_SMASH -> (physics.brokenPropCount - missionStartBrokenCount) >= m.targetCount
      MissionGoalType.VEHICLE_RACE -> dist < 9f && vehicles.activeVehicle() != null
      else -> dist < 7f
    }

    if (completed) {
      activeMission = null
      completedMissions.add(m.id)
      moneyTaka += m.rewardTaka
      audio.play(SoundEffect.CASH)
      missionResultBanner = Localization.pick(
        lang,
        "🎉 Mission Complete! +৳${m.rewardTaka} (${m.titleEn})",
        "🎉 মিশন সম্পন্ন! +৳${m.rewardTaka} (${m.titleBn})",
      )
      missionResultTimer = 5f
    }
  }

  fun startMission(mission: MissionDef) {
    activeMission = mission
    missionTimerSec = mission.timeLimitSec.toFloat()
    missionStartBrokenCount = physics.brokenPropCount
    // Teleport near mission start so player can immediately play it, or keep free-roam optional!
    vehicles.activeVehicle()?.let { it.isOccupied = false }
    vehicles.activeVehicleId = null
    physics.player.x = mission.startX
    physics.player.z = mission.startZ
    physics.player.y = world.groundHeightAt(mission.startX, 0f, mission.startZ)
    physics.player.vx = 0f; physics.player.vy = 0f; physics.player.vz = 0f
    npcs.activeSubtitle = Localization.pick(lang, mission.funnyDialogueEn, mission.funnyDialogueBn)
    npcs.subtitleTimer = 4.5f
  }

  fun abandonMission() {
    activeMission = null
  }

  fun teleportToDistrict(district: DistrictId) {
    vehicles.activeVehicle()?.let { it.isOccupied = false }
    vehicles.activeVehicleId = null
    physics.player.isRagdoll = false
    physics.player.x = district.centerX
    physics.player.z = district.centerZ
    physics.player.y = world.groundHeightAt(district.centerX, 0f, district.centerZ) + 0.3f
    physics.player.vx = 0f; physics.player.vy = 0f; physics.player.vz = 0f
  }

  fun resetPosition() {
    val d = world.detectDistrict(physics.player.x, physics.player.z)
    teleportToDistrict(d)
  }

  fun interact(): String? {
    // 1. Check nearest interactable station
    val nearestInt = findNearestInteractable()
    if (nearestInt != null) {
      nearestInt.stateOn = !nearestInt.stateOn
      return when (nearestInt.kind) {
        InteractableKind.TEA_STALL -> {
          audio.play(SoundEffect.TEA_CLINK)
          if (moneyTaka >= 15L) {
            moneyTaka -= 15L
            appearance = appearance.copy(expression = ExpressionType.HAPPY)
            announce("☕ Sipping hot ginger cha! (-৳15)", "☕ গরম আদা চা খেয়ে মন ফুরফুরে! (-৳১৫)")
          } else {
            announce("Cha Mama: 'Pay your tab first!'", "চা মামা: 'আগে বাকি টাকা দাও!'")
          }
        }
        InteractableKind.STREET_FOOD -> {
          audio.play(SoundEffect.TEA_CLINK)
          if (moneyTaka >= 40L) {
            moneyTaka -= 40L
            appearance = appearance.copy(expression = ExpressionType.LAUGHING)
            announce("😋 Crunchy Dhaka Fuchka & Jhalmuri! (-৳40)", "😋 ঝাল ঝাল ফুচকা! দারুণ স্বাদ! (-৳৪০)")
          } else {
            announce("Need ৳40 for Fuchka!", "ফুচকার জন্য ৪০ টাকা লাগবে!")
          }
        }
        InteractableKind.GROCERY_SHOP, InteractableKind.VEHICLE_DEALER, InteractableKind.HOME_DOOR -> {
          audio.play(SoundEffect.RICKSHAW_BELL)
          pendingShopModal = nearestInt.kind
          null
        }
        InteractableKind.ELECTRIC_BOX -> {
          weather.powerOutageActive = !nearestInt.stateOn
          audio.play(SoundEffect.SHATTER)
          npcs.addChaos(10f, "⚡ You flipped the DESCO main switch!", "⚡ এলাকার মেইন সুইচ বন্ধ করে দিয়েছো!", lang)
          null
        }
        InteractableKind.CEILING_FAN -> {
          announce("🌀 Ceiling fan toggled!", "🌀 সিলিং ফ্যানের স্পিড বাড়ানো হয়েছে!")
        }
        InteractableKind.TELEVISION -> {
          announce("📺 BTV News & Bangla Natok playing!", "📺 টিভিতে বাংলা নাটক চলছে!")
        }
        InteractableKind.CHAIR -> {
          physics.player.sittingTimer = 4f
          announce("🪑 Chilling on the red plastic chair!", "🪑 লাল প্লাস্টিক চেয়ারে বসে আড্ডা দিচ্ছো!")
        }
        InteractableKind.BED -> {
          weather.timeOfDayHours = (weather.timeOfDayHours + 6f) % 24f
          announce("🛏️ Power nap taken! Time advanced 6 hours.", "🛏️ দারুণ ঘুম হলো! ৬ ঘণ্টা কেটে গেছে।")
        }
        InteractableKind.FRIDGE -> {
          announce("🧊 Grabbed chilled mango juice from the fridge!", "🧊 ফ্রিজ থেকে ঠান্ডা আমসত্ত্ব ও শরবত খেলে!")
        }
        InteractableKind.WATER_TAP -> {
          audio.play(SoundEffect.SPLASH)
          physics.spawnParticles(nearestInt.x, nearestInt.y + 0.7f, nearestInt.z, 0xFF38BDF8.toInt(), 10, graphicsQuality.maxParticles)
          announce("🚰 Water tap splashing! Careful, floor is slippery!", "🚰 কল ছেড়ে দিয়েছো! রাস্তা পিচ্ছিল হয়ে যাচ্ছে!")
        }
        InteractableKind.CRICKET_WICKET -> {
          audio.play(SoundEffect.JUMP)
          moneyTaka += 80L
          announce("🏏 HUGE SIXER over the rooftop! +৳80 bet won!", "🏏 ছাদের ওপর দিয়ে বিশাল ছক্কা! +৳৮০ জিতেছো!")
        }
        InteractableKind.FOOTBALL -> {
          audio.play(SoundEffect.JUMP)
          physics.props.firstOrNull { it.kind == com.example.game.model.PropKind.FOOTBALL_PROP }?.let {
            it.vx = MathUtils.sinf(physics.player.yaw) * 18f
            it.vz = MathUtils.cosf(physics.player.yaw) * 18f
            it.vy = 7f
          }
          announce("⚽ GOAL! Rocket kick across the lane!", "⚽ গোওওওল! গলিতে দুর্দান্ত শট!")
        }
        InteractableKind.SLIPPER_PILE -> {
          if (!inventoryItems.contains("rubber_chappal")) inventoryItems.add("rubber_chappal")
          announce("🩴 Picked up Aero-Dynamic Rubber Slipper!", "🩴 সুপারসনিক রাবারের চপ্পল হাতে তুলে নিয়েছো!")
        }
        InteractableKind.SHOP_BELL -> {
          audio.play(SoundEffect.RICKSHAW_BELL)
          announce("🔔 Tring-tring! Shopkeeper glared at you!", "🔔 ক্রিং-ক্রিং! দোকানদার তাকিয়ে আছে!")
        }
        InteractableKind.FISHING_SPOT -> {
          audio.play(SoundEffect.SPLASH)
          moneyTaka += 150L
          audio.play(SoundEffect.CASH)
          if (!inventoryItems.contains("hilsha_fish")) inventoryItems.add("hilsha_fish")
          announce("🐟 Caught a shiny Buriganga Fish! +৳150", "🐟 বুড়িগঙ্গা থেকে তাজা মাছ ধরেছো! +৳১৫০")
        }
        InteractableKind.LADDER -> {
          physics.player.y = if (physics.player.y < 3f) 6.2f else 0.1f
          audio.play(SoundEffect.JUMP)
          announce("🪜 Climbed rooftop ladder!", "🪜 মই বেয়ে ছাদে উঠেছো!")
        }
      }
    }

    // 2. Otherwise talk to nearest NPC
    val npc = npcs.findNearestNpc(physics.player.x, physics.player.z)
    if (npc != null) {
      return npcs.talkToNpc(npc, lang)
    }

    // 3. Otherwise grab/throw nearest prop
    return physics.grabOrThrowNearestProp()
  }

  private fun announce(en: String, bn: String): String {
    val msg = Localization.pick(lang, en, bn)
    npcs.activeSubtitle = msg
    npcs.subtitleTimer = 3.2f
    return msg
  }

  fun buyShopItem(item: ShopItem): Boolean {
    if (moneyTaka < item.priceTaka) return false
    moneyTaka -= item.priceTaka
    audio.play(SoundEffect.CASH)
    when (item.category) {
      ShopItemCategory.FURNITURE -> if (!homeFurniture.contains(item.id)) homeFurniture.add(item.id)
      ShopItemCategory.UPGRADE -> {
        if (!homeFurniture.contains(item.id)) homeFurniture.add(item.id)
        homeLevel = (homeLevel + 1).coerceAtMost(5)
      }
      else -> inventoryItems.add(item.id)
    }
    return true
  }

  fun sellInventoryItem(itemId: String): Boolean {
    val idx = inventoryItems.indexOf(itemId)
    if (idx < 0) return false
    val def = ShopCatalog.items.find { it.id == itemId }
    val value = def?.sellValueTaka ?: 50L
    inventoryItems.removeAt(idx)
    moneyTaka += value
    audio.play(SoundEffect.CASH)
    return true
  }

  fun buyOrSpawnVehicle(type: VehicleType): Boolean {
    if (!ownedVehicles.contains(type)) {
      if (moneyTaka < type.priceTaka) return false
      moneyTaka -= type.priceTaka
      ownedVehicles.add(type)
      audio.play(SoundEffect.CASH)
    }
    val spawnX = if (type.isBoat) 0f else physics.player.x + 3.5f
    val spawnZ = if (type.isBoat) 148f else physics.player.z + 3.5f
    vehicles.spawn(type, spawnX, spawnZ, physics.player.yaw)
    if (type.isBoat) teleportToDistrict(DistrictId.RIVERSIDE)
    return true
  }

  fun findNearestInteractable(maxDist: Float = 4.2f): InteractablePoint? {
    var best: InteractablePoint? = null
    var bestD2 = maxDist * maxDist
    for (p in world.interactables) {
      val dx = p.x - physics.player.x
      val dz = p.z - physics.player.z
      val dy = kotlin.math.abs(p.y - physics.player.y)
      val d2 = dx * dx + dz * dz
      if (d2 < bestD2 && dy < 3.5f) {
        bestD2 = d2
        best = p
      }
    }
    return best
  }

  fun toSaveSlot(slotName: String): SaveSlot {
    val d = world.detectDistrict(physics.player.x, physics.player.z)
    return SaveSlot(
      slotName = slotName,
      posX = physics.player.x.toDouble(),
      posY = physics.player.y.toDouble(),
      posZ = physics.player.z.toDouble(),
      yaw = physics.player.yaw.toDouble(),
      district = d.name,
      moneyTaka = moneyTaka,
      chaosLevel = npcs.wantedStars.toLong(),
      timeOfDay = weather.timeOfDayHours.toDouble(),
      weather = weather.weather.name,
      ownedVehicles = ownedVehicles.map { it.name },
      completedMissions = completedMissions.toList(),
      unlockedDistricts = unlockedDistricts.map { it.name },
      inventoryItems = inventoryItems.take(40),
      homeFurniture = homeFurniture.take(20),
      homeLevel = homeLevel.toLong(),
      shirtColor = appearance.shirtColor.name,
      pantsColor = appearance.pantsColor.name,
      skinTone = appearance.skinTone.name,
      hairStyle = appearance.hairStyle.name,
      hatStyle = appearance.hatStyle.name,
      glassesStyle = appearance.glassesStyle.name,
      expression = appearance.expression.name,
    )
  }

  fun loadFromSaveSlot(slot: SaveSlot) {
    vehicles.activeVehicle()?.let { it.isOccupied = false }
    vehicles.activeVehicleId = null
    physics.player.x = slot.posX.toFloat()
    physics.player.y = slot.posY.toFloat().coerceAtLeast(0f)
    physics.player.z = slot.posZ.toFloat()
    physics.player.yaw = slot.yaw.toFloat()
    physics.player.vx = 0f; physics.player.vy = 0f; physics.player.vz = 0f
    moneyTaka = slot.moneyTaka
    weather.timeOfDayHours = slot.timeOfDay.toFloat().coerceIn(0f, 24f)
    weather.weather = WeatherType.entries.find { it.name == slot.weather } ?: WeatherType.CLEAR
    ownedVehicles.clear()
    slot.ownedVehicles.mapNotNull { name -> VehicleType.entries.find { it.name == name } }
      .ifEmpty { listOf(VehicleType.RICKSHAW, VehicleType.BICYCLE) }
      .forEach { ownedVehicles.add(it) }
    completedMissions.clear()
    completedMissions.addAll(slot.completedMissions)
    inventoryItems.clear()
    inventoryItems.addAll(slot.inventoryItems)
    homeFurniture.clear()
    homeFurniture.addAll(slot.homeFurniture)
    homeLevel = slot.homeLevel.toInt().coerceIn(1, 5)
    appearance = CharacterAppearance(
      shirtColor = ShirtColor.entries.find { it.name == slot.shirtColor } ?: ShirtColor.EMERALD,
      pantsColor = PantsColor.entries.find { it.name == slot.pantsColor } ?: PantsColor.DENIM,
      skinTone = SkinTone.entries.find { it.name == slot.skinTone } ?: SkinTone.WARM,
      hairStyle = HairStyle.entries.find { it.name == slot.hairStyle } ?: HairStyle.CLASSIC,
      hatStyle = HatStyle.entries.find { it.name == slot.hatStyle } ?: HatStyle.NONE,
      glassesStyle = GlassesStyle.entries.find { it.name == slot.glassesStyle } ?: GlassesStyle.NONE,
      expression = ExpressionType.entries.find { it.name == slot.expression } ?: ExpressionType.HAPPY,
    )
  }

  private fun buildHudSnapshot(district: DistrictId): HudSnapshot {
    val nearestInt = findNearestInteractable()
    val activeVeh = vehicles.activeVehicle()
    val nearestVeh = if (activeVeh == null) vehicles.findNearestEnterable(physics.player.x, physics.player.z) else null
    val grabbed = physics.player.grabbedPropId?.let { id -> physics.props.find { it.id == id } }
    val m = activeMission
    val mProg = if (m != null) {
      if (m.goalType == MissionGoalType.PROP_SMASH) {
        val done = (physics.brokenPropCount - missionStartBrokenCount).coerceAtMost(m.targetCount)
        "$done / ${m.targetCount}"
      } else {
        val dist = physics.player.let { sqrt((it.x - m.targetX) * (it.x - m.targetX) + (it.z - m.targetZ) * (it.z - m.targetZ)) }
        "${dist.toInt()}m"
      }
    } else ""

    return HudSnapshot(
      district = district,
      moneyTaka = moneyTaka,
      wantedStars = npcs.wantedStars,
      clockText = weather.formattedClock(),
      weather = weather.weather,
      powerOutage = weather.powerOutageActive,
      activeMission = m,
      missionTimeLeftSec = missionTimerSec.toInt().coerceAtLeast(0),
      missionProgressText = mProg,
      missionResultBanner = missionResultBanner,
      interactionPrompt = nearestInt?.let { Localization.pick(lang, it.kind.actionEn, it.kind.actionBn) },
      interactionKind = nearestInt?.kind,
      nearestVehiclePrompt = nearestVeh?.let {
        Localization.pick(lang, "Ride ${it.type.titleEn}", "${it.type.titleBn} চালাও")
      },
      inVehicleType = activeVeh?.type,
      vehicleSpeedKmh = activeVeh?.let { kotlin.math.abs(it.speed * 3.6f).toInt() } ?: 0,
      grabbedPropLabel = grabbed?.let { Localization.pick(lang, it.kind.labelEn, it.kind.labelBn) },
      subtitleText = if (npcs.subtitleTimer > 0f) npcs.activeSubtitle else null,
      eventBannerText = if (events.bannerTimer > 0f) events.currentBannerText else null,
      playerX = physics.player.x,
      playerY = physics.player.y,
      playerZ = physics.player.z,
      playerYaw = physics.player.yaw,
      isRagdoll = physics.player.isRagdoll,
      isSwimming = physics.player.isSwimming,
      isCrouching = physics.player.isCrouching,
      slowMotionActive = slowMotionActive,
      cameraMode = cameraMode,
      hideUiViralMode = hideUiViralMode,
      unlockedDistricts = unlockedDistricts.toSet(),
      completedMissions = completedMissions.toSet(),
      ownedVehicles = ownedVehicles.toSet(),
      inventoryItems = inventoryItems.toList(),
      homeFurniture = homeFurniture.toList(),
      homeLevel = homeLevel,
      appearance = appearance,
      lang = lang,
      graphicsQuality = graphicsQuality,
      isMuted = audio.isMuted,
      brokenPropCount = physics.brokenPropCount,
    )
  }
}
