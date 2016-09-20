package gt.edu.usac.ingenieria.ia.smartcarcontrol;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 */
public class CarTunningFragment extends DialogFragment implements SeekBar.OnSeekBarChangeListener, DialogInterface.OnClickListener {


    @BindView(R.id.move_time_seekbar)
    SeekBar moveTurnTimeSeekbar;
    @BindView(R.id.left_turn_time_seekbar)
    SeekBar leftTurnTimeSeekbar;
    @BindView(R.id.right_turn_time_seekbar)
    SeekBar rightTurnTimeSeekbar;
    @BindView(R.id.left_wheel_power_seekbar)
    SeekBar leftWheelPowerSeekbar;
    @BindView(R.id.right_wheel_power_seekbar)
    SeekBar rightWheelPowerSeekbar;

    @BindView(R.id.move_time_textview)
    TextView moveTurnTimeTextView;
    @BindView(R.id.left_turn_time_textview)
    TextView leftTurnTimeTextView;
    @BindView(R.id.right_turn_time_textview)
    TextView rightTurnTimeTextView;
    @BindView(R.id.left_wheel_power_textview)
    TextView leftWheelPowerTextView;
    @BindView(R.id.right_wheel_power_textview)
    TextView rightWheelPowerTextView;

    private OnCarTunningInteractionListener mListener;

    public CarTunningFragment() {
        // Required empty public constructor
    }

    public static CarTunningFragment newInstance(){
        return new CarTunningFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_car_tunning, null);

        ButterKnife.bind(this, rootView);

        init(getActivity().getString(R.string.pref_move_time_key), getActivity().getString(R.string.pref_default_move_time), 500, moveTurnTimeSeekbar, moveTurnTimeTextView);
        init(getActivity().getString(R.string.pref_left_turn_time_key), getActivity().getString(R.string.pref_default_left_turn_time), 500, leftTurnTimeSeekbar, leftTurnTimeTextView);
        init(getActivity().getString(R.string.pref_right_turn_time_key), getActivity().getString(R.string.pref_default_right_turn_time), 500, rightTurnTimeSeekbar, rightTurnTimeTextView);
        init(getActivity().getString(R.string.pref_left_wheel_power_key), getActivity().getString(R.string.pref_default_left_wheel_power), 100, leftWheelPowerSeekbar, leftWheelPowerTextView);
        init(getActivity().getString(R.string.pref_right_wheel_power_key), getActivity().getString(R.string.pref_default_right_wheel_power), 100, rightWheelPowerSeekbar, rightWheelPowerTextView);


        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.pref_header_car)
                .setPositiveButton(R.string.done, this)
                .setView(rootView)
                .create();
    }

    private void init(String sharedPrefKey, String sharedPrefDefaultValue, int max, SeekBar seekBar, TextView textView){
        int value = getCurrentValue(sharedPrefKey, sharedPrefDefaultValue);
        if(seekBar != null){
            seekBar.setMax(max);
            seekBar.setProgress(value);
            seekBar.setOnSeekBarChangeListener(this);
        }
        if(textView != null){
            textView.setText(String.valueOf(value));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof  OnCarTunningInteractionListener){
            mListener = (OnCarTunningInteractionListener) context;
        }else{
            throw new ClassCastException("Activity must implment OnCarTunningInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean byUser) {
        switch (seekBar.getId()){
            case R.id.move_time_seekbar:
                moveTurnTimeTextView.setText(String.valueOf(progress));
                break;
            case R.id.left_turn_time_seekbar:
                leftTurnTimeTextView.setText(String.valueOf(progress));
                break;
            case R.id.right_turn_time_seekbar:
                rightTurnTimeTextView.setText(String.valueOf(progress));
                break;
            case R.id.left_wheel_power_seekbar:
                leftWheelPowerTextView.setText(String.valueOf(progress));
                break;
            case R.id.right_wheel_power_seekbar:
                rightWheelPowerTextView.setText(String.valueOf(progress));
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // do nothing
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        switch (seekBar.getId()){
            case R.id.move_time_seekbar:
                saveValue(progress, getActivity().getString(R.string.pref_move_time_key));
                break;
            case R.id.left_turn_time_seekbar:
                saveValue(progress, getActivity().getString(R.string.pref_left_turn_time_key));
                break;
            case R.id.right_turn_time_seekbar:
                saveValue(progress, getActivity().getString(R.string.pref_right_turn_time_key));
                break;
            case R.id.left_wheel_power_seekbar:
                saveValue(progress, getActivity().getString(R.string.pref_left_wheel_power_key));
                break;
            case R.id.right_wheel_power_seekbar:
                saveValue(progress, getActivity().getString(R.string.pref_right_wheel_power_key));
                break;
        }
    }

    /**
     * Saves config in a sharedpref
     * @param value value to save
     * @param sharedPrefKey shared pref key
     */
    private void saveValue(int value, String sharedPrefKey){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putString(sharedPrefKey, String.valueOf(value));
        editor.apply();
    }

    /**
     * Gets the current value of the sharedpref
     * @param sharedPrefKey shared pref search
     * @param defaultPrefValue default value if not set yet
     * @return current value | default value
     */
    private int getCurrentValue(String sharedPrefKey, String defaultPrefValue){
        return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(sharedPrefKey, defaultPrefValue));
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if(which == DialogInterface.BUTTON_POSITIVE){
            mListener.done();
        }
    }

    public interface OnCarTunningInteractionListener{
        void done();
    }
}
