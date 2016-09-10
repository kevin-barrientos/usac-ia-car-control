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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements ControlsFragment.OnControlFragmentInteraction {


    private final Car mCar = new Car();
    private MazeFragment mMazeFragment;
    private SocketService mSocketService;
    private Boolean mBound = false;
    private Boolean mSocketConnected = false;

    @BindView(R.id.progressbar)
    ProgressBar mProgressBar;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SocketService.LocalBinder binder = (SocketService.LocalBinder) service;
            mSocketService = binder.getService();
            mSocketService.setResultReceiver(new MyReciver(new Handler()));
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMazeFragment = (MazeFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_maze);

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
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.fab)
    public void onFabClicked(View fab){
        if (!mBound) {
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);
        if(!mSocketConnected){
            mSocketService.connect();
        }else{
            mSocketService.closeConnection();
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
        mSocketService.sendMessage(direction);

    }

    private void connected(){
        mSocketConnected = true;
        mProgressBar.setVisibility(View.INVISIBLE);
        flipFab(R.animator.rotate_yaxis_180);
        changeFabIcon(R.drawable.ic_cloud_off_white_24dp);
    }

    private void disconnected(){
        mSocketConnected = false;
        mProgressBar.setVisibility(View.INVISIBLE);
        flipFab(R.animator.rotate_yaxis_0);
        changeFabIcon(R.drawable.ic_cloud_done_white_24dp);
    }

    private void flipFab(int resourceId){
        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(MainActivity.this, resourceId);
        set.setTarget(mFab);
        set.start();
    }

    private void changeFabIcon(final int drawableId){
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

    private void serverResponse(String response){
        try{
            int direction = -1;
            direction = Integer.parseInt(response);
            if(direction == ControlsFragment.MOVE){
                mMazeFragment.draw(mCar.move());
            }else{
                mCar.turn(direction);
            }
        }catch (NumberFormatException e){
            // do nothing
        }
    }

    @SuppressLint("ParcelCreator")
    public class MyReciver extends ResultReceiver{

        public MyReciver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            int action = resultData.getInt(SocketService.ACTION);

            if(resultCode == SocketService.SUCCESS){
                switch (action){
                    case SocketService.CONNECT:
                        connected();
                        break;
                    case SocketService.DISCONNECT:
                        disconnected();
                        break;
                    case SocketService.SERVER_RESPONSE:
                        String response = resultData.getString(SocketService.RESULT_MESSAGE, null);
                        serverResponse(response);
                        break;
                }
            }else {
                if(action == SocketService.CONNECT || action == SocketService.DISCONNECT){
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
                Snackbar.make(mFab, resultData.getString(SocketService.RESULT_MESSAGE, "Error"), Snackbar.LENGTH_SHORT).show();
            }
        }

    }
}
