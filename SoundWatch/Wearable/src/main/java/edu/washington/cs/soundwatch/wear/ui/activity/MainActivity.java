package edu.washington.cs.soundwatch.wear.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;
import android.os.Bundle;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONObject;
import edu.washington.cs.soundwatch.wear.R;

public class MainActivity extends AppCompatActivity {
    public static final String mBroadcastSoundPrediction = "edu.washington.cs.soundwatch.wear.SOUND_PREDICTION";
    private static Socket mSocket;
    private WearableRecyclerView mRecyclerView;
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSocket();
        setupRecyclerView();
    }

    private void setupSocket() {
        try {
            mSocket = IO.socket("YOUR_SERVER_URL"); // Replace with your actual server URL
            mSocket.on("new_message", onNewMessage);
            mSocket.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        }
    }

    private final Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    // Handle the message
                }
            });
        }
    };
}
