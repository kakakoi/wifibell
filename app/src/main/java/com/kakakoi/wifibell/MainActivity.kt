package com.kakakoi.wifibell

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.kakakoi.wifibell.ui.main.MainFragment


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
        checkPermission()
    }

    override fun onResume() {
        super.onResume()
        window.decorView.apply {
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }

    private fun checkPermission() {
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Log.i(TAG, "onCreate: Permission is granted.")
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.educational_permission_message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                val snackbar = Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.request_tethering),
                    Snackbar.LENGTH_INDEFINITE
                )
                snackbar.setAction(R.string.close, View.OnClickListener {
                    snackbar.dismiss()
                })
                    .show()
                Log.d(TAG, "onCreate: ACCESS_FINE_LOCATION is PERMISSION_GRANTED.")
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.required_location_permission_message),
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.config_system, View.OnClickListener {
                        val uriString = "package:" + this.getPackageName();
                        intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse(uriString)
                        )
                        this.startActivity(intent)
                    })
                    .show()
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }
}