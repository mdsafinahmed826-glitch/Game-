package com.example.game.engine

import com.example.game.model.DistrictId
import com.example.game.model.InteractableKind
import kotlin.math.abs

data class BuildingBlock(
  val id: String,
  val x: Float,
  val y: Float,
  val z: Float,
  val w: Float,
  val h: Float,
  val d: Float,
  val colorArgb: Int,
  val roofColorArgb: Int,
  val district: DistrictId,
  val hasWindows: Boolean = true,
  val isWalkableRoof: Boolean = false,
  val isArch: Boolean = false,
)

data class RoadSegment(
  val x: Float,
  val z: Float,
  val w: Float,
  val d: Float,
  val isMud: Boolean = false,
  val isBridge: Boolean = false,
  val y: Float = 0.02f,
)

data class WaterZone(
  val minX: Float,
  val maxX: Float,
  val minZ: Float,
  val maxZ: Float,
  val surfaceY: Float = -0.25f,
  val isRiver: Boolean = true,
)

data class RampOrStair(
  val minX: Float,
  val maxX: Float,
  val minZ: Float,
  val maxZ: Float,
  val startY: Float,
  val endY: Float,
  val axisZ: Boolean,
  val positiveDir: Boolean,
)

data class InteractablePoint(
  val id: String,
  val kind: InteractableKind,
  val x: Float,
  val y: Float,
  val z: Float,
  val district: DistrictId,
  var stateOn: Boolean = false,
)

data class TreeOrPropDecor(
  val x: Float,
  val y: Float,
  val z: Float,
  val scale: Float,
  val isBananaTree: Boolean = false,
  val isStreetLamp: Boolean = false,
)

data class HangingRope(
  val ax: Float,
  val ay: Float,
  val az: Float,
  val bx: Float,
  val by: Float,
  val bz: Float,
  var swingAngle: Float = 0f,
  var swingVel: Float = 0f,
  val bulbColor: Int = 0xFFFFB703.toInt(),
)

class WorldManager {
  val buildings = mutableListOf<BuildingBlock>()
  val colliders = mutableListOf<AABB>()
  val roads = mutableListOf<RoadSegment>()
  val waterZones = mutableListOf<WaterZone>()
  val ramps = mutableListOf<RampOrStair>()
  val interactables = mutableListOf<InteractablePoint>()
  val decors = mutableListOf<TreeOrPropDecor>()
  val ropes = mutableListOf<HangingRope>()
  val slipperyZones = mutableListOf<AABB>()

  init {
    buildWorld()
  }

