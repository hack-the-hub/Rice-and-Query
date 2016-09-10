package rice_and_query.rice_and_query;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Tourism_Map extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public String toilet_url = "http://www.belfastcity.gov.uk/nmsruntime/saveasdialog.aspx?lID=15256&sID=2430";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tourism__map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(54, -5);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        List<String> data = new ArrayList<>();
        try {
            URL url = new URL(toilet_url);
            data = new ApiFetch().execute(url).get();
            Log.i("this", data.toString());
        } catch (java.io.IOException | InterruptedException | ExecutionException e) {
            Log.e("that", e.getMessage());
        }

        for (String i : data) {
            String[] words = i.split(",");
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.title(words[0]);
            markerOptions.position(new LatLng(Double.parseDouble(words[6]), Double.parseDouble(words[5])));
            mMap.addMarker(markerOptions);
        }


    }
}

class ApiFetch extends AsyncTask<URL, Void, List<String>> {
    protected List<String> doInBackground(URL... urls) {
        try {
            URL url = urls[0];
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            List<String> list = new ArrayList<>();
            BufferedReader r = new BufferedReader(new InputStreamReader(in),1000);
            for (String line = r.readLine(); line != null; line =r.readLine()){
                list.add(line);
            }
            in.close();
            return list.subList(1, list.size()-1);
        }
        catch (IOException e) {
            Log.e("tag", e.getMessage());
            return null;
        }
    }
}