package hawk.privacy.bledoubt.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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
    private var backStackCallback: OnBackPressedCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val view = inflater.inflate(R.layout.radar_fragment, container, false)
        view.radar_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            Log.d(TAG, "isEnabled $isChecked")
            viewModel!!.setIsRadarEnabled(isChecked)
        }

        val nearbyBundle = Bundle()
        nearbyBundle.putInt(DeviceListFragment.ARG_LIST_TYPE, DeviceListFragment.NEARBY_TYPE)
        view.nearby_button.setOnClickListener {view ->
            parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DeviceListFragment::class.java, nearbyBundle)
                    .addToBackStack("launch_nearby")
                    .commit()
        }

        val suspiciousBundle = Bundle()
        suspiciousBundle.putInt(DeviceListFragment.ARG_LIST_TYPE, DeviceListFragment.SUSPICIOUS_TYPE)
        view.suspicious_button.setOnClickListener { view ->
            parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DeviceListFragment::class.java, suspiciousBundle)
                    .addToBackStack("launch_suspicious")
                    .commit()
        }

        val allBundle = Bundle()
        allBundle.putInt(DeviceListFragment.ARG_LIST_TYPE, DeviceListFragment.ALL_TYPE)
        view.all_button.setOnClickListener { view ->
            parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DeviceListFragment::class.java, allBundle)
                    .addToBackStack("launch_all")
                    .commit()
        }

        val safeBundle = Bundle()
        safeBundle.putInt(DeviceListFragment.ARG_LIST_TYPE, DeviceListFragment.SAFE_TYPE)
        view.safe_button.setOnClickListener { view ->
            parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DeviceListFragment::class.java, safeBundle)
                    .addToBackStack("launch_safe")
                    .commit()
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(RadarViewModel::class.java)
        viewModel?.getIsRadarEnabled()?.observe(viewLifecycleOwner, Observer { enabled: Boolean ->
            backStackCallback?.isEnabled = enabled
        })
        //Log.d(TAG, "Model $viewModel")


        // TODO: Use the ViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backStackCallback = object: OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                requireActivity().moveTaskToBack(true)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backStackCallback!!)
    }
}
