package hawk.privacy.bledoubt;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


public class LocationTracker implements LocationListener {
    protected static final String TAG = "[LocationTracker]";
    protected Location lastLocation = null;
    protected LocationRequest locationRequest;
    protected LocationCallback locationCallback;
    protected LocationListener locationListener;
    protected FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, location.toString());
        setLastLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}


    public LocationTracker(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        Log.i(TAG, "New tracker.");

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationRequest.setInterval(10 * 1000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                lastLocation = locationResult.getLastLocation();
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());// provider, (long) 2 * 60 * 1000, (float) 10, this);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the most recent location estimate.
     * @param location
     */
    protected synchronized void setLastLocation(Location location) {
        this.lastLocation = new Location(location);
    }

    public synchronized Location getLastLocation() {
        if (this.lastLocation != null)
            return new Location(this.lastLocation);
        return null;
    };
}
