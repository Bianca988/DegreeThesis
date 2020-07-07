package com.indooratlas.android.sdk.examples.wayfinding;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.EditText;
import android.content.Context;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.maps.model.LatLng;

import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.examples.R;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RunningAverageRssiFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;

public class RangingActivity extends Activity implements BeaconConsumer{

    protected static final String TAG = "RangingActivity";
    public LatLng center;

    private BeaconManager beaconManager;
    LocationFinder locationFinder = new LocationFinder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging);
        beaconManager = BeaconManager.getInstanceForApplication(this);

        beaconManager.bind(this);
    }



    @Override

    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override

    public void onBeaconServiceConnect() {
        RangeNotifier rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                int beacon_number = beacons.size();
              Beacon[] beacon_array = beacons.toArray(new Beacon[beacons.size()]);
                Beacon device1 = null, device2 = null, device3 = null;
                Constants constants = new Constants();
                double RSSI1Unfiltered = 0;
                float txPow1 = 0;
                double RSSI2Unfiltered = 0;
                float txPow2 = 0;
                double RSSI3Unfiltered = 0;
                float txPow3 = 0;
                if (beacon_number == 3) {
                 //Beacon[] beacon_array = beacons.toArray(new Beacon[beacons.size()]);
                    if (beacon_array[0].getIdentifier(0).toString().equals(Constants.DEVICE1_UUID))    {
                        device1 = beacon_array[0];
                        Log.d(TAG,"NOW I AM TAKING VALUE FOR DEVICE1 = BEACON ARRAY 0 ");
                    }  else  if (beacon_array[1].getIdentifier(0).toString().equals(Constants.DEVICE1_UUID)) {
                        device1 = beacon_array[1];
                        Log.d(TAG,"NOW I AM TAKING VALUE FOR DEVICE1 = BEACON ARRAY 1 ");
                    }   else {
                        device1 = beacon_array[2];
                        Log.d(TAG,"NOW I AM TAKING VALUE FOR DEVICE1 = BEACON ARRAY 2 ");
                    }
                     if (beacon_array[0].getIdentifier(0).toString().equals(Constants.DEVICE2_UUID)) {
                            device2 = beacon_array[0];
                            Log.d(TAG,"NOW I AM TAKING VALUE FOR DEVICE2 = BEACON ARRAY 0 ");
                    }  else  if (beacon_array[1].getIdentifier(0).toString().equals(Constants.DEVICE2_UUID)) {
                        device2 = beacon_array[1];
                        Log.d(TAG,"NOW I AM TAKING VALUE FOR DEVICE2= BEACON ARRAY 1 ");
                    }    else {
                        device2 = beacon_array[2];
                        Log.d(TAG,"NOW I AM TAKING VALUE FOR DEVICE2 = BEACON ARRAY 2 ");

                    }
                     if (beacon_array[0].getIdentifier(0).toString().equals(Constants.DEVICE3_UUID)) {
                         device3 = beacon_array[0];
                         Log.d(TAG,"NOW I AM TAKING VALUE FOR DEVICE3 = BEACON ARRAY 0");
                     }  else  if (beacon_array[1].getIdentifier(0).toString().equals(Constants.DEVICE3_UUID)){
                         device3 = beacon_array[1];
                         Log.d(TAG,"NOW I AM TAKING VALUE FOR DEVICE3 = BEACON ARRAY 1 ");
                     }   else {
                         device3 = beacon_array[2];
                         Log.d(TAG,"NOW I AM TAKING VALUE FOR DEVICE3 = BEACON ARRAY 2 ");
                     }
                        RSSI1Unfiltered = device1.getRssi();
                        RSSI2Unfiltered = device2.getRssi();
                        RSSI3Unfiltered = device3.getRssi();

                        txPow1 = device1.getTxPower();
                        txPow2 = device2.getTxPower();
                        txPow3 = device3.getTxPower();

                        double dis1 = locationFinder.calculateDistance(txPow1,RSSI1Unfiltered);
                        double dis2 = locationFinder.calculateDistance(txPow2,RSSI2Unfiltered);
                        double dis3 = locationFinder.calculateDistance(txPow3,RSSI3Unfiltered);

                        Log.d(TAG, "TxPower of " + beacon_array[0].getIdentifier(0) + "is " + txPow1 + " and RSSI is : " + RSSI1Unfiltered + " and is at " + dis1 + "   " + device1.getIdentifier(0));
                        Log.d(TAG, "TxPower of " + beacon_array[1].getIdentifier(0) + "is " + txPow2 + " and RSSI is : " + RSSI2Unfiltered + " and is at " + dis2 + " " + device2.getIdentifier(0));
                        Log.d(TAG, "TxPower of " + beacon_array[2].getIdentifier(0) + "is " + txPow3 + " and RSSI is : " + RSSI3Unfiltered + " and is at " + dis3 + " " + device3.getIdentifier(0));
                    RunningAverageRssiFilter.setSampleExpirationMilliseconds(3000l);
                 //pass location!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                 center = locationFinder.findLocation(RSSI1Unfiltered, txPow1, RSSI2Unfiltered, txPow2, RSSI3Unfiltered, txPow3);

                    sendCurrentLocation(center);
                 Log.d(TAG, "beacon in reagion" + beacons.size());
                 Log.d(TAG, "Current coordinates: asta e asta e !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + center.toString());


             }



                        }

                        private void sendCurrentLocation (LatLng center){
                            Log.d("sender", "Broadcasting message");
                            Intent intent = new Intent("new-current-location");
                            // You can also include some extra data.
                            intent.putExtra("b", center);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        }

                        private void logToDisplay ( final String s){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    EditText editText = RangingActivity.this.findViewById(R.id.textView3);
                                    editText.append(s + "\n");
                                }
                            });
                        }
                    } ;

                    try {

                        beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));

                        beaconManager.addRangeNotifier(rangeNotifier);

                    } catch (RemoteException e) {
                    }

                }

/*
       RangingActivity(BlockingQueue q)
       {
           queue = q;
       }


        public void run() {

            LatLng res;
            try
            {
                res = center;
                queue.put(res);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

        Object produce;
        }

*/

}
