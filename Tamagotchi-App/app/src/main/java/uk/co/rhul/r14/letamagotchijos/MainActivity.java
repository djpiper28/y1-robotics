package uk.co.rhul.r14.letamagotchijos;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import common.EmotionsInterface;
import common.netty.messages.MessagesIOHandler;
import common.netty.messages.SignalStrengthMessage;
import common.netty.messages.UserNotification;

/**
 * @author Danny
 * @version 1.1
 */
public class MainActivity extends AppCompatActivity {

    // Name for the SDP record when creating server socket
    private static final String NAME_INSECURE = "HC-05";

    // HC-05 UUID  "00001101-0000-1000-8000-00805F9B34FB"
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final String HAPPINESS_TAG = "happiness_tag",
            FEAR_TAG = "fear_tag",
            TIRED_TAG = "tired_tag",
            BORED_TAG = "bored_tag",
            HUNGER_TAG = "hunger_tag";
    private static final MessagesIOHandler[] btConn = new MessagesIOHandler[1];
    private static ServerSocket serverSocket;
    private static Socket socket;
    private static BluetoothAdapter localAdapter;
    private static BluetoothDevice ev3;
    private static InputStream ev3In;
    private static OutputStream ev3Out;
    private static Settings settings;

    public void stateDisconnected() {
        runOnUiThread(() -> {
            ConstraintLayout cont = findViewById(R.id.bluetooth_status_container);
            cont.setBackgroundResource(R.drawable.bluetoothstatus_disconnected);

            TextView textView = findViewById(R.id.bluetooth_status);
            textView.setText(getResources().getString(R.string.bluetooth_status_disconnected));
        });
    }

