package com.example.s18635_bikertracker.helpers

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import com.example.s18635_bikertracker.room.AppDatabase
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference


object Globals {
    lateinit var auth: FirebaseAuth
    lateinit var firebaseDb: DatabaseReference
    var synced : Boolean = false

    var sharedPref: SharedPreferences? = null
    var currUser:  User? = null
    var appDb:  AppDatabase? = null

    var gpsService: GpsService? = null
    var geoClient: GeofencingClient? = null



    @SuppressLint("UnspecifiedImmutableFlag", "MissingPermission")
    fun addGeofence(context: Context, listOfGeoPoints: PhotoGeoPoint) {
        val geofencePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent( context, GeofenceEventReceiver::class.java ),
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val geofence = Geofence.Builder()
            .setRequestId(listOfGeoPoints.photoDate)
            .setCircularRegion(listOfGeoPoints.geoPoint.latitude, listOfGeoPoints.geoPoint.longitude, 100f)
            .setExpirationDuration(60 * 60 * 1000)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()


        val geoRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geoClient!!.addGeofences(geoRequest, geofencePendingIntent)
            .addOnSuccessListener {
                Toast.makeText(context, "Dodano GEOFENCE POMYSLNIE ", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                it.printStackTrace()
                Toast.makeText(context, "NIE UDALO SIE DODAC GEOFENCE: ${it.message} ", Toast.LENGTH_SHORT).show()
            }
    }
}