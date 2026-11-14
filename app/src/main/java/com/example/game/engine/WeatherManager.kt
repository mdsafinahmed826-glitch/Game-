package com.example.game.engine

import com.example.game.model.WeatherType
import kotlin.random.Random

class WeatherManager(private val audio: AudioManager) {
  var timeOfDayHours: Float = 10.5f
  var weather: WeatherType = WeatherType.CLEAR
  var powerOutageActive: Boolean = false
  var lightningFlash: Float = 0f
  var autoCycleEnabled: Boolean = true
  private var weatherChangeTimer: Float = 95f

  fun step(dt: Float) {
    if (autoCycleEnabled) {
      // 1 real second = 3 in-game minutes (~8 min full day/night cycle)
      timeOfDayHours = (timeOfDayHours + dt * 0.05f) % 24f
    }
    weatherChangeTimer -= dt
    if (weatherChangeTimer <= 0f) {
      weatherChangeTimer = 80f + Random.nextFloat() * 60f
      val values = WeatherType.entries
      weather = values[Random.nextInt(values.size)]
    }

    audio.isRainActive = weather == WeatherType.RAIN || weather == WeatherType.MONSOON

    if (weather == WeatherType.MONSOON) {
      if (lightningFlash > 0f) {
        lightningFlash = (lightningFlash - dt * 3f).coerceAtLeast(0f)
      } else if (Random.nextFloat() < dt * 0.14f) {
        lightningFlash = 1f
      }
    } else {
      lightningFlash = 0f
    }
  }

  fun skyColorRgb(): Triple<Float, Float, Float> {
    // Sunrise 5..7, Day 7..17, Sunset 17..19, Night 19..5
    val base = when {
      timeOfDayHours in 5.5f..7.5f -> Triple(0.95f, 0.58f, 0.38f) // Golden sunrise
      timeOfDayHours in 7.5f..16.5f -> Triple(0.36f, 0.72f, 0.96f) // Bright Dhaka sky
      timeOfDayHours in 16.5f..19.0f -> Triple(0.92f, 0.42f, 0.34f) // Warm sunset
      else -> Triple(0.05f, 0.08f, 0.16f) // Starlit night
    }
    val darken = when (weather) {
      WeatherType.CLEAR -> 1.0f
      WeatherType.CLOUDY -> 0.76f
      WeatherType.RAIN -> 0.58f
      WeatherType.MONSOON -> 0.42f
      WeatherType.FOG -> 0.82f
    }
    val flash = lightningFlash * 0.45f
    return Triple(
      (base.first * darken + flash).coerceIn(0f, 1f),
      (base.second * darken + flash).coerceIn(0f, 1f),
      (base.third * darken + flash).coerceIn(0f, 1f),
    )
  }

  fun sunIntensity(): Float {
    val dayFactor = when {
      timeOfDayHours in 6f..18f -> 1.0f
      timeOfDayHours in 5f..6f -> (timeOfDayHours - 5f)
      timeOfDayHours in 18f..19f -> (19f - timeOfDayHours)
      else -> 0.25f
    }
    val weatherMul = when (weather) {
      WeatherType.CLEAR -> 1.0f
      WeatherType.CLOUDY -> 0.78f
      WeatherType.RAIN -> 0.62f
      WeatherType.MONSOON -> 0.48f
      WeatherType.FOG -> 0.72f
    }
    return (dayFactor * weatherMul + lightningFlash * 0.7f).coerceIn(0.2f, 1.3f)
  }

  fun formattedClock(): String {
    val h = timeOfDayHours.toInt() % 24
    val m = ((timeOfDayHours - timeOfDayHours.toInt()) * 60).toInt()
    val amPm = if (h < 12) "AM" else "PM"
    val h12 = if (h % 12 == 0) 12 else h % 12
    return "%02d:%02d %s".format(h12, m, amPm)
  }
}
