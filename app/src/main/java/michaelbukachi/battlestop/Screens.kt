package michaelbukachi.battlestop

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import michaelbukachi.battlestop.ui.theme.BattleStopTheme
import michaelbukachi.battlestop.ui.theme.MainColor

@Composable
fun KeyboardWidget(onKeyPressed: (String) -> Unit, modifier: Modifier = Modifier) {
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "00", "0", "BACKSPACE")

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        keys.chunked(3).forEach { chunk ->
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                chunk.forEach { buttonTxt ->
                    OutlinedButton(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, MainColor),
                        contentPadding = PaddingValues(0.dp),
                        onClick = { onKeyPressed(buttonTxt) },
                    ) {
                        if (buttonTxt == "BACKSPACE") {
                            Icon(
                                painterResource(id = R.drawable.backspace_outline),
                                contentDescription = "backspace",
                            )
                        } else {
                            Text(text = buttonTxt, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeDisplayText(text: String, modifier: Modifier = Modifier) {
    Row(horizontalArrangement = Arrangement.Center, modifier = modifier.fillMaxWidth()) {
        var textWithLabels = ""
        text.padStart(6, '0').reversed().forEachIndexed { index, c ->
            textWithLabels += when (index) {
                0 -> 's'
                2 -> 'm'
                4 -> 'h'
                else -> ""
            }
            textWithLabels += c
        }
        val annotatedString = buildAnnotatedString {
            textWithLabels.reversed().forEach { c ->
                if (c.isDigit()) {
                    withStyle(style = SpanStyle(fontSize = 64.sp)) {
                        append(c)
                    }
                } else {
                    withStyle(style = SpanStyle(fontSize = 18.sp)) {
                        append(c)
                    }
                }
            }
        }
        Text(
            text = annotatedString,
        )
    }
}

@Composable
fun CountDown(model: CountDownUi, modifier: Modifier = Modifier) {
    val progressAnimate by animateFloatAsState(
        targetValue = model.progress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
    )
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Text(
            model.text,
            fontSize = 64.sp,
        )
        CircularProgressIndicator(progress = progressAnimate, modifier = Modifier.size(256.dp))
    }
}

@Composable
fun MainScreen(mainViewModel: () -> MainViewModel?) {
    val imageLoader = LocalContext.current.imageLoader
    var settingsOpen by remember { mutableStateOf(true) }
    var timeText by remember { mutableStateOf("0") }
    val uiState = mainViewModel()?.uiState?.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp),
    ) {
        if (settingsOpen) {
            TimeDisplayText(text = timeText, modifier = Modifier.padding(bottom = 16.dp))
            KeyboardWidget(onKeyPressed = {
                if (it != "BACKSPACE") {
                    if (timeText.length < 6) {
                        timeText += it
                    }
                } else {
                    if (timeText.isNotEmpty()) {
                        timeText = timeText.substring(0, timeText.lastIndex)
                    }
                }
            }, modifier = Modifier.fillMaxWidth(0.8f))
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                border = BorderStroke(1.dp, MainColor),
                contentPadding = PaddingValues(0.dp),
                onClick = {
                    mainViewModel()?.setAndStartTimer(timeText)
                    settingsOpen = false
                },
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "start countdown",
                )
            }
        } else {
            CountDown(
                model = uiState?.value ?: CountDownUi(),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f),
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 20.dp),
            ) {
                OutlinedButton(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, MainColor),
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        mainViewModel()?.reset()
                    },
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "reset timer",
                    )
                }
                Box {
                    val logoPainter = rememberAsyncImagePainter(
                        R.drawable.logo,
                        imageLoader = imageLoader,
                    )

                    Image(
                        logoPainter,
                        contentDescription = "logo",
                        modifier = Modifier.size(160.dp),
                        contentScale = ContentScale.FillBounds,
                    )
                }
                OutlinedButton(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, MainColor),
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        mainViewModel()?.pauseTimer()
                    },
                ) {
                    if (uiState?.value?.paused == true) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "resume countdown",
                        )
                    } else {
                        Icon(
                            painterResource(id = R.drawable.pause),
                            contentDescription = "pause countdown",
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Divider(
            color = MainColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
        ) {
            IconButton(modifier = Modifier.size(64.dp), onClick = { settingsOpen = !settingsOpen }) {
                Icon(Icons.Default.Settings, contentDescription = "settings button")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BattleStopTheme {
        MainScreen { null }
    }
}

@Preview(showBackground = true)
@Composable
fun KeyboardWidgetPreview() {
    BattleStopTheme {
        KeyboardWidget({}, Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true)
@Composable
fun TimeDisplayTextPreview() {
    BattleStopTheme {
        TimeDisplayText("630")
    }
}

@Preview(showBackground = true)
@Composable
fun CountDownPreview() {
    BattleStopTheme {
        CountDown(
            model = CountDownUi(
                text = "1:22",
                progress = 0.7f,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(256.dp),
        )
    }
}
