package edu.washington.cs.soundwatch.wear.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.Wearable;
import edu.washington.cs.soundwatch.wear.utils.AlarmReceiver;
import edu.washington.cs.soundwatch.wear.application.MainApplication;
import static edu.washington.cs.soundwatch.wear.utils.Constants.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static edu.washington.cs.soundwatch.wear.utils.HelperUtils.convertSetToCommaSeparatedList;

/**
 * SnoozeSoundService is a service that handles snoozing sounds.
 * It receives an intent with the sound ID and label, and then sends a message to the phone to snooze the sound.
 * It also sets an alarm to remove the sound from the blocked list after a certain time.
 */
public class SnoozeSoundService extends IntentService {

    private static final String TAG = "SnoozeSoundService";

    private Set<String> connectedHostIds;

    public SnoozeSoundService() {
        super("SnoozeSoundService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent(): " + intent);

        if (intent != null) {
            String action = intent.getStringExtra("action");
            if (action != null && action.equals("removeBlockedSound")) {
                int blockedSoundId = intent.getIntExtra("blockedSoundId", 0);
                removeBlockedSound(blockedSoundId);
                return;
            }

            String snoozeTime = "10 mins";
            if (snoozeTime == null) {
                return;
            }
            final int blockedNotificationID = intent.getIntExtra(SOUND_ID, 0);
            final String soundLabel = intent.getStringExtra(SOUND_LABEL);
            String input = intent.getStringExtra(CONNECTED_HOST_IDS);
            if (input != null) {
                // There is a connected phone
                final Set<String> connectedHostIds = new HashSet<>(
                        Arrays.asList(
                                input.split(",")
                        )
                );
                this.connectedHostIds = connectedHostIds;
            }
            ((MainApplication) this.getApplication()).addBlockedSounds(blockedNotificationID);
            Log.i(TAG, "Add to list of blocked sounds " + blockedNotificationID);

            if (!snoozeTime.equals("Forever")) {
                Intent alarmIntent = new Intent(this,  AlarmReceiver.class);
                alarmIntent.setAction("edu.washington.cs.soundwatch.almMgr");
                alarmIntent.putExtra("blockedSoundId", blockedNotificationID);
                alarmIntent.putExtra(SOUND_LABEL, soundLabel);
                alarmIntent.putExtra(CONNECTED_HOST_IDS, convertSetToCommaSeparatedList(connectedHostIds));
                int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, uniqueInt, alarmIntent, 0);
                AlarmManager alarmMgr = (AlarmManager)this.getSystemService(ALARM_SERVICE);
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 10 * 60 * 1000, pendingIntent);
            }

            //Remove all notifications of this sound in the list
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.cancel(blockedNotificationID);

            Log.i(TAG, "Successfully blocked sounds");

            // Send a message to Phone to indicate this sound is blocked
            sendSnoozeSoundMessageToPhone(soundLabel);

        }
    }

    private void sendSnoozeSoundMessageToPhone(String soundLabel) {
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            DataOutputStream ds = new DataOutputStream(bas);
            if (connectedHostIds == null) {
                // No phone connected to send the message right now
                return;
            }
            for (String connectedHostId : connectedHostIds) {
                Log.d(TAG, "Sending snooze sound data to phone: " + soundLabel);
                Task<Integer> sendMessageTask =
                        Wearable.getMessageClient(this.getApplicationContext())
                                .sendMessage(connectedHostId, SOUND_SNOOZE_FROM_WATCH_PATH, soundLabel.getBytes());
            }
    }

    private void removeBlockedSound(int blockedSoundId) {
        if (blockedSoundId != 0) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.cancel(blockedSoundId);
            Log.d(TAG, "Removed blocked sound with ID: " + blockedSoundId);
        }
    }
}
