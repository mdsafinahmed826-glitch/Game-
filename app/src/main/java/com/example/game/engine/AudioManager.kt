package com.example.game.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

enum class SoundEffect {
  RICKSHAW_BELL,
  HORN,
  TEA_CLINK,
  SHATTER,
  FOOTSTEP,
  SPLASH,
  POLICE_WHISTLE,
  JUMP,
  CASH,
  BANG_RAGDOLL,
  BARK,
  GOAT_BLEAT,
}

private data class ActiveTone(
  val startFreq: Float,
  val endFreq: Float,
  var phase: Double = 0.0,
  var age: Float = 0f,
  val duration: Float,
  val amplitude: Float,
  val isNoise: Boolean = false,
  val isSquare: Boolean = false,
)

class AudioManager {
  @Volatile var isMuted: Boolean = false
  @Volatile var isRainActive: Boolean = false
  @Volatile var isBusyMarket: Boolean = false

  private val queue = ConcurrentLinkedQueue<ActiveTone>()
  private var running = false
  private var thread: Thread? = null
  private var track: AudioTrack? = null

  fun start() {
    if (running) return
    running = true
    thread = Thread({
      val sampleRate = 22050
      val minBuf = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
      ).coerceAtLeast(2048)

      val audioTrack = try {
        AudioTrack.Builder()
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_GAME)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          .setAudioFormat(
            AudioFormat.Builder()
              .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
              .setSampleRate(sampleRate)
              .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
              .build()
          )
          .setBufferSizeInBytes(minBuf)
          .setTransferMode(AudioTrack.MODE_STREAM)
          .build()
      } catch (_: Throwable) {
        running = false
        return@Thread
      }

      track = audioTrack
      try {
        audioTrack.play()
      } catch (_: Throwable) {
        running = false
        return@Thread
      }

      val chunkSamples = 512
      val pcm = ShortArray(chunkSamples)
      val active = mutableListOf<ActiveTone>()
      val dt = 1f / sampleRate
      var lpRain = 0f

      while (running) {
        while (true) {
          val next = queue.poll() ?: break
          if (active.size < 16) active.add(next)
        }

        if (isMuted) {
          pcm.fill(0)
          audioTrack.write(pcm, 0, pcm.size)
          continue
        }

        for (i in 0 until chunkSamples) {
          var sample = 0f
          // Ambient monsoon rain soundscape
          if (isRainActive) {
            val white = Random.nextFloat() * 2f - 1f
            lpRain = lpRain * 0.85f + white * 0.15f
            sample += lpRain * 0.12f
          }
          // Subtle city market murmur
          if (isBusyMarket) {
            val murmur = (Random.nextFloat() * 2f - 1f) * 0.015f
            sample += murmur
          }

          val iter = active.iterator()
          while (iter.hasNext()) {
            val t = iter.next()
            t.age += dt
            if (t.age >= t.duration) {
              iter.remove()
              continue
            }
            val progress = t.age / t.duration
            val env = 1f - progress
            val freq = t.startFreq + (t.endFreq - t.startFreq) * progress
            t.phase += 2.0 * PI * freq / sampleRate
            val osc = when {
              t.isNoise -> Random.nextFloat() * 2f - 1f
              t.isSquare -> if (sin(t.phase) >= 0.0) 1f else -1f
              else -> sin(t.phase).toFloat()
            }
            sample += osc * t.amplitude * env
          }

          val clamped = (sample * 16000f).coerceIn(-32767f, 32767f)
          pcm[i] = clamped.toInt().toShort()
        }

        try {
          audioTrack.write(pcm, 0, pcm.size)
        } catch (_: Throwable) {
          break
        }
      }

      try {
        audioTrack.stop()
        audioTrack.release()
      } catch (_: Throwable) {}
    }, "DeshiAudioMixer").apply {
      isDaemon = true
      start()
    }
  }

  fun stop() {
    running = false
    thread?.interrupt()
    thread = null
  }

  fun play(effect: SoundEffect) {
    if (isMuted) return
    when (effect) {
      SoundEffect.RICKSHAW_BELL -> {
        queue.offer(ActiveTone(2200f, 2150f, duration = 0.09f, amplitude = 0.35f))
        queue.offer(ActiveTone(2650f, 2600f, duration = 0.12f, amplitude = 0.3f))
      }
      SoundEffect.HORN -> {
        queue.offer(ActiveTone(340f, 330f, duration = 0.25f, amplitude = 0.32f, isSquare = true))
        queue.offer(ActiveTone(425f, 420f, duration = 0.25f, amplitude = 0.25f, isSquare = true))
      }
      SoundEffect.TEA_CLINK -> {
        queue.offer(ActiveTone(3200f, 2900f, duration = 0.07f, amplitude = 0.25f))
      }
      SoundEffect.SHATTER -> {
        queue.offer(ActiveTone(1200f, 250f, duration = 0.22f, amplitude = 0.45f, isNoise = true))
      }
      SoundEffect.FOOTSTEP -> {
        queue.offer(ActiveTone(150f, 80f, duration = 0.045f, amplitude = 0.12f, isNoise = true))
      }
      SoundEffect.SPLASH -> {
        queue.offer(ActiveTone(700f, 180f, duration = 0.28f, amplitude = 0.28f, isNoise = true))
      }
      SoundEffect.POLICE_WHISTLE -> {
        queue.offer(ActiveTone(2900f, 3300f, duration = 0.22f, amplitude = 0.30f))
      }
      SoundEffect.JUMP -> {
        queue.offer(ActiveTone(220f, 480f, duration = 0.12f, amplitude = 0.2f))
      }
      SoundEffect.CASH -> {
        queue.offer(ActiveTone(980f, 1480f, duration = 0.14f, amplitude = 0.28f))
      }
      SoundEffect.BANG_RAGDOLL -> {
        queue.offer(ActiveTone(140f, 55f, duration = 0.22f, amplitude = 0.45f, isNoise = true))
      }
      SoundEffect.BARK -> {
        queue.offer(ActiveTone(520f, 310f, duration = 0.14f, amplitude = 0.28f, isSquare = true))
      }
      SoundEffect.GOAT_BLEAT -> {
        queue.offer(ActiveTone(380f, 430f, duration = 0.24f, amplitude = 0.25f, isSquare = true))
      }
    }
  }
}
