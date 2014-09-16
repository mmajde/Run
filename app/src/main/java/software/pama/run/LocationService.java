package software.pama.run;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
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
    // Czas co jaki ma być uaktualniana informacja o lokalizacji.
    private static final int UPDATE_TIME_MS = 100;

    // Zapewnia dostęp do usług lokalizacji.
    public LocationManager locationManager;
    // Bieżąca lokalizacja.
    public Location currentLocation = null;
    // Przechowuje wątki serwisu.
    private Handler threadHandler = new Handler();
    // Pozwala powiązać serwis z aktywnością.
    private IBinder localBinder = new LocalBinder();
    // Pomocniczy toast do wyświetlania lokalizacji.
    private Toast mToast;

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        //getLastKnownLocation();
        mToast = Toast.makeText(getApplicationContext(), "Service started", Toast.LENGTH_LONG);
        mToast.show();
        showLocation.run();

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
        threadHandler.removeCallbacksAndMessages(showLocation);
        super.onDestroy();
    }

    /**
     * Wątek wyświetlający lokalizację.
      */
    private Runnable showLocation = new Runnable() {
        @Override
        public void run() {
            if(currentLocation != null) {
                mToast.setText(currentLocation.getLatitude() + " " + currentLocation.getLongitude());
                mToast.show();
            }
            threadHandler.postDelayed(this, 1000);
        }
    };

    public void onLocationChanged(final Location location)
    {
        Log.i("*********************", "Location changed");
        currentLocation = location;
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

    /**
     * Zwraca bięzącą lokalizację.
     */
    public Location getCurrentLocation() {
        return currentLocation;
    }
}