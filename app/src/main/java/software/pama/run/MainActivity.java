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

import com.google.android.gms.maps.model.LatLng;


public class MainActivity extends Activity {
    // Connection
    private LocationService mService;
    private boolean mBound = false;
    private Location mCurrentLocation;
    private float distance = 0;

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if(mService == null) {
                mBound = true;
                LocationService.LocalBinder binder = (LocationService.LocalBinder) iBinder;
                mService = binder.getLocationService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        final Button runService = (Button) findViewById(R.id.btn_run);
        runService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LocationService.class);
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                startService(intent);
            }
        });

        final Button getDistance = (Button) findViewById(R.id.btn_get_distance);
        getDistance.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateDistance();
            }
        });

    }

    private void updateDistance() {
        if (mBound) {
            Location location = mService.getmCurrentLocation();
            if (mCurrentLocation == null) {
                mCurrentLocation = location;
                return;
            }
            if (location == null) {
                return;
            }
            float dist = mCurrentLocation.distanceTo(location);
            if (dist > 0.0) {
                distance += dist;
                TextView txtDistance = (TextView) findViewById(R.id.txtDistance);
                txtDistance.setText(Float.toString(distance) + " m");
                Log.i("I", Float.toString(distance));
                mCurrentLocation = location;
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
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onDestroy();
    }
}
