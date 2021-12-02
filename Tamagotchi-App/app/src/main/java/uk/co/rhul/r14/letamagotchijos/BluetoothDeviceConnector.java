package uk.co.rhul.r14.letamagotchijos;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

interface OnDeviceSelected {
    void onDeviceSelected(BluetoothDevice device);
}

/**
 * <b>NOT THREAD SAFE BECAUSE ANDROID STUDIO DOES NOT LET ME PASS OBJECT PARAMETERS IN BUNDLES SO I DO
 * NOT CARE AT THIS POINT AND HAVE IGNORED THREAD SAFETY. ONLY ONE INSTANCE SHOULD RUN AT ONCE SO IT
 * IS FINE.</b>
 */
public class BluetoothDeviceConnector extends AppCompatActivity {

    public static OnDeviceSelected onDeviceSelected = null;
    public static boolean hasCancel = false;

    // Create a BroadcastReceiver for ACTION_FOUND.
    private BluetoothDeviceRecycleViewer adapter;

    /**
     * Call this to set the activitie's state up
     *
     * @param onDeviceSelected
     * @param hasCancel
     */
    public static void setVars(OnDeviceSelected onDeviceSelected, boolean hasCancel) {
        BluetoothDeviceConnector.onDeviceSelected = onDeviceSelected;
        BluetoothDeviceConnector.hasCancel = hasCancel;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (super.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        createUI(null);
                    }
                } else {
                    //Permission loop as the user is naughty and must accept the permissions
                    Toast.makeText(this, "Permission denied, please accept as it is required.", Toast.LENGTH_SHORT).show();
                    checkPermsAndroidQPlus();
                }
                break;
            }
        }
    }

    private boolean checkPermsAndroidQPlus() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            super.shouldShowRequestPermissionRationale("Location is needed so that this app can " +
                    "search for the EV3 and connect to it.");
            super.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_device_connector);

        //Check permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkPermsAndroidQPlus()) {
                createUI(savedInstanceState);
            }
        } else {
            createUI(savedInstanceState);
        }
    }

    private void createUI(Bundle savedInstanceState) {
        //Check bt
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            super.onBackPressed();
        }

        if (savedInstanceState == null) {
            RecyclerView viewer = findViewById(R.id.device_list);
            List<BluetoothDevice> devices = new LinkedList<>();

            for (BluetoothDevice d : bluetoothAdapter.getBondedDevices()) {
                Log.i(BluetoothDeviceConnector.class.toString(), String.format("Device: %s found and added to list", d.toString()));
                devices.add(d);
            }
            adapter = new BluetoothDeviceRecycleViewer(devices);
            viewer.setAdapter(adapter);

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
            mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            viewer.setLayoutManager(mLayoutManager);

            Button cancel = findViewById(R.id.cancel_button);
            cancel.setEnabled(BluetoothDeviceConnector.hasCancel);
            cancel.setOnClickListener(click -> {
                if (cancel.isEnabled()) {
                    super.onBackPressed();
                }
            });

            Button confirm = findViewById(R.id.confirm_button);
            confirm.setOnClickListener(click -> {
                if (adapter.getClicked() != null) {
                    BluetoothDeviceConnector.onDeviceSelected.onDeviceSelected(adapter.getClicked());
                    super.onBackPressed();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (hasCancel) super.onBackPressed();
    }
}