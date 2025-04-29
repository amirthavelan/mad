package com.example.amirapplication

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var imageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var dobEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var contactsEditText: EditText
    private lateinit var saveButton: Button

    private var selectedImage: ByteArray? = null
    private val CAMERA_REQUEST_CODE = 100
    private val CAMERA_PERMISSION_CODE = 101
    private val calendar = Calendar.getInstance()

    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Bind views
        imageView = findViewById(R.id.profileImageView)
        nameEditText = findViewById(R.id.nameEditText)
        dobEditText = findViewById(R.id.dobEditText)
        ageEditText = findViewById(R.id.ageEditText)
        contactsEditText = findViewById(R.id.contactsEditText)
        saveButton = findViewById(R.id.saveAllButton)

        databaseHelper = DatabaseHelper(this)
        username = intent.getStringExtra("username") ?: "defaultUser"

        // Load existing data
        val profile = databaseHelper.getProfile(username)
        profile?.let {
            nameEditText.setText(it.name)
            ageEditText.setText(it.age.toString())
            dobEditText.setText(it.dob ?: "")
            it.image?.let { img ->
                val bitmap = BitmapFactory.decodeByteArray(img, 0, img.size)
                imageView.setImageBitmap(bitmap)
                selectedImage = img
            }
        }

        val contacts = databaseHelper.getAllEmergencyContacts(username)
        contactsEditText.setText(contacts.joinToString(","))

        // ImageView click to open camera
        imageView.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            } else {
                openCamera()
            }
        }

        // Date picker dialog
        dobEditText.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, y, m, d ->
                val dateStr = "$d/${m + 1}/$y"
                dobEditText.setText(dateStr)
                ageEditText.setText(calculateAge(y, m, d).toString())
            }, year, month, day).show()
        }

        // Save profile
        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val age = ageEditText.text.toString().trim()
            val dob = dobEditText.text.toString().trim()
            val contacts = contactsEditText.text.toString().trim()

            if (name.isEmpty() || age.isEmpty() || contacts.isEmpty() || dob.isEmpty()) {
                Toast.makeText(this, "Please fill all details.", Toast.LENGTH_SHORT).show()
            } else {
                databaseHelper.insertOrUpdateProfile(username, name, age.toInt(), selectedImage)
                databaseHelper.insertEmergencyContacts(username, contacts)
                Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(bitmap)
            selectedImage = bitmapToByteArray(bitmap)
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun calculateAge(year: Int, month: Int, day: Int): Int {
        val dob = Calendar.getInstance().apply { set(year, month, day) }
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) age--
        return age
    }
}
