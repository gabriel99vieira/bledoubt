package hawk.privacy.bledoubt.ui.main

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import hawk.privacy.bledoubt.*
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import java.util.concurrent.TimeUnit

class RadarFragment : Fragment() {

    companion object {
        fun newInstance() = RadarFragment()
    }

    private lateinit var viewModel: RadarViewModel


    private fun observeViewModel() {
        viewModel.isRadarEnabled.observe(this, Observer {
            R.layout.radar_fragment-
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.radar_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(RadarViewModel::class.java)
        // TODO: Use the ViewModel
    }


}
