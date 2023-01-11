package com.example.s18635_bikertracker.helpers

class User(val email: String, val password: String){

    companion object{
        fun addNew(email: String, password: String) {
            Globals.sharedPref!!.edit().putString("email", email).apply()
            Globals.sharedPref!!.edit().putString("password", password).apply()

            Globals.currUser = User(email,password)
        }
        fun initIfExists() {
            val email: String? = Globals.sharedPref!!.getString("email", null)
            val password: String? = Globals.sharedPref!!.getString("password", null)

            if(email != null && password != null)
                Globals.currUser = User(email,password)
        }

        fun reset(){
            //Globals.sharedPref!!.edit().putString("email", null).apply()
            //Globals.sharedPref!!.edit().putString("password", null).apply()

            Globals.currUser = null
        }
    }
}