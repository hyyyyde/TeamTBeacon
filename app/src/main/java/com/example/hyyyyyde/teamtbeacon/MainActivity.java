package com.example.hyyyyyde.teamtbeacon;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.hyyyyyde.teamt_beacon.AltBeaconMapper;
import com.example.hyyyyyde.teamt_beacon.BeaconEventObserver;
import com.example.hyyyyyde.teamt_beacon.BeaconValue;

public class MainActivity extends AppCompatActivity {

    private AltBeaconMapper mBeacon = new AltBeaconMapper();

    private BeaconEventObserver mObserver = new BeaconEventObserver() {
        @Override
        public void onNotify(BeaconValue beaconValue) {
            Log.d("MainActivity", beaconValue.getUuid());
        }
    };

    private BeaconEventObserver mBeaconObserver = new BeaconEventObserver() {
        @Override
        public void onNotify(BeaconValue beaconValue) {
            Log.d("MainActivity", beaconValue.getUuid());
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d("BeaconServiceConnection", "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("BeaconServiceConnection", "onServiceConnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBeacon.addObserver(mObserver);
        mBeacon.addObserver(mBeaconObserver);

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MainActivity", "TestFunction onClick");
                mBeacon.testFunction(10);
            }
        });
    }
}
