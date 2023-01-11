package com.example.s18635_bikertracker.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.s18635_bikertracker.R
import com.example.s18635_bikertracker.databinding.HomeFragmentBinding
import com.example.s18635_bikertracker.helpers.Globals
import com.example.s18635_bikertracker.helpers.Globals.addGeofence
import com.example.s18635_bikertracker.helpers.GpsService
import com.example.s18635_bikertracker.helpers.Helpers
import com.example.s18635_bikertracker.helpers.PhotoGeoPoint
import com.example.s18635_bikertracker.room.RoutePhotosEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.osmdroid.api.IMapController
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread


@SuppressLint("UnspecifiedImmutableFlag")
class HomeFragment: Fragment() {
    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private var mapView: MapView? = null
    private lateinit var mapController: IMapController
    private var currLocationOverlay: MyLocationNewOverlay? = null
    private var myCompassOverlay : CompassOverlay? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)

        mapView = binding.mapView
        addDefaultOverlays()
        setCurrentPosition()

        mapView!!.setBuiltInZoomControls(true)
        mapView!!.setMultiTouchControls(true)
        mapView!!.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
        mapController = mapView!!.controller
        mapController.setZoom(18)

        binding.myLocButton.setOnClickListener{ setCurrentPosition() }
        binding.startRouteButton.setOnClickListener{ startRouteTracking() }
        binding.takePhotoButton.setOnClickListener{ resultTakeImage.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE)) }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onPause() {
        super.onPause()
        mapView?.onPause()
        myCompassOverlay?.disableCompass()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        myCompassOverlay?.enableCompass()

        if(Globals.gpsService != null){
            binding.startRouteButton.setColorFilter(Color.rgb(0,255,0))
            binding.takePhotoButton.visibility = View.VISIBLE
        }else{
            binding.startRouteButton.setColorFilter(Color.rgb(255,0,0))
            binding.takePhotoButton.visibility = View.GONE
        }

        addMarkers()
    }

    private fun setCurrentPosition(){
        if(currLocationOverlay != null)
            mapView!!.overlays.remove(currLocationOverlay)

        val moving = BitmapFactory.decodeResource(resources, R.drawable.move)
        val standing = BitmapFactory.decodeResource(resources, R.drawable.standing)

        currLocationOverlay = MyLocationNewOverlay(mapView).apply {
            enableMyLocation()
            enableFollowLocation()

            setPersonIcon(standing)
            setDirectionIcon(moving)
        }

        mapView!!.overlays.add(currLocationOverlay)
    }

    private fun addDefaultOverlays(){
        myCompassOverlay = CompassOverlay(context, mapView)
        mapView!!.overlays.add(myCompassOverlay)


        val rotationGestureOverlay = RotationGestureOverlay(context, mapView)
        rotationGestureOverlay.isEnabled = true
        mapView!!.setMultiTouchControls(true)
        mapView!!.overlays.add(rotationGestureOverlay)


        val metrics = this.resources.displayMetrics
        val scaleOverlay = ScaleBarOverlay(mapView)
        scaleOverlay.setCentred(true)
        scaleOverlay.setScaleBarOffset(metrics.widthPixels/2, 10)
        mapView!!.overlays.add(scaleOverlay)
    }

    private fun addMarkers(){
        val imagesGps = Globals.appDb!!.gpsDAO().getAllImages()

        imagesGps.forEach { e ->
            addMarker(
                GeoPoint(e.latitude, e.longitude),
                e.photoSaveDate,
                e.image
            )
        }
    }


    /// ON CLICK SECTION
    ///---------------------------------------------------------------------------------------------

    private fun startRouteTracking(){
        if(Globals.gpsService == null){
            val service = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
            if(!service.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Toast.makeText(requireActivity().applicationContext, "WYMAGANY GPS", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }else{
                binding.startRouteButton.setColorFilter(Color.rgb(0,255,0))
                binding.takePhotoButton.visibility = View.VISIBLE

                Globals.gpsService = GpsService()
                requireActivity().startService(
                    Intent(requireActivity(), Globals.gpsService!!::class.java)
                )
            }
        }else{
            binding.startRouteButton.setColorFilter(Color.rgb(255,0,0))
            binding.takePhotoButton.visibility = View.GONE

            requireActivity().stopService(
                Intent(requireActivity(), Globals.gpsService!!::class.java)
            )
            Globals.gpsService = null
        }
    }

    private val resultTakeImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == Activity.RESULT_OK){
            try{
                thread {
                    val image: Bitmap = result.data?.extras?.get("data") as Bitmap

                    createImage(image).let { x ->
                        val currentGEO = currLocationOverlay!!.myLocation
                        val lastRouteDate = Globals.appDb!!.gpsDAO().getRouteLastInsertedId()

                        val photoSave = Helpers.dateToString(LocalDateTime.now())
                        Globals.appDb!!.gpsDAO().savePhoto(
                           RoutePhotosEntity(
                               routeDate = lastRouteDate,
                               latitude = currentGEO.latitude,
                               longitude = currentGEO.longitude,
                               image = x,
                               photoSaveDate = photoSave
                           )
                        )

                        addMarker( currentGEO, lastRouteDate, x )
                        addGeofence(
                            requireActivity(),
                            PhotoGeoPoint(photoSave, currentGEO)
                        )
                    }
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }


    private fun createImage(image: Bitmap): String {
        lateinit var path: String
        runBlocking {
            val file = withContext(Dispatchers.IO) {
                File.createTempFile(
                    "JPEG_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}",
                    ".jpg",
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                )
            }

            path = file.absolutePath

            val fileOutputStream = withContext(Dispatchers.IO) {
                FileOutputStream(file)
            }
            image.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            withContext(Dispatchers.IO) {
                fileOutputStream.flush()
                fileOutputStream.close()
            }

        }
        return path
    }

    private fun addMarker(currentGEO: GeoPoint, routeDate: String, path: String) {
        val newMarker = Marker(binding.mapView)
        newMarker.position = currentGEO
        newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        newMarker.title = "Zdjęcie z trasy: $routeDate"
        newMarker.subDescription = path
        mapView!!.overlays.add(newMarker)
    }
}