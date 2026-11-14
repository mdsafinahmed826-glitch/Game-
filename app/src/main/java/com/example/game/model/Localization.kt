package com.example.game.model

object Localization {
  fun pick(lang: AppLang, en: String, bn: String): String = if (lang == AppLang.BN) bn else en

  val npcLinesEn = listOf(
    "Mama, one cup of hot ginger cha?",
    "Hey! Watch the rickshaw lane!",
    "Today's traffic is legendary!",
    "Is the Wi-Fi down again?",
    "Careful, my kacchi is precious!",
    "Who threw that slipper?!",
    "Let's play rooftop cricket at 4!",
    "Load shedding in 3... 2... 1!",
    "Fixed fare, no bargaining today!",
    "That goat just ate my samosa!",
  )

  val npcLinesBn = listOf(
    "মামা, এক কাপ কড়া আদা চা দেবো?",
    "ওই মিয়া, রিকশা দেখে চালাও!",
    "আজকে জ্যামটা দারুণ জমছে!",
    "আবার কি ওয়াইফাই চলে গেল?",
    "সাবধান, হাতে গরম কাচ্চি!",
    "কে ওই চপ্পল ছুড়লো রে?!",
    "বিকেলে ছাদে ক্রিকেট খেলবি নাকি?",
    "এই বুঝি কারেন্ট চলে গেল!",
    "মামা, ভাড়া কিন্তু একদাম!",
    "ছাগলটা আমার সিঙ্গারা নিয়ে ভাগলো!",
  )

  val policeLinesEn = listOf(
    "Hey! Stop right there, chaos maker!",
    "Whistle blown! Pull over the rickshaw!",
    "No slipper throwing on main road!",
  )

  val policeLinesBn = listOf(
    "এই থামো! রাস্তায় ঝামেলা কেন?",
    "বাঁশি বাজছে! রিকশা সাইড করো!",
    "মেইন রোডে চপ্পল ছোড়া নিষেধ!",
  )
}
