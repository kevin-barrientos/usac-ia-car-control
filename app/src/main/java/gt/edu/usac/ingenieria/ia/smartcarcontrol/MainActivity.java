package gt.edu.usac.ingenieria.ia.smartcarcontrol;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.dmitrymalkovich.android.ProgressFloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements ControlsFragment.OnControlFragmentInteraction, FragmentManager.OnBackStackChangedListener {


    private final Car mCar = new Car();
    @BindView(R.id.progressbar)
    ProgressBar mFabProgressBar;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.fab_wrapper)
    ProgressFloatingActionButton mFabWrapper;
    @BindView(R.id.loading)
    View mLoadingProgressBar;
    private MazeFragment mMazeFragment;
    private SocketService mSocketService;
    private Boolean mBound = false;
    private Boolean mIsControlFragmentVisible = false;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SocketService.LocalBinder binder = (SocketService.LocalBinder) service;
            mSocketService = binder.getService();
            mSocketService.setResultReceiver(new MainActivityReciver(new Handler()));
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    /**
     * Gesture detector to listen for fling up gesture and a fling down gesture.
     */
    private GestureDetectorCompat mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMazeFragment = (MazeFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_maze);

        mDetector = new GestureDetectorCompat(this, new GestureListener());

        this.getSupportFragmentManager().addOnBackStackChangedListener(this);

        ButterKnife.bind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsActivityIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsActivityIntent);
            return true;
        } else if (id == R.id.action_connect && mBound) {
            setVisibilityLoadingProgressBarVisibility(View.VISIBLE);
            mSocketService.connect();
        } else if (id == R.id.action_disconnect && mBound) {
            setVisibilityLoadingProgressBarVisibility(View.VISIBLE);
            mSocketService.closeConnection();
        } else if (id == R.id.action_erase_image) {
            mCar.init();
            mMazeFragment.mMazeCanvas.erase();
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.fab)
    public void onFabClicked(View fab) {
        if (!mBound) {
            return;
        }

        mFabProgressBar.setVisibility(View.VISIBLE);

        if (mCar.getMode() == Car.MODE_AUTOMATIC) {
            mSocketService.sendMessage(2); //send automatic mode command
        } else {
            mSocketService.sendMessage(1); //send manual mode command
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, SocketService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onControlClicked(int direction) {
        switch (direction){
            case ControlsFragment.LEFT:
                mSocketService.sendMessage(5); // turn left server command code
                break;
            case ControlsFragment.RIGHT:
                mSocketService.sendMessage(4); // turn right server command code
                break;
            case ControlsFragment.MOVE:
                mSocketService.sendMessage(3); // move forward server command code
                break;
            case ControlsFragment.MOVEBACK:
                mSocketService.sendMessage(6); // move back server command code
                break;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackStackChanged() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 1) {
            mIsControlFragmentVisible = true;
        } else if (count == 0) {
            mIsControlFragmentVisible = false;

            int fabMargin = (int) getResources().getDimension(R.dimen.fab_margin);
            setMargins(mFabWrapper, fabMargin, fabMargin, fabMargin, fabMargin);
        }
    }

    /**
     * Acknowledge the connection,and change {@link #mLoadingProgressBar} visibility to GONE.
     * @see #setVisibilityLoadingProgressBarVisibility(int)
     */
    private void connected() {
        setVisibilityLoadingProgressBarVisibility(View.GONE);
        Snackbar.make(mFab, "Connected :D", Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Acknowledge the disconnection,and change {@link #mLoadingProgressBar} visibility to GONE.
     * @see #setVisibilityLoadingProgressBarVisibility(int)
     */
    private void disconnected() {

        if(mCar.getMode() == Car.MODE_AUTOMATIC){
            mCar.setMode(Car.MODE_MANUAL);
            flipFab(R.animator.rotate_yaxis_0);
            changeFabIcon(R.drawable.ic_play_arrow_white_24dp);
        }

        setVisibilityLoadingProgressBarVisibility(View.GONE);
    }

    /**
     * Flip the {@link #mFab} using an Animator resource
     * @param resourceId the animator resource. {@link gt.edu.usac.ingenieria.ia.smartcarcontrol.R.animator#rotate_yaxis_0} |
     *                   {@link gt.edu.usac.ingenieria.ia.smartcarcontrol.R.animator#rotate_yaxis_180}
     */
    private void flipFab(int resourceId) {
        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(MainActivity.this, resourceId);
        set.setTarget(mFab);
        set.start();
    }

    /**
     * Changes {@link #mFab} icon
     * @param drawableId drawable resource id. {@link gt.edu.usac.ingenieria.ia.smartcarcontrol.R.drawable#ic_play_arrow_white_24dp} |
     *                   {@link gt.edu.usac.ingenieria.ia.smartcarcontrol.R.drawable#ic_stop_white_24dp}
     */
    private void changeFabIcon(final int drawableId) {
        new AsyncTask<Integer, Integer, Integer>() {
            @Override
            protected Integer doInBackground(Integer... integers) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                mFab.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, drawableId));
            }
        }.execute();
    }

    /**
     * Handles the response recived through the socket created by {@link #mSocketService}
     * @param response json string
     */
    private void serverResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("command")) { // we manully send a command

                int command = jsonObject.getInt("command");
                int resultCode = jsonObject.getInt("result_code");

                if (resultCode != 0) { // the command was not succesfully processed
                    Snackbar.make(mFab, jsonObject.has("message") ? jsonObject.getString("message") : "There was a server fault. Pleas try again later.", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                switch (command) {
                    case 1: // automatic mode command
                        mCar.setMode(Car.MODE_AUTOMATIC);
                        flipFab(R.animator.rotate_yaxis_180);
                        changeFabIcon(R.drawable.ic_stop_white_24dp);
                        break;
                    case 2: // manual mode command
                        mCar.setMode(Car.MODE_MANUAL);
                        flipFab(R.animator.rotate_yaxis_0);
                        changeFabIcon(R.drawable.ic_play_arrow_white_24dp);
                        break;
                }

            } else if (jsonObject.has("action")) { // the car took a move
                int action = jsonObject.getInt("action");
                if (action == Car.MOVE_FORWARD) {
                    mMazeFragment.draw(mCar.move());
                } else if(action == Car.MOVE_BACKWARD) {
                    mMazeFragment.draw(mCar.moveBack());
                }else {
                    mCar.turn(action);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows {@link ControlsFragment}
     */
    private void showControlsFragment() {
        MainActivity.this.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter, R.anim.exit)
                .replace(R.id.controls_containter, ControlsFragment.newInstance())
                .addToBackStack("Controls")
                .commit();

        int fabHeight = mFab.getHeight();
        int fabMargin = (int) getResources().getDimension(R.dimen.fab_margin);
        int controlsHeight = (int) getResources().getDimension(R.dimen.controls_heigth);

        setMargins(mFabWrapper, fabMargin, fabMargin, fabMargin, controlsHeight - fabHeight / 2);
    }

    /**
     * Hides {@link ControlsFragment}
     */
    private void hideControlsFragment() {
        MainActivity.this.getSupportFragmentManager().popBackStack();

        int fabMargin = (int) getResources().getDimension(R.dimen.fab_margin);
        setMargins(mFabWrapper, fabMargin, fabMargin, fabMargin, fabMargin);
    }

    /**
     * Sets the margin of any view instanceof {@link ViewGroup.MarginLayoutParams}
     * @param v the view
     * @param l left margin
     * @param t topo margin
     * @param r right margin
     * @param b bottom margin
     */
    private void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    /**
     * Sets the visibility fo the {@link #mLoadingProgressBar} animating it using
     * an animator.
     * @param visibility {@link View#VISIBLE} | {@link View#GONE}
     */
    private void setVisibilityLoadingProgressBarVisibility(int visibility) {
        int animatorId = visibility == View.VISIBLE ? R.animator.aparecer : R.animator.desaparecer;
        ;
        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(MainActivity.this, animatorId);
        set.setTarget(mLoadingProgressBar);
        set.start();
        mLoadingProgressBar.setVisibility(visibility);
    }

    /**
     * Result receiver that will be passed to {@link SocketService} to handle IPC.
     */
    @SuppressLint("ParcelCreator")
    private class MainActivityReciver extends ResultReceiver {

        public MainActivityReciver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            int action = resultData.getInt(SocketService.ACTION);

            if (resultCode == SocketService.SUCCESS) {
                switch (action) {
                    case SocketService.CONNECT:
                        connected();
                        break;
                    case SocketService.DISCONNECT:
                        disconnected();
                        break;
                    case SocketService.SERVER_RESPONSE:

                        if (mFabProgressBar.getVisibility() == View.VISIBLE) {
                            mFabProgressBar.setVisibility(View.INVISIBLE);
                        }

                        String response = resultData.getString(SocketService.RESULT_MESSAGE, null);
                        serverResponse(response);
                        break;
                }
            } else {
                if (action == SocketService.CONNECT || action == SocketService.DISCONNECT) {
                    setVisibilityLoadingProgressBarVisibility(View.GONE);
                } else if (action == SocketService.SEND) {
                    mFabProgressBar.setVisibility(View.INVISIBLE);
                }

                String errorMessage = resultData.getString(SocketService.RESULT_MESSAGE, "Error");
                Snackbar.make(mFab, errorMessage, errorMessage.length() > 45 ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * Class that extends from {@link android.view.GestureDetector.SimpleOnGestureListener}
     * to detect when the user flings up or down.
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (velocityY < 0 && !mIsControlFragmentVisible) { // Up
                showControlsFragment();
            } else if (velocityY > 0 && mIsControlFragmentVisible) { // down
                hideControlsFragment();
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}
