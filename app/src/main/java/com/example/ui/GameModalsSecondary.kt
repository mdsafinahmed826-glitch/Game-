package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.SaveSlot
import com.example.game.engine.HudSnapshot
import com.example.game.model.AppLang
import com.example.game.model.DistrictId
import com.example.game.model.ExpressionType
import com.example.game.model.GlassesStyle
import com.example.game.model.GraphicsQuality
import com.example.game.model.HairStyle
import com.example.game.model.HatStyle
import com.example.game.model.Localization
import com.example.game.model.PantsColor
import com.example.game.model.ShirtColor
import com.example.game.model.ShopCatalog
import com.example.game.model.ShopItemCategory
import com.example.game.model.SkinTone
import com.example.game.model.WeatherType
import com.example.ui.theme.DhakaEmeraldBright
import com.example.ui.theme.MarigoldGold
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RickshawCrimson

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CharacterModalContent(
  hud: HudSnapshot,
  viewModel: GameViewModel,
) {
  val app = hud.appearance
  Column(
    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Surface(color = Color(0xFF1A2930), shape = RoundedCornerShape(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Box(
          modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(app.shirtColor.argb)),
          contentAlignment = Alignment.Center,
        ) {
          Text(app.expression.emoji, style = MaterialTheme.typography.headlineMedium)
        }
        Column {
          Text("Expression: ${Localization.pick(hud.lang, app.expression.titleEn, app.expression.titleBn)}", color = MarigoldGold, style = MaterialTheme.typography.titleMedium)
          Text("${app.shirtColor.labelEn} • ${app.hatStyle.labelEn} • ${app.glassesStyle.labelEn}", color = Color(0xFFCBD5E1), style = MaterialTheme.typography.bodyMedium)
        }
      }
    }

    SectionHeader(Localization.pick(hud.lang, "Facial Expression", "চেহারার ভাব"))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      ExpressionType.entries.forEach { exp ->
        FilterChip(
          selected = app.expression == exp,
          onClick = { viewModel.updateAppearance(app.copy(expression = exp)) },
          label = { Text("${exp.emoji} ${Localization.pick(hud.lang, exp.titleEn, exp.titleBn)}") },
        )
      }
    }

    SectionHeader(Localization.pick(hud.lang, "Shirt / Panjabi Color", "শার্ট / পাঞ্জাবি"))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      ShirtColor.entries.forEach { sc ->
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(sc.argb))
            .border(if (app.shirtColor == sc) 3.dp else 1.dp, Color.White, CircleShape)
            .clickable { viewModel.updateAppearance(app.copy(shirtColor = sc)) }
        )
      }
    }

    SectionHeader(Localization.pick(hud.lang, "Pants / Lungi", "প্যান্ট / লুঙ্গি"))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      PantsColor.entries.forEach { pc ->
        FilterChip(
          selected = app.pantsColor == pc,
          onClick = { viewModel.updateAppearance(app.copy(pantsColor = pc)) },
          label = { Text(pc.labelEn) },
        )
      }
    }

    SectionHeader(Localization.pick(hud.lang, "Skin Tone", "গায়ের রঙ"))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      SkinTone.entries.forEach { st ->
        FilterChip(
          selected = app.skinTone == st,
          onClick = { viewModel.updateAppearance(app.copy(skinTone = st)) },
          label = { Text(st.labelEn) },
        )
      }
    }

    SectionHeader(Localization.pick(hud.lang, "Hair & Headgear", "চুল ও টুপি/গামছা"))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      HairStyle.entries.forEach { h ->
        FilterChip(
          selected = app.hairStyle == h,
          onClick = { viewModel.updateAppearance(app.copy(hairStyle = h)) },
          label = { Text(Localization.pick(hud.lang, h.labelEn, h.labelBn)) },
        )
      }
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      HatStyle.entries.forEach { h ->
        FilterChip(
          selected = app.hatStyle == h,
          onClick = { viewModel.updateAppearance(app.copy(hatStyle = h)) },
          label = { Text(Localization.pick(hud.lang, h.labelEn, h.labelBn)) },
        )
      }
    }

    SectionHeader(Localization.pick(hud.lang, "Glasses", "চশমা"))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      GlassesStyle.entries.forEach { g ->
        FilterChip(
          selected = app.glassesStyle == g,
          onClick = { viewModel.updateAppearance(app.copy(glassesStyle = g)) },
          label = { Text(Localization.pick(hud.lang, g.labelEn, g.labelBn)) },
        )
      }
    }
  }
}

