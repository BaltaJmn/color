package com.baltajmn.color

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.baltajmn.color.data.ChromaRepository

/** Four screens do not justify a navigation library. Friends stays hidden until v1.1. */
enum class Screen { Today, Year, Friends, Settings }

@Composable
fun App() {
    remember { ChromaRepository.load() }
    var screen by remember { mutableStateOf(Screen.Today) }
    MaterialTheme {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), Alignment.Center) {
            Text("Chroma: ${screen.name}")
        }
    }
}
