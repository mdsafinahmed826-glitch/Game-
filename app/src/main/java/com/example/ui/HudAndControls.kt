package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.game.engine.GameSimulation
import com.example.game.engine.HudSnapshot
import com.example.game.model.Localization
import com.example.game.model.WeatherType
import com.example.ui.theme.CardGlassDark
import com.example.ui.theme.DhakaEmeraldBright
import com.example.ui.theme.MarigoldGold
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RickshawCrimson
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun GameHudOverlay(
  hud: HudSnapshot,
  sim: GameSimulation,
  toastMessage: String?,
  viewModel: GameViewModel,
) {
  if (hud.hideUiViralMode) {
    // Minimal clean exit button for Viral Shorts/Reels recording
    Box(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .padding(16.dp),
      contentAlignment = Alignment.TopEnd,
    ) {
      Surface(
        color = Color(0x880D1418),
        shape = CircleShape,
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .clickable { viewModel.toggleHideUi() }
          .testTag("show_ui_button"),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(Icons.Default.Visibility, contentDescription = "Show HUD", tint = Color.White)
        }
      }
    }
    return
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(12.dp),
  ) {
    // TOP BAR: District, Time/Weather, Money, Mini-Map & Menu
    Row(
      modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top,
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // District + Clock + Weather Pill
        Surface(
          color = CardGlassDark,
          shape = RoundedCornerShape(18.dp),
          modifier = Modifier.border(1.dp, Color(0x4438BDF8), RoundedCornerShape(18.dp)),
        ) {
          Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(Color(hud.district.accentHex))
              )
              Text(
                text = Localization.pick(hud.lang, hud.district.titleEn, hud.district.titleBn),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
              )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "${hud.clockText} • ${Localization.pick(hud.lang, hud.weather.titleEn, hud.weather.titleBn)}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFCBD5E1),
              )
              if (hud.powerOutage) {
                Icon(Icons.Default.Bolt, contentDescription = "Power Outage", tint = RickshawCrimson, modifier = Modifier.size(14.dp))
              }
            }
          }
        }

        // Wallet & Chaos Stars Row
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
          Surface(color = Color(0xDD042219), shape = RoundedCornerShape(14.dp)) {
            Text(
              text = "৳ ${hud.moneyTaka}",
              style = MaterialTheme.typography.labelLarge,
              color = DhakaEmeraldBright,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
          }
          if (hud.wantedStars > 0) {
            Surface(color = Color(0xDD4C0519), shape = RoundedCornerShape(14.dp)) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                repeat(hud.wantedStars) {
                  Icon(Icons.Default.Star, contentDescription = "Chaos Star", tint = MarigoldGold, modifier = Modifier.size(15.dp))
                }
              }
            }
          }
          if (hud.inVehicleType != null) {
            Surface(color = Color(0xCC1E293B), shape = RoundedCornerShape(14.dp)) {
              Text(
                text = "${hud.vehicleSpeedKmh} km/h",
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              )
            }
          }
        }
      }

      // Top-Right: Mini-Map Radar + Quick Menu
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        MiniMapRadar(
          hud = hud,
          sim = sim,
          onClick = { viewModel.openModal(ActiveModal.WORLD_MAP) },
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Surface(
            color = CardGlassDark,
            shape = CircleShape,
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .clickable { viewModel.openModal(ActiveModal.MAIN_MENU) }
              .testTag("menu_button"),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(Icons.Default.Menu, contentDescription = "Main Menu", tint = Color.White)
            }
          }
          Surface(
            color = CardGlassDark,
            shape = CircleShape,
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .clickable { viewModel.toggleMute() }
              .testTag("mute_button"),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (hud.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = "Toggle Sound",
                tint = if (hud.isMuted) RickshawCrimson else DhakaEmeraldBright,
              )
            }
          }
        }
      }
    }

    // CENTER TOP: Active Optional Mission + Random Event / Dialogue Banners
    Column(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 92.dp)
        .fillMaxWidth(0.88f),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      hud.activeMission?.let { m ->
        Surface(
          color = Color(0xE61E293B),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.border(1.5.dp, MarigoldGold, RoundedCornerShape(16.dp)),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "★ ${Localization.pick(hud.lang, m.titleEn, m.titleBn)} (${hud.missionTimeLeftSec}s)",
                style = MaterialTheme.typography.labelLarge,
                color = MarigoldGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = "${Localization.pick(hud.lang, "Target", "লক্ষ্য")}: ${hud.missionProgressText} • ৳${m.rewardTaka}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
              )
            }
            IconButton(
              onClick = { viewModel.abandonMission() },
              modifier = Modifier.size(48.dp).testTag("abandon_mission_button"),
            ) {
              Icon(Icons.Default.Close, contentDescription = "Cancel mission", tint = RickshawCrimson)
            }
          }
        }
      }

      AnimatedVisibility(visible = hud.eventBannerText != null) {
        Surface(color = Color(0xE67F1D1D), shape = RoundedCornerShape(14.dp)) {
          Text(
            text = hud.eventBannerText ?: "",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          )
        }
      }

      AnimatedVisibility(visible = hud.missionResultBanner != null || toastMessage != null) {
        Surface(color = Color(0xE6065F46), shape = RoundedCornerShape(14.dp)) {
          Text(
            text = hud.missionResultBanner ?: toastMessage ?: "",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          )
        }
      }

      AnimatedVisibility(visible = hud.subtitleText != null) {
        Surface(color = Color(0xDD0F172A), shape = RoundedCornerShape(12.dp)) {
          Text(
            text = hud.subtitleText ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MarigoldGold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          )
        }
      }
    }

    // CONTEXT INTERACTION / VEHICLE / GRABBED PROMPTS ABOVE BOTTOM CONTROLS
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 148.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      if (hud.isRagdoll) {
        Surface(color = RickshawCrimson, shape = RoundedCornerShape(50)) {
          Text(
            text = Localization.pick(hud.lang, "💥 RAGDOLL TUMBLE!", "💥 ধপাস!"),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
          )
        }
      }
      if (hud.interactionPrompt != null) {
        Surface(
          color = DhakaEmeraldBright,
          shape = RoundedCornerShape(50),
          modifier = Modifier
            .clickable { viewModel.onInteractClicked() }
            .testTag("context_interact_pill"),
        ) {
          Text(
            text = "[E] ${hud.interactionPrompt}",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF042219),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
          )
        }
      }
      if (hud.nearestVehiclePrompt != null) {
        Surface(
          color = MarigoldGold,
          shape = RoundedCornerShape(50),
          modifier = Modifier
            .clickable { viewModel.onVehicleToggleClicked() }
            .testTag("context_vehicle_pill"),
        ) {
          Text(
            text = "[F] ${hud.nearestVehiclePrompt}",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF231800),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          )
        }
      }
      if (hud.grabbedPropLabel != null) {
        Surface(color = Color(0xDD1E293B), shape = RoundedCornerShape(50)) {
          Text(
            text = "${Localization.pick(hud.lang, "Holding", "হাতে আছে")}: ${hud.grabbedPropLabel}",
            style = MaterialTheme.typography.labelSmall,
            color = NeonCyan,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
          )
        }
      }
    }

    // BOTTOM LEFT: Virtual Analog Joystick
    VirtualJoystick(
      modifier = Modifier.align(Alignment.BottomStart).padding(6.dp),
      onMove = { x, y ->
        sim.moveInputX = x
        sim.moveInputY = y
      },
    )

    // BOTTOM CENTER: Social Media Viral Mode Quick Bar
    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      SmallViralChip(
        icon = Icons.Default.SlowMotionVideo,
        label = "0.35x",
        active = hud.slowMotionActive,
        tag = "viral_slowmo_button",
        onClick = { viewModel.toggleSlowMotion() },
      )
      SmallViralChip(
        icon = Icons.Default.CameraAlt,
        label = "Cam",
        active = hud.cameraMode != com.example.game.model.CameraAngleMode.THIRD_PERSON,
        tag = "viral_cam_button",
        onClick = { viewModel.cycleCameraAngle() },
      )
      SmallViralChip(
        icon = Icons.Default.VisibilityOff,
        label = "Clean",
        active = false,
        tag = "viral_hide_ui_button",
        onClick = { viewModel.toggleHideUi() },
      )
    }

    // BOTTOM RIGHT: Tactile Action Buttons
    Column(
      modifier = Modifier.align(Alignment.BottomEnd),
      horizontalAlignment = Alignment.End,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ActionCircleButton(
          icon = if (hud.inVehicleType != null) Icons.Default.NotificationsActive else Icons.Default.PanTool,
          label = if (hud.inVehicleType != null) "Horn" else if (hud.grabbedPropLabel != null) "Throw" else "Grab",
          tint = MarigoldGold,
          tag = "grab_or_horn_button",
          onClick = {
            if (hud.inVehicleType != null) viewModel.onHornClicked()
            else viewModel.onGrabThrowClicked()
          },
        )
        ActionCircleButton(
          icon = Icons.Default.TouchApp,
          label = "Act",
          tint = DhakaEmeraldBright,
          tag = "interact_button",
          onClick = { viewModel.onInteractClicked() },
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ActionCircleButton(
          icon = Icons.Default.DirectionsCar,
          label = if (hud.inVehicleType != null) "Exit" else "Ride",
          tint = NeonCyan,
          tag = "vehicle_button",
          onClick = { viewModel.onVehicleToggleClicked() },
        )
        ActionCircleButton(
          icon = Icons.Default.DirectionsRun,
          label = if (hud.inVehicleType != null) "Turbo" else "Jump",
          tint = if (hud.inVehicleType != null) RickshawCrimson else Color.White,
          tag = "jump_button",
          onClick = {
            if (hud.inVehicleType != null) {
              sim.isSprinting = !sim.isSprinting
              viewModel.onHornClicked()
            } else {
              viewModel.onJumpClicked()
            }
          },
        )
      }
    }
  }
}

