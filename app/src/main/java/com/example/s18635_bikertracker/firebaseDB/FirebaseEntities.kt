package com.example.s18635_bikertracker.firebaseDB

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FUserEntity(
    val routeList: List<FRouteEntity>? = null,
    val gpsList: List<FGpsLocationEntity>? = null,
    val photoList: List<FPhotoLocationEntity>? = null
)

@IgnoreExtraProperties
data class FRouteEntity(
    val routeDate: String? = null
)

@IgnoreExtraProperties
data class FGpsLocationEntity(
    val gpsSaveDate: String? = null,
    val routeDate: String? = null,

    val latitude: Double? = null,
    val longitude: Double? = null
)

@IgnoreExtraProperties
data class FPhotoLocationEntity(
    val photoSaveDate: String? = null,
    val routeDate: String? = null,

    val latitude: Double? = null,
    val longitude: Double? = null,
    val image: String? = null,
)