package fi.jamk.jsonexample;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private JSONArray kentat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // get data from a JSON-file (some old school work, shows golf courses in Finland)
        FetchDataTask task = new FetchDataTask();
        task.execute("http://student.labranet.jamk.fi/~K1698/web-ohjelmointi/oppimispaivakirja/Demo05/Teht1/kentat.json");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // set default location and zoom for the map (what it shows when user opens it)
        mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(62.2333333, 25.7333333),5) );

        // info window customization and stuff
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Context mContext = getApplicationContext();

                LinearLayout info = new LinearLayout(mContext);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(mContext);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(mContext);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
    }

    public void createMarkersFromJson(JSONArray kentat) throws JSONException {
        try {
            // goes through all the courses on the file
            for (int i = 0; i < kentat.length(); i++) {
                JSONObject kentta = kentat.getJSONObject(i);

                // info to put into the snippet
                StringBuilder stringbuilder = new StringBuilder();
                stringbuilder.append(kentta.getString("Osoite")).append("\n")
                        .append(kentta.getString("Puhelin")).append("\n")
                        .append(kentta.getString("Sahkoposti")).append("\n")
                        .append(kentta.getString("Webbi")).append("\n");

                // location (latitude and longitude)
                LatLng loc = new LatLng(Double.parseDouble(kentta.getString("lat")), Double.parseDouble(kentta.getString("lng")));

                // puts all the markers on map, with title and some info
                // if the course is certain type, use different colour on marker
                if (kentta.getString("Tyyppi").equals("Kulta")) {
                    mMap.addMarker(new MarkerOptions().position(loc)
                            .title(kentta.getString("Kentta"))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                            .snippet(stringbuilder.toString()));
                }

                else if (kentta.getString("Tyyppi").equals("Etu")) {
                    mMap.addMarker(new MarkerOptions().position(loc)
                            .title(kentta.getString("Kentta"))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .snippet(stringbuilder.toString()));
                }

                else {
                    mMap.addMarker(new MarkerOptions().position(loc)
                            .title(kentta.getString("Kentta"))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .snippet(stringbuilder.toString()));
                }
            }
        }
        catch (JSONException e) {

        }
    }

    class FetchDataTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... urls) {
            HttpURLConnection urlConnection = null;
            JSONObject json = null;
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                json = new JSONObject(stringBuilder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }
            return json;
        }

        protected void onPostExecute(JSONObject json) {
            try {
                // store courses
                kentat = json.getJSONArray("kentat");
                createMarkersFromJson(kentat);

            } catch (JSONException e) {
                Log.e("JSON", "Error getting data.");
            }
        }
    }
}