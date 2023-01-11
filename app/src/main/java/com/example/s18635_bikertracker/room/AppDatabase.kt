package com.example.s18635_bikertracker.room

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [
    (RouteEntity::class),
    (GpsLocationEntity::class),
    (RoutePhotosEntity::class)
], exportSchema = false, version = 4)
abstract class AppDatabase: RoomDatabase() {

    abstract fun gpsDAO(): GpsDAO
}