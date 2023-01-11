package com.example.s18635_bikertracker.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.s18635_bikertracker.databinding.HistoryFragmentBinding
import com.example.s18635_bikertracker.helpers.Globals
import com.example.s18635_bikertracker.helpers.Helpers
import com.example.s18635_bikertracker.helpers.RecyclerAdapter
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import java.time.Duration
import kotlin.concurrent.thread


class HistoryFragment: Fragment() {
    private var _binding: HistoryFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        _binding = HistoryFragmentBinding.inflate(inflater, container, false)


        binding.routeList.layoutManager = LinearLayoutManager(activity)

        if(Globals.synced){
            binding.routeList.adapter = RecyclerAdapter(::onItemListPressed)
            if(Globals.appDb!!.gpsDAO().getAllRoutes().isNotEmpty()){
                binding.textView2.visibility = View.GONE
            }
        }else{
            binding.textView2.text = "Poczekaj na zsynchronizowanie danych..."
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun onItemListPressed(routeId: String){
        try {
            binding.routeListView.visibility = View.GONE
            binding.contentRouteView.visibility = View.VISIBLE

            val routeData = Globals.appDb!!.gpsDAO().getRouteAllData(routeId)

            val initDate = Helpers.stringToDate(routeData.route.routeDate)
            val endDate = Helpers.stringToDate(routeData.gpsList.last().gpsSaveDate)
            val duration = Duration.between(initDate, endDate)
                .toString()
                .substring(2)
                .replace("(\\d[HMS])(?!$)", "$1 ")
                .lowercase()


            binding.routeStartDateTextView.text = initDate.toString()
            binding.routeEndDateTextView.text = endDate.toString()
            binding.routeETATextView.text = duration


            thread {
                val routes = ArrayList(routeData.gpsList.map { e -> GeoPoint(e.latitude, e.longitude) })
                val roadManager = OSRMRoadManager(activity, "XD")
                roadManager.setMean(OSRMRoadManager.MEAN_BY_BIKE)
                val road = roadManager.getRoad(routes)
                val pathOverlay = RoadManager.buildRoadOverlay(road)
                pathOverlay.color = Color.RED
                pathOverlay.width = 5f


                requireActivity().runOnUiThread{
                    binding.mapView.apply {
                        binding.mapView.apply {
                            setBuiltInZoomControls(true)
                            setMultiTouchControls(true)
                            zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
                        }

                        overlays.add(pathOverlay)
                        invalidate()

                        post {
                            zoomToBoundingBox(pathOverlay.bounds.increaseByScale(1.3f), false)
                        }
                    }
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

    }
}