@Composable
private fun VirtualJoystick(
  modifier: Modifier = Modifier,
  onMove: (Float, Float) -> Unit,
) {
  var knobX by remember { mutableFloatStateOf(0f) }
  var knobY by remember { mutableFloatStateOf(0f) }
  val maxRadius = 110f

  Box(
    modifier = modifier
      .size(126.dp)
      .clip(CircleShape)
      .background(Color(0x550F172A))
      .border(2.dp, Color(0x7710B981), CircleShape)
      .testTag("virtual_joystick")
      .pointerInput(Unit) {
        detectDragGestures(
          onDragEnd = {
            knobX = 0f; knobY = 0f
            onMove(0f, 0f)
          },
          onDragCancel = {
            knobX = 0f; knobY = 0f
            onMove(0f, 0f)
          },
          onDrag = { change, dragAmount ->
            change.consume()
            val nx = knobX + dragAmount.x
            val ny = knobY + dragAmount.y
            val dist = sqrt(nx * nx + ny * ny)
            if (dist > maxRadius) {
              knobX = nx * maxRadius / dist
              knobY = ny * maxRadius / dist
            } else {
              knobX = nx; knobY = ny
            }
            // Forward on joystick (negative screen Y) -> positive throttle/forward (+Y input)
            onMove(knobX / maxRadius, -knobY / maxRadius)
          },
        )
      },
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier
        .offset { IntOffset(knobX.roundToInt(), knobY.roundToInt()) }
        .size(52.dp)
        .clip(CircleShape)
        .background(DhakaEmeraldBright.copy(alpha = 0.85f))
        .border(2.dp, Color.White, CircleShape)
    )
  }
}

