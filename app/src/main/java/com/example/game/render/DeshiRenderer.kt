package com.example.game.render

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.game.engine.GameSimulation
import com.example.game.engine.MathUtils
import com.example.game.engine.NpcMood
import com.example.game.engine.VehicleEntity
import com.example.game.model.CameraAngleMode
import com.example.game.model.GlassesStyle
import com.example.game.model.HatStyle
import com.example.game.model.InteractableKind
import com.example.game.model.NpcRole
import com.example.game.model.VehicleType
import com.example.game.model.WeatherType
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class DeshiRenderer(private val sim: GameSimulation) : GLSurfaceView.Renderer {
  private lateinit var shader: ShaderProgram
  private lateinit var meshes: MeshBuffers

  private val projMatrix = FloatArray(16)
  private val viewMatrix = FloatArray(16)
  private val vpMatrix = FloatArray(16)
  private val modelMatrix = FloatArray(16)
  private val mvpMatrix = FloatArray(16)

  private var lastTimeNs = System.nanoTime()
  private var elapsedSec = 0f

  override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    GLES20.glEnable(GLES20.GL_BLEND)
    GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    shader = ShaderProgram()
    meshes = MeshBuffers()
  }

  override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    GLES20.glViewport(0, 0, width, height)
    val ratio = width.toFloat() / height.coerceAtLeast(1).toFloat()
    Matrix.perspectiveM(projMatrix, 0, 58f, ratio, 0.3f, 340f)
  }

  override fun onDrawFrame(gl: GL10?) {
    val now = System.nanoTime()
    val dt = ((now - lastTimeNs) / 1_000_000_000.0f).coerceIn(0.001f, 0.05f)
    lastTimeNs = now
    elapsedSec += dt

    sim.step(dt)

    val (skyR, skyG, skyB) = sim.weather.skyColorRgb()
    val sunInt = sim.weather.sunIntensity()
    val fogDensity = when (sim.weather.weather) {
      WeatherType.FOG -> 0.015f
      WeatherType.MONSOON -> 0.011f
      WeatherType.RAIN -> 0.008f
      else -> 0.0042f
    }

    GLES20.glClearColor(skyR, skyG, skyB, 1f)
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    GLES20.glUseProgram(shader.programId)

    // Camera placement
    val px = sim.physics.player.x
    val py = sim.physics.player.y
    val pz = sim.physics.player.z
    val yaw = sim.cameraYaw
    val pitch = sim.cameraPitch

    when (sim.cameraMode) {
      CameraAngleMode.THIRD_PERSON -> {
        val camX = px + sin(yaw) * cos(pitch) * sim.cameraDist
        val camY = (py + 1.5f + sin(pitch) * sim.cameraDist).coerceAtLeast(1.2f)
        val camZ = pz + cos(yaw) * cos(pitch) * sim.cameraDist
        Matrix.setLookAtM(viewMatrix, 0, camX, camY, camZ, px, py + 1.2f, pz, 0f, 1f, 0f)
      }
      CameraAngleMode.CINEMATIC -> {
        val orbit = elapsedSec * 0.55f + yaw
        val camX = px + sin(orbit) * 7.5f
        val camY = py + 3.2f
        val camZ = pz + cos(orbit) * 7.5f
        Matrix.setLookAtM(viewMatrix, 0, camX, camY, camZ, px, py + 1.1f, pz, 0f, 1f, 0f)
      }
      CameraAngleMode.LOW_ACTION -> {
        val camX = px + sin(yaw) * 4.5f
        val camY = py + 0.85f
        val camZ = pz + cos(yaw) * 4.5f
        Matrix.setLookAtM(viewMatrix, 0, camX, camY, camZ, px, py + 1.3f, pz, 0f, 1f, 0f)
      }
      CameraAngleMode.FIRST_PERSON -> {
        val lookX = px + sin(sim.physics.player.yaw) * 10f
        val lookZ = pz + cos(sim.physics.player.yaw) * 10f
        Matrix.setLookAtM(viewMatrix, 0, px, py + 1.65f, pz, lookX, py + 1.5f, lookZ, 0f, 1f, 0f)
      }
    }

    Matrix.multiplyMM(vpMatrix, 0, projMatrix, 0, viewMatrix, 0)

    GLES20.glUniform3f(shader.uLightDirLoc, 0.55f, 0.85f, 0.40f)
    GLES20.glUniform1f(shader.uSunIntensityLoc, sunInt)
    GLES20.glUniform3f(shader.uFogColorLoc, skyR, skyG, skyB)
    GLES20.glUniform1f(shader.uFogDensityLoc, fogDensity)

    val drawDistSq = sim.graphicsQuality.drawDistance * sim.graphicsQuality.drawDistance
    val isNight = sim.weather.timeOfDayHours < 6f || sim.weather.timeOfDayHours >= 18.5f
    val lightsOn = isNight && !sim.weather.powerOutageActive

    // 1. District Ground Planes
    drawBox(0f, -0.5f, 0f, 160f, 1f, 180f, 0f, 0f, 0f, 0xFF78716C.toInt()) // Old Dhaka stone
    drawBox(0f, -0.5f, -155f, 160f, 1f, 130f, 0f, 0f, 0f, 0xFF334155.toInt()) // Modern Dhaka plaza
    drawBox(-155f, -0.5f, 0f, 150f, 1f, 175f, 0f, 0f, 0f, 0xFF15803D.toInt()) // Residential lush green
    drawBox(-155f, -0.5f, -155f, 150f, 1f, 135f, 0f, 0f, 0f, 0xFF4D7C0F.toInt()) // Village paddy green
    drawBox(155f, -0.5f, -45f, 150f, 1f, 240f, 0f, 0f, 0f, 0xFF52525B.toInt()) // Commercial concrete
    drawBox(0f, -0.55f, 110f, 460f, 1f, 60f, 0f, 0f, 0f, 0xFFD6B98C.toInt()) // Riverside ghat bank

    // 2. Animated Buriganga River & Village Pond
    val waveY = -0.35f + sin(elapsedSec * 2.4f) * 0.05f
    for (w in sim.world.waterZones) {
      val cx = (w.minX + w.maxX) * 0.5f
      val cz = (w.minZ + w.maxZ) * 0.5f
      val ww = w.maxX - w.minX
      val wd = w.maxZ - w.minZ
      drawBox(cx, waveY - 0.5f, cz, ww, 1f, wd, 0f, 0f, 0f, 0xCC0284C7.toInt(), emissive = 0.06f)
    }

    // 3. Roads, Bridges & Stairs
    for (rd in sim.world.roads) {
      val col = if (rd.isMud) 0xFF92400E.toInt() else 0xFF1E293B.toInt()
      drawBox(rd.x, rd.y, rd.z, rd.w, 0.08f, rd.d, 0f, 0f, 0f, col)
    }
    for (r in sim.world.ramps) {
      val steps = 8
      for (i in 0 until steps) {
        val t = (i + 0.5f) / steps
        val sy = MathUtils.lerp(r.startY, r.endY, t)
        val sx = if (r.axisZ) (r.minX + r.maxX) * 0.5f else MathUtils.lerp(r.minX, r.maxX, if (r.positiveDir) t else 1f - t)
        val sz = if (r.axisZ) MathUtils.lerp(r.minZ, r.maxZ, if (r.positiveDir) t else 1f - t) else (r.minZ + r.maxZ) * 0.5f
        val sw = if (r.axisZ) (r.maxX - r.minX) else (r.maxX - r.minX) / steps
        val sd = if (r.axisZ) (r.maxZ - r.minZ) / steps else (r.maxZ - r.minZ)
        drawBox(sx, sy * 0.5f, sz, sw, sy.coerceAtLeast(0.3f), sd, 0f, 0f, 0f, 0xFF94A3B8.toInt())
      }
    }

    // 4. Buildings (with distance culling)
    for (b in sim.world.buildings) {
      val dx = b.x - px; val dz = b.z - pz
      if (dx * dx + dz * dz > drawDistSq) continue
      drawBox(b.x, b.y, b.z, b.w, b.h, b.d, 0f, 0f, 0f, b.colorArgb)
      // Roof cap
      drawBox(b.x, b.h + 0.25f, b.z, b.w + 0.5f, 0.5f, b.d + 0.5f, 0f, 0f, 0f, b.roofColorArgb)
      // Rooftop Gazi water tank on walkable buildings
      if (b.isWalkableRoof) {
        drawCylinder(b.x - 2f, b.h + 1.4f, b.z - 2f, 1.4f, 1.8f, 0f, 0xFF0F172A.toInt())
      }
      // Illuminated windows at night (when no load shedding)
      if (lightsOn && b.hasWindows) {
        drawBox(b.x, b.h * 0.55f, b.z + b.d * 0.5f + 0.05f, b.w * 0.65f, b.h * 0.5f, 0.1f, 0f, 0f, 0f, 0xFFFDE047.toInt(), emissive = 0.65f)
      }
    }

    // 5. Trees & Streetlamps
    for (dec in sim.world.decors) {
      val dx = dec.x - px; val dz = dec.z - pz
      if (dx * dx + dz * dz > drawDistSq) continue
      if (dec.isStreetLamp) {
        drawCylinder(dec.x, 2.5f, dec.z, 0.16f, 5f, 0f, 0xFF475569.toInt())
        val bulbCol = if (lightsOn) 0xFFFEF08A.toInt() else 0xFF64748B.toInt()
        drawBox(dec.x, 5.1f, dec.z, 0.55f, 0.35f, 0.55f, 0f, 0f, 0f, bulbCol, emissive = if (lightsOn) 0.9f else 0f)
      } else {
        drawCylinder(dec.x, 1.6f * dec.scale, dec.z, 0.45f * dec.scale, 3.2f * dec.scale, 0f, 0xFF78350F.toInt())
        val leafColor = if (dec.isBananaTree) 0xFF65A30D.toInt() else 0xFF15803D.toInt()
        drawBox(dec.x, 3.6f * dec.scale, dec.z, 2.6f * dec.scale, 1.9f * dec.scale, 2.6f * dec.scale, 0f, 0f, 0f, leafColor)
      }
    }

    // 6. Hanging Festival Bulb Ropes (Pendulum Physics)
    for (rope in sim.world.ropes) {
      val midX = (rope.ax + rope.bx) * 0.5f
      val midZ = (rope.az + rope.bz) * 0.5f
      if ((midX - px) * (midX - px) + (midZ - pz) * (midZ - pz) > drawDistSq) continue
      val sag = 0.8f
      for (i in 0..6) {
        val t = i / 6f
        val rx = MathUtils.lerp(rope.ax, rope.bx, t) + sin(rope.swingAngle) * 0.5f * sin(t * 3.14f)
        val ry = MathUtils.lerp(rope.ay, rope.by, t) - sin(t * 3.14f) * sag
        val rz = MathUtils.lerp(rope.az, rope.bz, t)
        val col = if (lightsOn || i % 2 == 0) rope.bulbColor else 0xFF475569.toInt()
        drawBox(rx, ry, rz, 0.25f, 0.25f, 0.25f, 0f, 0f, 0f, col, emissive = if (lightsOn) 0.85f else 0.15f)
      }
    }

    // 7. Interactive Stations & Glowing Prompts
    for (station in sim.world.interactables) {
      val dx = station.x - px; val dz = station.z - pz
      if (dx * dx + dz * dz > drawDistSq) continue
      val markerColor = when (station.kind) {
        InteractableKind.TEA_STALL -> 0xFFD97706.toInt()
        InteractableKind.STREET_FOOD -> 0xFFE11D48.toInt()
        InteractableKind.VEHICLE_DEALER -> 0xFF38BDF8.toInt()
        InteractableKind.ELECTRIC_BOX -> if (station.stateOn) 0xFF10B981.toInt() else 0xFFEF4444.toInt()
        InteractableKind.HOME_DOOR -> 0xFFFFB703.toInt()
        else -> 0xFF10B981.toInt()
      }
      drawBox(station.x, station.y + 0.6f, station.z, 1.3f, 1.2f, 1.3f, 0f, 0f, 0f, markerColor)
      val bob = sin(elapsedSec * 3.5f + station.x) * 0.18f
      drawBox(station.x, station.y + 1.85f + bob, station.z, 0.45f, 0.45f, 0.45f, 0f, elapsedSec * 1.5f, 0f, 0xFFFFB703.toInt(), emissive = 0.6f)
    }

    // 8. Active Mission Target Beacon Pillar
    sim.activeMission?.let { m ->
      drawCylinder(m.targetX, 12f, m.targetZ, 1.8f, 24f, 0f, 0x66FFB703, emissive = 0.8f)
      drawCylinder(m.targetX, 0.25f, m.targetZ, 4.2f, 0.2f, 0f, 0xAAF42A41.toInt(), emissive = 0.7f)
    }

    // 9. Dynamic Props & Debris
    for (p in sim.physics.props) {
      if (p.isBroken) continue
      val dx = p.x - px; val dz = p.z - pz
      if (dx * dx + dz * dz > drawDistSq) continue
      val s = p.kind.halfSize * 2f
      drawBox(p.x, p.y, p.z, s, s, s, p.rotX, p.rotY, 0f, p.kind.colorArgb)
    }
    for (pt in sim.physics.particles) {
      drawBox(pt.x, pt.y, pt.z, pt.size, pt.size, pt.size, elapsedSec * 4f, elapsedSec * 3f, 0f, pt.colorArgb)
    }

    // 10. Drivable Vehicles
    for (v in sim.vehicles.vehicles) {
      val dx = v.x - px; val dz = v.z - pz
      if (dx * dx + dz * dz > drawDistSq) continue
      drawVehicle(v, lightsOn)
    }

    // 11. Living NPCs & Animals
    for (n in sim.npcs.npcs) {
      val dx = n.x - px; val dz = n.z - pz
      if (dx * dx + dz * dz > drawDistSq) continue
      val swing = if (n.mood == NpcMood.RAGDOLL) 0.9f else sin(n.walkPhase) * 0.55f
      if (n.role.isAnimal) {
        val scale = if (n.role == NpcRole.COW) 1.3f else if (n.role == NpcRole.CHICKEN) 0.45f else 0.75f
        drawBox(n.x, n.y + 0.5f * scale, n.z, 0.55f * scale, 0.5f * scale, 1.0f * scale, n.ragdollPitch, n.yaw, n.ragdollRoll, n.role.shirtColor)
        drawBox(n.x + sin(n.yaw) * 0.55f * scale, n.y + 0.75f * scale, n.z + cos(n.yaw) * 0.55f * scale, 0.35f * scale, 0.35f * scale, 0.35f * scale, 0f, n.yaw, 0f, n.role.shirtColor)
      } else {
        drawHumanoid(
          x = n.x, y = n.y, z = n.z, yaw = n.yaw,
          ragPitch = n.ragdollPitch, ragRoll = n.ragdollRoll, limbSwing = swing,
          shirtColor = n.role.shirtColor, pantsColor = 0xFF1E293B.toInt(), skinColor = 0xFFC68642.toInt(),
          hat = if (n.role == NpcRole.POLICE) HatStyle.CAP else HatStyle.NONE,
          glasses = GlassesStyle.NONE, isCrouching = false,
        )
      }
    }

    // 12. Player Character (hidden if First Person or inside closed Bus/Car)
    val inVeh = sim.vehicles.activeVehicle()
    if (sim.cameraMode != CameraAngleMode.FIRST_PERSON && (inVeh == null || inVeh.type == VehicleType.RICKSHAW || inVeh.type == VehicleType.BICYCLE || inVeh.type == VehicleType.MOTORCYCLE || inVeh.type == VehicleType.SCOOTER || inVeh.type == VehicleType.BOAT)) {
      val p = sim.physics.player
      val app = sim.appearance
      val limbSwing = if (p.isRagdoll) 1.0f else if (p.vx * p.vx + p.vz * p.vz > 0.2f) sin(p.walkPhase) * 0.65f else 0f
      drawHumanoid(
        x = p.x, y = p.y, z = p.z, yaw = p.yaw,
        ragPitch = p.ragdollPitch, ragRoll = p.ragdollRoll, limbSwing = limbSwing,
        shirtColor = app.shirtColor.argb, pantsColor = app.pantsColor.argb, skinColor = app.skinTone.argb,
        hat = app.hatStyle, glasses = app.glassesStyle, isCrouching = p.isCrouching || p.sittingTimer > 0f,
      )
    }

    // 13. Monsoon / Rain 3D Streaks
    if (sim.weather.weather == WeatherType.RAIN || sim.weather.weather == WeatherType.MONSOON) {
      val drops = if (sim.weather.weather == WeatherType.MONSOON) 55 else 25
      for (i in 0 until drops) {
        val rx = px + (Random(i * 13 + (elapsedSec * 20).toInt()).nextFloat() - 0.5f) * 28f
        val rz = pz + (Random(i * 31 + (elapsedSec * 20).toInt()).nextFloat() - 0.5f) * 28f
        val ry = py + Random(i * 7 + (elapsedSec * 30).toInt()).nextFloat() * 14f
        drawBox(rx, ry, rz, 0.04f, 0.9f, 0.04f, 0.1f, 0f, 0f, 0xAA93C5FD.toInt(), emissive = 0.2f)
      }
    }
  }

  private fun drawVehicle(v: VehicleEntity, lightsOn: Boolean) {
    val y = v.y + v.suspensionBounce
    when (v.type) {
      VehicleType.RICKSHAW -> {
        drawBox(v.x, y + 0.55f, v.z, 1.3f, 0.45f, 2.2f, 0f, v.yaw, 0f, v.type.primaryColor)
        drawBox(v.x - sin(v.yaw) * 0.35f, y + 1.35f, v.z - cos(v.yaw) * 0.35f, 1.35f, 0.95f, 1.1f, 0f, v.yaw, 0f, v.type.secondaryColor)
        drawWheel(v.x + sin(v.yaw) * 0.85f, y + 0.35f, v.z + cos(v.yaw) * 0.85f, v.yaw + v.steerAngle)
        drawWheel(v.x - sin(v.yaw) * 0.7f + cos(v.yaw) * 0.68f, y + 0.38f, v.z - cos(v.yaw) * 0.7f - sin(v.yaw) * 0.68f, v.yaw)
        drawWheel(v.x - sin(v.yaw) * 0.7f - cos(v.yaw) * 0.68f, y + 0.38f, v.z - cos(v.yaw) * 0.7f + sin(v.yaw) * 0.68f, v.yaw)
      }
      VehicleType.CNG -> {
        drawBox(v.x, y + 0.8f, v.z, 1.5f, 1.1f, 2.5f, 0f, v.yaw, 0f, v.type.primaryColor)
        drawBox(v.x, y + 1.48f, v.z, 1.45f, 0.28f, 2.2f, 0f, v.yaw, 0f, v.type.secondaryColor)
      }
      VehicleType.LOCAL_BUS -> {
        drawBox(v.x, y + 1.4f, v.z, 2.4f, 2.2f, 6.4f, 0f, v.yaw, 0f, v.type.primaryColor)
        drawBox(v.x, y + 1.85f, v.z, 2.45f, 0.65f, 5.6f, 0f, v.yaw, 0f, v.type.secondaryColor)
      }
      VehicleType.MINI_TRUCK, VehicleType.PICKUP, VehicleType.DELIVERY_VAN -> {
        drawBox(v.x, y + 1.0f, v.z, 2.0f, 1.35f, 4.0f, 0f, v.yaw, 0f, v.type.primaryColor)
        drawBox(v.x + sin(v.yaw) * 1.1f, y + 1.15f, v.z + cos(v.yaw) * 1.1f, 1.95f, 1.15f, 1.4f, 0f, v.yaw, 0f, v.type.secondaryColor)
      }
      VehicleType.BOAT -> {
        drawBox(v.x, y + 0.3f, v.z, 1.6f, 0.55f, 4.4f, 0f, v.yaw, 0f, v.type.primaryColor)
        drawBox(v.x, y + 0.9f, v.z, 1.2f, 0.55f, 1.8f, 0f, v.yaw, 0f, v.type.secondaryColor)
      }
      VehicleType.BICYCLE, VehicleType.MOTORCYCLE, VehicleType.SCOOTER -> {
        drawBox(v.x, y + 0.62f, v.z, 0.45f, 0.65f, 1.8f, 0f, v.yaw, 0f, v.type.primaryColor)
        drawWheel(v.x + sin(v.yaw) * 0.68f, y + 0.34f, v.z + cos(v.yaw) * 0.68f, v.yaw + v.steerAngle)
        drawWheel(v.x - sin(v.yaw) * 0.68f, y + 0.34f, v.z - cos(v.yaw) * 0.68f, v.yaw)
      }
      VehicleType.PRIVATE_CAR -> {
        drawBox(v.x, y + 0.68f, v.z, 1.9f, 0.72f, 4.1f, 0f, v.yaw, 0f, v.type.primaryColor)
        drawBox(v.x, y + 1.28f, v.z, 1.7f, 0.55f, 2.2f, 0f, v.yaw, 0f, v.type.secondaryColor)
      }
    }
    if (lightsOn && !v.type.isBoat) {
      drawBox(v.x + sin(v.yaw) * 1.25f, y + 0.7f, v.z + cos(v.yaw) * 1.25f, 0.6f, 0.22f, 0.15f, 0f, v.yaw, 0f, 0xFFFEF08A.toInt(), emissive = 0.9f)
    }
  }

  private fun drawWheel(x: Float, y: Float, z: Float, yaw: Float) {
    drawBox(x, y, z, 0.18f, 0.65f, 0.65f, 0f, yaw, 0f, 0xFF0F172A.toInt())
  }

  private fun drawHumanoid(
    x: Float, y: Float, z: Float, yaw: Float,
    ragPitch: Float, ragRoll: Float, limbSwing: Float,
    shirtColor: Int, pantsColor: Int, skinColor: Int,
    hat: HatStyle, glasses: GlassesStyle, isCrouching: Boolean,
  ) {
    val crouchOffset = if (isCrouching) -0.35f else 0f
    val rx = cos(yaw); val rz = -sin(yaw)
    // Torso
    drawBox(x, y + 1.08f + crouchOffset, z, 0.56f, 0.72f, 0.32f, ragPitch, yaw, ragRoll, shirtColor)
    // Head
    drawBox(x, y + 1.66f + crouchOffset, z, 0.38f, 0.38f, 0.38f, ragPitch, yaw, ragRoll, skinColor)
    // Hat
    if (hat != HatStyle.NONE) {
      val hatCol = when (hat) {
        HatStyle.GAMCHA -> 0xFFE11D48.toInt()
        HatStyle.CAP -> 0xFF059669.toInt()
        HatStyle.TOPI -> 0xFFF8FAFC.toInt()
        HatStyle.RICKSHAW_HAT -> 0xFFEAB308.toInt()
        HatStyle.NONE -> 0
      }
      val hatW = if (hat == HatStyle.RICKSHAW_HAT) 0.75f else 0.44f
      drawBox(x, y + 1.90f + crouchOffset, z, hatW, 0.16f, hatW, ragPitch, yaw, ragRoll, hatCol)
    }
    // Glasses
    if (glasses != GlassesStyle.NONE) {
      drawBox(x + sin(yaw) * 0.2f, y + 1.68f + crouchOffset, z + cos(yaw) * 0.2f, 0.34f, 0.11f, 0.08f, ragPitch, yaw, ragRoll, 0xFF0F172A.toInt())
    }
    // Legs
    val legH = if (isCrouching) 0.42f else 0.68f
    drawBox(x + rx * 0.15f, y + 0.38f, z + rz * 0.15f, 0.22f, legH, 0.22f, ragPitch + limbSwing, yaw, ragRoll, pantsColor)
    drawBox(x - rx * 0.15f, y + 0.38f, z - rz * 0.15f, 0.22f, legH, 0.22f, ragPitch - limbSwing, yaw, ragRoll, pantsColor)
    // Arms
    drawBox(x + rx * 0.40f, y + 1.05f + crouchOffset, z + rz * 0.40f, 0.18f, 0.62f, 0.18f, ragPitch - limbSwing, yaw, ragRoll, skinColor)
    drawBox(x - rx * 0.40f, y + 1.05f + crouchOffset, z - rz * 0.40f, 0.18f, 0.62f, 0.18f, ragPitch + limbSwing, yaw, ragRoll, skinColor)
  }

  private fun drawBox(
    x: Float, y: Float, z: Float,
    sx: Float, sy: Float, sz: Float,
    rx: Float, ry: Float, rz: Float,
    argb: Int, emissive: Float = 0f,
  ) {
    Matrix.setIdentityM(modelMatrix, 0)
    Matrix.translateM(modelMatrix, 0, x, y, z)
    if (ry != 0f) Matrix.rotateM(modelMatrix, 0, Math.toDegrees(ry.toDouble()).toFloat(), 0f, 1f, 0f)
    if (rx != 0f) Matrix.rotateM(modelMatrix, 0, Math.toDegrees(rx.toDouble()).toFloat(), 1f, 0f, 0f)
    if (rz != 0f) Matrix.rotateM(modelMatrix, 0, Math.toDegrees(rz.toDouble()).toFloat(), 0f, 0f, 1f)
    Matrix.scaleM(modelMatrix, 0, sx, sy, sz)
    Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)

    val a = ((argb ushr 24) and 0xFF) / 255f
    val r = ((argb ushr 16) and 0xFF) / 255f
    val g = ((argb ushr 8) and 0xFF) / 255f
    val b = (argb and 0xFF) / 255f

    GLES20.glUniformMatrix4fv(shader.uMvpLoc, 1, false, mvpMatrix, 0)
    GLES20.glUniformMatrix4fv(shader.uModelLoc, 1, false, modelMatrix, 0)
    GLES20.glUniform4f(shader.uColorLoc, r, g, b, a)
    GLES20.glUniform1f(shader.uEmissiveLoc, emissive)

    GLES20.glEnableVertexAttribArray(shader.aPosLoc)
    GLES20.glVertexAttribPointer(shader.aPosLoc, 3, GLES20.GL_FLOAT, false, 0, meshes.cubePos)
    GLES20.glEnableVertexAttribArray(shader.aNormLoc)
    GLES20.glVertexAttribPointer(shader.aNormLoc, 3, GLES20.GL_FLOAT, false, 0, meshes.cubeNorm)
    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, meshes.cubeCount)
  }

  private fun drawCylinder(
    x: Float, y: Float, z: Float,
    diameter: Float, height: Float, ry: Float,
    argb: Int, emissive: Float = 0f,
  ) {
    Matrix.setIdentityM(modelMatrix, 0)
    Matrix.translateM(modelMatrix, 0, x, y, z)
    if (ry != 0f) Matrix.rotateM(modelMatrix, 0, Math.toDegrees(ry.toDouble()).toFloat(), 0f, 1f, 0f)
    Matrix.scaleM(modelMatrix, 0, diameter, height, diameter)
    Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)

    val a = ((argb ushr 24) and 0xFF) / 255f
    val r = ((argb ushr 16) and 0xFF) / 255f
    val g = ((argb ushr 8) and 0xFF) / 255f
    val b = (argb and 0xFF) / 255f

    GLES20.glUniformMatrix4fv(shader.uMvpLoc, 1, false, mvpMatrix, 0)
    GLES20.glUniformMatrix4fv(shader.uModelLoc, 1, false, modelMatrix, 0)
    GLES20.glUniform4f(shader.uColorLoc, r, g, b, a)
    GLES20.glUniform1f(shader.uEmissiveLoc, emissive)

    GLES20.glEnableVertexAttribArray(shader.aPosLoc)
    GLES20.glVertexAttribPointer(shader.aPosLoc, 3, GLES20.GL_FLOAT, false, 0, meshes.cylPos)
    GLES20.glEnableVertexAttribArray(shader.aNormLoc)
    GLES20.glVertexAttribPointer(shader.aNormLoc, 3, GLES20.GL_FLOAT, false, 0, meshes.cylNorm)
    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, meshes.cylCount)
  }
}
