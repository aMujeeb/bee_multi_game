package com.mujapps.bigbee

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource

import bigbee.composeapp.generated.resources.Res
import bigbee.composeapp.generated.resources.background
import bigbee.composeapp.generated.resources.bee_sprite
import bigbee.composeapp.generated.resources.moving_background
import bigbee.composeapp.generated.resources.pipe
import bigbee.composeapp.generated.resources.pipe_cap
import com.mujapps.bigbee.domain.Game
import com.mujapps.bigbee.domain.GameStatus
import com.mujapps.bigbee.util.GamingFontFamily
import com.stevdza_san.sprite.component.drawSpriteView
import com.stevdza_san.sprite.domain.SpriteSheet
import com.stevdza_san.sprite.domain.SpriteSpec
import com.stevdza_san.sprite.domain.rememberSpriteState
import org.jetbrains.compose.resources.imageResource


const val BEE_SPRITE_SIZE = 80
const val PIPE_CAP_HEIGHT = 50f

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

        val mSpriteState = rememberSpriteState(
            totalFrames = 9,
            framesPerRow = 3
        )

        val mSpriteSpec = remember {
            SpriteSpec(
                screenWidth = screenWidth.toFloat(),
                default = SpriteSheet(
                    frameWidth = BEE_SPRITE_SIZE,
                    frameHeight = BEE_SPRITE_SIZE,
                    image = Res.drawable.bee_sprite
                )
            )
        }

        val mSheetImage = mSpriteSpec.imageBitmap
        val mCurrentFrame by mSpriteState.currentFrame.collectAsState()

        //Tilt or rotate the character when falling in this case
        val mAnimateAngle by animateFloatAsState(
            targetValue = when {
                mGame.beeVelocity > mGame.beeMaxVelocity / 1.1 -> 30f //(Around 90% max velocity. can adjust)
                else -> 0f
            }
        )

        //Removed when Button controlled start assigned
        /* LaunchedEffect(Unit) {
             mGame.start()
             mSpriteState.start()
         }*/

        LaunchedEffect(mGame.status) {
            while (mGame.status == GameStatus.Started) {
                withFrameMillis { //Synchronize animations with frame base updates. Suspended function in jetpack compose to make animations smooth
                    mGame.updateGameProgress()
                }
            }
            if (mGame.status == GameStatus.Over) mSpriteState.stop()
        }

        DisposableEffect(Unit) {
            onDispose {
                mSpriteState.stop()
                mSpriteState.cleanup()
            }
        }

        val mBackGroundOffsetX = remember { Animatable(0f) } //To animate movement
        var mImageWidth by remember { mutableStateOf(0) }
        //Use to store the width of first image. This will help to calculate to append the second image starting point.
        // In order to create a loop. one after other

        val mPipeImage = imageResource(Res.drawable.pipe)
        val mPipeCapImage = imageResource(Res.drawable.pipe_cap)

        //Make the animation moving repeatedly
        LaunchedEffect(mGame.status) {
            while (mGame.status == GameStatus.Started) {
                mBackGroundOffsetX.animateTo(
                    targetValue = -mImageWidth.toFloat(), //If not '-' will move opposite
                    animationSpec = infiniteRepeatable(
                        tween(
                            durationMillis = 4000,
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Restart
                    )
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(Res.drawable.background),
                contentDescription = "BackGround Image",
                contentScale = ContentScale.Crop
            )

            Image(
                modifier = Modifier.fillMaxSize()
                    .onSizeChanged {
                        mImageWidth = it.width //Set width initially
                    }
                    .offset { //This to animate X position and keep y = 0
                        IntOffset(
                            x = mBackGroundOffsetX.value.toInt(),
                            y = 0
                        )
                    },
                painter = painterResource(Res.drawable.moving_background),
                contentDescription = "Bottom Image",
                contentScale = ContentScale.FillHeight
            )

            //Copy the image back(Second Image) without Size modifier & add the width of 1st image

            Image(
                modifier = Modifier.fillMaxSize()
                    .offset { //This to animate X position and keep y = 0
                        IntOffset(
                            x = mBackGroundOffsetX.value.toInt() + mImageWidth,
                            y = 0
                        )
                    },
                painter = painterResource(Res.drawable.moving_background),
                contentDescription = "Bottom Image",
                contentScale = ContentScale.FillHeight
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
            /*drawCircle(
                color = Color.Blue,
                radius = mGame.bee.radius,
                center = Offset(
                    x = mGame.bee.x,
                    y = mGame.bee.y
                )
            )*/
            //Rotation manipulation
            rotate(
                degrees = mAnimateAngle,
                pivot = Offset(
                    x = mGame.bee.x - mGame.beeRadius,
                    y = mGame.bee.y - mGame.beeRadius
                )
            ) {
                drawSpriteView(
                    spriteState = mSpriteState,
                    spriteSpec = mSpriteSpec,
                    currentFrame = mCurrentFrame,
                    image = mSheetImage,
                    offset = IntOffset(
                        x = mGame.bee.x.toInt(),
                        y = mGame.bee.y.toInt()
                    )
                )
            }

            mGame.mPipePairs.forEach { pipePair ->
                /* drawRect(
                     color = Color.Blue,
                     topLeft = Offset(
                         x = pipePair.x - mGame.pipeWidth / 2,
                         y = 0f
                     ),
                     size = Size(mGame.pipeWidth, pipePair.topHeight)
                 )

                 drawRect(
                     color = Color.Blue,
                     topLeft = Offset(
                         x = pipePair.x - mGame.pipeWidth / 2,
                         y = pipePair.y + mGame.pipeGapSize / 2
                     ),
                     size = Size(mGame.pipeWidth, pipePair.bottomHeight)
                 )
                 */

                drawImage(
                    image = mPipeImage,
                    dstOffset = IntOffset(
                        x = (pipePair.x - (mGame.pipeWidth / 2)).toInt(),
                        y = 0
                    ),
                    dstSize = IntSize(
                        width = mGame.pipeWidth.toInt(),
                        height = (pipePair.topHeight - PIPE_CAP_HEIGHT).toInt()
                    )
                )

                drawImage(
                    image = mPipeCapImage,
                    dstOffset = IntOffset(
                        x = (pipePair.x - mGame.pipeWidth / 2).toInt(),
                        y = (pipePair.topHeight - PIPE_CAP_HEIGHT).toInt()
                    ),
                    dstSize = IntSize(
                        width = mGame.pipeWidth.toInt(),
                        height = PIPE_CAP_HEIGHT.toInt()
                    )
                )

                drawImage(
                    image = mPipeCapImage,
                    dstOffset = IntOffset(
                        x = (pipePair.x - (mGame.pipeWidth / 2)).toInt(),
                        y = (pipePair.y + mGame.pipeGapSize / 2).toInt()
                    ),
                    dstSize = IntSize(
                        width = mGame.pipeWidth.toInt(),
                        height = PIPE_CAP_HEIGHT.toInt()
                    )
                )

                drawImage(
                    image = mPipeImage,
                    dstOffset = IntOffset(
                        x = (pipePair.x - (mGame.pipeWidth / 2)).toInt(),
                        y = (pipePair.y + mGame.pipeGapSize /2 + PIPE_CAP_HEIGHT).toInt()
                    ),
                    dstSize = IntSize(
                        width = mGame.pipeWidth.toInt(),
                        height = (pipePair.bottomHeight - PIPE_CAP_HEIGHT).toInt()
                    )
                )
            }
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

        if (mGame.status == GameStatus.Idle) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        mGame.start() //Have to remove any Launch effect starts
                        mSpriteState.start()
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                        .padding(start = 24.dp, end = 24.dp),
                    shape = RoundedCornerShape(size = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(alpha = 04f)
                    )
                ) {
                    Text(
                        text = "Start",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        fontFamily = GamingFontFamily()
                    )
                }
            }
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
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Score : 0",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp,
                    fontFamily = GamingFontFamily()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        mGame.reStartGame() //Have to remove any Launch effect starts
                        mSpriteState.start()
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                        .padding(start = 24.dp, end = 24.dp),
                    shape = RoundedCornerShape(size = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(alpha = 04f)
                    )
                ) {
                    Text(
                        text = "Re-Start",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        fontFamily = GamingFontFamily()
                    )
                }
            }
        }
    }
}