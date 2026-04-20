package com.mujapps.bigbee.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import bigbee.composeapp.generated.resources.Res
import bigbee.composeapp.generated.resources.cursive_regular
import org.jetbrains.compose.resources.Font

@Composable
fun GamingFontFamily() = FontFamily(Font(Res.font.cursive_regular))