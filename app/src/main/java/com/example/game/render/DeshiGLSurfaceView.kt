package com.example.game.render

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.game.engine.GameSimulation

@SuppressLint("ClickableViewAccessibility")
@Composable
fun DeshiGLSurfaceView(
  sim: GameSimulation,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val glView = remember {
    var prevX = 0f
    var prevY = 0f
    GLSurfaceView(context).apply {
      setEGLContextClientVersion(2)
      setRenderer(DeshiRenderer(sim))
      renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
      setOnTouchListener { _, event ->
        when (event.actionMasked) {
          MotionEvent.ACTION_DOWN -> {
            prevX = event.x
            prevY = event.y
          }
          MotionEvent.ACTION_MOVE -> {
            val dx = event.x - prevX
            val dy = event.y - prevY
            prevX = event.x
            prevY = event.y
            sim.cameraYaw -= dx * 0.0055f
            sim.cameraPitch = (sim.cameraPitch + dy * 0.0045f).coerceIn(0.12f, 1.25f)
          }
        }
        true
      }
    }
  }

  DisposableEffect(glView) {
    glView.onResume()
    onDispose {
      glView.onPause()
    }
  }

  AndroidView(
    factory = { glView },
    modifier = modifier,
  )
}