    public void stateConnected() {
        runOnUiThread(() -> {
            ConstraintLayout cont = findViewById(R.id.bluetooth_status_container);
            cont.setBackgroundResource(R.drawable.bluetooth_status_connected);

            TextView textView = findViewById(R.id.bluetooth_status);
            textView.setText(getResources().getString(R.string.bluetooth_status_connected));
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((TextView) findViewById(R.id.ip_addr)).setText("IP Address: " + getIPAddress(true));

        /*
         * Create the emotion views for happiness, courage, curiosity
         */
        if (savedInstanceState == null) {

            stateDisconnected();
            Bundle bundle = EmotionDisplayFragment.getBundle(0, "Happiness", "placeholder");

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setReorderingAllowed(true)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .add(R.id.emotion_bar_container, EmotionDisplayFragment.class, bundle, HAPPINESS_TAG);

            bundle = EmotionDisplayFragment.getBundle(0, "Fear", "placeholder");
            fragmentTransaction.add(R.id.emotion_bar_container, EmotionDisplayFragment.class, bundle, FEAR_TAG);

            bundle = EmotionDisplayFragment.getBundle(0, "Tiredness", "placeholder");
            fragmentTransaction.add(R.id.emotion_bar_container, EmotionDisplayFragment.class, bundle, TIRED_TAG);

            bundle = EmotionDisplayFragment.getBundle(0, "Boredom", "placeholder");
            fragmentTransaction.add(R.id.emotion_bar_container, EmotionDisplayFragment.class, bundle, BORED_TAG);

            bundle = EmotionDisplayFragment.getBundle(0, "Hunger", "placeholder");
            fragmentTransaction.add(R.id.emotion_bar_container, EmotionDisplayFragment.class, bundle, HUNGER_TAG)
                    .commit();

            bluetoothInitialise();

            settings = new Settings(this.getBaseContext());

            Runnable changeSettings = () -> {
                NewRobotScreen.setVars(str -> {
                    settings.setTamagotchiName(str);
                    Intent intent = new Intent(this, BluetoothDeviceConnector.class);
                    startActivity(intent);
                }, false);

                BluetoothDeviceConnector.setVars(device -> {
                    ((TextView) findViewById(R.id.tamagotchi_name)).setText(settings.getTamagotchiName());

                    ev3 = device;
                    settings.setEv3MAC(ev3.getAddress());

                    try {
                        settings.saveSettings(this.getBaseContext());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //Start bluetooth listener in another thread
                    (new Thread(() -> this.startBluetoothListener(fragmentManager.getFragments()))).start();
                }, false);

                Intent intent = new Intent(this, NewRobotScreen.class);
                startActivity(intent);
            };

            Button settingsButton = findViewById(R.id.settings_button);
            settingsButton.setOnClickListener(click -> changeSettings.run());

            ConstraintLayout cont = findViewById(R.id.bluetooth_status_container);
            cont.setOnClickListener(click -> {
                BluetoothDeviceConnector.setVars(device -> {
                    ev3 = device;
                    settings.setEv3MAC(ev3.getAddress());

                    try {
                        settings.saveSettings(this.getBaseContext());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //Start bluetooth listener in another thread
                    (new Thread(() -> {
                        this.startBluetoothListener(fragmentManager.getFragments());
                    })).start();
                }, true);

                Intent intent = new Intent(this, BluetoothDeviceConnector.class);
                startActivity(intent);
            });

            if (settings.isNewBot()) {
                changeSettings.run();
            } else {
                ((TextView) findViewById(R.id.tamagotchi_name)).setText(settings.getTamagotchiName());

                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter == null) {
                    // Device doesn't support Bluetooth
                    super.onBackPressed();
                }

                for (BluetoothDevice d : bluetoothAdapter.getBondedDevices())
                    if (d.getAddress().equals(settings.getEv3MAC()))
                        ev3 = d;

                //Start bluetooth listener in another thread
                (new Thread(() -> {
                    this.startBluetoothListener(fragmentManager.getFragments());
                })).start();
            }

            // Start rubiks cube interface
            (new Thread(() -> {
                RubiksCubeDecoder cube = new RubiksCubeDecoder(localAdapter, this.getBaseContext(), btConn);
            })).start();
        }
    }

    /**
     * This connects the fragments to the corresponding field in the emotions
     *
     * @param fragmentList list of fragments
     * @since 1.0
     */
    private EmotionsInterface connectFragmentsToInterface(List<Fragment> fragmentList) {

        //Connect fragments to emotions interface which is actually an abstract class
        EmotionDisplayFragment[] happinessFragment = new EmotionDisplayFragment[1];
        EmotionDisplayFragment[] fearFragment = new EmotionDisplayFragment[1];
        EmotionDisplayFragment[] tiredFragment = new EmotionDisplayFragment[1];
        EmotionDisplayFragment[] hungerFragment = new EmotionDisplayFragment[1];
        EmotionDisplayFragment[] boredomFragment = new EmotionDisplayFragment[1];

        for (Fragment fragment : fragmentList) {
            switch (((EmotionDisplayFragment) fragment).getTag()) {
                case HAPPINESS_TAG:
                    happinessFragment[0] = (EmotionDisplayFragment) fragment;
                    break;
                case FEAR_TAG:
                    fearFragment[0] = (EmotionDisplayFragment) fragment;
                    break;
                case HUNGER_TAG:
                    hungerFragment[0] = (EmotionDisplayFragment) fragment;
                    break;
                case BORED_TAG:
                    boredomFragment[0] = (EmotionDisplayFragment) fragment;
                    break;
                case TIRED_TAG:
                    tiredFragment[0] = (EmotionDisplayFragment) fragment;
                    break;
                default:
                    Log.d(this.getClass().toString(),
                            String.format("Error invalid tag %s in processed fragment.",
                                    fragment.getTag()));
                    break;
            }
        }

        EmotionsInterface emotionsInterface = new EmotionsInterface() {
            @Override
            public void onBordemSet(float newBordem) {
                runOnUiThread(() -> {
                    boredomFragment[0].setEmotionState(newBordem);
                    String state = newBordem < 0.5f ? "Bored" : "Not Bored";

                    boredomFragment[0].setEmotionStatus(state);
                });
            }

            @Override
            public void onCourageSet(float newEmotion) {
            }

            @Override
            public void onCuriositySet(float newEmotion) {
            }

            @Override
            public void onFearSet(float newFear) {
                runOnUiThread(() -> {
                    fearFragment[0].setEmotionState(newFear);
                    String state = newFear < 0.5f ? "Scared" : "Not Scared";

                    fearFragment[0].setEmotionStatus(state);
                    Log.i(MainActivity.class.toString(), "Fear set");
                });
            }

            @Override
            public void onHappinessSet(float newEmotion) {
                runOnUiThread(() -> {
                    happinessFragment[0].setEmotionState(newEmotion);
                    String state = newEmotion < 0.5f ? "Sad" : "Happy";

                    happinessFragment[0].setEmotionStatus(state);
                    Log.i(MainActivity.class.toString(), "Happiness set");
                });
            }

            @Override
            public void onHungerSet(float newHunger) {
                runOnUiThread(() -> {
                    hungerFragment[0].setEmotionState(newHunger);
                    String state = newHunger < 0.5f ? "Hungry" : "Full";

                    hungerFragment[0].setEmotionStatus(state);
                    Log.i(MainActivity.class.toString(), "Hunger set");
                });
            }

            @Override
            public void onTiredSet(float newTired) {
                runOnUiThread(() -> {
                    tiredFragment[0].setEmotionState(newTired);
                    String state = newTired < 0.5f ? "Tired" : "Not Tired";

                    tiredFragment[0].setEmotionStatus(state);
                    Log.i(MainActivity.class.toString(), "Tired set");
                });
            }
        };

        return emotionsInterface;
    }

    /**
     * Starts the bluetooth listener and allows the messages to update the fields.
     *
     * @param fragmentList
     * @since 1.0
     */
    private void startBluetoothListener(List<Fragment> fragmentList) {
        //Get emotions interface
        EmotionsInterface emotionsInterface = connectFragmentsToInterface(fragmentList);

        String[] CHANNEL_ID = {Notification.CATEGORY_MESSAGE};
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Tamatgotchi", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            CHANNEL_ID[0] = "Tamatgotchi";
        }


        Context context = this;
        //Connect to bluetooth
        btConn[0] = new MessagesIOHandler(emotionsInterface) {
            private int id = 0;

            @Override
            protected void onNotification(UserNotification notification) {
                id++;

                runOnUiThread(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    }
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID[0])
                            .setSmallIcon(R.drawable.icon)
                            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                            .setContentTitle(notification.getNotifTitle())
                            .setContentText(notification.getNotifMessage())
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setColorized(true)
                            .setAutoCancel(true)
                            .setColor(getColor(R.color.bluetooth_connected));

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                    // notificationId is a unique int for each notification that you must define
                    notificationManager.notify(id, builder.build());
                });
                Log.i(MainActivity.class.toString(), "Notif sent " + notification.toString());
            }

            @Override
            protected void onIOException(IOException e) {
                stateDisconnected();

                disconnect();

                try {
                    bluetoothConnect(btConn[0]);
                } catch (IOException ioException) {
                    ioException.printStackTrace();

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }

                    onIOException(e);
                }
            }

