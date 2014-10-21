package software.pama.run.run;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import software.pama.run.R;
import software.pama.run.location.LocationServiceManager;


public class MainActivity extends Activity {

    /** Czas określający co ile ms zaktualizować wyświetlane dane. */
    private final long THREAD_UPDATE_MS = 2000;

    /** Przechowuje wątki aktywności. */
    private Handler threadHandler;
    /** Zapewnia komunikację z LocationService i jego obsługę. */
    private LocationServiceManager userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        initializeClassFields();
        setRunServiceButton();
        setGetDistanceButton();
    }

    @Override
    protected void onDestroy() {
        stopLocationService();
        stopThreads();
        super.onDestroy();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    private void initializeClassFields() {
        userLocation = new LocationServiceManager(getApplicationContext());
        threadHandler = new Handler();
    }

    private void setGetDistanceButton() {
        final Button getDistance = (Button) findViewById(R.id.btn_get_distance);
        getDistance.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });
    }

    private void setRunServiceButton() {
        final Button runService = (Button) findViewById(R.id.btn_run);
        runService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                userLocation.startLocationService();
                showLocation.run();
            }
        });
    }

    /**
     * Wątek wyświetlający całkowitą lokalizację.
     */
    private Runnable showLocation = new Runnable() {
        @Override
        public void run() {
            updateUI();
            delayThreadPost();
        }
    };

    private void delayThreadPost() {
        threadHandler.postDelayed(showLocation, THREAD_UPDATE_MS);
    }

    private void updateUI() {
        userLocation.updateLocationDetails();
        float totalDistance = userLocation.getUserTotalDistance();
        TextView txtDistance = (TextView) findViewById(R.id.txtDistance);
        txtDistance.setText(Float.toString(totalDistance) + " m");
        Log.d("D", "Total distance: " + totalDistance);
    }

    private void stopThreads() {
        threadHandler.removeCallbacksAndMessages(showLocation);
    }

    private void stopLocationService() {
        userLocation.stopLocationService();
    }
}
