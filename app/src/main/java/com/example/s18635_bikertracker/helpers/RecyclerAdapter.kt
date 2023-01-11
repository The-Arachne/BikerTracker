package com.example.s18635_bikertracker.helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.s18635_bikertracker.R
import java.time.Duration


class RecyclerAdapter(val f: (routeId: String) -> Unit) : RecyclerView.Adapter<RecyclerAdapter.CustomVH>() {

    private var data = Globals.appDb!!.gpsDAO().getAllRoutes()


    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomVH {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.route_card, parent, false)
        return CustomVH(cellForRow)
    }

    override fun onBindViewHolder(holder: CustomVH, position: Int) {
        val dataOne = data[position]

        val initDate = Helpers.stringToDate(dataOne.routeDate)
        val endDateString = Globals.appDb!!.gpsDAO().getRouteLastGPSDate(dataOne.routeDate)
        val endDate = Helpers.stringToDate(endDateString)

        val duration = Duration.between(initDate, endDate)
            .toString()
            .substring(2)
            .replace("(\\d[HMS])(?!$)", "$1 ")
            .lowercase()

        holder.routeStartDateTextView.text = initDate.toString()
        holder.routeLengthTextView.text = duration
    }

    inner class CustomVH(view: View) : RecyclerView.ViewHolder(view){
        var routeStartDateTextView: TextView = view.findViewById(R.id.textView_routeDate)
        var routeLengthTextView: TextView = view.findViewById(R.id.textView_routeLenght)

        init {
            view.setOnClickListener{
                val position = adapterPosition
                f(data[position].routeDate)
            }
        }
    }
}

