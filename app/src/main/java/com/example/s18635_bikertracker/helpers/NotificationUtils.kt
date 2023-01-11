package com.example.s18635_bikertracker.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.example.s18635_bikertracker.MainActivity
import com.example.s18635_bikertracker.R
import org.osmdroid.util.GeoPoint


private const val NOTIFICATION_ID = 33
private const val CHANNEL_ID = "GeofenceChannel"


fun NotificationManager.sendGeofenceEnteredNotification(context: Context, triggeringGeofence: String) {

    val contentIntent = Intent(context, MainActivity::class.java)
    val contentPendingIntent = PendingIntent.getActivity(
        context,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val triggeredGeo = Globals.appDb!!.gpsDAO().getImageByDate(triggeringGeofence)

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle("Byłeś tu w ramach trasy: ${triggeredGeo.routeDate}")
        .setContentText("Zrobiłeś tu zdjęcie: ${triggeredGeo.image}")
        .setSmallIcon(R.drawable.bicycle)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(contentPendingIntent)
        .build()

    notify(NOTIFICATION_ID, builder)

}

fun createChannel(context: Context){
    val notificationChannel = NotificationChannel(
        CHANNEL_ID,
        context.getString(R.string.channel_name),
        NotificationManager.IMPORTANCE_HIGH
    )
        .apply {
            setShowBadge(false)
        }

    notificationChannel.enableLights(true)
    notificationChannel.lightColor = Color.RED
    notificationChannel.enableVibration(true)
    notificationChannel.description = "BRUH"

    val notificationManager = context.getSystemService(NotificationManager::class.java)
    notificationManager.createNotificationChannel(notificationChannel)
}


data class PhotoGeoPoint(val photoDate: String, val geoPoint: GeoPoint)