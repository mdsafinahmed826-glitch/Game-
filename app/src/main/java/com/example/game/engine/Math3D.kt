package com.example.game.engine

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vec3(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f) {
  fun distSq(ox: Float, oz: Float): Float {
    val dx = x - ox
    val dz = z - oz
    return dx * dx + dz * dz
  }

  fun dist2D(ox: Float, oz: Float): Float = sqrt(distSq(ox, oz))
}

data class AABB(
  val minX: Float,
  val minY: Float,
  val minZ: Float,
  val maxX: Float,
  val maxY: Float,
  val maxZ: Float,
  val isWalkableRoof: Boolean = false,
  val isSlippery: Boolean = false,
) {
  fun containsXZ(px: Float, pz: Float, margin: Float = 0f): Boolean =
    px >= minX - margin && px <= maxX + margin && pz >= minZ - margin && pz <= maxZ + margin
}

object MathUtils {
  fun clamp(v: Float, min: Float, max: Float): Float =
    if (v < min) min else if (v > max) max else v

  fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * clamp(t, 0f, 1f)

  fun lerpAngle(a: Float, b: Float, t: Float): Float {
    var diff = (b - a) % (Math.PI.toFloat() * 2f)
    if (diff > Math.PI.toFloat()) diff -= Math.PI.toFloat() * 2f
    if (diff < -Math.PI.toFloat()) diff += Math.PI.toFloat() * 2f
    return a + diff * clamp(t, 0f, 1f)
  }

  fun normalizeAngle(a: Float): Float {
    var res = a % (Math.PI.toFloat() * 2f)
    if (res > Math.PI.toFloat()) res -= Math.PI.toFloat() * 2f
    if (res < -Math.PI.toFloat()) res += Math.PI.toFloat() * 2f
    return res
  }

  fun sinf(a: Float): Float = sin(a.toDouble()).toFloat()
  fun cosf(a: Float): Float = cos(a.toDouble()).toFloat()
  fun absf(v: Float): Float = abs(v)
}
