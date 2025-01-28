package edu.washington.cs.soundwatch.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import edu.washington.cs.soundwatch.R;

public class HelpFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.help_fragment, container, false);

        final Button tutorialBtn = view.findViewById(R.id.tutorial_btn);
        tutorialBtn.setOnClickListener(v -> {
            Log.d("HelpFragment", "onClick called");
            Intent tutorial = new Intent(getActivity(), Tutorial.class);
            startActivity(tutorial);
        });

        final Button watchTutorialBtn = view.findViewById(R.id.watch_tutorial_btn);
        watchTutorialBtn.setOnClickListener(v -> {
            Log.d("HelpFragment", "onClick called");
            Intent tutorial = new Intent(getActivity(), WatchTutorial.class);
            startActivity(tutorial);
        });

        // Get display metrics
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        ConstraintLayout constraintLayout = view.findViewById(R.id.help_layout);
        ViewGroup.LayoutParams layoutParams = constraintLayout.getLayoutParams();
        layoutParams.width = screenWidth;
        layoutParams.height = (int) (screenHeight * 0.95);
        constraintLayout.setLayoutParams(layoutParams);

        return view;
    }
}