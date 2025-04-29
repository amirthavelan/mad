package com.example.amirapplication

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.location.Address
import java.util.*

class LocationActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var tvAddress: TextView
    private lateinit var myLocationOverlay: MyLocationNewOverlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm_prefs", MODE_PRIVATE))
        setContentView(R.layout.activity_location)

        map = findViewById(R.id.map)
        tvAddress = findViewById(R.id.tvAddress)

        map.setMultiTouchControls(true)

        // Set up current location overlay
        myLocationOverlay = MyLocationNewOverlay(map)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()

        // Check for location permissions and enable overlay
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.overlays.add(myLocationOverlay)
        }

        // Set zoom level and map center to the first location (Kalavasal, Madurai)
        val initialLocation = GeoPoint(10.0100, 78.1190) // Default map center (Kalavasal, Madurai)
        map.controller.setZoom(13.0)
        map.controller.setCenter(initialLocation)

        // List of locations to mark with names
        val locations = listOf(
            GeoPoint(10.0100, 78.1190),  // Kalavasal
            GeoPoint(9.9193, 78.1181),  // Thiagarajar College of Engineering
            initialLocation              // Your current location (this can be replaced with your own current location coordinates)
        )

        val markers = listOf(
            "My address", // Label for Kalavasal
            "Friend 1: Kalavasal", // Label for Kalavasal
            "Friend 2: Thiagarajar College of Engineering" // Label for Thiagarajar College
        )

        // Geocoder to get the address from latitude and longitude
        val geocoder = Geocoder(this, Locale.getDefault())

        // Clear previous addresses
        tvAddress.text = ""

        // Add markers to map for each location
        for (i in locations.indices) {
            val marker = Marker(map)
            marker.position = locations[i]
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = markers[i]
            map.overlays.add(marker)

            // Get the address from the latitude and longitude
            try {
                val addressList: List<Address>? = geocoder.getFromLocation(locations[i].latitude, locations[i].longitude, 1)
                if (addressList != null && addressList.isNotEmpty()) {
                    val address = addressList[0].getAddressLine(0)
                    tvAddress.append("${markers[i]}: $address\n\n")
                } else {
                    tvAddress.append("${markers[i]}: Address not found.\n\n")
                }
            } catch (e: Exception) {
                tvAddress.append("${markers[i]}: Failed to fetch address.\n\n")
                e.printStackTrace()
            }

        }
    }
}
