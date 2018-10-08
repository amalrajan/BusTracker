package com.suresh.smokie.bustracker;

import android.graphics.Color;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import javax.annotation.Nullable;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback  {

    GoogleMap mMap;
    Marker mMarker;
    String mDuration;
    LatLng mBusLocation;
    Polyline mPolyLine;
    String mBusNumber = "bus_11";
    List<Marker> mAddedBusStops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAddedBusStops = new ArrayList<>();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id == R.id.slider_bus_9) {
            mBusNumber = "bus_9";
            mPolyLine.remove();
            removeMarkers();

            onMapReady(mMap);
        }
        if(id == R.id.slider_bus_11) {
            mBusNumber = "bus_11";
            mPolyLine.remove();
            removeMarkers();
            onMapReady(mMap);
        }
        if(id == R.id.slider_bus_12) {
            mBusNumber = "bus_12";
            mPolyLine.remove();
            removeMarkers();
            onMapReady(mMap);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void setBusMarker(final GoogleMap googleMap, final String busNumber) {
        final String documentPath = "locations/" + busNumber;
        DocumentReference busRef = FirebaseFirestore.getInstance().document(documentPath);

        busRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot document, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("MainActivity.class", "Listen failed.", e);
                    return;
                }
                if (document != null && document.exists()) {
                    String name = document.getString("name");
                    double lat = document.getDouble("latitude");
                    double lng = document.getDouble("longitude");
                    mBusLocation = new LatLng(lat, lng);
                    if(mMarker == null) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(mBusLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_32)).title(name);
                        mMarker = googleMap.addMarker(markerOptions);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mBusLocation, 14.0f));
                    } else {
                        mMarker.setPosition(mBusLocation);
                    }

                    Log.d("MainActivity.class", "Current data: " + document.getData());
                } else {
                    Log.d("MainActivity.class", "Current data: null");
                }
            }
        });
    }

    private List<StopInfo> setStopsMarker(final GoogleMap googleMap, String busNumber) {
        CollectionReference busRef = FirebaseFirestore.getInstance().collection(busNumber);

        final List<StopInfo> stopInfos = new ArrayList<>();


        busRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            List<LatLng> waypoints = new ArrayList<>();
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        StopInfo stopInfo = document.toObject(StopInfo.class);
                        stopInfos.add(stopInfo);
                        LatLng location = stopInfo.getLatLngObject();
                        waypoints.add(location);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop_32)).title(stopInfo.getName());
                        Marker busStop = googleMap.addMarker(markerOptions);
                        mAddedBusStops.add(busStop);
                        Log.e("MainActivity.class", document.getId() + " => " + document.getData());
                    }
                    LatLng origin = mBusLocation;
                    LatLng dest = stopInfos.get(stopInfos.size()-1).getLatLngObject();

                    //Getting URL to the Google Directions API
                    String url = Utils.getDirectionsUrl(origin, dest, waypoints);

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                } else {
                    Log.e("MainActivity.class", "Error getting documents: ", task.getException());
                }
            }
        });
        return stopInfos;
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


    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = Utils.downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("MainActivity.class", "Duration: " + mDuration);
            Log.e("DisplayActivyt.class", result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String,String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String,String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String,String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String duration;

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String,String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap point = path.get(j);

                    double lat = Double.parseDouble((String)point.get("lat"));
                    double lng = Double.parseDouble((String)point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mPolyLine = mMap.addPolyline(lineOptions);
            }
        }
    }

    private void removeMarkers() {
        for(int i = 0; i<mAddedBusStops.size(); i++) {
            mAddedBusStops.get(i).remove();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setStopsMarker(googleMap, mBusNumber);
        setBusMarker(googleMap, mBusNumber);
    }
}
