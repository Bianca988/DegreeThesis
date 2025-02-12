package com.indooratlas.android.sdk.examples.wayfinding;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;

public class MonitoringActivity extends Activity {

    protected static final String TAG = "MonitoringActivity";

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;

    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        verifyBluetooth();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("This app needs background location access");
                            builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                            builder.setPositiveButton(android.R.string.ok, null);
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {


                                @TargetApi(23)

                                @Override

                                public void onDismiss(DialogInterface dialog) {
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                            PERMISSION_REQUEST_BACKGROUND_LOCATION);
                                }


                            });
                            builder.show();
                        } else {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Functionality limited");
                            builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
                            builder.setPositiveButton(android.R.string.ok, null);
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                }
                            });
                            builder.show();
                        }
                    }
                }
            } else {

                if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,

                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION},

                            PERMISSION_REQUEST_FINE_LOCATION);

                } else {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setTitle("Functionality limited");

                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");

                    builder.setPositiveButton(android.R.string.ok, null);

                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {


                        @Override

                        public void onDismiss(DialogInterface dialog) {

                        }


                    });

                    builder.show();

                }


            }

        }

    }


    @Override

    public void onRequestPermissionsResult(int requestCode,

                                           String permissions[], int[] grantResults) {

        switch (requestCode) {

            case PERMISSION_REQUEST_FINE_LOCATION: {

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(TAG, "fine location permission granted");

                } else {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setTitle("Functionality limited");

                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.");

                    builder.setPositiveButton(android.R.string.ok, null);

                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {


                        @Override

                        public void onDismiss(DialogInterface dialog) {

                        }


                    });

                    builder.show();

                }

                return;

            }

            case PERMISSION_REQUEST_BACKGROUND_LOCATION: {

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(TAG, "background location permission granted");

                } else {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setTitle("Functionality limited");

                    builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons when in the background.");

                    builder.setPositiveButton(android.R.string.ok, null);

                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {


                        @Override

                        public void onDismiss(DialogInterface dialog) {

                        }


                    });

                    builder.show();

                }

                return;

            }

        }

    }

    @Override

    public void onResume() {

        super.onResume();


    }


    @Override

    public void onPause() {

        super.onPause();


    }


    private void verifyBluetooth() {
        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //finish();
                        //System.exit(0);
                        }
                });
                builder.show();
            }
        } catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    //finish();
                    //System.exit(0);
                }
            });
            builder.show();
        }


    }


    public void updateLog(final String log) {

        Log.d(TAG, "work");
    }


}


















