package com.example.s18635_bikertracker.fragments

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.s18635_bikertracker.LoginActivity
import com.example.s18635_bikertracker.R
import com.example.s18635_bikertracker.databinding.SettingsFragmentBinding
import com.example.s18635_bikertracker.helpers.Globals
import com.example.s18635_bikertracker.helpers.User


class SettingFragment: Fragment() {
    private var _binding: SettingsFragmentBinding? = null
    private val binding get() = _binding!!
    lateinit var media: MediaPlayer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        _binding = SettingsFragmentBinding.inflate(inflater, container, false)

        binding.loginTextView.text = Globals.currUser!!.email
        media = MediaPlayer.create(requireActivity(), R.raw.hurray)
        media.isLooping = false

        binding.logOutButton.setOnClickListener{
            User.reset()

            activity?.let{
                val intent = Intent(it, LoginActivity::class.java)

                if(Globals.gpsService != null){
                    requireActivity().stopService(
                        Intent(requireActivity(), Globals.gpsService!!::class.java)
                    )
                    Globals.gpsService = null
                }

                it.startActivity(intent)
                it.finish()
            }
        }

        binding.button.setOnClickListener {
            media.start()
        }

        return binding.root
    }

    override fun onDestroyView() {
        media.release()
        _binding = null

        super.onDestroyView()
    }
}