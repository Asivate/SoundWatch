package edu.washington.cs.soundwatch.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.Fragment;
import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;
import edu.washington.cs.soundwatch.R;

public class Tutorial extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TutorialActivity", "onCreated called");

        addSlide(AppIntroFragment.newInstance(
            "INSTRUCTIONS",
            "",
            R.drawable.warning,
            Color.parseColor("#000000")
        ));

        addSlide(AppIntroFragment.newInstance(
            "INSTRUCTIONS",
            "",
            R.drawable.data_privacy,
            Color.parseColor("#000000")
        ));

        addSlide(AppIntroFragment.newInstance(
            "INSTRUCTIONS",
            "",
            R.drawable.important_notice,
            Color.parseColor("#000000")
        ));

        addSlide(AppIntroFragment.newInstance(
            "Tutorial 1",
            "",
            R.drawable.tutorial_1,
            Color.parseColor("#000000")
        ));

        addSlide(AppIntroFragment.newInstance(
            "Tutorial 2",
            "",
            R.drawable.tutorial_2,
            Color.parseColor("#000000")
        ));

        addSlide(AppIntroFragment.newInstance(
            "Tutorial 3",
            "",
            R.drawable.tutorial_3,
            Color.parseColor("#000000")
        ));

        addSlide(AppIntroFragment.newInstance(
            "Tutorial 4",
            "",
            R.drawable.tutorial_4,
            Color.parseColor("#000000")
        ));

        addSlide(AppIntroFragment.newInstance(
            "Tutorial 5",
            "",
            R.drawable.tutorial_5,
            Color.parseColor("#000000")
        ));

        addSlide(AppIntroFragment.newInstance(
            "Tutorial 5",
            "",
            R.drawable.tutorial_6,
            Color.parseColor("#000000")
        ));

        setSkipButtonEnabled(false);
    }

    @Override
    protected void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }

    @Override
    protected void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }
}