package hawk.privacy.bledoubt;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;


public class LocationTracker {
    protected Location lastLocation = null;
    protected LocationListener locationListener;

    public LocationTracker() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setLastLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };


    }

    /**
     * Sets the most recent location estimate.
     * @param location
     */
    protected synchronized void setLastLocation(Location location) {
        this.lastLocation = new Location(location);
    }

    public synchronized Location getLastLocation() {
        return new Location(this.lastLocation);
    };
}
