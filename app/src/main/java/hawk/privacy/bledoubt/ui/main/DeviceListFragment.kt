package hawk.privacy.bledoubt.ui.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import hawk.privacy.bledoubt.BeaconHistory
import hawk.privacy.bledoubt.DeviceMetadata
import hawk.privacy.bledoubt.DeviceRecyclerViewAdapter
import hawk.privacy.bledoubt.R

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [DeviceListFragment.OnListFragmentInteractionListener] interface.
 */
class DeviceListFragment : Fragment() {
    // TODO: Customize parameters
    private var columnCount = 1
    private var filter = 0;

    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
            filter = it.getInt(ARG_LIST_TYPE)

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_device_list_list, container, false)
        val devices = getFilteredDevices(filter);

        // Set the adapter
        val deviceAdapter = DeviceRecyclerViewAdapter(ArrayList(), context)
        Log.d("Devices", devices.value.toString())
        Log.d(tag, "Tag " + filter)
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = deviceAdapter
            }
        }
        devices.observe(viewLifecycleOwner, deviceAdapter)
        return view
    }

    private fun getFilteredDevices(filterType: Int): LiveData<List<DeviceMetadata>> {
        val beaconHistory = BeaconHistory.getAppBeaconHistory(requireActivity())
        when(filterType) {
            NEARBY_TYPE -> return beaconHistory.liveNearbyDeviceList
            ALL_TYPE -> return  beaconHistory.liveDeviceList
            SUSPICIOUS_TYPE -> return  beaconHistory.liveSuspiciousDeviceList
            SAFE_TYPE -> return  beaconHistory.liveSafeDeviceList
            else -> return beaconHistory.liveDeviceList
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListFragmentInteraction(item: DeviceMetadata?)
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"
        const val ARG_LIST_TYPE = "List_type"
        const val NEARBY_TYPE = 0
        const val ALL_TYPE = 1
        const val SUSPICIOUS_TYPE = 2
        const val SAFE_TYPE = 3

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
                DeviceListFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
                }
    }
}
