package software.pama.run;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {
    // Serwis otrzymujący uaktualnienia lokalizacji.
    private LocationService locationService;
    // Wartość definiująca czy istnieje połączenie z serwisem.
    private boolean serviceBounded = false;
    // Bieżąca lokalizacja użytkownika.
    private Location currentLocation;
    // Całkowity przebyty dystans.
    private float totalDistance = 0;

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
                currentLocation = location;
                return;
            }
            if (location == null) {
                return;
            }
            float dist = currentLocation.distanceTo(location);
            if (dist >= 0.0) {
                totalDistance += dist;
                TextView txtDistance = (TextView) findViewById(R.id.txtDistance);
                txtDistance.setText(Float.toString(totalDistance) + " m");
                Log.i("I", Float.toString(totalDistance));
                currentLocation = location;
            }
        }
    }

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
        Log.v("STOP_ACTIVITY_1", "DONE");
        if(serviceBounded) {
            Log.v("STOP_ACTIVITY_2", "DONE");
            unbindService(mConnection);
            serviceBounded = false;
            locationService.stopSelf();
        }
        super.onDestroy();
    }
}
