package com.baltajmn.color

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.baltajmn.color.data.Capture
import com.baltajmn.color.data.FilePicker
import com.baltajmn.color.data.Lock
import com.baltajmn.color.data.Reminder
import com.baltajmn.color.data.Route
import com.baltajmn.color.review.Review
import com.baltajmn.color.social.handleLinkIntent
import java.lang.ref.WeakReference

// FragmentActivity and not ComponentActivity: the biometric lock of v1.2 needs a fragment host.
class MainActivity : FragmentActivity() {

    // The Activity only carries the answer across: what is waiting for it lives in Capture.
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
        Lock.host = WeakReference(this)
        Review.host = WeakReference(this)
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
        // A recreated Activity is handed the intent it was launched with again, and following it a
        // second time would reopen the paywall out of nowhere.
        if (savedInstanceState == null) {
            Route.pending = intent?.getStringExtra("screen")
            handleLinkIntent(intent)
        }
        setContent { App() }
    }

    // The dark mode turning on at dusk no longer recreates the Activity (configChanges), so the
    // status bar icons and the window behind Compose are picked again here.
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        enableEdgeToEdge()
        window.setBackgroundDrawableResource(R.color.widget_background)
    }

    // singleTask: a widget tapped while the app is open arrives here and not in onCreate.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Route.pending = intent.getStringExtra("screen")
        handleLinkIntent(intent)
    }

    // The launchers belong to this instance's registry: leaving them in a process wide object would
    // hold the dead Activity and then throw when something tried to launch them.
    override fun onDestroy() {
        Capture.launchCamera = null
        Capture.launchGallery = null
        Reminder.onNeedsPermission = null
        FilePicker.createDocument = null
        FilePicker.openDocument = null
        Lock.host = null
        Review.host = null
        super.onDestroy()
    }
}
