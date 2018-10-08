package com.suresh.smokie.bustracker;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

public class DisplayActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    void setBusMarker(final GoogleMap googleMap, final String busNumber) {
        final String documentPath = "locations/" + busNumber;
        DocumentReference busRef = FirebaseFirestore.getInstance().document(documentPath);

        busRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot document, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("DisplayActivity.class", "Listen failed.", e);
                    return;
                }
                if (document != null && document.exists()) {
                    String name = document.getString("name");
                    double lat = document.getDouble("latitude");
                    double lng = document.getDouble("longitude");
                    LatLng location = new LatLng(lat, lng);
                    if(mMarker == null) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_32)).title(name);
                        mMarker = googleMap.addMarker(markerOptions);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14.0f));
                    } else {
                        mMarker.setPosition(location);
                    }

                    Log.d("DisplayActivity.class", "Current data: " + document.getData());
                } else {
                    Log.d("DisplayActivity.class", "Current data: null");
                }
            }
        });
    }

    private List<StopInfo> setStopsMarker(final GoogleMap googleMap, String busNumber) {
        CollectionReference busRef = FirebaseFirestore.getInstance().collection(busNumber);

        final List<StopInfo> stopInfos = new ArrayList<>();


        busRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        StopInfo stopInfo = document.toObject(StopInfo.class);
                        stopInfos.add(stopInfo);
                        LatLng location = stopInfo.getLatLngObject();
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop_32)).title(stopInfo.getName());
                        googleMap.addMarker(markerOptions);
                        Log.e("DisplayActivity.class", document.getId() + " => " + document.getData());
                    }
                    LatLng origin = stopInfos.get(0).getLatLngObject();
                    LatLng dest = stopInfos.get(1).getLatLngObject();

                    //Getting URL to the Google Directions API
                    String url = Utils.getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                } else {
                    Log.e("DisplayActivity.class", "Error getting documents: ", task.getException());
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
       mMap = googleMap;
       setStopsMarker(googleMap, "bus_12");
       setBusMarker(googleMap, "bus_12");
    }

    private void toBeNamed() {

    }

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

            Log.e("DisplayActivyt.class", result);
            //ParserTask parserTask = new ParserTask();
            //parserTask.execute(result);

        }
    }

    /*private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap>> routes = null;

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
        protected void onPostExecute(List<List<HashMap>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }

// Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }*/
}
