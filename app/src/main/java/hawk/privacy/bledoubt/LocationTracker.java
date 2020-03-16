package hawk.privacy.bledoubt;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;

import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;


public class LocationTracker implements LocationListener {
    protected static final String TAG = "[LocationTracker]";
    protected Location lastLocation = null;
    protected LocationListener locationListener;


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


    public LocationTracker(LocationManager locationManager) {
        Log.i(TAG, "New tracker.");

        String provider = LocationManager.GPS_PROVIDER; //locationManager.getProvider(.to);
        locationManager.requestLocationUpdates(provider, (long)  2 * 60*1000, (float) 10, this);//requestLocationUpdates(provider, 2 * 60 * 1000, 10, locationListener);
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
