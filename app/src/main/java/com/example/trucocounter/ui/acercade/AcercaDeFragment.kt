package com.example.trucocounter.ui.acercade

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.trucocounter.databinding.FragmentAcercaDeBinding

class AcercaDeFragment : Fragment() {

    private var _binding: FragmentAcercaDeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAcercaDeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
