package com.example.game.render

import android.opengl.GLES20
import android.util.Log

class ShaderProgram {
  val programId: Int
  val uMvpLoc: Int
  val uModelLoc: Int
  val uColorLoc: Int
  val uLightDirLoc: Int
  val uSunIntensityLoc: Int
  val uFogColorLoc: Int
  val uFogDensityLoc: Int
  val uEmissiveLoc: Int
  val aPosLoc: Int
  val aNormLoc: Int

  init {
    val vs = compileShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
    val fs = compileShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
    programId = GLES20.glCreateProgram().also { prog ->
      GLES20.glAttachShader(prog, vs)
      GLES20.glAttachShader(prog, fs)
      GLES20.glLinkProgram(prog)
    }
    uMvpLoc = GLES20.glGetUniformLocation(programId, "uMvp")
    uModelLoc = GLES20.glGetUniformLocation(programId, "uModel")
    uColorLoc = GLES20.glGetUniformLocation(programId, "uColor")
    uLightDirLoc = GLES20.glGetUniformLocation(programId, "uLightDir")
    uSunIntensityLoc = GLES20.glGetUniformLocation(programId, "uSunIntensity")
    uFogColorLoc = GLES20.glGetUniformLocation(programId, "uFogColor")
    uFogDensityLoc = GLES20.glGetUniformLocation(programId, "uFogDensity")
    uEmissiveLoc = GLES20.glGetUniformLocation(programId, "uEmissive")
    aPosLoc = GLES20.glGetAttribLocation(programId, "aPos")
    aNormLoc = GLES20.glGetAttribLocation(programId, "aNorm")
  }

  private fun compileShader(type: Int, src: String): Int {
    val shader = GLES20.glCreateShader(type)
    GLES20.glShaderSource(shader, src)
    GLES20.glCompileShader(shader)
    val compiled = IntArray(1)
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
    if (compiled[0] == 0) {
      Log.e("ShaderProgram", "Shader compile error: ${GLES20.glGetShaderInfoLog(shader)}")
    }
    return shader
  }

  private companion object {
    const val VERTEX_SHADER = """
      uniform mat4 uMvp;
      uniform mat4 uModel;
      attribute vec3 aPos;
      attribute vec3 aNorm;
      varying vec3 vWorldNormal;
      varying float vDist;
      void main() {
        vec4 clipPos = uMvp * vec4(aPos, 1.0);
        gl_Position = clipPos;
        vWorldNormal = normalize(mat3(uModel) * aNorm);
        vDist = clipPos.w;
      }
    """

    const val FRAGMENT_SHADER = """
      precision mediump float;
      uniform vec4 uColor;
      uniform vec3 uLightDir;
      uniform float uSunIntensity;
      uniform vec3 uFogColor;
      uniform float uFogDensity;
      uniform float uEmissive;
      varying vec3 vWorldNormal;
      varying float vDist;
      void main() {
        vec3 n = normalize(vWorldNormal);
        float ndl = max(dot(n, normalize(uLightDir)), 0.0);
        // Hemisphere bounce lighting for rich outdoor GI feel
        float hemi = 0.5 + 0.5 * n.y;
        vec3 ambient = mix(vec3(0.20, 0.24, 0.28), vec3(0.55, 0.62, 0.70), hemi) * max(uSunIntensity, 0.32);
        vec3 diffuse = vec3(1.0, 0.96, 0.88) * ndl * uSunIntensity * 0.85;
        vec3 lit = uColor.rgb * (ambient + diffuse + vec3(uEmissive));
        float fogFactor = clamp(exp(-pow(max(vDist, 0.0) * uFogDensity, 1.6)), 0.0, 1.0);
        vec3 finalRgb = mix(uFogColor, lit, fogFactor);
        gl_FragColor = vec4(finalRgb, uColor.a);
      }
    """
  }
}
