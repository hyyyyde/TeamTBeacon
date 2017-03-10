package com.example.hyyyyyde.teamt_beacon;


import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.util.Log;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class AltBeaconMapper implements BootstrapNotifier {

    private BeaconManager beaconManager;

    // iBeaconのデータを認識するためのParserフォーマット
    public static final String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    /**
     * オブザーバーリスト
     */
    private CopyOnWriteArrayList<BeaconEventObserver> observers = new CopyOnWriteArrayList<BeaconEventObserver>();

    /**
     * オブザーバーの取得
     *
     * @return オブザーバー
     */
    private CopyOnWriteArrayList<BeaconEventObserver> getObservers() {
        return observers;
    }

    /**
     * Observerを追加
     *
     * @param observer observer
     */
    public void addObserver(BeaconEventObserver observer) {
        observers.add(observer);
    }

    /**
     * Observerを削除
     *
     * @param observer observer
     */
    public void deleteObserver(BeaconEventObserver observer) {
        observers.remove(observer);
    }

    private void notifyObserver(BeaconValue beaconValue) {
        CopyOnWriteArrayList<BeaconEventObserver> observers = getObservers();
        for (BeaconEventObserver observer : observers) {
            observer.onNotify(beaconValue);
        }
    }

    private Handler testHandler = new Handler();

    public void testFunction(final int maxNumber) {
        testHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Random random = new Random();

                BeaconValue beaconValue = new BeaconValue();
                beaconValue.setUuid(String.valueOf(random.nextInt(maxNumber) + 1));

                notifyObserver(beaconValue);

                testHandler.postDelayed(this, 3000);
            }
        }, 3000);
    }

    public void startBeacon(Context context) {
        beaconManager = BeaconManager.getInstanceForApplication(context);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));

    }

    @Override
    public void didEnterRegion(Region region) {
        Log.d("AlbBeaconMapper", "Enter Region");
    }

    @Override
    public void didExitRegion(Region region) {
        Log.d("AlbBeaconMapper", "Exit Region");
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        Log.d("AlbBeaconMapper", "Determine Sate: " + state);
    }

    @Override
    public Context getApplicationContext() {
        return null;
    }
}
