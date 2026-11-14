package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.game.engine.HudSnapshot
import com.example.game.model.DistrictId
import com.example.game.model.Localization
import com.example.game.model.MissionCatalog
import com.example.game.model.VehicleType
import com.example.ui.theme.DhakaEmeraldBright
import com.example.ui.theme.MarigoldGold
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RickshawCrimson

@Composable
fun ModalContainer(
  title: String,
  onClose: () -> Unit,
  content: @Composable () -> Unit,
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xAA060C0F))
      .clickable(onClick = onClose)
      .padding(16.dp),
    contentAlignment = Alignment.Center,
  ) {
    Surface(
      color = Color(0xF5111D23),
      shape = RoundedCornerShape(26.dp),
      tonalElevation = 12.dp,
      modifier = Modifier
        .widthIn(max = 620.dp)
        .fillMaxWidth()
        .fillMaxHeight(0.86f)
        .clickable(enabled = false, onClick = {}),
    ) {
      Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MarigoldGold,
          )
          IconButton(
            onClick = onClose,
            modifier = Modifier.size(48.dp).testTag("modal_close_button"),
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
          content()
        }
      }
    }
  }
}

@Composable
fun MainMenuContent(
  hud: HudSnapshot,
  viewModel: GameViewModel,
  onSignOut: () -> Unit,
) {
  val menuItems = listOf(
    Triple(Icons.Default.PlayArrow, Localization.pick(hud.lang, "Continue Free Roam", "খেলা চালিয়ে যাও"), { viewModel.closeModal() }),
    Triple(Icons.Default.Explore, Localization.pick(hud.lang, "Districts & Map", "এলাকা ও ম্যাপ"), { viewModel.openModal(ActiveModal.WORLD_MAP) }),
    Triple(Icons.Default.SportsEsports, Localization.pick(hud.lang, "30 Optional Missions", "৩০টি ঐচ্ছিক মিশন"), { viewModel.openModal(ActiveModal.MISSIONS) }),
    Triple(Icons.Default.Face, Localization.pick(hud.lang, "Character & Expressions", "চরিত্র সাজাও"), { viewModel.openModal(ActiveModal.CHARACTER) }),
    Triple(Icons.Default.DirectionsCar, Localization.pick(hud.lang, "Garage (11 Vehicles)", "গ্যারেজ (১১ গাড়ি)"), { viewModel.openModal(ActiveModal.GARAGE) }),
    Triple(Icons.Default.Home, Localization.pick(hud.lang, "Shops & My Home", "দোকান ও বাসা"), { viewModel.openModal(ActiveModal.SHOP_AND_HOME) }),
    Triple(Icons.Default.CloudUpload, Localization.pick(hud.lang, "Cloud Save / Load", "ক্লাউড সেভ ও লোড"), { viewModel.openModal(ActiveModal.SAVE_SLOTS) }),
    Triple(Icons.Default.Settings, Localization.pick(hud.lang, "Settings & Weather", "সেটিংস ও আবহাওয়া"), { viewModel.openModal(ActiveModal.SETTINGS) }),
  )

  Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
    LazyVerticalGrid(
      columns = GridCells.Adaptive(minSize = 155.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.weight(1f),
    ) {
      items(menuItems) { (icon, label, action) ->
        Card(
          onClick = action,
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2D35)),
          shape = RoundedCornerShape(18.dp),
          modifier = Modifier.height(88.dp),
        ) {
          Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Icon(icon, contentDescription = label, tint = DhakaEmeraldBright, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = Color.White, maxLines = 1)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
      Button(
        onClick = {
          viewModel.triggerViralClipBurst()
          viewModel.closeModal()
        },
        colors = ButtonDefaults.buttonColors(containerColor = RickshawCrimson),
        modifier = Modifier.weight(1f).height(48.dp).testTag("viral_burst_button"),
      ) {
        Icon(Icons.Default.VideoCall, contentDescription = "Viral Mode", modifier = Modifier.size(18.dp))
        Text(" Viral Chaos!", style = MaterialTheme.typography.labelLarge)
      }
      OutlinedButton(
        onClick = { viewModel.startNewGame() },
        modifier = Modifier.height(48.dp).testTag("new_game_button"),
      ) {
        Icon(Icons.Default.RestartAlt, contentDescription = "New Game")
        Text(" New")
      }
      IconButton(
        onClick = onSignOut,
        modifier = Modifier.size(48.dp).testTag("sign_out_button"),
      ) {
        Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = RickshawCrimson)
      }
    }
  }
}