@Composable
private fun ActionCircleButton(
  icon: ImageVector,
  label: String,
  tint: Color,
  tag: String,
  onClick: () -> Unit,
) {
  Surface(
    color = CardGlassDark,
    shape = CircleShape,
    modifier = Modifier
      .size(58.dp)
      .border(1.5.dp, tint.copy(alpha = 0.7f), CircleShape)
      .clip(CircleShape)
      .clickable(onClick = onClick)
      .testTag(tag),
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
      Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
  }
}

@Composable
private fun SmallViralChip(
  icon: ImageVector,
  label: String,
  active: Boolean,
  tag: String,
  onClick: () -> Unit,
) {
  Surface(
    color = if (active) RickshawCrimson else CardGlassDark,
    shape = RoundedCornerShape(50),
    modifier = Modifier
      .height(48.dp)
      .clip(RoundedCornerShape(50))
      .clickable(onClick = onClick)
      .testTag(tag),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(16.dp))
      Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
  }
}

@Composable
private fun MiniMapRadar(
  hud: HudSnapshot,
  sim: GameSimulation,
  onClick: () -> Unit,
) {
  Surface(
    color = Color(0xCC0A1920),
    shape = RoundedCornerShape(18.dp),
    modifier = Modifier
      .size(86.dp)
      .border(1.5.dp, Color(0x8838BDF8), RoundedCornerShape(18.dp))
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
      .testTag("minimap_button"),
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val cx = size.width / 2f
      val cy = size.height / 2f
      val scale = 0.45f

      // Buriganga river band
      val riverOffset = (160f - hud.playerZ) * scale
      drawRect(
        color = Color(0xAA0284C7),
        topLeft = Offset(0f, cy + riverOffset - 16f),
        size = androidx.compose.ui.geometry.Size(size.width, 36f),
      )

      // Nearby vehicles (yellow dots)
      for (v in sim.vehicles.vehicles) {
        val vx = cx + (v.x - hud.playerX) * scale
        val vz = cy + (v.z - hud.playerZ) * scale
        if (vx in 4f..size.width - 4f && vz in 4f..size.height - 4f) {
          drawCircle(MarigoldGold, radius = 2.6f, center = Offset(vx, vz))
        }
      }

      // Active Mission Target (blinking crimson dot)
      hud.activeMission?.let { m ->
        val mx = (cx + (m.targetX - hud.playerX) * scale).coerceIn(6f, size.width - 6f)
        val mz = (cy + (m.targetZ - hud.playerZ) * scale).coerceIn(6f, size.height - 6f)
        drawCircle(RickshawCrimson, radius = 5.5f, center = Offset(mx, mz))
      }

      // Player indicator + facing ray
      val fx = sin(hud.playerYaw) * 11f
      val fz = cos(hud.playerYaw) * 11f
      drawLine(Color.White, Offset(cx, cy), Offset(cx + fx, cy + fz), strokeWidth = 2.5f)
      drawCircle(DhakaEmeraldBright, radius = 4.2f, center = Offset(cx, cy))
    }
  }
}
