package edu.washington.cs.soundwatch.wear.ui.activity;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import androidx.wear.widget.WearableRecyclerView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONObject;
import java.net.URISyntaxException;

public class MainActivity extends WearableActivity implements WearableRecyclerView.OnScrollListener {
    private static final String SERVER_URL = "http://your-server-url:3000";
    private static Socket mSocket;
    private WearableRecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupSocket();
        setupRecyclerView();
    }

    private void setupSocket() {
        try {
            mSocket = IO.socket(SERVER_URL);
            mSocket.on("new_message", onNewMessage);
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void setupRecyclerView() {
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.addOnScrollListener(this);
    }

    @Override
    public void onScrollStateChanged(WearableRecyclerView recyclerView, int newState) {
        // Handle scroll state changes
    }

    @Override
    public void onScrolled(WearableRecyclerView recyclerView, int dx, int dy) {
        // Handle scrolling
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
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

    public static Socket getMSocket() {
        return mSocket;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off("new_message", onNewMessage);
    }
}