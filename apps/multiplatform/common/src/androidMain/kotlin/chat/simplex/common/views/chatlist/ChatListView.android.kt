package chat.simplex.common.views.chatlist

import android.app.Activity
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chat.simplex.common.ANDROID_CALL_TOP_PADDING
import chat.simplex.common.model.durationText
import chat.simplex.common.platform.*
import chat.simplex.common.ui.theme.*
import chat.simplex.common.views.call.*
import chat.simplex.common.views.helpers.*
import chat.simplex.res.MR
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock

private val CALL_INTERACTIVE_AREA_HEIGHT = 64.dp
private val CALL_TOP_OFFSET = (-10).dp
private val CALL_TOP_GREEN_LINE_HEIGHT = ANDROID_CALL_TOP_PADDING - CALL_TOP_OFFSET
private val CALL_BOTTOM_ICON_OFFSET = (-15).dp
private val CALL_BOTTOM_ICON_HEIGHT = CALL_INTERACTIVE_AREA_HEIGHT + CALL_BOTTOM_ICON_OFFSET
private val CALL_TEXT_SIZE = 13.sp

@Composable
actual fun ActiveCallInteractiveArea(call: Call, newChatSheetState: MutableStateFlow<AnimatedViewState>) {
  val onClick = { platform.androidStartCallActivity(false) }
  Box(Modifier.offset(y = CALL_TOP_OFFSET).height(CALL_INTERACTIVE_AREA_HEIGHT)) {
    val source = remember { MutableInteractionSource() }
    val indication = rememberRipple(bounded = true, 3000.dp)
    Box(Modifier.height(CALL_TOP_GREEN_LINE_HEIGHT).clickable(onClick = onClick, indication = indication, interactionSource = source)) {
      GreenLine(call)
    }
    Box(
      Modifier
        .offset(y = CALL_BOTTOM_ICON_OFFSET)
        .size(CALL_BOTTOM_ICON_HEIGHT)
        .background(SimplexGreen, CircleShape)
        .clip(CircleShape)
        .clickable(onClick = onClick, indication = indication, interactionSource = source)
        .align(Alignment.BottomCenter),
      contentAlignment = Alignment.Center
    ) {
      val media = call.peerMedia ?: call.localMedia
      if (media == CallMediaType.Video) {
        Icon(painterResource(MR.images.ic_videocam_filled), null, Modifier.size(27.dp).offset(x = 2.dp), tint = Color.White)
      } else {
        Icon(painterResource(MR.images.ic_call_filled), null, Modifier.size(30.dp).offset(x = 0.dp), tint = Color.White)
      }
    }
  }
}

@Composable
private fun GreenLine(call: Call) {
  Row(
    Modifier
      .fillMaxSize()
      .background(SimplexGreen)
      .padding(top = -CALL_TOP_OFFSET)
      .padding(horizontal = DEFAULT_PADDING),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    if (chatModel.users.size > 1) {
      UserName(call.user.displayName)
    }
    Spacer(Modifier.weight(1f))
    CallDuration(call)
  }
  DisposableEffectOnGone {
    chatModel.activeCallViewIsCollapsed.value = false
  }
  val window = (LocalContext.current as Activity).window
  DisposableEffect(Unit) {
    window.statusBarColor = SimplexGreen.toArgb()
    onDispose {
      window.statusBarColor = Color.Black.toArgb()
    }
  }
}

@Composable
private fun UserName(name: String) {
  Text(name, color = Color.White, fontSize = CALL_TEXT_SIZE)
}

@Composable
private fun CallDuration(call: Call) {
  val connectedAt = call.connectedAt
  if (connectedAt != null) {
    val time = remember { mutableStateOf(durationText(0)) }
    LaunchedEffect(Unit) {
      while (true) {
        time.value = durationText((Clock.System.now() - connectedAt).inWholeSeconds.toInt())
        delay(250)
      }
    }
    val text = time.value
    val sp40Or50 = with(LocalDensity.current) { if (text.length >= 6) 60.sp.toDp() else 42.sp.toDp() }
    val offset = with(LocalDensity.current) { 7.sp.toDp() }
    Text(text, Modifier.offset(x = offset).widthIn(min = sp40Or50), color = Color.White, fontSize = CALL_TEXT_SIZE)
  }
}
