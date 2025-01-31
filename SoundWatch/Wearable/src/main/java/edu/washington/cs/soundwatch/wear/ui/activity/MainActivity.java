package edu.washington.cs.soundwatch.wear.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONObject;
import edu.washington.cs.soundwatch.wear.R;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import edu.washington.cs.soundwatch.wear.utils.Constants;
import edu.washington.cs.soundwatch.wear.utils.SoundRecorder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.tasks.Tasks;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import android.widget.ImageView;
import android.graphics.Color;
import android.content.SharedPreferences;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String mBroadcastSoundPrediction = "edu.washington.cs.soundwatch.wear.SOUND_PREDICTION";
    private static Socket mSocket;
    private WearableRecyclerView mRecyclerView;
    public static final String TAG = "MainActivity";
    private static final String SERVER_URL = "http://10.12.73.50:3000"; // Server running on Wi-Fi IP
    private SoundRecorder soundRecorder;
    private BroadcastReceiver soundPredictionReceiver;
    private static final int PERMISSION_REQUEST_CODE = 123;
    private TextView connectionStatusText;
    private ImageView connectionStatusIcon;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private ScheduledExecutorService executorService;
    private String connectedPhoneId;
    private static final String CONNECTION_PREFS = "connection_prefs";
    private static final String CONNECTION_TEST_PATH = "/connection_test";
    private static final String WATCH_CONNECTION_STATUS_PATH = "/watch_connection_status";
    private String connectedPhoneName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize connection UI elements
        connectionStatusText = findViewById(R.id.connection_status);
        connectionStatusIcon = findViewById(R.id.connection_icon);

        // Initialize executor service for background tasks
        executorService = Executors.newSingleThreadScheduledExecutor();

        // Start connection monitoring
        startConnectionMonitoring();

        // Try to reconnect to last known phone
        reconnectToLastPhone();

        // Initialize connection status UI
        updateConnectionStatus("Waiting for phone...");

        // Set up Wear message client
        setupWearMessageClient();

        if (!Constants.ARCHITECTURE.equals(Constants.WATCH_ONLY_ARCHITECTURE)) {
            setupSocket();
        } else {
            Log.d(TAG, "Running in watch-only mode - initializing local sound processing");
            if (checkAndRequestPermissions()) {
                initializeSoundRecorder();
            }
        }

        setupRecyclerView();

        // Set up mic button click handler
        ImageButton micButton = findViewById(R.id.mic);
        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Mic button clicked - Starting listening process");
                if (checkAndRequestPermissions()) {
                    startListening();
                }
            }
        });
    }

    private boolean checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.RECORD_AUDIO },
                    PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void initializeSoundRecorder() {
        try {
            soundRecorder = new SoundRecorder(this, "temp_audio.wav");
            // Set up broadcast receiver for sound predictions
            soundPredictionReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction() != null && intent.getAction().equals(mBroadcastSoundPrediction)) {
                        String prediction = intent.getStringExtra(Constants.AUDIO_LABEL);
                        Log.d(TAG, "Received sound prediction: " + prediction);
                        TextView soundDisplay = findViewById(R.id.soundDisplay);
                        soundDisplay.setText(prediction);
                    }
                }
            };
            IntentFilter filter = new IntentFilter(mBroadcastSoundPrediction);
            registerReceiver(soundPredictionReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing SoundRecorder: " + e.getMessage());
            Toast.makeText(this, "Error initializing sound recorder: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                if (Constants.ARCHITECTURE.equals(Constants.WATCH_ONLY_ARCHITECTURE)) {
                    initializeSoundRecorder();
                }
                startListening();
            } else {
                // Permission denied
                Toast.makeText(this, "Microphone permission is required to detect sounds",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupSocket() {
        try {
            Log.d(TAG, "Setting up socket connection to: " + SERVER_URL);
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnection = true;
            opts.reconnectionAttempts = 3;
            opts.timeout = 20000; // Increased timeout
            opts.reconnectionDelay = 1000;
            opts.reconnectionDelayMax = 5000;
            opts.transports = new String[] { "polling", "websocket" }; // Try polling first, then websocket
            opts.upgrade = true;
            opts.rememberUpgrade = true;
            mSocket = IO.socket(SERVER_URL, opts);
            Log.d(TAG, "Socket options configured successfully");

            mSocket.on(Socket.EVENT_CONNECT, onConnect);
            mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on("connection_response", args -> {
                Log.d(TAG, "Received connection response: " + (args.length > 0 ? args[0].toString() : "no data"));
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Server connected", Toast.LENGTH_SHORT).show());
            });
            mSocket.on("audio_label", args -> {
                Log.d(TAG, "Received audio_label event: " + (args.length > 0 ? args[0].toString() : "no data"));
                if (args.length > 0) {
                    runOnUiThread(() -> {
                        TextView soundDisplay = findViewById(R.id.soundDisplay);
                        try {
                            JSONObject data = (JSONObject) args[0];
                            String label = data.getString("label");
                            Log.d(TAG, "Setting sound display text to: " + label);
                            soundDisplay.setText(label);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing audio_label: " + e.getMessage(), e);
                        }
                    });
                }
            });
            mSocket.connect();
            Log.d(TAG, "Socket connection initiated");
        } catch (Exception e) {
            Log.e(TAG, "Socket setup failed: " + e.getMessage(), e);
            runOnUiThread(() -> Toast.makeText(MainActivity.this,
                    "Connection failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private final Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Socket connected successfully");
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Connected to server", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Connection toast shown");
            });
        }
    };

    private final Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Socket disconnected. Attempting to reconnect...");
            runOnUiThread(() -> Toast.makeText(MainActivity.this,
                    "Disconnected from server", Toast.LENGTH_SHORT).show());
        }
    };

    private final Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String error = args.length > 0 ? args[0].toString() : "unknown";
            Log.e(TAG, "Connection error: " + error);
            runOnUiThread(() -> Toast.makeText(MainActivity.this,
                    "Connection error: " + error, Toast.LENGTH_SHORT).show());
        }
    };

    private void setupRecyclerView() {
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
    }

    public static Socket getMSocket() {
        return mSocket;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off("new_message", onNewMessage);
            mSocket.off(Socket.EVENT_CONNECT, onConnect);
            mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
            mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        }
        if (soundRecorder != null) {
            soundRecorder.cleanup();
        }
        if (soundPredictionReceiver != null) {
            unregisterReceiver(soundPredictionReceiver);
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private final Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        Log.d(TAG, "Received message: " + data.toString());
                        // Handle the message
                    } catch (Exception e) {
                        Log.e(TAG, "Error handling message: " + e.getMessage(), e);
                    }
                }
            });
        }
    };

    private void startListening() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Microphone permission is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // First check if we have a valid connection
        if (!isConnectedToPhone()) {
            Toast.makeText(this, "Please wait for phone connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show immediate visual feedback
        TextView soundDisplay = findViewById(R.id.soundDisplay);
        TextView dontShowDisplay = findViewById(R.id.dontshowDisplay);

        soundDisplay.setText(R.string.listening);
        soundDisplay.setVisibility(View.VISIBLE);
        dontShowDisplay.setVisibility(View.GONE);

        if (Constants.ARCHITECTURE.equals(Constants.WATCH_ONLY_ARCHITECTURE)) {
            // Use local sound processing
            Log.d(TAG, "Starting local sound processing");

            // Start recording and processing
            if (soundRecorder != null) {
                try {
                    soundRecorder.startRecording(null);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting recording: " + e.getMessage());
                    Toast.makeText(this, "Error starting recording: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    soundDisplay.setText(R.string.error_recording);
                }
            } else {
                Log.e(TAG, "SoundRecorder not initialized");
                Toast.makeText(this, "Error: Sound recorder not initialized. Please restart the app.",
                        Toast.LENGTH_LONG).show();
                soundDisplay.setText(R.string.error_initializing);
                initializeSoundRecorder();
            }
        } else {
            // Use server connection
            if (mSocket != null && mSocket.connected()) {
                Log.d(TAG, "Socket status - connected: " + mSocket.connected());
                Log.d(TAG, "Sending start_listening event to server");
                mSocket.emit("start_listening");
            } else {
                String status = mSocket == null ? "Socket is null" : "Socket is disconnected";
                Log.e(TAG, "Cannot start listening: " + status);
                soundDisplay.setText(R.string.error_server_connection);

                // Try to reconnect
                if (mSocket != null) {
                    Log.d(TAG, "Attempting to reconnect existing socket");
                    mSocket.connect();
                } else {
                    Log.d(TAG, "Setting up new socket connection");
                    setupSocket();
                }
            }
        }
    }

    private boolean isConnectedToPhone() {
        try {
            List<Node> nodes = Tasks.await(Wearable.getNodeClient(this).getConnectedNodes());
            return !nodes.isEmpty();
        } catch (Exception e) {
            Log.e(TAG, "Error checking phone connection: " + e.getMessage());
            return false;
        }
    }

    private void setupWearMessageClient() {
        MessageClient messageClient = Wearable.getMessageClient(this);
        messageClient.addListener(messageEvent -> {
            if (messageEvent.getPath().equals(CONNECTION_TEST_PATH)) {
                String message = new String(messageEvent.getData());
                if (message.startsWith("phone_connected:")) {
                    String[] parts = message.split(":");
                    if (parts.length >= 3) {
                        String phoneModel = parts[1];
                        String androidVersion = parts[2];
                        connectedPhoneName = phoneModel;
                        updateConnectionStatus("Connected to " + phoneModel + " (Android " + androidVersion + ")");

                        // Save connection info
                        SharedPreferences prefs = getSharedPreferences(CONNECTION_PREFS, MODE_PRIVATE);
                        prefs.edit()
                                .putString("connected_phone_model", phoneModel)
                                .putString("connected_phone_android", androidVersion)
                                .putLong("last_connection_time", System.currentTimeMillis())
                                .apply();

                        // Send acknowledgment back to phone
                        messageClient.sendMessage(
                                messageEvent.getSourceNodeId(),
                                WATCH_CONNECTION_STATUS_PATH,
                                "watch_ready".getBytes())
                                .addOnFailureListener(
                                        e -> Log.e(TAG, "Failed to send acknowledgment: " + e.getMessage()));
                    }
                }
            }
        });
    }

    private void updateConnectionStatus(final String status) {
        runOnUiThread(() -> {
            if (connectionStatusText != null) {
                connectionStatusText.setText(status);
                connectionStatusText.setVisibility(View.VISIBLE);

                // Update UI colors based on connection state
                if (status.startsWith("Connected")) {
                    connectionStatusText.setTextColor(getResources().getColor(R.color.connection_success));
                    if (connectionStatusIcon != null) {
                        connectionStatusIcon.setImageResource(android.R.drawable.presence_online);
                        connectionStatusIcon.setColorFilter(getResources().getColor(R.color.connection_success));
                    }
                } else {
                    connectionStatusText.setTextColor(getResources().getColor(R.color.connection_error));
                    if (connectionStatusIcon != null) {
                        connectionStatusIcon.setImageResource(android.R.drawable.presence_offline);
                        connectionStatusIcon.setColorFilter(getResources().getColor(R.color.connection_error));
                    }
                }

                // Show toast for important status changes
                if (status.startsWith("Connected") || status.equals("Disconnected")) {
                    showConnectionToast(status.startsWith("Connected"),
                            status.startsWith("Connected") ? status.substring(13) : null);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if we have saved connection info
        SharedPreferences prefs = getSharedPreferences(CONNECTION_PREFS, MODE_PRIVATE);
        String savedPhoneModel = prefs.getString("connected_phone_model", null);
        String savedAndroidVersion = prefs.getString("connected_phone_android", null);
        long lastConnectionTime = prefs.getLong("last_connection_time", 0);

        // Only use saved info if it's recent (within last hour)
        if (savedPhoneModel != null && savedAndroidVersion != null &&
                System.currentTimeMillis() - lastConnectionTime < 3600000) {
            updateConnectionStatus("Connected to " + savedPhoneModel + " (Android " + savedAndroidVersion + ")");
        } else {
            updateConnectionStatus("Waiting for phone...");
            // Trigger a new connection check
            checkConnectionStatus();
        }
    }

    private void startConnectionMonitoring() {
        executorService.scheduleAtFixedRate(() -> {
            checkConnectionStatus();
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void checkConnectionStatus() {
        try {
            Tasks.await(Wearable.getNodeClient(this).getConnectedNodes())
                    .stream()
                    .findFirst()
                    .ifPresent(node -> {
                        updateConnectionUI(true, node);
                        connectedPhoneId = node.getId();
                        saveConnectedPhoneInfo(node);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error checking connection status: " + e.getMessage());
            updateConnectionUI(false, null);
        }
    }

    private void updateConnectionUI(boolean connected, Node phoneNode) {
        mainHandler.post(() -> {
            if (connected && phoneNode != null) {
                connectionStatusText.setText(String.format("Connected to %s", phoneNode.getDisplayName()));
                connectionStatusText.setTextColor(getColor(R.color.connection_success));
                if (connectionStatusIcon != null) {
                    connectionStatusIcon.setImageResource(android.R.drawable.presence_online);
                }
                showConnectionToast(true, phoneNode.getDisplayName());
            } else {
                connectionStatusText.setText("Disconnected");
                connectionStatusText.setTextColor(getColor(R.color.connection_error));
                if (connectionStatusIcon != null) {
                    connectionStatusIcon.setImageResource(android.R.drawable.presence_offline);
                }
                showConnectionToast(false, null);
            }
        });
    }

    private void showConnectionToast(boolean connected, String deviceName) {
        String message = connected ? String.format("Connected to %s", deviceName) : "Disconnected from phone";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void saveConnectedPhoneInfo(Node phoneNode) {
        SharedPreferences prefs = getSharedPreferences(CONNECTION_PREFS, MODE_PRIVATE);
        prefs.edit()
                .putString("last_connected_phone_id", phoneNode.getId())
                .putString("last_connected_phone_name", phoneNode.getDisplayName())
                .apply();
    }

    private void reconnectToLastPhone() {
        SharedPreferences prefs = getSharedPreferences(CONNECTION_PREFS, MODE_PRIVATE);
        String lastPhoneId = prefs.getString("last_connected_phone_id", null);

        if (lastPhoneId != null) {
            Log.d(TAG, "Attempting to reconnect to last known phone: " + lastPhoneId);
            checkConnectionStatus();
        }
    }

    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(CONNECTION_TEST_PATH)) {
            String message = new String(messageEvent.getData());
            if (message.startsWith("phone_connected:")) {
                String[] parts = message.split(":");
                if (parts.length >= 3) {
                    String phoneModel = parts[1];
                    String androidVersion = parts[2];
                    updatePhoneDetails(phoneModel, androidVersion);
                }
            }
        }
    }

    private void updatePhoneDetails(String phoneModel, String androidVersion) {
        mainHandler.post(() -> {
            String details = String.format("Connected to %s (Android %s)", phoneModel, androidVersion);
            connectionStatusText.setText(details);

            // Save these details
            SharedPreferences prefs = getSharedPreferences(CONNECTION_PREFS, MODE_PRIVATE);
            prefs.edit()
                    .putString("connected_phone_model", phoneModel)
                    .putString("connected_phone_android", androidVersion)
                    .apply();
        });
    }
}