  private fun buildWorld() {
    // Interconnected Arterial Roads across the 6 Districts (-220..220)
    // Main North-South Avenue: Riverside -> Old Dhaka -> Modern Dhaka
    roads.add(RoadSegment(0f, 0f, 14f, 340f))
    // Main East-West Avenue: Residential -> Old Dhaka -> Commercial
    roads.add(RoadSegment(0f, 0f, 360f, 14f))
    // Northern Cross-Road: Village -> Modern Dhaka -> Commercial North
    roads.add(RoadSegment(0f, -135f, 360f, 12f, isMud = false))
    // Village Mud Roads
    roads.add(RoadSegment(-135f, -135f, 10f, 120f, isMud = true))
    roads.add(RoadSegment(-135f, -70f, 10f, 130f, isMud = true))
    // Commercial Loop
    roads.add(RoadSegment(135f, -45f, 12f, 180f))
    // Residential Lanes
    roads.add(RoadSegment(-135f, 0f, 10f, 130f))
    // Riverside Corniche Road
    roads.add(RoadSegment(0f, 118f, 320f, 11f))

    // Modern Dhaka Flyover Ramp & Elevated Deck
    roads.add(RoadSegment(24f, -135f, 8f, 80f, isBridge = true, y = 4.2f))
    ramps.add(RampOrStair(20f, 28f, -95f, -75f, 0f, 4.2f, axisZ = true, positiveDir = false))
    ramps.add(RampOrStair(20f, 28f, -195f, -175f, 0f, 4.2f, axisZ = true, positiveDir = true))

    // Buriganga River (South) & Village Pond (North-West)
    waterZones.add(WaterZone(-240f, 240f, 132f, 235f, surfaceY = -0.35f, isRiver = true))
    waterZones.add(WaterZone(-175f, -145f, -175f, -148f, surfaceY = -0.25f, isRiver = false))

    // Slippery puddle / tap zones
    slipperyZones.add(AABB(-151f, 0f, 5f, -145f, 1.5f, 11f, isSlippery = true))
    slipperyZones.add(AABB(-10f, 0f, 110f, 10f, 1.5f, 120f, isSlippery = true))

    // === DISTRICT A: OLD DHAKA (center 0, 0) ===
    addBuilding("old_arch_1", -20f, -20f, 12f, 9f, 10f, 0xFF991B1B.toInt(), 0xFF7F1D1D.toInt(), DistrictId.OLD_DHAKA, isArch = true)
    addBuilding("old_shop_1", 20f, -18f, 10f, 7f, 10f, 0xFFB45309.toInt(), 0xFF78350F.toInt(), DistrictId.OLD_DHAKA)
    addBuilding("old_tenement_1", -22f, 20f, 11f, 11f, 11f, 0xFFBE123C.toInt(), 0xFF881337.toInt(), DistrictId.OLD_DHAKA)
    addBuilding("old_bazaar_1", 22f, 22f, 12f, 6.5f, 12f, 0xFFCA8A04.toInt(), 0xFF854D0E.toInt(), DistrictId.OLD_DHAKA)
    addBuilding("old_alley_1", -42f, -28f, 9f, 9f, 12f, 0xFF9F1239.toInt(), 0xFF4C0519.toInt(), DistrictId.OLD_DHAKA)
    addBuilding("old_alley_2", 42f, 32f, 10f, 8f, 10f, 0xFFD97706.toInt(), 0xFF78350F.toInt(), DistrictId.OLD_DHAKA)
    // Hanging festival bulb ropes across Old Dhaka street
    ropes.add(HangingRope(-14f, 5.5f, -12f, 14f, 5.5f, -12f, bulbColor = 0xFFFFB703.toInt()))
    ropes.add(HangingRope(-14f, 5.5f, 14f, 14f, 5.5f, 14f, bulbColor = 0xFFF42A41.toInt()))

    // === DISTRICT B: MODERN DHAKA (center 0, -135) ===
    addBuilding("mod_tower_1", -26f, -158f, 16f, 28f, 16f, 0xFF1E3A8A.toInt(), 0xFF0F172A.toInt(), DistrictId.MODERN_DHAKA)
    addBuilding("mod_tower_2", 44f, -160f, 18f, 34f, 16f, 0xFF0369A1.toInt(), 0xFF0C4A6E.toInt(), DistrictId.MODERN_DHAKA)
    addBuilding("mod_mall", -28f, -108f, 22f, 14f, 16f, 0xFF334155.toInt(), 0xFF1E293B.toInt(), DistrictId.MODERN_DHAKA)
    addBuilding("mod_uni", 44f, -106f, 16f, 16f, 14f, 0xFF475569.toInt(), 0xFF0F172A.toInt(), DistrictId.MODERN_DHAKA)
    ropes.add(HangingRope(-18f, 7f, -135f, 18f, 7f, -135f, bulbColor = 0xFF38BDF8.toInt()))

    // === DISTRICT C: RESIDENTIAL AREA (center -135, 0) ===
    // Player's Home + Walkable Rooftop with Staircase!
    addBuilding("res_home", -154f, 18f, 14f, 6f, 12f, 0xFF059669.toInt(), 0xFF065F46.toInt(), DistrictId.RESIDENTIAL, isWalkableRoof = true)
    ramps.add(RampOrStair(-147f, -142f, 12f, 24f, 0f, 6f, axisZ = true, positiveDir = true))
    // Neighbor Apartment with Rooftop Cricket & Water Tank
    addBuilding("res_apt_1", -115f, -20f, 14f, 8f, 14f, 0xFF0D9488.toInt(), 0xFF115E59.toInt(), DistrictId.RESIDENTIAL, isWalkableRoof = true)
    ramps.add(RampOrStair(-108f, -103f, -27f, -13f, 0f, 8f, axisZ = true, positiveDir = true))
    addBuilding("res_house_2", -155f, -22f, 11f, 6f, 11f, 0xFF2563EB.toInt(), 0xFF1E40AF.toInt(), DistrictId.RESIDENTIAL)
    addBuilding("res_house_3", -115f, 24f, 12f, 7f, 11f, 0xFFE11D48.toInt(), 0xFF9F1239.toInt(), DistrictId.RESIDENTIAL)

    // === DISTRICT D: VILLAGE AREA (center -135, -135) ===
    addBuilding("vil_hut_1", -158f, -116f, 9f, 4.2f, 8f, 0xFFD97706.toInt(), 0xFF94A3B8.toInt(), DistrictId.VILLAGE)
    addBuilding("vil_hut_2", -114f, -158f, 9f, 4.0f, 8f, 0xFFA16207.toInt(), 0xFF64748B.toInt(), DistrictId.VILLAGE)
    addBuilding("vil_haat", -116f, -114f, 12f, 4.5f, 9f, 0xFF65A30D.toInt(), 0xFF3F6212.toInt(), DistrictId.VILLAGE)

    // === DISTRICT E: COMMERCIAL HUB (center 135, -45) ===
    addBuilding("com_warehouse_1", 158f, -68f, 18f, 9f, 16f, 0xFF475569.toInt(), 0xFF1E293B.toInt(), DistrictId.COMMERCIAL)
    addBuilding("com_bus_terminal", 156f, -20f, 18f, 8f, 14f, 0xFFEA580C.toInt(), 0xFF9A3412.toInt(), DistrictId.COMMERCIAL)
    addBuilding("com_market", 112f, -62f, 14f, 7f, 14f, 0xFFCA8A04.toInt(), 0xFF713F12.toInt(), DistrictId.COMMERCIAL)
    addBuilding("com_tower", 114f, -22f, 12f, 18f, 12f, 0xFF334155.toInt(), 0xFF0F172A.toInt(), DistrictId.COMMERCIAL)

    // === DISTRICT F: BURIGANGA RIVERSIDE (center 0, 135) ===
    addBuilding("riv_ghat_office", -28f, 102f, 10f, 5f, 8f, 0xFF0891B2.toInt(), 0xFF155E75.toInt(), DistrictId.RIVERSIDE)
    addBuilding("riv_market_1", 28f, 102f, 12f, 5f, 8f, 0xFFB45309.toInt(), 0xFF78350F.toInt(), DistrictId.RIVERSIDE)
    // Wooden Boat Ghat Pier extending into the river
    roads.add(RoadSegment(0f, 134f, 8f, 22f, isMud = true, isBridge = true, y = 0.2f))

    // === INTERACTABLES (Almost everything interactive!) ===
    interactables.addAll(listOf(
      InteractablePoint("int_tea_old", InteractableKind.TEA_STALL, -8f, 0f, 4f, DistrictId.OLD_DHAKA),
      InteractablePoint("int_food_old", InteractableKind.STREET_FOOD, 9f, 0f, 10f, DistrictId.OLD_DHAKA),
      InteractablePoint("int_mudi_old", InteractableKind.GROCERY_SHOP, 13f, 0f, -15f, DistrictId.OLD_DHAKA),
      InteractablePoint("int_bell_old", InteractableKind.SHOP_BELL, 13f, 0f, -10f, DistrictId.OLD_DHAKA),
      InteractablePoint("int_desco_old", InteractableKind.ELECTRIC_BOX, -18f, 0f, 24f, DistrictId.OLD_DHAKA, stateOn = true),
      InteractablePoint("int_garage_old", InteractableKind.VEHICLE_DEALER, 10f, 0f, -24f, DistrictId.OLD_DHAKA),
      InteractablePoint("int_slipper_old", InteractableKind.SLIPPER_PILE, -6f, 0f, -12f, DistrictId.OLD_DHAKA),
      InteractablePoint("int_chair_old", InteractableKind.CHAIR, -10f, 0f, 6f, DistrictId.OLD_DHAKA),

      InteractablePoint("int_desco_mod", InteractableKind.ELECTRIC_BOX, -20f, 0f, -145f, DistrictId.MODERN_DHAKA, stateOn = true),
      InteractablePoint("int_garage_mod", InteractableKind.VEHICLE_DEALER, 14f, 0f, -130f, DistrictId.MODERN_DHAKA),
      InteractablePoint("int_food_mod", InteractableKind.STREET_FOOD, -12f, 0f, -122f, DistrictId.MODERN_DHAKA),

      InteractablePoint("int_home_door", InteractableKind.HOME_DOOR, -146f, 0f, 18f, DistrictId.RESIDENTIAL),
      InteractablePoint("int_home_fan", InteractableKind.CEILING_FAN, -152f, 6f, 18f, DistrictId.RESIDENTIAL, stateOn = true),
      InteractablePoint("int_home_tv", InteractableKind.TELEVISION, -156f, 6f, 16f, DistrictId.RESIDENTIAL),
      InteractablePoint("int_home_bed", InteractableKind.BED, -152f, 6f, 21f, DistrictId.RESIDENTIAL),
      InteractablePoint("int_home_fridge", InteractableKind.FRIDGE, -156f, 6f, 21f, DistrictId.RESIDENTIAL),
      InteractablePoint("int_res_tap", InteractableKind.WATER_TAP, -148f, 0f, 8f, DistrictId.RESIDENTIAL),
      InteractablePoint("int_res_cricket", InteractableKind.CRICKET_WICKET, -115f, 8f, -20f, DistrictId.RESIDENTIAL),
      InteractablePoint("int_res_football", InteractableKind.FOOTBALL, -125f, 0f, 8f, DistrictId.RESIDENTIAL),
      InteractablePoint("int_res_ladder", InteractableKind.LADDER, -144f, 0f, 18f, DistrictId.RESIDENTIAL),

      InteractablePoint("int_vil_haat", InteractableKind.GROCERY_SHOP, -126f, 0f, -125f, DistrictId.VILLAGE),
      InteractablePoint("int_vil_tea", InteractableKind.TEA_STALL, -142f, 0f, -126f, DistrictId.VILLAGE),

      InteractablePoint("int_com_isp", InteractableKind.ELECTRIC_BOX, 152f, 0f, -48f, DistrictId.COMMERCIAL, stateOn = true),
      InteractablePoint("int_com_garage", InteractableKind.VEHICLE_DEALER, 140f, 0f, -35f, DistrictId.COMMERCIAL),

      InteractablePoint("int_riv_fish", InteractableKind.FISHING_SPOT, 0f, 0.2f, 143f, DistrictId.RIVERSIDE),
      InteractablePoint("int_riv_food", InteractableKind.STREET_FOOD, 18f, 0f, 110f, DistrictId.RIVERSIDE),
    ))

    // === TREES & STREETLAMPS ===
    val treeCoords = listOf(
      -15f to -45f, 16f to -48f, -135f to -48f, -165f to -135f,
      -140f to -165f, -100f to -145f, -120f to -95f, -45f to 110f,
      45f to 110f, -85f to 110f, 85f to 110f, -105f to 12f,
      -168f to -5f, 98f to -12f, 102f to -80f,
    )
    treeCoords.forEachIndexed { idx, (tx, tz) ->
      decors.add(TreeOrPropDecor(tx, 0f, tz, 1.0f + (idx % 3) * 0.25f, isBananaTree = idx % 2 == 0))
    }
    // Streetlamps along roads
    for (z in -170..100 step 34) {
      decors.add(TreeOrPropDecor(8.5f, 0f, z.toFloat(), 1f, isStreetLamp = true))
      decors.add(TreeOrPropDecor(-8.5f, 0f, z.toFloat(), 1f, isStreetLamp = true))
    }
    for (x in -150..150 step 40) {
      if (abs(x) > 15) {
        decors.add(TreeOrPropDecor(x.toFloat(), 0f, 8.5f, 1f, isStreetLamp = true))
      }
    }
  }