            @Override
            protected void onSignalStrength(double strength) {
                //Do nothing
            }

            @Override
            protected void onSignalStrengthReq() {
                BluetoothGattCallback whatIsAGattAndWhereDoIFindOne = new BluetoothGattCallback() {
                    @Override
                    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                        super.onReadRemoteRssi(gatt, rssi, status);

                        SignalStrengthMessage msg = new SignalStrengthMessage(rssi);
                        try {
                            btConn[0].sendMessage(msg);
                        } catch (IOException e) {
                            Log.e(MainActivity.class.toString(), "Error sending rssi.");
                            e.printStackTrace();
                        }

                        gatt.disconnect();
                    }
                };

                ev3.connectGatt(getBaseContext(), true, whatIsAGattAndWhereDoIFindOne);
                Log.i(MainActivity.class.toString(), "Sig strength sent");
            }

            @Override
            protected void onDeathMessage() {
                //TODO: show death screen
                Log.i(MainActivity.class.toString(), "Death recv");
            }
        };

        boolean connected = false;
        while (!connected) {
            try {
                Log.i(MainActivity.class.toString(), "Trying to connect");
                bluetoothConnect(btConn[0]);
                connected = true;
            } catch (IOException e) {
                Log.i(MainActivity.class.toString(), "Connect failed");
                e.printStackTrace();
            }
        }
    }

    private void bluetoothInitialise() {
        // Enables Bluetooth if not enabled
        localAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!localAdapter.isEnabled()) {
            localAdapter.enable();
        }
    }

    private void bluetoothConnect(MessagesIOHandler handler) throws IOException {
        bluetoothInitialise();

        if (serverSocket == null) serverSocket = new ServerSocket(4200);
        socket = serverSocket.accept();
        socket.setKeepAlive(true);

        ev3In = socket.getInputStream();
        ev3Out = socket.getOutputStream();
        handler.setIOStreams(ev3In, ev3Out);
        handler.startPollingThread();

        stateConnected();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void disconnect() {
        //Close io streams and socket
        btConn[0].stopPollingThread();

        try {
            ev3In.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ev3Out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        btConn[0].stopPollingThread();
    }

    /**
     * (source : https://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device-from-code/13007325#13007325)
     * Get IP address from first non-localhost interface
     *
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        } // for now eat exceptions
        return "";
    }

}
