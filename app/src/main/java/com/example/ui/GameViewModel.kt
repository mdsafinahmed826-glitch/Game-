package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameSaveRepository
import com.example.data.SaveSlot
import com.example.data.UserProfile
import com.example.game.engine.GameSimulation
import com.example.game.engine.HudSnapshot
import com.example.game.model.AppLang
import com.example.game.model.CameraAngleMode
import com.example.game.model.CharacterAppearance
import com.example.game.model.DistrictId
import com.example.game.model.GraphicsQuality
import com.example.game.model.InteractableKind
import com.example.game.model.Localization
import com.example.game.model.MissionDef
import com.example.game.model.ShopItem
import com.example.game.model.VehicleType
import com.example.game.model.WeatherType
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface CloudUiState<out T> {
  data object Loading : CloudUiState<Nothing>
  data class Success<T>(val data: T) : CloudUiState<T>
  data class Error(val message: String) : CloudUiState<Nothing>
}

enum class ActiveModal {
  NONE,
  MAIN_MENU,
  MISSIONS,
  CHARACTER,
  GARAGE,
  SHOP_AND_HOME,
  WORLD_MAP,
  SAVE_SLOTS,
  SETTINGS,
}

class GameViewModel(
  private val repository: GameSaveRepository,
  private val currentUserId: String,
  private val userDisplayName: String,
) : ViewModel() {

  val sim = GameSimulation()

  private val _hudState = MutableStateFlow(sim.latestHud)
  val hudState: StateFlow<HudSnapshot> = _hudState.asStateFlow()

  private val _activeModal = MutableStateFlow(ActiveModal.NONE)
  val activeModal: StateFlow<ActiveModal> = _activeModal.asStateFlow()

  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  val saveSlotsState: StateFlow<CloudUiState<List<SaveSlot>>> =
    repository
      .observeSaveSlots()
      .map<List<SaveSlot>, CloudUiState<List<SaveSlot>>> { CloudUiState.Success(it) }
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing save slots", rootError)
        emit(CloudUiState.Error(rootError.message ?: "Failed to load cloud saves"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = CloudUiState.Loading,
      )

  val profileState: StateFlow<UserProfile?> =
    repository
      .observeUserProfile()
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        Log.w(TAG, "Error observing profile", firestoreError ?: error)
        emit(null)
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = null,
      )

  init {
    sim.startAudio()
    viewModelScope.launch {
      repository.ensureUserProfile(userDisplayName)
      val autoSlot = repository.getSaveSlot("autosave").getOrNull()
      if (autoSlot != null) {
        sim.loadFromSaveSlot(autoSlot)
        showToast(
          Localization.pick(
            sim.lang,
            "Cloud autosave restored (${autoSlot.district})",
            "ক্লাউড অটোসেভ লোড হয়েছে!",
          )
        )
      }
    }

    // Sync simulation HUD snapshot to Compose & check pending interactable modals
    viewModelScope.launch {
      while (isActive) {
        _hudState.value = sim.latestHud
        val pending = sim.pendingShopModal
        if (pending != null) {
          sim.pendingShopModal = null
          _activeModal.value = when (pending) {
            InteractableKind.VEHICLE_DEALER -> ActiveModal.GARAGE
            else -> ActiveModal.SHOP_AND_HOME
          }
        }
        delay(80L)
      }
    }

    // Periodic cloud autosave every 35 seconds
    viewModelScope.launch {
      while (isActive) {
        delay(35_000L)
        saveToCloud("autosave", "Auto Save", silent = true)
      }
    }
  }

  fun openModal(modal: ActiveModal) {
    _activeModal.value = modal
  }

  fun closeModal() {
    _activeModal.value = ActiveModal.NONE
  }

  fun showToast(msg: String) {
    _toastMessage.value = msg
    viewModelScope.launch {
      delay(3000L)
      if (_toastMessage.value == msg) _toastMessage.value = null
    }
  }

  fun onInteractClicked() {
    val msg = sim.interact()
    if (msg != null) showToast(msg)
  }

  fun onJumpClicked() = sim.physics.jump()

  fun onCrouchToggled() {
    sim.physics.player.isCrouching = !sim.physics.player.isCrouching
  }

  fun onGrabThrowClicked() {
    val msg = sim.physics.grabOrThrowNearestProp()
    if (msg != null) showToast(msg)
  }

  fun onVehicleToggleClicked() {
    val msg = sim.vehicles.toggleEnterExit()
    if (msg != null) showToast(msg)
  }

  fun onHornClicked() {
    sim.vehicles.honk()
    sim.npcs.reactToHorn(sim.physics.player.x, sim.physics.player.z, sim.lang)
  }

  fun toggleSlowMotion() {
    sim.slowMotionActive = !sim.slowMotionActive
    showToast(
      if (sim.slowMotionActive) "🎬 Slow-Mo 0.35x ON" else "🎬 Normal Speed"
    )
  }

  fun cycleCameraAngle() {
    val modes = CameraAngleMode.entries
    sim.cameraMode = modes[(modes.indexOf(sim.cameraMode) + 1) % modes.size]
    showToast("📷 ${Localization.pick(sim.lang, sim.cameraMode.titleEn, sim.cameraMode.titleBn)}")
  }

  fun toggleHideUi() {
    sim.hideUiViralMode = !sim.hideUiViralMode
  }

  fun triggerViralClipBurst() {
    sim.cameraMode = CameraAngleMode.CINEMATIC
    sim.slowMotionActive = true
    sim.events.triggerRandomNow(sim.lang)
    showToast(
      Localization.pick(
        sim.lang,
        "📸 Viral Reel Mode! Orbiting + Slow-Mo active!",
        "📸 ভাইরাল রিল মোড চালু! স্লো-মো ও সিনেম্যাটিক ক্যামেরা!",
      )
    )
  }

  fun startMission(mission: MissionDef) {
    sim.startMission(mission)
    closeModal()
  }

  fun abandonMission() {
    sim.abandonMission()
  }

  fun teleportToDistrict(district: DistrictId) {
    sim.teleportToDistrict(district)
    closeModal()
    showToast("📍 ${Localization.pick(sim.lang, district.titleEn, district.titleBn)}")
  }

  fun updateAppearance(newApp: CharacterAppearance) {
    sim.appearance = newApp
  }

  fun buyItem(item: ShopItem) {
    val ok = sim.buyShopItem(item)
    showToast(
      if (ok) "✅ ${Localization.pick(sim.lang, item.nameEn, item.nameBn)}"
      else "❌ ${Localization.pick(sim.lang, "Not enough Taka!", "পর্যাপ্ত টাকা নেই!")}"
    )
  }

  fun sellItem(itemId: String) {
    if (sim.sellInventoryItem(itemId)) {
      showToast("💰 ${Localization.pick(sim.lang, "Sold for Taka!", "বিক্রি হয়েছে!")}")
    }
  }

  fun buyOrSpawnVehicle(type: VehicleType) {
    val ok = sim.buyOrSpawnVehicle(type)
    if (ok) {
      closeModal()
      showToast("🚗 ${Localization.pick(sim.lang, "${type.titleEn} ready!", "${type.titleBn} নামানো হয়েছে!")}")
    } else {
      showToast("❌ ${Localization.pick(sim.lang, "Need ৳${type.priceTaka}!", "৳${type.priceTaka} লাগবে!")}")
    }
  }

  fun setLanguage(lang: AppLang) {
    sim.lang = lang
    viewModelScope.launch {
      repository.updateUserPreferences(
        language = if (lang == AppLang.BN) "bn" else "en",
        graphicsQuality = sim.graphicsQuality.key,
      )
    }
  }

  fun setGraphicsQuality(q: GraphicsQuality) {
    sim.graphicsQuality = q
    viewModelScope.launch {
      repository.updateUserPreferences(
        language = if (sim.lang == AppLang.BN) "bn" else "en",
        graphicsQuality = q.key,
      )
    }
  }

  fun setWeather(w: WeatherType) {
    sim.weather.weather = w
  }

  fun setTimeOfDay(hour: Float) {
    sim.weather.timeOfDayHours = hour
  }

  fun toggleMute() {
    sim.audio.isMuted = !sim.audio.isMuted
  }

  fun startNewGame() {
    sim.loadFromSaveSlot(SaveSlot(userId = currentUserId, slotName = "New Game"))
    sim.abandonMission()
    closeModal()
    showToast("🌟 ${Localization.pick(sim.lang, "Fresh start in Old Dhaka!", "পুরান ঢাকা থেকে নতুন যাত্রা!")}")
  }

  fun saveToCloud(slotId: String, slotName: String, silent: Boolean = false) {
    viewModelScope.launch {
      val slot = sim.toSaveSlot(slotName)
      val res = repository.saveGameSlot(slotId, slot)
      if (!silent) {
        showToast(
          if (res.isSuccess) "☁️ ${Localization.pick(sim.lang, "Saved to $slotName!", "ক্লাউডে সেভ হয়েছে!")}"
          else "❌ Save failed: ${res.exceptionOrNull()?.localizedMessage}"
        )
      }
    }
  }

  fun loadFromCloud(slot: SaveSlot) {
    sim.loadFromSaveSlot(slot)
    closeModal()
    showToast("☁️ ${Localization.pick(sim.lang, "Loaded ${slot.slotName}", "${slot.slotName} লোড হয়েছে")}")
  }

  fun deleteCloudSlot(slotId: String) {
    viewModelScope.launch {
      repository.deleteSaveSlot(slotId)
    }
  }

  override fun onCleared() {
    super.onCleared()
    sim.stopAudio()
  }

  private companion object {
    const val TAG = "GameViewModel"
  }
}
