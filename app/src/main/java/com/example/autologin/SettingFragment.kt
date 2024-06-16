package com.example.autologin

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.autologin.databinding.FragmentLoginBinding
import com.example.autologin.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {
    private var _binding:FragmentSettingBinding?=null
    private val binding get() = _binding!!
    private var debugMode:Boolean=false
    private lateinit var sp: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingBinding.inflate(inflater,container,false)
        sp = requireContext().getSharedPreferences("sp_config", Context.MODE_PRIVATE)
        debugMode = sp.getBoolean("debugMode",false)
        binding.checkBox.isChecked = debugMode
        binding.checkBox.setOnClickListener{
            debugMode=!debugMode
            sp.edit().putBoolean("debugMode",debugMode).apply()
        }
        return binding.root
    }
}