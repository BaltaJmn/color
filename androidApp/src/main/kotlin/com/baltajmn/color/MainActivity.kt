package com.baltajmn.color

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.baltajmn.color.data.AndroidContext
import com.baltajmn.color.data.Capture

// FragmentActivity and not ComponentActivity: the biometric lock of v1.2 needs a fragment host.
class MainActivity : FragmentActivity() {

    // The Activity only carries the answer across: what is waiting for it lives in Capture, which
    // outlives this instance when the system recreates it behind the camera.
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture(), Capture::onCamera)

    private val pickPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia(), Capture::onGallery)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidContext.init(this)
        Capture.launchCamera = { uri -> takePicture.launch(uri) }
        // The photo picker asks for no permission: the user hands over one image and nothing else.
        Capture.launchGallery = {
            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        setContent { App() }
    }

    // The launchers belong to this instance's registry: leaving them in a process wide object would
    // hold the dead Activity and then throw when something tried to launch them.
    override fun onDestroy() {
        Capture.launchCamera = null
        Capture.launchGallery = null
        super.onDestroy()
    }
}
