package com.example.game.render

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MeshBuffers {
  val cubePos: FloatBuffer
  val cubeNorm: FloatBuffer
  val cubeCount = 36

  val cylPos: FloatBuffer
  val cylNorm: FloatBuffer
  val cylCount: Int

  init {
    val cubeVertices = floatArrayOf(
      // Front (+Z)
      -0.5f,-0.5f, 0.5f,  0.5f,-0.5f, 0.5f,  0.5f, 0.5f, 0.5f,
      -0.5f,-0.5f, 0.5f,  0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f,
      // Back (-Z)
       0.5f,-0.5f,-0.5f, -0.5f,-0.5f,-0.5f, -0.5f, 0.5f,-0.5f,
       0.5f,-0.5f,-0.5f, -0.5f, 0.5f,-0.5f,  0.5f, 0.5f,-0.5f,
      // Left (-X)
      -0.5f,-0.5f,-0.5f, -0.5f,-0.5f, 0.5f, -0.5f, 0.5f, 0.5f,
      -0.5f,-0.5f,-0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f,-0.5f,
      // Right (+X)
       0.5f,-0.5f, 0.5f,  0.5f,-0.5f,-0.5f,  0.5f, 0.5f,-0.5f,
       0.5f,-0.5f, 0.5f,  0.5f, 0.5f,-0.5f,  0.5f, 0.5f, 0.5f,
      // Top (+Y)
      -0.5f, 0.5f, 0.5f,  0.5f, 0.5f, 0.5f,  0.5f, 0.5f,-0.5f,
      -0.5f, 0.5f, 0.5f,  0.5f, 0.5f,-0.5f, -0.5f, 0.5f,-0.5f,
      // Bottom (-Y)
      -0.5f,-0.5f,-0.5f,  0.5f,-0.5f,-0.5f,  0.5f,-0.5f, 0.5f,
      -0.5f,-0.5f,-0.5f,  0.5f,-0.5f, 0.5f, -0.5f,-0.5f, 0.5f,
    )
    val cubeNormals = floatArrayOf(
      0f,0f,1f, 0f,0f,1f, 0f,0f,1f, 0f,0f,1f, 0f,0f,1f, 0f,0f,1f,
      0f,0f,-1f, 0f,0f,-1f, 0f,0f,-1f, 0f,0f,-1f, 0f,0f,-1f, 0f,0f,-1f,
      -1f,0f,0f, -1f,0f,0f, -1f,0f,0f, -1f,0f,0f, -1f,0f,0f, -1f,0f,0f,
      1f,0f,0f, 1f,0f,0f, 1f,0f,0f, 1f,0f,0f, 1f,0f,0f, 1f,0f,0f,
      0f,1f,0f, 0f,1f,0f, 0f,1f,0f, 0f,1f,0f, 0f,1f,0f, 0f,1f,0f,
      0f,-1f,0f, 0f,-1f,0f, 0f,-1f,0f, 0f,-1f,0f, 0f,-1f,0f, 0f,-1f,0f,
    )
    cubePos = toBuffer(cubeVertices)
    cubeNorm = toBuffer(cubeNormals)

    val segs = 12
    val cPos = ArrayList<Float>(segs * 12 * 3)
    val cNorm = ArrayList<Float>(segs * 12 * 3)
    for (i in 0 until segs) {
      val a0 = (2.0 * PI * i / segs).toFloat()
      val a1 = (2.0 * PI * (i + 1) / segs).toFloat()
      val x0 = cos(a0) * 0.5f; val z0 = sin(a0) * 0.5f
      val x1 = cos(a1) * 0.5f; val z1 = sin(a1) * 0.5f
      // Side quad (2 tris)
      cPos.addAll(listOf(x0, -0.5f, z0, x1, -0.5f, z1, x1, 0.5f, z1))
      cNorm.addAll(listOf(cos(a0), 0f, sin(a0), cos(a1), 0f, sin(a1), cos(a1), 0f, sin(a1)))
      cPos.addAll(listOf(x0, -0.5f, z0, x1, 0.5f, z1, x0, 0.5f, z0))
      cNorm.addAll(listOf(cos(a0), 0f, sin(a0), cos(a1), 0f, sin(a1), cos(a0), 0f, sin(a0)))
      // Top cap
      cPos.addAll(listOf(0f, 0.5f, 0f, x0, 0.5f, z0, x1, 0.5f, z1))
      cNorm.addAll(listOf(0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f))
      // Bottom cap
      cPos.addAll(listOf(0f, -0.5f, 0f, x1, -0.5f, z1, x0, -0.5f, z0))
      cNorm.addAll(listOf(0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f))
    }
    cylPos = toBuffer(cPos.toFloatArray())
    cylNorm = toBuffer(cNorm.toFloatArray())
    cylCount = cPos.size / 3
  }

  private fun toBuffer(arr: FloatArray): FloatBuffer =
    ByteBuffer.allocateDirect(arr.size * 4)
      .order(ByteOrder.nativeOrder())
      .asFloatBuffer()
      .apply {
        put(arr)
        position(0)
      }
}
