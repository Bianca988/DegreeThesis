package com.indooratlas.android.sdk.examples.wayfinding;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IAOrientationListener;
import com.indooratlas.android.sdk.IAOrientationRequest;
import com.indooratlas.android.sdk.IAPOI;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.IARoute;
import com.indooratlas.android.sdk.IAWayfindingListener;
import com.indooratlas.android.sdk.IAWayfindingRequest;
import com.indooratlas.android.sdk.examples.R;
import com.indooratlas.android.sdk.examples.SdkExample;
import com.indooratlas.android.sdk.resources.IAFloorPlan;
import com.indooratlas.android.sdk.resources.IALatLng;
import com.indooratlas.android.sdk.resources.IALocationListenerSupport;
import com.indooratlas.android.sdk.resources.IAVenue;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

@SdkExample(description = R.string.example_wayfinding_description)
public class WayfindingOverlayActivity extends FragmentActivity
        implements GoogleMap.OnMapClickListener, OnMapReadyCallback {

    private static final String TAG = "IndoorAtlasExample";

    /* used to decide when bitmap should be downscaled */
    private static final int MAX_DIMENSION = 2048;

    //kalman filter

    private static final double KALMAN_R = 0.125d;
    private static final double KALMAN_Q = 0.5d;


    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private Circle mCircle;
    private IARegion mOverlayFloorPlan = null;
    private GroundOverlay mGroundOverlay = null;
    private IALocationManager mIALocationManager;
    private Target mLoadTarget;
    private boolean mCameraPositionNeedsUpdating = true; // update on first location
    private Marker mDestinationMarker;
    private Marker mHeadingMarker;
    private IAVenue mVenue;
    private List<Marker> mPoIMarkers = new ArrayList<>();
    private List<Polyline> mPolylines = new ArrayList<>();
    private IARoute mCurrentRoute;

    private IAWayfindingRequest mWayfindingDestination;
    private IAWayfindingListener mWayfindingListener = new IAWayfindingListener() {
        @Override
        public void onWayfindingUpdate(IARoute route) {
            mCurrentRoute = route;
            if (hasArrivedToDestination(route)) {
                // stop wayfinding
                showInfo("You're there!");
                mCurrentRoute = null;
                mWayfindingDestination = null;
                mIALocationManager.removeWayfindingUpdates();
            }
            updateRouteVisualization();
        }
    };

    private IAOrientationListener mOrientationListener = new IAOrientationListener() {
        @Override
        public void onHeadingChanged(long timestamp, double heading) {
            updateHeading(heading);
        }

        @Override
        public void onOrientationChange(long timestamp, double[] quaternion) {
            // we do not need full device orientation in this example, just the heading
        }
    };

    private int mFloor;

    // circle
    private void showLocationCircle(LatLng center, double accuracyRadius) {

        if (mCircle == null) {
            // location can received before map is initialized, ignoring those updates
            if (mMap != null) {
                mCircle = mMap.addCircle(new CircleOptions()
                        .center(center)
                        .radius(accuracyRadius)
                        .fillColor(0x201681FB)
                        .strokeColor(0x500A78DD)
                        .zIndex(1.0f)
                        .visible(true)
                        .strokeWidth(5.0f));
                mHeadingMarker = mMap.addMarker(new MarkerOptions()
                        .position(center)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_blue_dot))
                        .anchor(0.5f, 0.5f)
                        .flat(true));
            }
        } else {
            // move existing markers position to received location
            mCircle.setCenter(center);
            mHeadingMarker.setPosition(center);
            mCircle.setRadius(accuracyRadius);
        }
    }

    private void updateHeading(double heading) {
        if (mHeadingMarker != null) {
            mHeadingMarker.setRotation((float) heading);
        }
    }

    private IALocationListener mListener = new IALocationListenerSupport() {

        public void onLocationChanged(IALocation location) {
            /*
            //Log.d(TAG, "NEW" + location.getLatitude() + " " + location.getLongitude());
            if (mMap == null) {
                return;
            }

            LatLng center2 = new LatLng(location.getLatitude(),location.getLongitude());
            final int newFloor = location.getFloorLevel();
            if (mFloor != newFloor) {
                updateRouteVisualization();
            }
            mFloor = newFloor;

            //showLocationCircle(center, location.getAccuracy());

            if (mCameraPositionNeedsUpdating) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center2, 15.5f));
                mCameraPositionNeedsUpdating = false;
            }
            */
        }

    };

    /**
     * Listener that changes overlay if needed
     */
    private IARegion.Listener mRegionListener = new IARegion.Listener() {
        @Override
        public void onEnterRegion(final IARegion region) {
            if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
                Log.d(TAG, "enter floor plan " + region.getId());
                mCameraPositionNeedsUpdating = true; // entering new fp, need to move camera
                if (mGroundOverlay != null) {
                    mGroundOverlay.remove();
                    mGroundOverlay = null;
                }
                mOverlayFloorPlan = region; // overlay will be this (unless error in loading)
                fetchFloorPlanBitmap(region.getFloorPlan());
                //setupPoIs(mVenue.getPOIs(), region.getFloorPlan().getFloorLevel());
            } else if (region.getType() == IARegion.TYPE_VENUE) {
                mVenue = region.getVenue();
            }
        }

        @Override
        public void onExitRegion(IARegion region) {
        }

    };
/*
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    };
*/


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Bundle args = intent.getExtras();
            LatLng center = args.getParcelable("b");
            Log.d("Location received in wayfinding!!", String.valueOf(center));

            if (mMap == null) {
                return;
            }

            updateRouteVisualization();

            showLocationCircle(center, 5.5f);

            if (mCameraPositionNeedsUpdating) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 15.5f));
                mCameraPositionNeedsUpdating = false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // prevent the screen going to sleep while app is on foreground
        findViewById(android.R.id.content).setKeepScreenOn(true);

        // instantiate IALocationManager
        mIALocationManager = IALocationManager.create(this);


        // Try to obtain the map from the SupportMapFragment.
        ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map))
                .getMapAsync(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("new-current-location"));
