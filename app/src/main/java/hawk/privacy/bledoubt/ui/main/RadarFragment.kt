package hawk.privacy.bledoubt.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import hawk.privacy.bledoubt.R
import hawk.privacy.bledoubt.databinding.RadarFragmentBinding


class RadarFragment : Fragment() {

    companion object {
        fun newInstance() = RadarFragment()
    }
    private val TAG = "[RadarFragment]"
    private var viewModel: RadarViewModel? = null
    private var backStackCallback: OnBackPressedCallback? = null
    private lateinit var binding: RadarFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RadarFragmentBinding.inflate(layoutInflater)
        backStackCallback = object: OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                requireActivity().moveTaskToBack(true)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backStackCallback!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val view = binding.root
        binding.radarSwitch.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "isEnabled $isChecked")
            viewModel!!.setIsRadarEnabled(isChecked)
        }

        val nearbyBundle = Bundle()
        nearbyBundle.putInt(DeviceListFragment.ARG_LIST_TYPE, DeviceListFragment.NEARBY_TYPE)
        binding.nearbyButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace<DeviceListFragment>(R.id.fragment_container, null, nearbyBundle)
                .addToBackStack("launch_nearby")
                .commit()
        }

        val suspiciousBundle = Bundle()
        suspiciousBundle.putInt(DeviceListFragment.ARG_LIST_TYPE, DeviceListFragment.SUSPICIOUS_TYPE)
        binding.suspiciousButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace<DeviceListFragment>(R.id.fragment_container, null, suspiciousBundle)
                .addToBackStack("launch_suspicious")
                .commit()
        }

        val allBundle = Bundle()
        allBundle.putInt(DeviceListFragment.ARG_LIST_TYPE, DeviceListFragment.ALL_TYPE)
        binding.allButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace<DeviceListFragment>(R.id.fragment_container, null, allBundle)
                .addToBackStack("launch_all")
                .commit()
        }

        val safeBundle = Bundle()
        safeBundle.putInt(DeviceListFragment.ARG_LIST_TYPE, DeviceListFragment.SAFE_TYPE)
        binding.allButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace<DeviceListFragment>(R.id.fragment_container, null, safeBundle)
                    .addToBackStack("launch_safe")
                    .commit()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get<RadarViewModel>()
        viewModel?.getIsRadarEnabled()?.observe(viewLifecycleOwner, { enabled: Boolean ->
            backStackCallback?.isEnabled = enabled
        })
    }
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProvider(requireActivity()).get(RadarViewModel::class.java)
//        viewModel?.getIsRadarEnabled()?.observe(viewLifecycleOwner, Observer { enabled: Boolean ->
//            backStackCallback?.isEnabled = enabled
//        })
//        //Log.d(TAG, "Model $viewModel")
//
//
//        // TODO: Use the ViewModel
//    }
}
