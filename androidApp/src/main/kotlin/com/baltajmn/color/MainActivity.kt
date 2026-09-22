package com.baltajmn.color

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.baltajmn.color.data.AndroidContext
import com.baltajmn.color.data.Capture
import com.baltajmn.color.data.FilePicker
import com.baltajmn.color.data.Reminder
import com.baltajmn.color.data.Route
import com.baltajmn.color.social.handleLoginIntent

// FragmentActivity and not ComponentActivity: the biometric lock of v1.2 needs a fragment host.
class MainActivity : FragmentActivity() {

    // The Activity only carries the answer across: what is waiting for it lives in Capture, which
    // outlives this instance when the system recreates it behind the camera.
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture(), Capture::onCamera)

    private val pickPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia(), Capture::onGallery)

    private val createBackup =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip"), FilePicker::onPicked)

    private val openBackup =
        registerForActivityResult(ActivityResultContracts.OpenDocument(), FilePicker::onPicked)

    private val askNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidContext.init(this)
        // The permission is asked the moment the reminder is switched on and never before.
        Reminder.onNeedsPermission = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        FilePicker.createDocument = { name -> createBackup.launch(name) }
        FilePicker.openDocument = { openBackup.launch(arrayOf("application/zip", "application/json", "*/*")) }
        Capture.launchCamera = { uri -> takePicture.launch(uri) }
        // The photo picker asks for no permission: the user hands over one image and nothing else.
        Capture.launchGallery = {
            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        Route.pending = intent?.getStringExtra("screen")
        handleLoginIntent(intent)
        setContent { App() }
    }

    // singleTask: a widget tapped while the app is open arrives here and not in onCreate.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Route.pending = intent.getStringExtra("screen")
        handleLoginIntent(intent)
    }

    // The launchers belong to this instance's registry: leaving them in a process wide object would
    // hold the dead Activity and then throw when something tried to launch them.
    override fun onDestroy() {
        Capture.launchCamera = null
        Capture.launchGallery = null
        Reminder.onNeedsPermission = null
        FilePicker.createDocument = null
        FilePicker.openDocument = null
        super.onDestroy()
    }
}
