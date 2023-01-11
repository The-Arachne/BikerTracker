package com.example.s18635_bikertracker.firebaseDB

import android.content.Context
import android.widget.Toast
import com.example.s18635_bikertracker.helpers.Globals
import com.example.s18635_bikertracker.room.GpsLocationEntity
import com.example.s18635_bikertracker.room.RouteEntity
import com.example.s18635_bikertracker.room.RoutePhotosEntity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class FirebaseDAO {
    companion object{
        private suspend fun setNewData(userData: FUserEntity, email: String) = coroutineScope {
            withContext(Dispatchers.Default) {
                Globals.firebaseDb
                    .child("users")
                    .child(email.replace(".", "_"))
                    .setValue(userData)
            }
        }


        private fun saveAccountData(email: String?) {
            if(email == null) return

            val listRoutes = Globals.appDb!!.gpsDAO().getAllRoutes().map { e ->
                FRouteEntity(e.routeDate)
            }

            val listGps = Globals.appDb!!.gpsDAO().getAllGps().map { e ->
                FGpsLocationEntity(e.gpsSaveDate, e.routeDate, e.latitude, e.longitude)
            }

            val listPhotos = Globals.appDb!!.gpsDAO().getAllImages().map { e ->
                FPhotoLocationEntity(e.photoSaveDate, e.routeDate, e.latitude, e.longitude, e.image)
            }

            runBlocking {
                setNewData(FUserEntity(listRoutes,listGps,listPhotos), email)
            }
        }


        fun firebaseSyncAccounts(context: Context, x: String?, y: String) {
            val prevEmail = x?.replace(".", "_")
            val newEmail = y.replace(".", "_")

            Globals.firebaseDb
                .child("users")
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val prev = snapshot.children
                            .filter { it.key == prevEmail }
                            .map { it.getValue(FUserEntity::class.java) }
                            .firstOrNull()

                        val new = snapshot.children
                            .filter { it.key == newEmail }
                            .map { it.getValue(FUserEntity::class.java) }
                            .firstOrNull()

                       syncAccounts(context, prev, prevEmail, new, newEmail )
                    }

                    override fun onCancelled(error: DatabaseError) {
                        val x = error
                        println(x)
                    }
                })
        }

        fun syncAccounts(context: Context, prev: FUserEntity?, prevEmail: String?, new: FUserEntity?, newEmail: String,) {

            if(prev?.routeList?.isNotEmpty() == true){
                val roomConverted = Globals.appDb!!.gpsDAO().getAllRoutes().map { FRouteEntity(it.routeDate) }
                val sum = prev.routeList + roomConverted
                val diff = sum.groupBy { it.routeDate }.filter { it.value.size == 1 }.flatMap { it.value }

                if(diff.isNotEmpty()) {
                    val roomGpsList = prev.gpsList
                    val roomPhotoList = prev.photoList

                    diff.forEach { e ->
                        if( e.routeDate != null && !roomConverted.contains(e) ){
                            Globals.appDb!!.gpsDAO().newRoute(RouteEntity(e.routeDate))

                            val partRoomGpsList = roomGpsList?.filter { it.routeDate == e.routeDate }
                            if(!partRoomGpsList.isNullOrEmpty()){
                                partRoomGpsList.forEach { f ->
                                    Globals.appDb!!.gpsDAO().saveLocation(GpsLocationEntity(
                                        f.gpsSaveDate!!, f.routeDate!!, f.latitude!!, f.longitude!!
                                    ))
                                }
                            }

                            val partRoomPhotoList = roomPhotoList?.filter { it.routeDate == e.routeDate }
                            if(!partRoomPhotoList.isNullOrEmpty()){
                                partRoomPhotoList.forEach { f ->
                                    Globals.appDb!!.gpsDAO().savePhoto(RoutePhotosEntity(
                                        f.photoSaveDate!!, f.routeDate!!, f.latitude!!, f.longitude!!, f.image!!
                                    ))
                                }
                            }
                        }
                    }

                }
            }

            saveAccountData(prevEmail)

            if( newEmail == prevEmail ){
                Globals.synced = true
                Toast.makeText(context, "Zsynchronizowano dane", Toast.LENGTH_SHORT).show()
                return
            }else{
                Globals.appDb!!.clearAllTables()
                if(new?.routeList?.isNotEmpty() != true){
                    Globals.synced = true
                    Toast.makeText(context, "Zsynchronizowano dane", Toast.LENGTH_SHORT).show()
                    return
                }else{
                    new.routeList.forEach { e ->
                        Globals.appDb!!.gpsDAO().newRoute(RouteEntity(e.routeDate!!))
                    }

                    new.gpsList!!.forEach { e ->
                        Globals.appDb!!.gpsDAO().saveLocation(
                            GpsLocationEntity(
                                e.gpsSaveDate!!, e.routeDate!!, e.latitude!!, e.longitude!!
                            )
                        )
                    }

                    new.photoList!!.forEach { e ->
                        Globals.appDb!!.gpsDAO().savePhoto(
                            RoutePhotosEntity(
                                e.photoSaveDate!!, e.routeDate!!, e.latitude!!, e.longitude!!, e.image!!
                            )
                        )
                    }

                    Globals.synced = true
                    Toast.makeText(context, "Zsynchronizowano dane", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }
    }
}