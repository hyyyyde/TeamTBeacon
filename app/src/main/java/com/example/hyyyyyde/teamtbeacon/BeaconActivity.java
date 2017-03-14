package com.example.hyyyyyde.teamtbeacon;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.hyyyyyde.teamt_beacon.AltBeaconService;
import com.example.hyyyyyde.teamt_beacon.BeaconEventObserver;
import com.example.hyyyyyde.teamt_beacon.BeaconValue;

public class BeaconActivity extends AppCompatActivity {

    private static final String TAG = "BeaconActivity";

    private String uuid = "123456789";

    private Integer major = null;
    private Integer minor = null;

    private AltBeaconService mBeacon;

    private boolean serviceConnected = false;

    private BeaconEventObserver mObserver = new BeaconEventObserver() {
        @Override
        public void onNotify(BeaconValue beaconValue) {
            Log.d(TAG, "UUID:" + beaconValue.getUuid()
                    + " major:" + beaconValue.getMajor()
                    + " minor:" + beaconValue.getMinor()
                    + " distance:" + beaconValue.getDistance()
                    + " rssi:" + beaconValue.getRssi());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);

        // Start Beacon
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (serviceConnected) {
                    mBeacon.startBeacon(uuid, major, minor);
                }
            }
        });

        // Stop Beacon
        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (serviceConnected) {
                    mBeacon.stopBeacon();
                }
            }
        });
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "ServiceConnected");
            AltBeaconService.AltBeaconBinder binder = (AltBeaconService.AltBeaconBinder) iBinder;
            mBeacon = binder.getService();
            serviceConnected = true;
            mBeacon.addObserver(mObserver);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "ServiceDisconnected");
            serviceConnected = false;
        }
    };

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();

        Intent intent = new Intent(this, AltBeaconService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();

        if (serviceConnected) {
            mBeacon.stopBeacon();
            unbindService(serviceConnection);
            serviceConnected = false;
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

        if (serviceConnected) {
            mBeacon.stopBeacon();
            unbindService(serviceConnection);
            serviceConnected = false;
        }
    }
}
