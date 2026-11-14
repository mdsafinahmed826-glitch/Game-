package com.example.ui

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.model.Localization
import com.example.game.render.DeshiGLSurfaceView

@Composable
fun GameScreen(viewModel: GameViewModel) {
  val hud by viewModel.hudState.collectAsStateWithLifecycle()
  val activeModal by viewModel.activeModal.collectAsStateWithLifecycle()
  val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
  val saveSlotsState by viewModel.saveSlotsState.collectAsStateWithLifecycle()

  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(Unit) {
    try {
      focusRequester.requestFocus()
    } catch (_: Exception) {}
  }

  BackHandler(enabled = activeModal != ActiveModal.NONE) {
    viewModel.closeModal()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .focusRequester(focusRequester)
      .focusable()
      .onPreviewKeyEvent { ev ->
        val isDown = ev.type == KeyEventType.KeyDown
        val isUp = ev.type == KeyEventType.KeyUp
        when (ev.nativeKeyEvent.keyCode) {
          KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_DPAD_UP -> {
            viewModel.sim.moveInputY = if (isDown) 1f else if (isUp) 0f else viewModel.sim.moveInputY
            true
          }
          KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_DPAD_DOWN -> {
            viewModel.sim.moveInputY = if (isDown) -1f else if (isUp) 0f else viewModel.sim.moveInputY
            true
          }
          KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_DPAD_LEFT -> {
            viewModel.sim.moveInputX = if (isDown) -1f else if (isUp) 0f else viewModel.sim.moveInputX
            true
          }
          KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_DPAD_RIGHT -> {
            viewModel.sim.moveInputX = if (isDown) 1f else if (isUp) 0f else viewModel.sim.moveInputX
            true
          }
          KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
            viewModel.sim.isSprinting = isDown
            true
          }
          KeyEvent.KEYCODE_SPACE -> {
            if (isDown) viewModel.onJumpClicked()
            true
          }
          KeyEvent.KEYCODE_E -> {
            if (isDown) viewModel.onInteractClicked()
            true
          }
          KeyEvent.KEYCODE_F -> {
            if (isDown) viewModel.onVehicleToggleClicked()
            true
          }
          KeyEvent.KEYCODE_R -> {
            if (isDown) viewModel.sim.resetPosition()
            true
          }
          KeyEvent.KEYCODE_H -> {
            if (isDown) viewModel.onHornClicked()
            true
          }
          else -> false
        }
      },
  ) {
    // 1. Live 60FPS 3D Open World Surface
    DeshiGLSurfaceView(
      sim = viewModel.sim,
      modifier = Modifier.fillMaxSize(),
    )

    // 2. Interactive HUD & Touch Controls
    GameHudOverlay(
      hud = hud,
      sim = viewModel.sim,
      toastMessage = toastMessage,
      viewModel = viewModel,
    )

    // 3. Modals (with BackHandler)
    if (activeModal != ActiveModal.NONE) {
      val title = when (activeModal) {
        ActiveModal.MAIN_MENU -> "DESHI CHAOS"
        ActiveModal.MISSIONS -> Localization.pick(hud.lang, "Optional Missions (30)", "ঐচ্ছিক মিশন (৩০টি)")
        ActiveModal.CHARACTER -> Localization.pick(hud.lang, "Character & Expressions", "চরিত্র কাস্টমাইজ")
        ActiveModal.GARAGE -> Localization.pick(hud.lang, "Dhaka Garage (11 Vehicles)", "ঢাকা গ্যারেজ")
        ActiveModal.SHOP_AND_HOME -> Localization.pick(hud.lang, "Bazaar & My Home", "বাজার ও নিজের বাসা")
        ActiveModal.WORLD_MAP -> Localization.pick(hud.lang, "6 Districts World Map", "বিশ্ব মানচিত্র")
        ActiveModal.SAVE_SLOTS -> Localization.pick(hud.lang, "Cloud Save & Load", "ক্লাউড সেভ")
        ActiveModal.SETTINGS -> Localization.pick(hud.lang, "Settings & Weather", "সেটিংস ও আবহাওয়া")
        ActiveModal.NONE -> ""
      }

      ModalContainer(
        title = title,
        onClose = { viewModel.closeModal() },
      ) {
        when (activeModal) {
          ActiveModal.MAIN_MENU ->
            MainMenuContent(
              hud = hud,
              viewModel = viewModel,
              onSignOut = {
                signOutUser(credentialManager, scope)
              },
            )
          ActiveModal.MISSIONS -> MissionsModalContent(hud = hud, viewModel = viewModel)
          ActiveModal.CHARACTER -> CharacterModalContent(hud = hud, viewModel = viewModel)
          ActiveModal.GARAGE -> GarageModalContent(hud = hud, viewModel = viewModel)
          ActiveModal.SHOP_AND_HOME -> ShopAndHomeModalContent(hud = hud, viewModel = viewModel)
          ActiveModal.WORLD_MAP -> WorldMapModalContent(hud = hud, viewModel = viewModel)
          ActiveModal.SAVE_SLOTS -> SaveSlotsModalContent(hud = hud, saveSlotsState = saveSlotsState, viewModel = viewModel)
          ActiveModal.SETTINGS -> SettingsModalContent(hud = hud, viewModel = viewModel)
          ActiveModal.NONE -> {}
        }
      }
    }
  }
}
