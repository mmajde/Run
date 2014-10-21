package software.pama.run.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.util.Log;
import android.widget.Toast;

/**
 * Serwis pobierający lokalizację użytkownika zarówno gdy aplikacja jest w trakcie działania
 * jak i wtedy gdy jest zatrzymana.
 */
public class LocationService extends Service implements LocationListener
{
    private static final int LOCATION_UPDATE_TIME_MS = 500;
    private static final int LOCATION_UPDATE_DIST_M = 15;
    private static final int LOCATION_ACCURACY_M = 20;

    public Location currentLocation;
    // Zapewnia dostęp do usług lokalizacji.
    public LocationManager locationManager;
    // Pozwala powiązać serwis z aktywnością.
    private IBinder localBinder;

    @Override
    public void onCreate()
    {
        super.onCreate();
        localBinder = new LocalBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_TIME_MS, LOCATION_UPDATE_DIST_M, this);
        //getLastKnownLocation();
        Toast mToast = Toast.makeText(getApplicationContext(), "Service started", Toast.LENGTH_LONG);
        mToast.show();

        return START_STICKY;
    }

    private void getLastKnownLocation() {
        if(currentLocation == null) {
            String list[] = {
                    LocationManager.GPS_PROVIDER,
                    LocationManager.NETWORK_PROVIDER,
                    LocationManager.PASSIVE_PROVIDER };
            for (String s : list) {
                Location tmp = locationManager.getLastKnownLocation(s);
                if (currentLocation == null || tmp.getTime() > currentLocation.getTime()) {
                    currentLocation = tmp;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.v("STOP_SERVICE", "DONE");
        locationManager.removeUpdates(this);
        super.onDestroy();
    }

    public void onLocationChanged(final Location location)
    {
        Log.i("*********************", "Location changed");
        if(isAccurate(location)) {
            currentLocation = location;
        }
    }

    public void onProviderDisabled(String provider)
    {
        Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
    }

    public void onProviderEnabled(String provider)
    {
        Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(SyncStateContract.Constants.DATA, provider + " \nSTATUS: " + status);
    }

    private boolean isAccurate(Location location) {
        if(location == null)
            return false;
        Log.d("D", "Location accuracy: " + location.getAccuracy());
        if(location.getAccuracy() > 0.0 && location.getAccuracy() < LOCATION_ACCURACY_M) {
            return true;
        }
        return false;
    }

    /**
     * Klasa pozwalająca powiązać serwis z aktywnością.
     */
    public class LocalBinder extends Binder {
        public LocationService getLocationService() {
            return LocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }
}