package com.example.s18635_bikertracker

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.s18635_bikertracker.databinding.ActivityMainBinding
import com.example.s18635_bikertracker.fragments.HistoryFragment
import com.example.s18635_bikertracker.fragments.HomeFragment
import com.example.s18635_bikertracker.fragments.SettingFragment
import com.example.s18635_bikertracker.helpers.Globals
import com.example.s18635_bikertracker.helpers.createChannel
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.osmdroid.config.Configuration


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNav : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createChannel(this)

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        loadFragment(HomeFragment())
        bottomNav = binding.bottomNav.apply {
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.home -> {
                        loadFragment(HomeFragment())
                    }
                    R.id.history -> {
                        loadFragment(HistoryFragment())
                    }
                    R.id.settings -> {
                        loadFragment(SettingFragment())
                    }
                }
                true
            }
        }

        Globals.geoClient = LocationServices.getGeofencingClient(this)
    }

    private fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(binding.container.id, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed() {
        if(bottomNav.selectedItemId == R.id.home){
            this.moveTaskToBack(true)
        }else{
            bottomNav.selectedItemId = R.id.home
        }
    }
}