/*


        if(args == null){
            Intent myIntent = new Intent(this, RangingActivity.class);
            this.startActivity(myIntent);
           // Log.d(TAG,"LocationLocation!!"+center);
        } else{
            Intent myIntent = new Intent(this, RangingActivity.class);
            this.startActivity(myIntent);
            LatLng center = args.getParcelable("b");
            Log.d("Location!!", String.valueOf(center));
            //  showLocationCircle() to show the blue dot
            showLocationCircle(  center, 66);
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // remember to clean up after ourselves
        mIALocationManager.destroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // start receiving location updates & monitor region changes
        mIALocationManager.requestLocationUpdates(IALocationRequest.create(), mListener);
        mIALocationManager.registerRegionListener(mRegionListener);
        mIALocationManager.registerOrientationListener(
                // update if heading changes by 1 degrees or more
                new IAOrientationRequest(1, 0),
                mOrientationListener);

        if (mWayfindingDestination != null) {
            mIALocationManager.requestWayfindingUpdates(mWayfindingDestination, mWayfindingListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister location & region changes
        //mIALocationManager.removeLocationUpdates(mListener);
        mIALocationManager.unregisterRegionListener(mRegionListener);
        mIALocationManager.unregisterOrientationListener(mOrientationListener);

        if (mWayfindingDestination != null) {
            mIALocationManager.removeWayfindingUpdates();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // do not show Google's outdoor location
        mMap.setMyLocationEnabled(false);
        mMap.setOnMapClickListener(this);

        // disable various Google maps UI elements that do not work indoors
        mMap.getUiSettings().setMapToolbarEnabled(false);
        LatLng point = new LatLng(47.686174,22.479376);
        mMap.addMarker(new MarkerOptions()
                .position(point)
                .title("Living"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));


        // do not consume the event so that the popup with marker name is displayed

    }


    private void setupPoIs(List<IAPOI> pois, int currentFloorLevel) {

        Log.d(TAG, pois.size() + " PoI(s)");

        // remove any existing markers

        for (Marker m : mPoIMarkers) {

            m.remove();

        }

        mPoIMarkers.clear();

        for (IAPOI poi : pois) {

            if (poi.getFloor() == currentFloorLevel) {

                mPoIMarkers.add(mMap.addMarker(new MarkerOptions()

                        .title(poi.getName())

                        .position(new LatLng(poi.getLocation().latitude, poi.getLocation().longitude))

                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));

            }

        }

    }


    /**
     * Sets bitmap of floor plan as ground overlay on Google Maps
     */
    private void setupGroundOverlay(IAFloorPlan floorPlan, Bitmap bitmap) {

        if (mGroundOverlay != null) {
            mGroundOverlay.remove();
        }

        if (mMap != null) {
            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
            IALatLng iaLatLng = floorPlan.getCenter();
            LatLng center = new LatLng(iaLatLng.latitude, iaLatLng.longitude);
            GroundOverlayOptions fpOverlay = new GroundOverlayOptions()
                    .image(bitmapDescriptor)
                    .zIndex(0.0f)
                    .position(center, floorPlan.getWidthMeters(), floorPlan.getHeightMeters())
                    .bearing(floorPlan.getBearing());

            mGroundOverlay = mMap.addGroundOverlay(fpOverlay);
        }
    }

    /**
     * Download floor plan using Picasso library.
     */
    private void fetchFloorPlanBitmap(final IAFloorPlan floorPlan) {

        if (floorPlan == null) {
            Log.e(TAG, "null floor plan in fetchFloorPlanBitmap");
            return;
        }

        final String url = floorPlan.getUrl();
        Log.d(TAG, "loading floor plan bitmap from " + url);

        mLoadTarget = new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.d(TAG, "onBitmap loaded with dimensions: " + bitmap.getWidth() + "x"
                        + bitmap.getHeight());
                if (mOverlayFloorPlan != null && floorPlan.getId().equals(mOverlayFloorPlan.getId())) {
                    Log.d(TAG, "showing overlay");
                    setupGroundOverlay(floorPlan, bitmap);
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // N/A
            }

            @Override
            public void onBitmapFailed(Drawable placeHolderDrawable) {
                showInfo("Failed to load bitmap");
                mOverlayFloorPlan = null;
            }
        };

        RequestCreator request = Picasso.with(this).load(url);

        final int bitmapWidth = floorPlan.getBitmapWidth();
        final int bitmapHeight = floorPlan.getBitmapHeight();

        if (bitmapHeight > MAX_DIMENSION) {
            request.resize(0, MAX_DIMENSION);
        } else if (bitmapWidth > MAX_DIMENSION) {
            request.resize(MAX_DIMENSION, 0);
        }

        request.into(mLoadTarget);
    }

    private void showInfo(String text) {
        final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), text,
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.button_close, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    @Override
    public void onMapClick(LatLng point) {
        /*if (mPoIMarkers.isEmpty()) {
            // if PoIs exist, only allow wayfinding to PoI markers
            setWayfindingTarget(point, true);
        }*/
    }

    /* private void setWayfindingTarget(LatLng point, boolean addMarker) {
     point = new LatLng(22.479402682259106,47.686123965050896);
         if (mMap == null) {
             Log.w(TAG, "map not loaded");
             return;
         }
             mMap.addMarker( new MarkerOptions()
             .position(point)
             .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
     }*/
    private boolean hasArrivedToDestination(IARoute route) {
        return false;
    }

    private void updateRouteVisualization() {

    }



}
