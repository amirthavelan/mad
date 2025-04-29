package com.example.amirapplication

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.telephony.SmsManager
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val emergencyContacts = listOf("9092939937", "9092139273") // Update as needed
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val SMS_PERMISSION_REQUEST_CODE = 1002
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var profileImageView: ImageView


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Home"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val btnLocation = findViewById<Button>(R.id.btnLocation)
        val btnTasks = findViewById<ImageButton>(R.id.btnTasks)
        val emergencyButton = findViewById<ImageButton>(R.id.btnEmergency)
        val webView = findViewById<WebView>(R.id.youtubeWebView)
        val welcomeText = findViewById<TextView>(R.id.welcomeText)

        val videos = listOf(
            "https://www.youtube.com/embed/ZXsQAXx_ao0",
            "https://www.youtube.com/embed/UNQhuFL6CWg",
            "https://www.youtube.com/embed/wnHW6o8WMas",
            "https://www.youtube.com/embed/IdTMDpizis8"
        )
        val randomVideo = videos.random()
        val username = intent.getStringExtra("username") ?: "Guest"

        welcomeText.text = "Welcome, $username!"

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl(randomVideo)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        databaseHelper = DatabaseHelper(this)

        val dbHelper = DatabaseHelper(this)
        val profile = dbHelper.getProfile(username)


        profileImageView = findViewById(R.id.profileImageView)
        val channel = NotificationChannel("taskChannel", "Task Reminder", NotificationManager.IMPORTANCE_HIGH)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)




        if (profile != null) {
            // Destructure the profile data
            val (name, age, image) = profile

            // Set text for name and age


            // Load the profile image using Glide
            Glide.with(this)
                .load(image) // image could be a URL, file path, or Base64 string
                .into(profileImageView)
        }



        val profileButton = findViewById<ImageButton>(R.id.btnProfile) // Make sure you have this ID in your XML
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("username", username)

            startActivity(intent)
        }






        emergencyButton.setOnClickListener {
            if (checkPermissions()) {
                sendEmergencyAlert()
            } else {
                requestPermissions()
            }
        }

        btnLocation.setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }

        btnTasks.setOnClickListener {
            startActivity(Intent(this, TaskActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_media -> {
                val intent = Intent(this, MediaActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.menu_settings -> {
                Toast.makeText(this, "Settings Page Coming Soon", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.menu_notifications -> {
                Toast.makeText(this, "Notifications Coming Soon", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.menu_users -> {
                Toast.makeText(this, "Registered Users Coming Soon", Toast.LENGTH_SHORT).show()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkPermissions(): Boolean {
        val locationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val smsPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
        return locationPermission == PackageManager.PERMISSION_GRANTED && smsPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun sendEmergencyAlert() {
        val username = intent.getStringExtra("username") ?: return
        val contactNumbers = databaseHelper.getAllEmergencyContacts(username)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null && contactNumbers.isNotEmpty()) {
                val message = "Emergency! I need help. My location: https://maps.google.com/?q=${location.latitude},${location.longitude}"
                for (number in contactNumbers) {
                    SmsManager.getDefault().sendTextMessage(number, null, message, null, null)
                }
                Toast.makeText(this, "Emergency alert sent to contacts!", Toast.LENGTH_LONG).show()
            } else {
                getCurrentLocation()
            }
        }
    }


    private fun getCurrentLocation() {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        val location = result.lastLocation
                        location?.let {
                            val message = "Emergency! I need help. My location: https://maps.google.com/?q=${it.latitude},${it.longitude}"
                            for (number in emergencyContacts) {
                                SmsManager.getDefault().sendTextMessage(number, null, message, null, null)
                            }
                            Toast.makeText(this@MainActivity, "Emergency alert sent with updated location!", Toast.LENGTH_LONG).show()
                        } ?: run {
                            Toast.makeText(this@MainActivity, "Unable to get your current location!", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                Looper.getMainLooper()
            )
        }
    }
}
