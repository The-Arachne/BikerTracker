package com.example.s18635_bikertracker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.s18635_bikertracker.databinding.ActivityLoginBinding
import com.example.s18635_bikertracker.firebaseDB.FirebaseDAO
import com.example.s18635_bikertracker.helpers.Globals
import com.example.s18635_bikertracker.helpers.User
import com.example.s18635_bikertracker.room.AppDatabase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.concurrent.thread


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private var lastReqCode:Int = 0

    //INITIATION SECTION
    //----------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        Globals.auth = Firebase.auth

        setContentView(binding.root)

        binding.loginButt.setOnClickListener { onProceedClick() }
    }

    override fun onStart() {
        super.onStart()
        loadSharedPreferences()
    }

    override fun onResume() {
        User.initIfExists()
        binding.emailEt.setText(Globals.currUser?.email)
        binding.passwEt.setText(Globals.currUser?.password)
        super.onResume()
    }

    private fun loadSharedPreferences() {
        if(Globals.sharedPref != null) return

        Globals.sharedPref = getSharedPreferences("user", MODE_PRIVATE)
        Globals.appDb = Room
            .databaseBuilder(applicationContext, AppDatabase::class.java, "GpsDB")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }



    //BUSINESS SECTION
    //----------------------------------------------------------------------------------------------
    private fun onProceedClick(){
        val ihm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        ihm.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)

        clickProgress()

        thread {
            if(!checkPermissions())
                checkAndRequestPermissions()
            else{
                val email: String = binding.emailEt.text.toString().lowercase()
                val passwd: String = binding.passwEt.text.toString()

                if(email.isEmpty() || passwd.isEmpty()){
                    runOnUiThread{
                        Toast.makeText(this, "Uzupełnij dane", Toast.LENGTH_SHORT).show()
                    }
                    clickProgress()
                }else if(!checkInternetConnection(this)){
                    runOnUiThread{
                        Toast.makeText(this, "Sprawdź połączenie z internetem", Toast.LENGTH_LONG).show()
                    }
                    clickProgress()
                }else{
                    Globals.auth.signInWithEmailAndPassword(email, passwd)
                        .addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                syncAccounts(email)
                                User.addNew(email, passwd)

                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                        }
                        .addOnFailureListener{ excp ->
                            Toast.makeText(this, excp.localizedMessage, Toast.LENGTH_LONG).show()
                            clickProgress()
                        }
                }
            }
        }
    }

    private fun syncAccounts(newEmail: String) {
        Globals.firebaseDb = Firebase.database.reference

        FirebaseDAO.firebaseSyncAccounts(applicationContext, Globals.currUser?.email, newEmail)
    }

    private fun clickProgress(){
        runOnUiThread {
            if(binding.progressBar.visibility == View.GONE){
                binding.progressBar.visibility = View.VISIBLE
                binding.loginButt.isEnabled = false
            }else{
                binding.progressBar.visibility = View.GONE
                binding.loginButt.isEnabled = true
            }
        }
    }

    private fun checkInternetConnection(context: Context): Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

            else -> false
        }
    }



    //PERMISSION SECTION
    //----------------------------------------------------------------------------------------------
    private fun checkPermissions(): Boolean {
        if(checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return false
        if(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) return false
        if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) return false
        if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) return false
        if(checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return false
        if(checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return false
        if(checkSelfPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) return false

        return true
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()

        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) permissions.add(android.Manifest.permission.CAMERA)

        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            permissions.add(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        if(permissions.any()){
            lastReqCode = (Math.random() * 1000).toInt()
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), lastReqCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            lastReqCode -> {
                if(grantResults.isEmpty() || grantResults.contains(PackageManager.PERMISSION_DENIED)){
                    Toast.makeText(this, "Do poprawnego działania aplikacji musisz zgodzić się na wszystkie pozwolenia!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}