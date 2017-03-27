package com.example.hyyyyyde.teamt_beacon;


import android.app.Service;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
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
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public class AltBeaconService extends Service implements BeaconConsumer {

    // ログ出力時のタグ
    private static final String TAG = "AltBeaconService";

    // iBeacon領域監視マネージャ
    private BeaconManager beaconManager;

    // iBeaconトランスミッタ
    private BeaconTransmitter beaconTransmitter;

    // iBeacon送信用データ
    private BeaconValue sendBeaconValue;

    // iBeacon領域監視データ
    private BeaconValue regionBeaconValue;

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
    private CopyOnWriteArrayList<BeaconEventObserver> observers = new CopyOnWriteArrayList<>();

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

    public void startBeacon(BeaconValue send, BeaconValue region) {
        sendBeaconValue = send;
        regionBeaconValue = region;

        transmitter();
        receiver();
    }

    public void stopBeacon() {
        Log.d(TAG, "stopBeacon");
        if (beaconManager.isBound(this)) {
            beaconManager.unbind(this);
        }
        if (beaconTransmitter.isStarted()) {
            beaconTransmitter.stopAdvertising();
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
                if (collection.size() > 0) {
                    for (Beacon beacon : collection) {
                        BeaconValue beaconValue = new BeaconValue();
                        beaconValue.setUuid(beacon.getId1().toString());
                        beaconValue.setMajor(beacon.getId2().toString());
                        beaconValue.setMinor(beacon.getId3().toString());
                        beaconValue.setDistance(beacon.getDistance());
                        beaconValue.setRssi(beacon.getRssi());
                        beaconValue.setTxPower(beacon.getTxPower());
                        beaconValue.setConnected(true);

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

        CopyOnWriteArrayList<BeaconEventObserver> observers = getObservers();
        for (BeaconEventObserver observer : observers) {
            deleteObserver(observer);
        }

        if (beaconManager.isBound(this)) {
            beaconManager.removeAllMonitorNotifiers();
            beaconManager.removeAllRangeNotifiers();
            beaconManager.unbind(this);
        }
        if (beaconTransmitter.isStarted()) {
            beaconTransmitter.stopAdvertising();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binderBinder;
    }

    @Nullable
    private Identifier generateBeaconMajor(String beaconMajor) {
        if (beaconMajor != null) {
            return Identifier.parse(String.valueOf(beaconMajor));
        }
        return null;
    }

    @Nullable
    private Identifier generateBeaconMinor(String beaconMinor) {
        if (beaconMinor != null) {
            return Identifier.parse(String.valueOf(beaconMinor));
        }
        return null;
    }

    @Nullable
    private Identifier generateBeaconUuid(String beaconUuid) {
        if (beaconUuid != null) {
            return Identifier.parse(String.valueOf(beaconUuid));
        }
        return null;
    }

    /**
     * 監視対象領域を生成する
     *
     * @return Region
     */
    private Region generateRegion() {
        return new Region("teamT-beacon",
                generateBeaconUuid(regionBeaconValue.getUuid()),
                generateBeaconMajor(regionBeaconValue.getMajor()),
                generateBeaconMinor(regionBeaconValue.getMinor()));
    }

    private void notifyObserver(BeaconValue beaconValue) {
        CopyOnWriteArrayList<BeaconEventObserver> observers = getObservers();
        for (BeaconEventObserver observer : observers) {
            observer.onNotify(beaconValue);
        }
    }

    private void transmitter() {
        Beacon beacon = new Beacon.Builder()
                .setId1(sendBeaconValue.getUuid())
                .setId2(sendBeaconValue.getMajor())
                .setId3(sendBeaconValue.getMinor())
                .build();
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout(IBEACON_FORMAT);
        beaconTransmitter = new BeaconTransmitter(this, beaconParser);

        beaconTransmitter.startAdvertising(beacon);
    }

    private void receiver() {
        beaconManager.removeAllMonitorNotifiers();
        beaconManager.removeAllRangeNotifiers();
//        beaconManager.setForegroundBetweenScanPeriod(5000);
//        beaconManager.setForegroundScanPeriod(3000);

        // BeaconのフォーマットはiBeacon限定
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));

        beaconManager.bind(this);
    }
}
