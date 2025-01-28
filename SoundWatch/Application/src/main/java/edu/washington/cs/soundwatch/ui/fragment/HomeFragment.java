package edu.washington.cs.soundwatch.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.washington.cs.soundwatch.R;

public class HomeFragment extends Fragment {
    private static final String PREFS_NAME = "SoundWatchPreferences";

    // Emergency Sounds
    private CheckBox fireAlarmCheckBox;

    // Home Sounds
    private CheckBox doorCheckBox;
    private CheckBox waterRunningCheckBox;
    private CheckBox microwaveCheckBox;
    private CheckBox doorBellCheckBox;

    // Human/Animal Sounds
    private CheckBox speechCheckBox;
    private CheckBox dogBarkCheckBox;
    private CheckBox catMeowCheckBox;
    private CheckBox babyCryingCheckBox;

    // Outdoor Sounds
    private CheckBox carHonkCheckBox;
    private CheckBox vehicleCheckBox;

    // Tools Sounds
    private CheckBox drillCheckBox;
    private CheckBox vacuumCheckBox;
    private CheckBox hairDryerCheckBox;

    // Additional Sounds
    private CheckBox knockingCheckBox;
    private CheckBox typingCheckBox;
    private CheckBox coughingCheckBox;

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Emergency Sounds
        fireAlarmCheckBox = view.findViewById(R.id.fire_smoke_alarm);

        // Initialize Home Sounds
        doorCheckBox = view.findViewById(R.id.door_in_use);
        waterRunningCheckBox = view.findViewById(R.id.water_running);
        microwaveCheckBox = view.findViewById(R.id.microwave);
        doorBellCheckBox = view.findViewById(R.id.door_bell);

        // Initialize Human/Animal Sounds
        speechCheckBox = view.findViewById(R.id.speech);
        dogBarkCheckBox = view.findViewById(R.id.dog_bark);
        catMeowCheckBox = view.findViewById(R.id.cat_meow);
        babyCryingCheckBox = view.findViewById(R.id.baby_crying);

        // Initialize Outdoor Sounds
        carHonkCheckBox = view.findViewById(R.id.car_honk);
        vehicleCheckBox = view.findViewById(R.id.vehicle);

        // Initialize Tools Sounds
        drillCheckBox = view.findViewById(R.id.drill);
        vacuumCheckBox = view.findViewById(R.id.vacuum);
        hairDryerCheckBox = view.findViewById(R.id.hair_dryer);

        // Initialize Additional Sounds
        knockingCheckBox = view.findViewById(R.id.knocking);
        typingCheckBox = view.findViewById(R.id.typing);
        coughingCheckBox = view.findViewById(R.id.coughing);

        // Restore checkbox states
        restoreCheckboxStates();

        // Set up click listeners for all checkboxes
        setupCheckBoxListener(fireAlarmCheckBox, "Fire/Smoke Alarm");
        setupCheckBoxListener(doorCheckBox, "Door");
        setupCheckBoxListener(waterRunningCheckBox, "Water Running");
        setupCheckBoxListener(microwaveCheckBox, "Microwave");
        setupCheckBoxListener(doorBellCheckBox, "Door Bell");
        setupCheckBoxListener(speechCheckBox, "Speech");
        setupCheckBoxListener(dogBarkCheckBox, "Dog Bark");
        setupCheckBoxListener(catMeowCheckBox, "Cat Meow");
        setupCheckBoxListener(babyCryingCheckBox, "Baby Crying");
        setupCheckBoxListener(carHonkCheckBox, "Car Horn");
        setupCheckBoxListener(vehicleCheckBox, "Vehicle");
        setupCheckBoxListener(drillCheckBox, "Drill");
        setupCheckBoxListener(vacuumCheckBox, "Vacuum");
        setupCheckBoxListener(hairDryerCheckBox, "Hair Dryer");
        setupCheckBoxListener(knockingCheckBox, "Knocking");
        setupCheckBoxListener(typingCheckBox, "Typing");
        setupCheckBoxListener(coughingCheckBox, "Coughing");

        return view;
    }

    private void setupCheckBoxListener(CheckBox checkBox, String soundName) {
        if (checkBox != null) {
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String status = isChecked ? "enabled" : "disabled";
                Toast.makeText(getContext(),
                        soundName + " detection " + status,
                        Toast.LENGTH_SHORT).show();

                // Save checkbox state
                saveCheckboxState(checkBox.getId(), isChecked);

                // TODO: Implement sound detection logic
            });
        }
    }

    private void saveCheckboxState(int checkboxId, boolean isChecked) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(String.valueOf(checkboxId), isChecked);
        editor.apply();
    }

    private void restoreCheckboxStates() {
        restoreCheckbox(fireAlarmCheckBox);
        restoreCheckbox(doorCheckBox);
        restoreCheckbox(waterRunningCheckBox);
        restoreCheckbox(microwaveCheckBox);
        restoreCheckbox(doorBellCheckBox);
        restoreCheckbox(speechCheckBox);
        restoreCheckbox(dogBarkCheckBox);
        restoreCheckbox(catMeowCheckBox);
        restoreCheckbox(babyCryingCheckBox);
        restoreCheckbox(carHonkCheckBox);
        restoreCheckbox(vehicleCheckBox);
        restoreCheckbox(drillCheckBox);
        restoreCheckbox(vacuumCheckBox);
        restoreCheckbox(hairDryerCheckBox);
        restoreCheckbox(knockingCheckBox);
        restoreCheckbox(typingCheckBox);
        restoreCheckbox(coughingCheckBox);
    }

    private void restoreCheckbox(CheckBox checkBox) {
        if (checkBox != null) {
            boolean savedState = sharedPreferences.getBoolean(
                    String.valueOf(checkBox.getId()), false);
            checkBox.setChecked(savedState);
        }
    }
}