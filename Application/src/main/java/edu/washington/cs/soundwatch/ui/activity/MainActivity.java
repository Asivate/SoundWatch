package edu.washington.cs.soundwatch.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends androidx.appcompat.app.AppCompatActivity {
    private static final String SERVER_URL = "http://10.0.2.2:3000"; // For Android emulator, 10.0.2.2 points to host's
    // localhost
    private static final String TEST_E2E_LATENCY_SERVER = "http://10.0.2.2:3000";
    private static final String MODEL_LATENCY_SERVER = "http://10.0.2.2:3000";
    private static final String DEFAULT_SERVER = "http://10.0.2.2:3000";

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.FOREGROUND_SERVICE
    };

    private boolean checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // Handle Android 13+ notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Handle storage permissions for Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
            }
        } else {
            // For older Android versions, check all permissions
            for (String permission : REQUIRED_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(permission);
                }
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            Map<String, Boolean> permissionResults = new HashMap<>();

            for (int i = 0; i < permissions.length; i++) {
                permissionResults.put(permissions[i], grantResults[i] == PackageManager.PERMISSION_GRANTED);
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                }
            }

            if (allPermissionsGranted) {
                // All permissions granted, initialize the app
                initializeApp();
                if (isFirst(MainActivity.this)) {
                    handleFirstLaunch();
                }
            } else {
                // Check which permissions were denied and show appropriate messages
                StringBuilder message = new StringBuilder("The following permissions are required:\n\n");
                boolean shouldShowSettings = false;

                for (Map.Entry<String, Boolean> entry : permissionResults.entrySet()) {
                    if (!entry.getValue()) {
                        String permissionName = getPermissionFriendlyName(entry.getKey());
                        message.append("• ").append(permissionName).append("\n");

                        // Check if user clicked "Don't ask again"
                        if (!shouldShowRequestPermissionRationale(entry.getKey())) {
                            shouldShowSettings = true;
                        }
                    }
                }

                message.append("\nWithout these permissions, some features may not work properly.");

                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("Permissions Required")
                        .setMessage(message.toString())
                        .setCancelable(false);

                if (shouldShowSettings) {
                    builder.setPositiveButton("Open Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    });
                } else {
                    builder.setPositiveButton("Try Again", (dialog, which) -> checkAndRequestPermissions());
                }

                builder.setNegativeButton("Exit", (dialog, which) -> finish())
                        .create()
                        .show();
            }
        }
    }

    private String getPermissionFriendlyName(String permission) {
        switch (permission) {
            case Manifest.permission.RECORD_AUDIO:
                return "Microphone (required for sound detection)";
            case Manifest.permission.READ_EXTERNAL_STORAGE:
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "Storage (required for saving sound data)";
            case Manifest.permission.POST_NOTIFICATIONS:
                return "Notifications (required for sound alerts)";
            case Manifest.permission.FOREGROUND_SERVICE:
                return "Background Service (required for continuous sound detection)";
            default:
                return permission;
        }
    }
}