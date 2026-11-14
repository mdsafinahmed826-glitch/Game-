package com.example.game.engine

import com.example.game.model.AppLang
import com.example.game.model.Localization
import com.example.game.model.NpcRole
import com.example.game.model.WeatherType
import kotlin.random.Random

class RandomEventManager(
  private val weather: WeatherManager,
  private val npcManager: NpcManager,
  private val physics: PhysicsManager,
  private val audio: AudioManager,
) {
  private var cooldown = 38f
  private var lastEventIdx = -1
  var currentBannerText: String = ""
  var bannerTimer: Float = 0f

  fun step(dt: Float, lang: AppLang) {
    if (bannerTimer > 0f) bannerTimer -= dt
    cooldown -= dt
    if (cooldown > 0f) return

    cooldown = 42f + Random.nextFloat() * 35f
    var idx = Random.nextInt(9)
    if (idx == lastEventIdx) idx = (idx + 1) % 9
    lastEventIdx = idx

    triggerEvent(idx, lang)
  }

  fun triggerRandomNow(lang: AppLang) {
    var idx = Random.nextInt(9)
    if (idx == lastEventIdx) idx = (idx + 1) % 9
    lastEventIdx = idx
    triggerEvent(idx, lang)
  }

  private fun triggerEvent(idx: Int, lang: AppLang) {
    when (idx) {
      0 -> {
        weather.powerOutageActive = !weather.powerOutageActive
        show(
          lang,
          if (weather.powerOutageActive) "⚡ Load Shedding! Whole neighborhood power went out!"
          else "💡 Electricity Restored! Neighborhood cheers!",
          if (weather.powerOutageActive) "⚡ লোডশেডিং! পাড়ার কারেন্ট চলে গেছে!"
          else "💡 কারেন্ট চলে এসেছে! পাড়ায় খুশির জোয়ার!",
        )
      }
      1 -> {
        audio.play(SoundEffect.GOAT_BLEAT)
        npcManager.npcs.filter { it.role == NpcRole.GOAT }.forEach {
          it.mood = NpcMood.PANICKED
          it.stateTimer = 5f
        }
        show(
          lang,
          "🐐 A naughty goat just stole a packet of kacchi from the bazaar!",
          "🐐 একটি দুষ্টু ছাগল বাজার থেকে বিরিয়ানির প্যাকেট নিয়ে পালিয়েছে!",
        )
      }
      2 -> {
        val cow = npcManager.npcs.firstOrNull { it.role == NpcRole.COW }
        if (cow != null) {
          cow.x = physics.player.x + 12f
          cow.z = physics.player.z + 4f
          cow.targetX = cow.x
          cow.targetZ = cow.z
          cow.stateTimer = 10f
        }
        show(
          lang,
          "🐄 VIP Cow has sat down in the middle of the road! Total jam!",
          "🐄 রাস্তার ঠিক মাঝখানে গরু বসে পড়েছে! তীব্র যানজট!",
        )
      }
      3 -> {
        show(
          lang,
          "☕ Cha Mama is shouting about unpaid tea bills across Old Dhaka!",
          "☕ পুরান ঢাকায় বাকি চা টাকার জন্য চা মামা চিল্লাচিল্লি করছেন!",
        )
      }
      4 -> {
        npcManager.npcs.filter { it.role == NpcRole.CHICKEN }.forEach {
          it.mood = NpcMood.PANICKED
          it.stateTimer = 6f
        }
        show(
          lang,
          "🐔 Market chicken escaped! Chaos across the stalls!",
          "🐔 বাজারের মুরগি খাঁচা ভেঙে দৌড় দিয়েছে! হুলস্থুল কাণ্ড!",
        )
      }
      5 -> {
        weather.weather = WeatherType.MONSOON
        show(
          lang,
          "⛈️ Sudden Dhaka Monsoon! Roads are turning slippery!",
          "⛈️ হঠাৎ ঝুম বৃষ্টি! ঢাকা শহরের রাস্তা পিচ্ছিল হয়ে গেছে!",
        )
      }
      6 -> {
        audio.play(SoundEffect.SHATTER)
        show(
          lang,
          "🏏 Street cricket sixer just shattered Angry Uncle's window!",
          "🏏 গলির ক্রিকেটের ছক্কায় রাগী আঙ্কেলের জানালার কাঁচ ভেঙেছে!",
        )
      }
      7 -> {
        val dog = npcManager.npcs.firstOrNull { it.role == NpcRole.DOG }
        if (dog != null) {
          dog.mood = NpcMood.ANGRY_CHASE
          dog.stateTimer = 5f
          audio.play(SoundEffect.BARK)
        }
        show(
          lang,
          "🐕 Tommy the street dog spotted your snacks and is chasing you!",
          "🐕 রাস্তার কুকুর টমি তোমার সিঙ্গারা দেখে পিছু নিয়েছে!",
        )
      }
      else -> {
        val uncle = npcManager.npcs.firstOrNull { it.role == NpcRole.NEIGHBOR }
        if (uncle != null) {
          uncle.mood = NpcMood.ANGRY_CHASE
          uncle.stateTimer = 6f
        }
        show(
          lang,
          "😡 Angry Uncle marched outside to complain about the loud noise!",
          "😡 রাগী আঙ্কেল শোরগোল শুনে লাঠি হাতে বেরিয়ে এসেছেন!",
        )
      }
    }
  }

  private fun show(lang: AppLang, en: String, bn: String) {
    currentBannerText = Localization.pick(lang, en, bn)
    bannerTimer = 5.5f
  }
}
