package uk.co.rhul.r14.letamagotchijos;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;

/**
 * This fragment is for the showing of each of the emotion bars.
 *
 * @author Danny
 * @version 1.0
 */
public class EmotionDisplayFragment extends Fragment {

    /**
     * These strings are the keys for the bundle.
     *
     * @since 1.0
     */
    public static String EMOTION_NAME = "emotion_name",
            EMOTION_STATUS = "emotion_status",
            EMOTION_STATE = "emotion_state";

    private float emotionState;
    private String emotionName, emotionStatus;

    private View view;

    /**
     * Used by the fragment transaction thing not the user.
     *
     * @since 1.0
     */
    public EmotionDisplayFragment() {
        super(R.layout.emotion_fragment);
    }

    /**
     * This method will create the bundle.
     *
     * @param emotionState  the flop value which will be shown in the progress bar
     * @param emotionName   the name of the emotion
     * @param emotionStatus the status string of the emotion
     * @since 1.0
     */
    public static Bundle getBundle(float emotionState, String emotionName, String emotionStatus) {
        Bundle bundle = new Bundle();

        bundle.putFloat(EMOTION_STATE, emotionState);
        bundle.putString(EMOTION_NAME, emotionName);
        bundle.putString(EMOTION_STATUS, emotionStatus);

        return bundle;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        this.view = view;

        //Get the data from the bundle
        this.setEmotionName(requireArguments().getString(EMOTION_NAME));
        this.setEmotionStatus(requireArguments().getString(EMOTION_STATUS));
        this.setEmotionState(requireArguments().getFloat(EMOTION_STATE));

        //The setters put the data in the view
    }

    /**
     * Sets the emotion state and updates the progress bar representing it
     *
     * @param emotionState float between -1 and 1 from the EmotionsInterface
     * @since 1.0
     */
    public void setEmotionState(float emotionState) {
        Log.i("Emotion: " + this.emotionName, "Set to " + emotionState);

        if (!Float.isNaN(emotionState)) {
            this.emotionState = emotionState;
            ((LinearProgressIndicator) view.findViewById(R.id.emotion_need_bar)).setProgress(Math.round(this.emotionState * 100));
        }
    }

    /**
     * Updates the emotion name and the UI
     *
     * @param emotionName the name of the emotion
     * @since 1.0
     */
    public void setEmotionName(String emotionName) {
        this.emotionName = emotionName;
        ((TextView) view.findViewById(R.id.emotion_title)).setText(this.emotionName);
    }

    /**
     * Updates the emotion status and the UI
     *
     * @param emotionStatus the status of the emotion
     * @since 1.0
     */
    public void setEmotionStatus(String emotionStatus) {
        this.emotionStatus = emotionStatus;
        ((TextView) view.findViewById(R.id.emotion_status)).setText(this.emotionStatus);
    }

}
