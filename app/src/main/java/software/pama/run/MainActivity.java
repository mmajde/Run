package software.pama.run;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {

    // Czas określający co ile ms zaktualizować lokalizację.
    private final long UPDATE_TIME_MS = 2000;
    // Serwis otrzymujący uaktualnienia lokalizacji.
    private LocationService locationService;
    // Wartość definiująca czy istnieje połączenie z serwisem.
    private boolean serviceBounded = false;
    // Bieżąca lokalizacja użytkownika.
    private Location currentLocation;
    // Całkowity przebyty dystans.
    private float totalDistance = 0;
    // Przechowuje wątki aktywności.
    private Handler threadHandler = new Handler();

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if(locationService == null) {
                serviceBounded = true;
                LocationService.LocalBinder binder = (LocationService.LocalBinder) iBinder;
                locationService = binder.getLocationService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            locationService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        final Button runService = (Button) findViewById(R.id.btn_run);
        runService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bindAndStartService();
                showLocation.run();
            }
        });

        final Button getDistance = (Button) findViewById(R.id.btn_get_distance);
        getDistance.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateLocationAndDistance();
            }
        });

    }

    /**
     * Powiązuje aktywność z serwisem i uruchamia serwis.
     */
    private void bindAndStartService() {
        Intent intent = new Intent(getApplicationContext(), LocationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    /**
     * Uaktualnia biężącą lokalizację oraz wylicza całkowity dystans.
     */
    private void updateLocationAndDistance() {
        if (serviceBounded) {
            Location location = locationService.getCurrentLocation();
            if (currentLocation == null) {
                if(isAccurate(location))
                    currentLocation = location;
                return;
            }
            if (location == null) {
                return;
            }

//            if (dist >= 0.0) {
                if(isAccurate(location)) {
                    float dist = currentLocation.distanceTo(location);
                    totalDistance += dist;
                    TextView txtDistance = (TextView) findViewById(R.id.txtDistance);
                    txtDistance.setText(Float.toString(totalDistance) + " m");
                    Log.d("D", "Total distance: " + totalDistance);
                    currentLocation = location;
                }
//            }
        }
    }

    /**
     * Metoda sprawdzająca czy lokalizacja pobrana z serwisu jest wystarczająco dokładna.
     */
    private boolean isAccurate(Location location) {
        if(location == null)
            return false;
        Log.d("D", "Location accuracy: " + location.getAccuracy());
        if(location.getAccuracy() > 0.0 && location.getAccuracy() < 10.0) {
            return true;
        }
        return false;
    }

    /**
     * Wątek wyświetlający lokalizację.
     */
    private Runnable showLocation = new Runnable() {
        @Override
        public void run() {
            updateLocationAndDistance();
            threadHandler.postDelayed(this, UPDATE_TIME_MS);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(serviceBounded) {
            unbindService(mConnection);
            serviceBounded = false;
            locationService.stopSelf();
        }
        threadHandler.removeCallbacksAndMessages(showLocation);
        super.onDestroy();
    }
}
