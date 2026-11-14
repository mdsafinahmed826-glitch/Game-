package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "Dhaka Explorer",
  val language: String = "en",
  val graphicsQuality: String = "high",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  fun toCreateMap(uid: String): Map<String, Any> =
    mapOf(
      "userId" to uid,
      "displayName" to displayName.ifBlank { "Dhaka Explorer" }.take(50),
      "language" to if (language in listOf("en", "bn")) language else "en",
      "graphicsQuality" to if (graphicsQuality in listOf("low", "medium", "high")) graphicsQuality else "high",
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
}

@IgnoreExtraProperties
data class SaveSlot(
  @DocumentId val id: String = "",
  val userId: String = "",
  val slotName: String = "Auto Save",
  val posX: Double = 0.0,
  val posY: Double = 0.0,
  val posZ: Double = 0.0,
  val yaw: Double = 0.0,
  val district: String = "OLD_DHAKA",
  val moneyTaka: Long = 1200L,
  val chaosLevel: Long = 0L,
  val timeOfDay: Double = 10.5,
  val weather: String = "CLEAR",
  val ownedVehicles: List<String> = listOf("RICKSHAW", "BICYCLE"),
  val completedMissions: List<String> = emptyList(),
  val unlockedDistricts: List<String> = listOf(
    "OLD_DHAKA",
    "MODERN_DHAKA",
    "RESIDENTIAL",
    "VILLAGE",
    "COMMERCIAL",
    "RIVERSIDE",
  ),
  val inventoryItems: List<String> = listOf("hot_cha", "shingara"),
  val homeFurniture: List<String> = listOf("ceiling_fan", "charpai_bed", "crt_tv"),
  val homeLevel: Long = 1L,
  val shirtColor: String = "EMERALD",
  val pantsColor: String = "DENIM",
  val skinTone: String = "WARM",
  val hairStyle: String = "CLASSIC",
  val hatStyle: String = "NONE",
  val glassesStyle: String = "NONE",
  val expression: String = "HAPPY",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  fun toCreateMap(uid: String): Map<String, Any> =
    mapOf(
      "userId" to uid,
      "slotName" to slotName.ifBlank { "Auto Save" }.take(40),
      "posX" to posX.coerceIn(-4900.0, 4900.0),
      "posY" to posY.coerceIn(-100.0, 400.0),
      "posZ" to posZ.coerceIn(-4900.0, 4900.0),
      "yaw" to yaw.coerceIn(-99.0, 99.0),
      "district" to district,
      "moneyTaka" to moneyTaka.coerceIn(0L, 100000000L),
      "chaosLevel" to chaosLevel.coerceIn(0L, 5L),
      "timeOfDay" to timeOfDay.coerceIn(0.0, 24.0),
      "weather" to weather,
      "ownedVehicles" to ownedVehicles.take(15),
      "completedMissions" to completedMissions.take(50),
      "unlockedDistricts" to unlockedDistricts.take(10),
      "inventoryItems" to inventoryItems.take(40),
      "homeFurniture" to homeFurniture.take(20),
      "homeLevel" to homeLevel.coerceIn(1L, 5L),
      "shirtColor" to shirtColor.take(24),
      "pantsColor" to pantsColor.take(24),
      "skinTone" to skinTone.take(24),
      "hairStyle" to hairStyle.take(24),
      "hatStyle" to hatStyle.take(24),
      "glassesStyle" to glassesStyle.take(24),
      "expression" to expression.take(24),
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )

  fun toUpdateMap(): Map<String, Any> =
    mapOf(
      "slotName" to slotName.ifBlank { "Auto Save" }.take(40),
      "posX" to posX.coerceIn(-4900.0, 4900.0),
      "posY" to posY.coerceIn(-100.0, 400.0),
      "posZ" to posZ.coerceIn(-4900.0, 4900.0),
      "yaw" to yaw.coerceIn(-99.0, 99.0),
      "district" to district,
      "moneyTaka" to moneyTaka.coerceIn(0L, 100000000L),
      "chaosLevel" to chaosLevel.coerceIn(0L, 5L),
      "timeOfDay" to timeOfDay.coerceIn(0.0, 24.0),
      "weather" to weather,
      "ownedVehicles" to ownedVehicles.take(15),
      "completedMissions" to completedMissions.take(50),
      "unlockedDistricts" to unlockedDistricts.take(10),
      "inventoryItems" to inventoryItems.take(40),
      "homeFurniture" to homeFurniture.take(20),
      "homeLevel" to homeLevel.coerceIn(1L, 5L),
      "shirtColor" to shirtColor.take(24),
      "pantsColor" to pantsColor.take(24),
      "skinTone" to skinTone.take(24),
      "hairStyle" to hairStyle.take(24),
      "hatStyle" to hatStyle.take(24),
      "glassesStyle" to glassesStyle.take(24),
      "expression" to expression.take(24),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
}
