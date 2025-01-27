package edu.washington.cs.soundwatch.wear.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.washington.cs.soundwatch.wear.ui.activity.MainActivity;
import edu.washington.cs.soundwatch.wear.service.SnoozeSoundService;
import static edu.washington.cs.soundwatch.wear.utils.Constants.*;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String SOUND_UNSNOOZE_FROM_WATCH_PATH = "/SOUND_UNSNOOZE_FROM_WATCH_PATH";
    public static final String TAG = "AlarmReceiver";
    private static final String CONNECTED_HOST_IDS = "CONNECTED_HOST_IDS";
    private static final String SOUND_LABEL = "SOUND_LABEL";
    private Set<String> connectedHostIds;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(MainActivity.TAG, "Alarm received");
        int blockedNotificationID = intent.getIntExtra("blockedSoundId", 0);
        // Send intent to SnoozeSoundService instead of casting context
        Intent serviceIntent = new Intent(context, SnoozeSoundService.class);
        serviceIntent.putExtra("action", "removeBlockedSound");
        serviceIntent.putExtra("blockedSoundId", blockedNotificationID);
        context.startService(serviceIntent);
        
        final String soundLabel = intent.getStringExtra(SOUND_LABEL);
        String input = intent.getStringExtra(CONNECTED_HOST_IDS);
        Log.i(TAG, "Connected host id: " + input);
        if (input != null) {
            // There is a connected phone
            final Set<String> connectedHostIds = new HashSet<>(
                    Arrays.asList(
                            input.split(",")
                    )
            );
            this.connectedHostIds = connectedHostIds;
            sendUnSnoozeSoundMessageToPhone(context, soundLabel);
        }
    }

    private void sendUnSnoozeSoundMessageToPhone(Context context, String soundLabel) {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        DataOutputStream ds = new DataOutputStream(bas);
        if (connectedHostIds == null) {
            // No phone connected to send the message right now
            return;
        }
        for (String connectedHostId : connectedHostIds) {
            Log.d(TAG, "Sending unsnooze sound data to phone:" + soundLabel);
            Task<Integer> sendMessageTask =
                    Wearable.getMessageClient(context)
                            .sendMessage(connectedHostId, SOUND_UNSNOOZE_FROM_WATCH_PATH, soundLabel.getBytes());
        }
    }
}