@Composable
fun MissionsModalContent(
  hud: HudSnapshot,
  viewModel: GameViewModel,
) {
  var selectedDistrict by remember { mutableStateOf<DistrictId?>(null) }
  val filtered = remember(selectedDistrict) {
    if (selectedDistrict == null) MissionCatalog.missions
    else MissionCatalog.missions.filter { it.district == selectedDistrict }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    Text(
      text = Localization.pick(
        hud.lang,
        "Completed: ${hud.completedMissions.size} / 30 • All missions are optional!",
        "সম্পন্ন: ${hud.completedMissions.size} / ৩০ • সব মিশন ঐচ্ছিক!",
      ),
      style = MaterialTheme.typography.bodyMedium,
      color = NeonCyan,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
      FilterChip(
        selected = selectedDistrict == null,
        onClick = { selectedDistrict = null },
        label = { Text("All (30)") },
      )
      FilterChip(
        selected = selectedDistrict == DistrictId.OLD_DHAKA,
        onClick = { selectedDistrict = DistrictId.OLD_DHAKA },
        label = { Text("Old Dhaka") },
      )
      FilterChip(
        selected = selectedDistrict == DistrictId.RESIDENTIAL,
        onClick = { selectedDistrict = DistrictId.RESIDENTIAL },
        label = { Text("Residential") },
      )
    }
    Spacer(modifier = Modifier.height(8.dp))
    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(bottom = 12.dp),
    ) {
      items(filtered, key = { it.id }) { m ->
        val done = hud.completedMissions.contains(m.id)
        Card(
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2930)),
          shape = RoundedCornerShape(16.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = "#${m.number} ${Localization.pick(hud.lang, m.titleEn, m.titleBn)}",
                  style = MaterialTheme.typography.titleMedium,
                  color = MarigoldGold,
                )
                if (done) {
                  Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = DhakaEmeraldBright, modifier = Modifier.size(16.dp))
                }
              }
              Text(
                text = Localization.pick(hud.lang, m.descEn, m.descBn),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCBD5E1),
              )
              Text(
                text = "${Localization.pick(hud.lang, m.district.titleEn, m.district.titleBn)} • ৳${m.rewardTaka} • ${m.timeLimitSec}s",
                style = MaterialTheme.typography.labelSmall,
                color = DhakaEmeraldBright,
              )
            }
            Button(
              onClick = { viewModel.startMission(m) },
              colors = ButtonDefaults.buttonColors(containerColor = DhakaEmeraldBright, contentColor = Color(0xFF042219)),
              modifier = Modifier.height(48.dp).testTag("start_mission_${m.id}"),
            ) {
              Text(if (done) "Replay" else "Play")
            }
          }
        }
      }
    }
  }
}

@Composable
fun GarageModalContent(
  hud: HudSnapshot,
  viewModel: GameViewModel,
) {
  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(bottom = 12.dp),
  ) {
    items(VehicleType.entries) { v ->
      val owned = hud.ownedVehicles.contains(v)
      Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2930)),
        shape = RoundedCornerShape(16.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = Localization.pick(hud.lang, v.titleEn, v.titleBn),
              style = MaterialTheme.typography.titleMedium,
              color = Color.White,
            )
            Text(
              text = "Max ${(v.maxSpeed * 3.6f).toInt()} km/h • ${if (v.isBoat) "Water" else "Road"}",
              style = MaterialTheme.typography.labelSmall,
              color = NeonCyan,
            )
          }
          Button(
            onClick = { viewModel.buyOrSpawnVehicle(v) },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (owned) DhakaEmeraldBright else MarigoldGold,
              contentColor = Color(0xFF061612),
            ),
            modifier = Modifier.height(48.dp).testTag("vehicle_btn_${v.name}"),
          ) {
            Text(
              text = if (owned) Localization.pick(hud.lang, "Spawn", "নামান")
              else "Buy ৳${v.priceTaka}",
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }
    }
  }
}
