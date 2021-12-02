/**
 * This is the bluetooth class for the rubiks cube to connect and manage the messages from it (when an axis is turned)
 *
 * @author James
 * @version 1.0
 **/

package uk.co.rhul.r14.letamagotchijos;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RubiksCubeBLEInterface {
    private static final String TAG = "Rubiks Cube Interface";
    private static final UUID CUBE_SERVICE_UUID = UUID.fromString("0000aadb-0000-1000-8000-00805f9b34fb");
    private static final UUID CUBE_CHARACTERISTIC_STATE = UUID.fromString("0000aadc-0000-1000-8000-00805f9b34fb");
    private final BluetoothAdapter btAdapter;
    private BluetoothLeScanner BLEScanner;
    private BluetoothGatt btGatt;
    private BluetoothGattService cubeService;
    private BluetoothGattCharacteristic cubeCharacteristicState;
    private byte[] cubeState = new byte[]{};
    private final RubiksCubeDecoder cubeDecoder;
    private final Context context;
    //final SparseArray<byte[]> manufacturerData = new SparseArray<>();
    private final BluetoothGattCallback btGattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            cubeCharacteristicState = characteristic;
            //Log.i(TAG, "Characteristic Read!");
            //updateCubeState();
            cubeDecoder.newCubeState();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            cubeCharacteristicState = characteristic;
            //Log.i(TAG, "Characteristic Changed!");
            //updateCubeState();
            cubeDecoder.newCubeState();
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt,
                                            int status,
                                            int newState) {
            if (BluetoothProfile.STATE_CONNECTED == newState) {
                try {
                    TimeUnit.MILLISECONDS.sleep(600);
                    btGatt.discoverServices();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG, "State: " + newState);

        }

        public void onServicesDiscovered(BluetoothGatt gatt,
                                         int status) {

            //setGattAttributes(gatt);
            for (BluetoothGattService s : gatt.getServices()) {
                //Log.i(TAG, "Service UUID: "+s.getUuid());
                if (s.getUuid().equals(CUBE_SERVICE_UUID)) {
                    setGattAttributes();
                    break;
                }
            }
        }

        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor,
                                     int status) {
            Log.i(TAG, "Received descriptor: " + descriptor.getUuid());

        }
    };
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            String manufacturerData = bytesToHex(result.getScanRecord().getManufacturerSpecificData().get(911));
            if (manufacturerData != null) {// TODO: the manufacturer data changes?!?!? wtf, I'm not a fan, we need to find something static that we can identify the cube with, it's a bit janky rn
                //51030202640003
                //if(manufacturerData.equals("51030202500003")){ // using manufacturer data instead of the mac address allows all xaiomi cubes to be used instead of just mine
                Log.i(TAG, "Found Cube: " + result.getDevice().getAddress() + " rssi: " +
                        result.getRssi() +
                        "Manufacturer Data Hex: " + bytesToHex(result.getScanRecord().getManufacturerSpecificData().get(911))); // mManufacturerSpecificData={911=[81, 3, 2, 2, 80, 0, 3]}
                connectDevice(context, result.getDevice());
                BLEScanner.stopScan(this);
                //} else {
                //   Log.i(TAG, "Found: "+result.getDevice().getAddress());
                //}
            }
            /*if(result.getDevice().getAddress().equals("D3:99:D4:9C:54:C3")){ // filters out all other devices, temporary change
                Log.i(TAG, "Found Cube: " + result.getDevice().getAddress() + " rssi: " + result.getRssi() + "Manufacturer Data Hex: "+bytesToHex(result.getScanRecord().getManufacturerSpecificData().get(911))); // mManufacturerSpecificData={911=[81, 3, 2, 2, 80, 0, 3]}
                connectDevice(result.getDevice());
                BLEScanner.stopScan(this);

                //mBluetoothGatt.readCharacteristic(characteristic);
            }*/
        }
    };

    public RubiksCubeBLEInterface(BluetoothAdapter btAdapterIn, RubiksCubeDecoder rCubeDecoder, Context context) {
        btAdapter = btAdapterIn;
        cubeDecoder = rCubeDecoder;
        this.context = context;
        //manufacturerData.append(911, {81, 3, 2, 2, 80, 0, 3});
        //BluetoothAdapter btAdapter = getSystemService(BluetoothAdapter.BLUETOOTH_SERVICE).adapter;
        //(new Thread(() -> { // make a new thread to listen for the BLE cube
        //})).start();
    }

    private static String bytesToHex(byte[] bytes) {
        if (bytes == null) return null;
        char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    } // from stackoverflow

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    } // from stackoverflow

    public void startBluetoothListener() {
        //btAdapter.startLeScan(SERVICE_UUID, leScanCallback);
        //bt = new BluetoothLeService();
        BLEScanner = btAdapter.getBluetoothLeScanner();
        BLEScanner.startScan(leScanCallback);
        Log.i(TAG, "Started Scanning!");
    }

    private void setGattAttributes() {
        cubeService = btGatt.getService(CUBE_SERVICE_UUID);
        if (cubeService != null) {
            cubeCharacteristicState = cubeService.getCharacteristic(CUBE_CHARACTERISTIC_STATE);
            if (cubeCharacteristicState == null) {
                Log.w(TAG, "Characteristic is null!");
            } else {
                if (btGatt.setCharacteristicNotification(cubeCharacteristicState, true)) {
                    Log.i(TAG, "Characteristic Notifications Set!");
                    for (BluetoothGattDescriptor b : cubeCharacteristicState.getDescriptors()) {
                        Log.i(TAG, "Descriptor UUID: " + b.getUuid());
                    }
                    BluetoothGattDescriptor descriptor = cubeCharacteristicState.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    btGatt.writeDescriptor(descriptor);
                    btGatt.readCharacteristic(cubeCharacteristicState);
                } else {
                    Log.w(TAG, "Error, notifications not set");
                }


            }
        } else {
            Log.w(TAG, "Cube Service Is Null!");
        }
    }

    public String getCubeStateString() {
        updateCubeState();
        if (cubeState != null) {
            String out = bytesToHex(cubeState);
            Log.i(TAG, "Cube State Hex Val:" + out);
            return out;
        } else {
            Log.w(TAG, "Error, characteristic value is null!");
            return null;
        }
    }

    private void updateCubeState() {
        cubeState = cubeCharacteristicState.getValue();
    }

    private void connectDevice(Context context, BluetoothDevice d) {
        btGatt = d.connectGatt(context, true, btGattCallback); // what do I set as the context?! aaa
    }

    public byte[] getCubeState() {
        updateCubeState();
        return cubeState;
    }
}
