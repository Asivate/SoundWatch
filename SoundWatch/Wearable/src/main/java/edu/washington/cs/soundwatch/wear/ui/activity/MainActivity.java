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

public class MainActivity extends AppCompatActivity {
    public static final String mBroadcastSoundPrediction = "edu.washington.cs.soundwatch.wear.SOUND_PREDICTION";
    private static Socket mSocket;
    private WearableRecyclerView mRecyclerView;
    public static final String TAG = "MainActivity";
    private static final String SERVER_URL = "http://10.12.73.50:3000"; // Server running on Wi-Fi IP
    private SoundRecorder soundRecorder;
    private BroadcastReceiver soundPredictionReceiver;
    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        if (Constants.ARCHITECTURE.equals(Constants.WATCH_ONLY_ARCHITECTURE)) {
            // Use local sound processing
            Log.d(TAG, "Starting local sound processing");
            TextView soundDisplay = findViewById(R.id.soundDisplay);
            soundDisplay.setText(R.string.listening);

            // Hide the instruction text
            TextView dontShowDisplay = findViewById(R.id.dontshowDisplay);
            dontShowDisplay.setVisibility(View.GONE);

            // Start recording and processing
            if (soundRecorder != null) {
                try {
                    soundRecorder.startRecording(null);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting recording: " + e.getMessage());
                    Toast.makeText(this, "Error starting recording: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "SoundRecorder not initialized");
                Toast.makeText(this, "Error: Sound recorder not initialized. Please restart the app.",
                        Toast.LENGTH_LONG).show();
                initializeSoundRecorder();
            }
        } else {
            // Use server connection
            if (mSocket != null && mSocket.connected()) {
                Log.d(TAG, "Socket status - connected: " + mSocket.connected());
                Log.d(TAG, "Sending start_listening event to server");
                mSocket.emit("start_listening");

                // Update UI to show listening state
                TextView soundDisplay = findViewById(R.id.soundDisplay);
                soundDisplay.setText(R.string.listening);
                Log.d(TAG, "Updated display to listening state");

                // Hide the instruction text
                TextView dontShowDisplay = findViewById(R.id.dontshowDisplay);
                dontShowDisplay.setVisibility(View.GONE);
                Log.d(TAG, "Hidden instruction text");
            } else {
                String status = mSocket == null ? "Socket is null" : "Socket is disconnected";
                Log.e(TAG, "Cannot start listening: " + status);
                Log.e(TAG, "Socket connection state: " + (mSocket != null ? mSocket.connected() : "null"));

                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Not connected to server", Toast.LENGTH_SHORT).show());

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
}
