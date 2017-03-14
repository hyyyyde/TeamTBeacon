package com.example.hyyyyyde.teamt_beacon;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public class AltBeaconService extends Service implements BeaconConsumer {

    // ログ出力時のタグ
    private static final String TAG = "AltBeaconService";

    private BeaconManager beaconManager;

    private String beaconUuid;
    private Integer beaconMajor;
    private Integer beaconMinor;

    private final IBinder binderBinder = new AltBeaconBinder();

    public class AltBeaconBinder extends Binder {
        public AltBeaconService getService() {
            return AltBeaconService.this;
        }
    }

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

    public void startBeacon(String uuid, Integer major, Integer minor) {
        beaconUuid = uuid;
        beaconMajor = major;
        beaconMinor = minor;
        beaconManager.removeAllMonitorNotifiers();
        beaconManager.removeAllRangeNotifiers();
        beaconManager.setForegroundBetweenScanPeriod(5000);
        beaconManager.setForegroundScanPeriod(3000);

        // BeaconのフォーマットはiBeacon限定
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));

        beaconManager.bind(this);
    }

    public void stopBeacon() {
        Log.d(TAG, "stopBeacon");
        if (beaconManager.isBound(this)) {
            beaconManager.unbind(this);
        }
    }

    @Override
    public void onBeaconServiceConnect() {

        // iBeacon領域監視
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.d(TAG, "Enter Region");
                try {
                    // Ranging開始
                    beaconManager.startRangingBeaconsInRegion(generateRegion());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
                Log.d(TAG, "Exit Region");
                try {
                    // Ranging終了
                    beaconManager.stopRangingBeaconsInRegion(generateRegion());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.d(TAG, "Determine State " + state);
                if (state == 0) {
                    didExitRegion(region);
                } else if (state == 1) {
                    didEnterRegion(region);
                }
            }
        });

        try {
            // iBeacon領域監視開始
            beaconManager.startMonitoringBeaconsInRegion(generateRegion());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // iBeacon Ranging監視
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                Log.d(TAG, "RangeBeaconsInRegion");
                if (collection.size() > 0) {
                    for (Beacon beacon : collection) {
                        BeaconValue beaconValue = new BeaconValue();
                        beaconValue.setUuid(beacon.getId1().toString());
                        beaconValue.setMajor(beacon.getId2().toString());
                        beaconValue.setMinor(beacon.getId3().toString());
                        beaconValue.setDistance(beacon.getDistance());
                        beaconValue.setRssi(beacon.getRssi());
                        notifyObserver(beaconValue);
                    }
                }
            }
        });
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        beaconManager = BeaconManager.getInstanceForApplication(this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (beaconManager.isBound(this)) {
            beaconManager.removeAllMonitorNotifiers();
            beaconManager.removeAllRangeNotifiers();
            beaconManager.unbind(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binderBinder;
    }

    @Nullable
    private Identifier generateBeaconMajor() {
        if (beaconMajor != null) {
            return Identifier.parse(String.valueOf(beaconMajor));
        }
        return null;
    }

    @Nullable
    private Identifier generateBeaconMinor() {
        if (beaconMinor != null) {
            return Identifier.parse(String.valueOf(beaconMinor));
        }
        return null;
    }

    /**
     * 監視対象領域を生成する
     *
     * @return Region
     */
    private Region generateRegion() {
        return new Region("teamT-beacon", null, generateBeaconMajor(), generateBeaconMinor());
    }
}