package software.pama.run;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationListener;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMyLocationChangeListener {
    // Might be null if Google Play services APK is not available.
    private GoogleMap mMap;
    // Zmienna przybiera wartosc true jesli mozna z powodzeniem odwolywac sie do map
    private boolean locationEnabled = false;
    // Linia rysowana na mapie
    private PolylineOptions line;
    // Przebyty dystans
    private float distance = 0;
    // Nasza ostatnia zmierzona lokalizacja
    private  Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        line = new PolylineOptions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        // Uruchamiamy funkcję która nas lokalizuje oraz utrzymuje połączenie z GPS-em
        mMap.setMyLocationEnabled(true);
        // Ustawiamy listener reagujacy na kazda zmianę pozycji, po każdej zmianie uruchamiana zostaje funkcja onMyLocationChange
        mMap.setOnMyLocationChangeListener(this);
    }

    @Override
    public void onMyLocationChange(Location location) {
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        float zoom;
        float dist;
        // Jeśli dopiero co uruchomiliśmy mapy (jeśli zostala wywołana ta funkcja to znaczy ze mapy sa juz gotowe)
        if(!locationEnabled) {
            zoom = 15;
            // Aby wiedzieć że juz lokalizacja jest dostępna
            locationEnabled = true;
            // Nadanie pierwszej wartości zmiennej
            lastLocation = location;
        }
        else
            zoom = mMap.getCameraPosition().zoom;

        CameraUpdate myLoc = CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder().target(latlng).zoom(zoom).build());
        //mMap.animateCamera(myLoc);

        // Wyliczamy dystans
        dist = calculateDistance(lastLocation, location);
        // Jeśli wiekszy niż 4m
        if(dist > 4.0) {
            drawLine(latlng);
            distance += dist;
            TextView txtDistance = (TextView) findViewById(R.id.txtDistance);
            txtDistance.setText(Float.toString(distance) + " m");
            lastLocation = location;
        }
    }

    /**
     * Funkcja rysująca linię na podstawie parametru LatLng
     *
     * @param latlng - odcinek pomiędzy dwoma lokalizacjami
     */
    public void drawLine(LatLng latlng) {
        // Czyścimy mapę
        mMap.clear();
        // Rysujemy calą linię z dodanym nowym punktem
        mMap.addPolyline(line.add(latlng));
    }

    /**
     * Funkcja wyliczająca dystans pomiędzy dwoma punktami na mapie
     *
     * @param p1 - lokalizacja pierwszego punktu
     * @param p2 - lokalizaja drugiego punktu
     */
    public float calculateDistance(Location p1, Location p2) {
        return p1.distanceTo(p2);
    }
}