  private fun addBuilding(
    id: String,
    x: Float,
    z: Float,
    w: Float,
    h: Float,
    d: Float,
    colorArgb: Int,
    roofColorArgb: Int,
    district: DistrictId,
    isWalkableRoof: Boolean = false,
    isArch: Boolean = false,
  ) {
    buildings.add(
      BuildingBlock(
        id = id,
        x = x,
        y = h * 0.5f,
        z = z,
        w = w,
        h = h,
        d = d,
        colorArgb = colorArgb,
        roofColorArgb = roofColorArgb,
        district = district,
        isWalkableRoof = isWalkableRoof,
        isArch = isArch,
      )
    )
    colliders.add(
      AABB(
        minX = x - w * 0.5f,
        minY = 0f,
        minZ = z - d * 0.5f,
        maxX = x + w * 0.5f,
        maxY = h,
        maxZ = z + d * 0.5f,
        isWalkableRoof = isWalkableRoof,
      )
    )
  }

  fun detectDistrict(x: Float, z: Float): DistrictId {
    return when {
      z > 90f -> DistrictId.RIVERSIDE
      z < -85f && x < -75f -> DistrictId.VILLAGE
      z < -85f -> DistrictId.MODERN_DHAKA
      x < -75f -> DistrictId.RESIDENTIAL
      x > 75f -> DistrictId.COMMERCIAL
      else -> DistrictId.OLD_DHAKA
    }
  }

