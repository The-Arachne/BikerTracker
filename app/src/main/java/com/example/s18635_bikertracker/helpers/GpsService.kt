package com.example.s18635_bikertracker.helpers

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import com.example.s18635_bikertracker.room.GpsLocationEntity
import com.example.s18635_bikertracker.room.RouteEntity
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.thread


class GpsService: Service() {
    private lateinit var locationManager: LocationManager
    private lateinit var routeDate: String
    private var loop: Boolean = true

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        routeDate = Helpers.dateToString(LocalDateTime.now())
        Globals.appDb!!.gpsDAO().newRoute( RouteEntity(
            routeDate = routeDate
        ))
        super.onCreate()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if(checkGPS()){
            thread {
                Looper.prepare()
                while (loop){
                    addGps()

                    Thread.sleep(10000)
                }
            }
        }
        return START_NOT_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun addGps(){
        val curr: Location? = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        if(curr != null){
            val entry = GpsLocationEntity(
                routeDate = routeDate,
                gpsSaveDate = Helpers.dateToString(LocalDateTime.now()),
                latitude = curr.latitude,
                longitude = curr.longitude
            )
            Globals.appDb!!.gpsDAO().saveLocation(entry = entry)
        }
    }

    private fun checkGPS(): Boolean {
        return if(checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(applicationContext, "Daj uprawnienia na GPS", Toast.LENGTH_LONG).show()
            false
        }else if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(applicationContext, "Włącz GPS", Toast.LENGTH_LONG).show()
            false
        }else if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            Toast.makeText(applicationContext, "Dla lepszej dokładności włącz internet", Toast.LENGTH_LONG).show()
            true
        }else{
            true
        }
    }

    override fun onDestroy() {
        loop = false
        addGps()
        super.onDestroy()
    }
}