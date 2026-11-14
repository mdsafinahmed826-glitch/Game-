package com.example.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.game.model.AppLang
import com.example.game.model.Localization
import com.example.ui.theme.CardGlassDark
import com.example.ui.theme.DhakaEmeraldBright
import com.example.ui.theme.MarigoldGold
import com.example.ui.theme.NightAsphalt
import com.example.ui.theme.RickshawCrimson
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

fun attemptAutoSignIn(
  context: Context,
  credentialManager: CredentialManager,
  onAuthSuccess: () -> Unit,
  onUnauthenticated: () -> Unit,
  scope: CoroutineScope,
) {
  if (Firebase.auth.currentUser != null) {
    onAuthSuccess()
    return
  }
  val clientId =
    try {
      context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
      onUnauthenticated()
      return
    }

  val googleIdOption =
    GetGoogleIdOption.Builder()
      .setFilterByAuthorizedAccounts(true)
      .setServerClientId(clientId)
      .setAutoSelectEnabled(true)
      .build()

  val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

  scope.launch {
    try {
      val result = credentialManager.getCredential(context, request)
      val credential = result.credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        Firebase.auth.signInWithCredential(authCredential).await()
        onAuthSuccess()
      } else {
        onUnauthenticated()
      }
    } catch (_: Exception) {
      onUnauthenticated()
    }
  }
}

fun onGoogleSignInClicked(
  context: Context,
  credentialManager: CredentialManager,
  onAuthSuccess: () -> Unit,
  onAuthError: (String) -> Unit,
  scope: CoroutineScope,
  onAuthCancelled: () -> Unit = {},
) {
  val clientId =
    try {
      context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
      onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
      return
    }

  val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
  val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

  scope.launch {
    try {
      val activity = context as? Activity
      if (activity == null) {
        onAuthError("Unable to resolve Activity context for sign-in")
        return@launch
      }
      val result = credentialManager.getCredential(activity, request)
      val credential = result.credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        Firebase.auth.signInWithCredential(authCredential).await()
        onAuthSuccess()
      } else {
        onAuthError("Unexpected credential type")
      }
    } catch (e: GetCredentialCancellationException) {
      Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
      onAuthCancelled()
    } catch (e: Exception) {
      Log.e("Auth", "Google Sign-In failed", e)
      onAuthError(e.localizedMessage ?: "Sign in failed")
    }
  }
}

fun signOutUser(
  credentialManager: CredentialManager,
  scope: CoroutineScope,
  onSignOutComplete: () -> Unit = {},
) {
  scope.launch {
    withContext(NonCancellable) {
      try {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
      } catch (e: Exception) {
        Log.w("Auth", "Failed to clear credential state: ${e.message}")
      } finally {
        Firebase.auth.signOut()
        onSignOutComplete()
      }
    }
  }
}

@Composable
fun AuthScreen(
  onAuthSuccess: () -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  var isLoading by remember { mutableStateOf(false) }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var lang by remember { mutableStateOf(AppLang.EN) }

  LaunchedEffect(Unit) {
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = onAuthSuccess,
      onUnauthenticated = {},
      scope = scope,
    )
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(NightAsphalt)
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_dhaka_hero),
      contentDescription = "Dhaka street key art",
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
    )
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0x660D1418),
              Color(0xAA0D1418),
              Color(0xF50D1418),
            )
          )
        )
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          color = Color(0xCC006A4E),
          shape = RoundedCornerShape(50),
        ) {
          Text(
            text = "OPEN WORLD SANDBOX 3D",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
          )
        }
        FilterChip(
          selected = lang == AppLang.BN,
          onClick = { lang = if (lang == AppLang.EN) AppLang.BN else AppLang.EN },
          label = { Text(if (lang == AppLang.EN) "বাংলা" else "EN") },
          leadingIcon = {
            Icon(Icons.Default.Language, contentDescription = "Toggle language", modifier = Modifier.size(16.dp))
          },
          modifier = Modifier.testTag("auth_lang_toggle"),
        )
      }

      Surface(
        color = CardGlassDark,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 8.dp,
        modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = "DESHI CHAOS",
            style = MaterialTheme.typography.displayLarge,
            color = MarigoldGold,
            textAlign = TextAlign.Center,
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "বাংলাদেশের ঝামেলা, এবার তোমার নিয়ন্ত্রণে!",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
          )
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = Localization.pick(
              lang,
              "Explore 6 Bangladesh districts freely: drive Rickshaws, CNGs, Buses & Boats, smash props with ragdoll physics, survive monsoon storms, or tackle 30 hilarious optional missions.",
              "৬টি বাংলা এলাকা ঘুরে বেড়াও: রিকশা, সিএনজি, বাস ও নৌকা চালাও, ফিজিক্স-সহ মজার ঝামেলা করো, আর ৩০টি ঐচ্ছিক মিশন খেলো!",
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCBD5E1),
            textAlign = TextAlign.Center,
          )

          Spacer(modifier = Modifier.height(18.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
          ) {
            FeatureBadge(Icons.Default.Public, Localization.pick(lang, "6 Districts", "৬ এলাকা"))
            FeatureBadge(Icons.Default.DirectionsCar, Localization.pick(lang, "11 Vehicles", "১১ যানবাহন"))
            FeatureBadge(Icons.Default.SportsEsports, Localization.pick(lang, "30 Missions", "৩০ মিশন"))
          }

          if (errorMsg != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = errorMsg ?: "",
              color = RickshawCrimson,
              style = MaterialTheme.typography.bodyMedium,
              textAlign = TextAlign.Center,
            )
          }

          Spacer(modifier = Modifier.height(20.dp))
          Button(
            onClick = {
              isLoading = true
              errorMsg = null
              onGoogleSignInClicked(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                  isLoading = false
                  onAuthSuccess()
                },
                onAuthError = { msg ->
                  isLoading = false
                  errorMsg = msg
                },
                scope = scope,
                onAuthCancelled = {
                  isLoading = false
                },
              )
            },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
              containerColor = DhakaEmeraldBright,
              contentColor = Color(0xFF042219),
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .clip(RoundedCornerShape(16.dp))
              .testTag("google_signin_button"),
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color(0xFF042219),
                strokeWidth = 2.5.dp,
              )
            } else {
              Text(
                text = Localization.pick(lang, "Sign in with Google & Play", "গুগল দিয়ে সাইন-ইন করে খেলো"),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun FeatureBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Surface(
      color = Color(0x33FFB703),
      shape = RoundedCornerShape(12.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = MarigoldGold,
        modifier = Modifier.padding(8.dp).size(20.dp),
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White)
  }
}