@Composable
fun ShopAndHomeModalContent(
  hud: HudSnapshot,
  viewModel: GameViewModel,
) {
  Column(modifier = Modifier.fillMaxSize()) {
    Surface(color = Color(0xFF042219), shape = RoundedCornerShape(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text("Wallet: ৳${hud.moneyTaka}", color = DhakaEmeraldBright, style = MaterialTheme.typography.titleMedium)
        Text("Home Lv.${hud.homeLevel} • ${hud.homeFurniture.size} items", color = MarigoldGold, style = MaterialTheme.typography.titleMedium)
      }
    }
    Spacer(modifier = Modifier.height(8.dp))
    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(bottom = 12.dp),
    ) {
      items(ShopCatalog.items, key = { it.id }) { item ->
        val ownedFurniture = item.category in listOf(ShopItemCategory.FURNITURE, ShopItemCategory.UPGRADE) && hud.homeFurniture.contains(item.id)
        val invCount = hud.inventoryItems.count { it == item.id }
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2930)), shape = RoundedCornerShape(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(Localization.pick(hud.lang, item.nameEn, item.nameBn), color = Color.White, style = MaterialTheme.typography.titleMedium)
              Text("${item.category.name} • ৳${item.priceTaka}" + if (invCount > 0) " (x$invCount)" else "", color = NeonCyan, style = MaterialTheme.typography.labelSmall)
            }
            if (invCount > 0 && item.category == ShopItemCategory.ITEM) {
              Button(
                onClick = { viewModel.sellItem(item.id) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                modifier = Modifier.padding(end = 6.dp).height(48.dp),
              ) {
                Text("Sell ৳${item.sellValueTaka}")
              }
            }
            Button(
              onClick = { viewModel.buyItem(item) },
              enabled = !ownedFurniture,
              colors = ButtonDefaults.buttonColors(containerColor = DhakaEmeraldBright, contentColor = Color(0xFF042219)),
              modifier = Modifier.height(48.dp),
            ) {
              Text(if (ownedFurniture) "Owned" else "Buy")
            }
          }
        }
      }
    }
  }
}

@Composable
fun WorldMapModalContent(
  hud: HudSnapshot,
  viewModel: GameViewModel,
) {
  Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Text(
      text = Localization.pick(
        hud.lang,
        "Tap any Bangladesh district to fast-travel immediately in Free Roam!",
        "যেকোনো এলাকায় যেতে ট্যাপ করো (সম্পূর্ণ ফ্রি-রোম)!",
      ),
      style = MaterialTheme.typography.bodyMedium,
      color = Color(0xFFCBD5E1),
    )
    DistrictId.entries.forEach { d ->
      val isCurrent = d == hud.district
      Card(
        onClick = { viewModel.teleportToDistrict(d) },
        colors = CardDefaults.cardColors(containerColor = if (isCurrent) Color(0xFF064E3B) else Color(0xFF1A2930)),
        shape = RoundedCornerShape(16.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(Localization.pick(hud.lang, d.titleEn, d.titleBn), color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(if (isCurrent) "Current Location" else "Fast Travel", color = Color(d.accentHex), style = MaterialTheme.typography.labelSmall)
          }
          Button(onClick = { viewModel.teleportToDistrict(d) }, modifier = Modifier.height(48.dp)) {
            Text(Localization.pick(hud.lang, "Go", "যাও"))
          }
        }
      }
    }
    Button(
      onClick = {
        viewModel.sim.events.triggerRandomNow(hud.lang)
        viewModel.closeModal()
      },
      colors = ButtonDefaults.buttonColors(containerColor = RickshawCrimson),
      modifier = Modifier.fillMaxWidth().height(50.dp),
    ) {
      Text(Localization.pick(hud.lang, "Trigger Funny Random Event Now!", "মজার র‍্যান্ডম ঘটনা ঘটাও!"))
    }
  }
}

