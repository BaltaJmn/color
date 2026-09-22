package com.baltajmn.color

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.baltajmn.color.data.AndroidContext

// FragmentActivity and not ComponentActivity: the biometric lock of v1.2 needs a fragment host.
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidContext.init(this)
        setContent { App() }
    }
}
