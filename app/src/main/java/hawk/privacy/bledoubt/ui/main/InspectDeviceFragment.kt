package hawk.privacy.bledoubt.ui.main

import android.os.Bundle
import com.mapbox.mapboxsdk.Mapbox
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import hawk.privacy.bledoubt.Trajectory
import hawk.privacy.bledoubt.BeaconHistory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.Property
import hawk.privacy.bledoubt.R
import hawk.privacy.bledoubt.databinding.InspectDeviceFragmentBinding

import java.util.ArrayList

class InspectDeviceFragment : Fragment() {
    //private var mapView: MapView? = null
    private var bluetoothAddress: String? = null
    private lateinit var binding: InspectDeviceFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = InspectDeviceFragmentBinding.inflate(layoutInflater)

        arguments?.let {
            bluetoothAddress = it.getString(BLUETOOTH_ADDRESS_MESSAGE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {



        Log.d(tag, "la" +  bluetoothAddress)
        //Mapbox.getInstance(this, resources.getString(R.string.mapbox_api_key))
        binding.inspectLayoutTitle.text = bluetoothAddress

        val traj = BeaconHistory.getAppBeaconHistory(inflater.context).getTrajectory(bluetoothAddress)

        binding.mapView!!.onCreate(savedInstanceState)
        binding.mapView!!.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) { style -> // Add data for trajectory to renderer;
                style.addSource(
                    GeoJsonSource(
                        "line-source",
                        FeatureCollection.fromFeature(
                            trajectoryToMapFeature(traj)
                        )
                    )
                )

                // TODO: Make this a dot instead of a line if size(traj) = 1.

                // Apply visual style to trajectory data
                style.addLayer(
                    LineLayer("linelayer", "line-source").withProperties(
                        PropertyFactory.lineDasharray(arrayOf(0.01f, 2f)),
                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                        PropertyFactory.lineWidth(5f),
                        PropertyFactory.lineColor(Color.parseColor("#e55e5e"))
                    )
                )
            }

            // Set the camera to something amicable to the user.
            val latLngBoundsBuilder = LatLngBounds.Builder()
            for (detection in traj) {
                latLngBoundsBuilder.include(LatLng(detection.latitude, detection.longitude))
            }
            // TODO: Make this less hacky. Goal is to avoid failure case with just one point.
            Log.d(tag, bluetoothAddress + " " + traj.toString())
            if (traj.size() > 1) {
                latLngBoundsBuilder.include(
                    LatLng(
                        traj.detections[0].latitude + EPSILON_LATLONG,
                        traj.detections[0].longitude + EPSILON_LATLONG
                    )
                )
                latLngBoundsBuilder.include(
                    LatLng(
                        traj.detections[0].latitude - EPSILON_LATLONG,
                        traj.detections[0].longitude - EPSILON_LATLONG
                    )
                )
                val bounds = latLngBoundsBuilder.build()
                mapboxMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        bounds,
                        MAP_CAMERA_DEFAULT_PADDING
                    )
                )
            }
        }
        return binding.root
    }




    override fun onStart() {
        super.onStart()
        binding.mapView!!.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView!!.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView!!.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView!!.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView!!.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView!!.onSaveInstanceState(outState)
    }

    companion object {
        const val BLUETOOTH_ADDRESS_MESSAGE = "hawk.privacy.bledoubt.bluetooth_address"
        const val MAP_CAMERA_DEFAULT_PADDING = 100
        const val EPSILON_LATLONG = 0.00001

        /**
         * Create a displayable Mapsbox feature from the trajectory.
         * @param traj
         * @return feature
         */
        fun trajectoryToMapFeature(traj: Trajectory): Feature {
            val trajectoryCoordinates = ArrayList<Point>()
            for (det in traj) {
                trajectoryCoordinates.add(Point.fromLngLat(det.longitude, det.latitude))
            }
            return Feature.fromGeometry(
                LineString.fromLngLats(trajectoryCoordinates)
            )
        }
    }
}