@Composable
fun SaveSlotsModalContent(
  hud: HudSnapshot,
  saveSlotsState: CloudUiState<List<SaveSlot>>,
  viewModel: GameViewModel,
) {
  Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
      listOf("slot_1" to "Slot 1", "slot_2" to "Slot 2", "autosave" to "Auto").forEach { (id, label) ->
        Button(
          onClick = { viewModel.saveToCloud(id, label) },
          colors = ButtonDefaults.buttonColors(containerColor = DhakaEmeraldBright, contentColor = Color(0xFF042219)),
          modifier = Modifier.weight(1f).height(48.dp).testTag("save_$id"),
        ) {
          Icon(Icons.Default.Save, contentDescription = label, modifier = Modifier.size(16.dp))
          Text(" $label")
        }
      }
    }

    when (saveSlotsState) {
      is CloudUiState.Loading -> Text("Syncing cloud slots...", color = NeonCyan)
      is CloudUiState.Error -> Text(saveSlotsState.message, color = RickshawCrimson)
      is CloudUiState.Success -> {
        if (saveSlotsState.data.isEmpty()) {
          Text(
            Localization.pick(hud.lang, "No cloud saves yet. Tap a slot above to save!", "এখনো কোনো সেভ নেই। উপরে ট্যাপ করে সেভ করো!"),
            color = Color(0xFFCBD5E1),
          )
        } else {
          LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(saveSlotsState.data, key = { it.id }) { slot ->
              Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2930)), shape = RoundedCornerShape(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(slot.slotName, color = MarigoldGold, style = MaterialTheme.typography.titleMedium)
                    Text("${slot.district} • ৳${slot.moneyTaka} • ${slot.completedMissions.size} missions", color = Color(0xFFCBD5E1), style = MaterialTheme.typography.labelSmall)
                  }
                  IconButton(onClick = { viewModel.loadFromCloud(slot) }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.CloudDownload, contentDescription = "Load", tint = DhakaEmeraldBright)
                  }
                  IconButton(onClick = { viewModel.deleteCloudSlot(slot.id) }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RickshawCrimson)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsModalContent(
  hud: HudSnapshot,
  viewModel: GameViewModel,
) {
  Column(
    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    SectionHeader(Localization.pick(hud.lang, "Language / ভাষা", "ভাষা / Language"))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      FilterChip(selected = hud.lang == AppLang.EN, onClick = { viewModel.setLanguage(AppLang.EN) }, label = { Text("English") })
      FilterChip(selected = hud.lang == AppLang.BN, onClick = { viewModel.setLanguage(AppLang.BN) }, label = { Text("বাংলা (Bangla)") })
    }

    SectionHeader(Localization.pick(hud.lang, "Graphics Quality (LOD & Particles)", "গ্রাফিক্স কোয়ালিটি"))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      GraphicsQuality.entries.forEach { q ->
        FilterChip(
          selected = hud.graphicsQuality == q,
          onClick = { viewModel.setGraphicsQuality(q) },
          label = { Text(Localization.pick(hud.lang, q.titleEn, q.titleBn)) },
        )
      }
    }

    SectionHeader(Localization.pick(hud.lang, "Sandbox Weather Control", "আবহাওয়া পরিবর্তন"))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      WeatherType.entries.forEach { w ->
        FilterChip(
          selected = hud.weather == w,
          onClick = { viewModel.setWeather(w) },
          label = { Text(Localization.pick(hud.lang, w.titleEn, w.titleBn)) },
        )
      }
    }

    SectionHeader("${Localization.pick(hud.lang, "Time of Day", "সময়")}: ${hud.clockText}")
    Slider(
      value = viewModel.sim.weather.timeOfDayHours,
      onValueChange = { viewModel.setTimeOfDay(it) },
      valueRange = 0f..23.9f,
    )
  }
}

@Composable
private fun SectionHeader(text: String) {
  Text(text = text, style = MaterialTheme.typography.titleMedium, color = MarigoldGold)
}
