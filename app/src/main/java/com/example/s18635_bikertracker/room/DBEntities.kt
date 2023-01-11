package com.example.s18635_bikertracker.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation


@Entity
data class RouteEntity(
    @PrimaryKey
    val routeDate: String,

    val test: Boolean = false
)

data class RouteWithLists(
    @Embedded val route: RouteEntity,

    @Relation(
        parentColumn = "routeDate",
        entityColumn = "routeDate"
    )
    val gpsList: List<GpsLocationEntity>,

    @Relation(
        parentColumn = "routeDate",
        entityColumn = "routeDate"
    )
    val photosList: List<RoutePhotosEntity>
)


@Entity
data class GpsLocationEntity(
    @PrimaryKey
    val gpsSaveDate: String,
    val routeDate: String,

    val latitude: Double,
    val longitude: Double
)

@Entity
data class RoutePhotosEntity(
    @PrimaryKey
    val photoSaveDate: String,
    val routeDate: String,

    val latitude: Double,
    val longitude: Double,
    val image: String
)
