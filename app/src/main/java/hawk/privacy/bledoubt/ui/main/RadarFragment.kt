package hawk.privacy.bledoubt.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import hawk.privacy.bledoubt.DeviceListFragment
import hawk.privacy.bledoubt.R
import kotlinx.android.synthetic.main.radar_fragment.view.*

class RadarFragment : Fragment() {

    companion object {
        fun newInstance() = RadarFragment()
    }
    private val TAG = "[RadarFragment]"
    private var viewModel: RadarViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val view = inflater.inflate(R.layout.radar_fragment, container, false)
        view.radar_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            Log.d(TAG, "isEnabled $isChecked")
            viewModel!!.setIsRadarEnabled(isChecked)
        }

        view.nearby_button.setOnClickListener {view ->
            parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DeviceListFragment::class.java, null)
                    .addToBackStack("launch_nearby")
                    .commit()
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(RadarViewModel::class.java)
        Log.d(TAG, "Model $viewModel")


        // TODO: Use the ViewModel
    }


}
