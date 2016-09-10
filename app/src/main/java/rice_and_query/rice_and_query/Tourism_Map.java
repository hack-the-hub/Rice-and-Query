package rice_and_query.rice_and_query;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public String toilet_url = "http://www.belfastcity.gov.uk/nmsruntime/saveasdialog.aspx?lID=15256&sID=2430";
    public String translink_url = "https://www.opendatani.gov.uk/dataset/76fb7478-cc19-4254-af3c-b269596bc711/resource/fe49e26a-7324-4644-9a61-bd736aa5e8fc/download/translink-stations-ni.geojson";

    private boolean load_toilets = false;
    private boolean load_transport = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    }

    public void select(View view) {
        setContentView(R.layout.selection);
    }

    public void transport(View view) {
        load_transport = true;
        doTheThing();
    }

    public void toilets(View view) {
        load_toilets = true;
        doTheThing();
    }

    private void doTheThing() {
        setContentView(R.layout.activity_tourism__map);
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


        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(54.5968, -6.80);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        List<String> toiletData = new ArrayList<>();
        JSONObject translinkData = new JSONObject();
        try {
            URL url = new URL(toilet_url);
            toiletData = new ApiFetchCsv().execute(url).get();
            url = new URL(translink_url);
            translinkData = new ApiFetchJson().execute(url).get();
            Log.i("this", toiletData.toString());
        } catch (java.io.IOException | InterruptedException | ExecutionException e) {
            Log.e("that", e.getMessage());
        }

        if (load_toilets) {
            for (String i : toiletData) {
                String[] words = i.split(",");
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title(words[0]);
                markerOptions.snippet("Public Toilet");
                markerOptions.position(new LatLng(Double.parseDouble(words[6]), Double.parseDouble(words[5])));
                googleMap.addMarker(markerOptions);
            }
        }

        JSONArray stations = translinkData.optJSONArray("features");

        if (load_transport) {
            try {
                for (int i = 0; i < stations.length(); i++) {
                    JSONObject station = stations.getJSONObject(i);
                    JSONObject station_props = station.optJSONObject("properties");
                    String name = station_props.optString("Station");
                    String type = station_props.optString("Type").equals("R") ? "Rail" : "Bus";

                    JSONArray json_coords = station.optJSONObject("geometry").optJSONArray("coordinates");
                    LatLng latLng = new LatLng(json_coords.getDouble(1), json_coords.getDouble(0));

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.title(name);
                    markerOptions.snippet(type + " Station");
                    markerOptions.position(latLng);
                    googleMap.addMarker(markerOptions);
                }
            }
            catch (JSONException e) {
                Log.e("error", e.getMessage());
            }
        }
    }
}

class ApiFetchCsv extends AsyncTask<URL, Void, List<String>> {
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

class ApiFetchJson extends AsyncTask<URL, Void, JSONObject> {
    protected JSONObject doInBackground(URL... urls) {
        try {
            URL url = urls[0];
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(in),1000);
            for (String line = r.readLine(); line != null; line =r.readLine()){
                stringBuilder.append(line);
                stringBuilder.append('\n');
            }
            String json_string = stringBuilder.toString();
            JSONObject json = new JSONObject();
            try {
                json = new JSONObject(json_string);
            }
            catch (JSONException e) {
                Log.e("error", e.getMessage());
            }

            in.close();
            return json;
        }
        catch (IOException e) {
            Log.e("tag", e.getMessage());
            return null;
        }
    }
}