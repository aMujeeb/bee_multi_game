package com.mujapps.bigbee

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource

import bigbee.composeapp.generated.resources.Res
import bigbee.composeapp.generated.resources.background
import com.mujapps.bigbee.domain.Game
import com.mujapps.bigbee.domain.GameStatus
import com.mujapps.bigbee.util.GamingFontFamily

@Composable
@Preview
fun App() {
    MaterialTheme {

        var screenWidth by remember { mutableStateOf(0) }
        var screenHeight by remember { mutableStateOf(0) }

        //Make it a mutable state since when Game over or other conditions apply the game state should be updated
        var mGame by remember {
            mutableStateOf(
                Game()
            )
        }

        LaunchedEffect(Unit) {
            mGame.start()
        }

        LaunchedEffect(mGame.status) {
            while (mGame.status == GameStatus.Started) {
                withFrameMillis { //Synchronize animations with frame base updates. Suspended function in jetpack compose to make animations smooth
                    mGame.updateGameProgress()
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(Res.drawable.background),
                contentDescription = "BackGround Image",
                contentScale = ContentScale.Crop
            )
        }

        Canvas(
            modifier = Modifier.fillMaxSize()
                .onGloballyPositioned {
                    val size = it.size
                    if (screenWidth != size.width || screenHeight != size.height) {
                        screenWidth = size.width
                        screenHeight = size.height
                        mGame = mGame.copy(
                            screenWidth = size.width,
                            screenHeight = size.height
                        )
                    }
                }
                .clickable {
                    if (mGame.status == GameStatus.Started) {
                        mGame.jump()
                    }
                }) {
            drawCircle(
                color = Color.Blue,
                radius = mGame.bee.radius,
                center = Offset(
                    x = mGame.bee.x,
                    y = mGame.bee.y
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Score 0",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = GamingFontFamily()
            )

            Text(
                text = "Best 0",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                fontFamily = GamingFontFamily()
            )
        }

        if (mGame.status == GameStatus.Over) {
            Column(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Game Over",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    fontFamily = GamingFontFamily()
                )
            }
        }
    }
}