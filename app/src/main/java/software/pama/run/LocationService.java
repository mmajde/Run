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

public class LocationService extends Service implements LocationListener
{
    private static final int UPDATE_TIME_MS = 100;

    public LocationManager locationManager;
    public Location mCurrentLocation = null;
    private Handler handler = new Handler();
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

//    public void runLocationService() {
//
//    }

    private void getLastKnownLocation() {
        if(mCurrentLocation == null) {
            String list[] = {
                    LocationManager.GPS_PROVIDER,
                    LocationManager.NETWORK_PROVIDER,
                    LocationManager.PASSIVE_PROVIDER };
            for (String s : list) {
                Location tmp = locationManager.getLastKnownLocation(s);
                if (mCurrentLocation == null || tmp.getTime() > mCurrentLocation.getTime()) {
                    mCurrentLocation = tmp;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.v("STOP_SERVICE", "DONE");
        locationManager.removeUpdates(this);
        handler.removeCallbacksAndMessages(showLocation);
        stopSelf();
        super.onDestroy();
    }

    private Runnable showLocation = new Runnable() {
        @Override
        public void run() {
            if(mCurrentLocation != null) {
                mToast.setText(mCurrentLocation.getLatitude() + " " + mCurrentLocation.getLongitude());
                mToast.show();
            }
            handler.postDelayed(this, 1000);
        }
    };

    public void onLocationChanged(final Location loc)
    {
        Log.i("*********************", "Location changed");
        mCurrentLocation = loc;
    }

    public void onProviderDisabled(String provider)
    {
        Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
    }


    public void onProviderEnabled(String provider)
    {
        Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
    }


    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        Log.d(SyncStateContract.Constants.DATA, provider + " \nSTATUS: " + status);
    }

    private IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public LocationService getLocationService() {
            return LocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public Location getmCurrentLocation() {
        return mCurrentLocation;
    }
}