package gt.edu.usac.ingenieria.ia.smartcarcontrol;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnControlFragmentInteraction} interface
 * to handle interaction events.
 * Use the {@link ControlsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ControlsFragment extends Fragment {

    public static final int MOVE = 100
            ;
    public static final int LEFT = Car.LEFT;
    public static final int RIGHT = Car.RIGTH;

    private OnControlFragmentInteraction mListener;

    public ControlsFragment() {
        // Required empty public constructor
    }

    public static ControlsFragment newInstance() {
        return new ControlsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_controls, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnControlFragmentInteraction) {
            mListener = (OnControlFragmentInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnControlClicked");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @OnClick(R.id.control_button_up)
    void onUpClicked(){
        mListener.onControlClicked(MOVE);
    }

    @OnClick(R.id.control_button_left)
    void onLeftClicked(){
        mListener.onControlClicked(LEFT);
    }

    @OnClick(R.id.control_button_right)
    void onRightClicked(){
        mListener.onControlClicked(RIGHT);
    }

    public interface OnControlFragmentInteraction {
        void onControlClicked(int direction);
    }
}
