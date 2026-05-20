package com.example.aurafarm2

import androidx.compose.runtime.*

@Composable
fun App() {

    var showSplash by remember {
        mutableStateOf(true)
    }

    if (showSplash) {

        SplashScreen(
            onFinished = {
                showSplash = false
            }
        )

    } else {

        MainActivity()
    }
}