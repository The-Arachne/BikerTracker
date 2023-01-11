package com.example.s18635_bikertracker.helpers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent


class GeofenceEventReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geoEvent = GeofencingEvent.fromIntent(intent)

        if (geoEvent == null) {
            println("ERROR: GEOEVENT JEST NULL")
            return
        }
        if (geoEvent.hasError() ) {
            println("ERROR: " + GeofenceErrorMessages.getErrorString(geoEvent.errorCode))
            return
        }

        if(geoEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
            val triggeringGeofenceId = geoEvent.triggeringGeofences!![0].requestId
            val notificationManager = ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
            )as NotificationManager

            notificationManager.sendGeofenceEnteredNotification(
                context,
                triggeringGeofenceId
            )
        }
    }
}

object GeofenceErrorMessages {
    fun getErrorString( errorCode: Int): String {
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE ->
                "Serwis GEOFENCE niedostępny, aby naprawić: ustawienia->lokalizacja->tryb i wybierz wysoka jakość"

            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES ->
                "Apka ma zarejestrowane za dużo punktów GEOFENCE"

            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS ->
                "Podałeś za dużo pendingIntent do wywołań addGeofence"

            else -> "Nieznany błąd GEOFENCE"
        }
    }
}