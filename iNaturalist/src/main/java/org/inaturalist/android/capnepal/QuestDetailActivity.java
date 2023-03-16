package org.inaturalist.android.capnepal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.LocationRequest;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.MultiPoint;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.ColorUtils;
import com.mapbox.turf.TurfConversion;
import com.mapbox.turf.TurfTransformation;
import com.squareup.picasso.Picasso;

import org.inaturalist.android.BuildConfig;
import org.inaturalist.android.R;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class QuestDetailActivity extends AppCompatActivity implements LocationEngineListener {
    @Override
    public void onConnected() {
        this.mapView.getMapAsync(mapboxMap1 -> {
            if (locationLayer == null)
                locationLayer = new LocationLayerPlugin(mapView, mapboxMap1);
            this.mapboxMap = mapboxMap1;
            locationLayer.setRenderMode(RenderMode.COMPASS);
        });
//        LocationRequest locationRequest = new LocationRequest();
//        locationRequest.setInterval(3000);
//        locationRequest.setFastestInterval(3000);
//        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }

    @Override
    public void onLocationChanged(Location location) {
//        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14), 1500);
    }
    private LocationLayerPlugin locationLayer;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final String TAG = QuestDetailActivity.class.getSimpleName();
    private FillLayer polygonLayer;
    private GeoJsonSource geoJsonSource;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quest);
        Mapbox.getInstance(this,null);
        Intent intent = getIntent();
        Quest quest = (Quest) intent.getSerializableExtra("Quest");

        TextView tvLName = (TextView) findViewById(R.id.tvLName);
        TextView tvSName = (TextView) findViewById(R.id.tvSName);
        TextView quest_description = (TextView) findViewById(R.id.quest_description);
        TextView quest_submission = (TextView) findViewById(R.id.quest_submission);
        TextView questBottom = (TextView) findViewById(R.id.questBottom);
        ImageView ivSpecies = (ImageView) findViewById(R.id.ivSpecies);

        tvLName.setText(quest.getLocalName());
        tvSName.setText(quest.getScientificName());
        quest_description.setText(quest.getDescription());

        questBottom.setText(String.format("%s%s", getResources().getString(R.string.challange), quest.getChallange()));
        String dueString;
        if(quest.getDueInt() < 0) {
            dueString = getResources().getString(R.string.quest_expired);
        } else {
            dueString = String.format("%d %s", quest.getDueInt(), getResources().getString(R.string.days_remaining));
        }
        quest_submission.setText(dueString);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setStyleUrl(getString(R.string.base_url) + "styles/retro?key=" + getString(R.string.baato_access_token));
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap ->
        {
            //remove mapbox attribute
            mapboxMap.getUiSettings().setAttributionEnabled(false);
            mapboxMap.getUiSettings().setLogoEnabled(false);
            this.mapboxMap = mapboxMap;
            if(checkPermissions()) {
                if (locationLayer == null)
                    locationLayer = new LocationLayerPlugin(mapView, mapboxMap);
                locationLayer.setRenderMode(RenderMode.COMPASS);
            }
            Gson gson = new Gson();
            List<Point> pointList = new ArrayList<>();
            List<LatLng> latLngs = new ArrayList<>();
            AreaModel areaModel = gson.fromJson(quest.getArea(), AreaModel.class);
            int i = 0;
            for (List<Double> coordinates: areaModel.getFeatures().get(0).getGeometry().getCoordinates().get(0)
            ) {
                pointList.add(i, Point.fromLngLat(coordinates.get(0), coordinates.get(1)));
                latLngs.add(new LatLng(coordinates.get(1), coordinates.get(0)));
                i++;
            }

            LatLngBounds.Builder builder = new LatLngBounds.Builder().includes(latLngs);
            LatLngBounds bounds = builder.build();
            geoJsonSource = new GeoJsonSource("source-id");
            polygonLayer = new FillLayer("layer-id", "source-id");
            Feature multiPointFeature = Feature.fromGeometry(MultiPoint.fromLngLats(pointList));
            FeatureCollection collection = FeatureCollection.fromFeature(multiPointFeature);
            geoJsonSource.setGeoJson(collection);
            mapboxMap.addSource(geoJsonSource);
            polygonLayer.withProperties(
                    PropertyFactory.fillOpacity(0.2f),
                    PropertyFactory.fillColor("#000000"),
                    PropertyFactory.fillOutlineColor("#000000")
            );
//             PropertyFactory.fillColor(ColorUtils.colorToRgbaString(Color.parseColor("#000000"))),
            //                    PropertyFactory.fillOutlineColor(ColorUtils.colorToRgbaString(Color.parseColor("#bbbbbb")))
            mapboxMap.addLayer(polygonLayer);
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 300, 300, 300,300));
        });

        Picasso.with(this).load(quest.getImage()).resize(100,100).centerInside()
                .placeholder(R.drawable.ic_image_gray_24dp)
                .error(R.drawable.ic_error_black_24dp)
                .into(ivSpecies);
//       Log.wtf("area object:", String.valueOf(areaModel.getFeatures().get(0).getGeometry().getCoordinates().get(0).get(0)));

    }


    @Override
    public void onStart() {
        super.onStart();
        if (!checkPermissions()) {
            requestPermissions();
        }
//        else {
//            performPendingGeofenceTask();
//        }
        mapView.onStart();
        if (locationLayer != null && checkPermissions())
            locationLayer.onStart();
    }
    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        if (locationLayer != null)
            locationLayer.onStop();

    }
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (locationLayer != null)
            locationLayer.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (locationLayer != null)
            locationLayer.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        boolean vals = permissionState == PackageManager.PERMISSION_GRANTED;
        return vals;
    }
    private void showSnackbar(final String text) {
        View container = findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }
    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(QuestDetailActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(QuestDetailActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted.");
                onConnected();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }
}