  fun groundHeightAt(x: Float, y: Float, z: Float): Float {
    var bestY = 0f
    // Check water depth
    for (w in waterZones) {
      if (x >= w.minX && x <= w.maxX && z >= w.minZ && z <= w.maxZ) {
        bestY = w.surfaceY - 0.5f
      }
    }
    // Check ramps / stairs
    for (r in ramps) {
      if (x >= r.minX && x <= r.maxX && z >= r.minZ && z <= r.maxZ) {
        val t = if (r.axisZ) {
          val raw = (z - r.minZ) / (r.maxZ - r.minZ)
          if (r.positiveDir) raw else 1f - raw
        } else {
          val raw = (x - r.minX) / (r.maxX - r.minX)
          if (r.positiveDir) raw else 1f - raw
        }
        val rampY = MathUtils.lerp(r.startY, r.endY, t)
        if (rampY > bestY) bestY = rampY
      }
    }
    // Check bridge / ghat deck
    for (rd in roads) {
      if (rd.isBridge && x >= rd.x - rd.w * 0.5f && x <= rd.x + rd.w * 0.5f &&
        z >= rd.z - rd.d * 0.5f && z <= rd.z + rd.d * 0.5f
      ) {
        if (y >= rd.y - 1.2f && rd.y > bestY) bestY = rd.y
      }
    }
    // Check walkable building roofs
    for (c in colliders) {
      if (c.isWalkableRoof && c.containsXZ(x, z, 0.35f) && y >= c.maxY - 1.2f) {
        if (c.maxY > bestY) bestY = c.maxY
      }
    }
    return bestY
  }

  fun isInWater(x: Float, z: Float): WaterZone? {
    for (w in waterZones) {
      if (x >= w.minX && x <= w.maxX && z >= w.minZ && z <= w.maxZ) return w
    }
    return null
  }

  fun isSlipperyAt(x: Float, z: Float): Boolean {
    for (s in slipperyZones) {
      if (s.containsXZ(x, z)) return true
    }
    return false
  }
}
