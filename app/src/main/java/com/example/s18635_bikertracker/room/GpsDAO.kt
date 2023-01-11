package com.example.s18635_bikertracker.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction


@Dao
interface GpsDAO {

    //INSERT NEW
    @Insert
    fun newRoute(entry: RouteEntity)
    @Insert
    fun saveLocation(entry: GpsLocationEntity)
    @Insert
    fun savePhoto(entry: RoutePhotosEntity)


    //SELECT
    @Query("SELECT * FROM RouteEntity")
    fun getAllRoutes(): List<RouteEntity>

    @Query("SELECT * FROM RoutePhotosEntity")
    fun getAllImages(): List<RoutePhotosEntity>

    @Query("SELECT * FROM GpsLocationEntity")
    fun getAllGps(): List<GpsLocationEntity>

    @Transaction
    @Query("SELECT * FROM RouteEntity WHERE routeDate = :routeDD LIMIT 1")
    fun getRouteAllData(routeDD: String) : RouteWithLists


    @Query("SELECT * FROM RoutePhotosEntity WHERE photoSaveDate = :photoDD LIMIT 1")
    fun getImageByDate(photoDD: String): RoutePhotosEntity

    @Query("SELECT routeDate FROM RouteEntity ORDER BY routeDate DESC LIMIT 1")
    fun getRouteLastInsertedId(): String

    @Query("SELECT gpsSaveDate FROM GpsLocationEntity WHERE routeDate = :routeDD ORDER BY gpsSaveDate DESC LIMIT 1")
    fun getRouteLastGPSDate(routeDD: String): String

}