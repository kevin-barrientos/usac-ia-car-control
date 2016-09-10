package gt.edu.usac.ingenieria.ia.smartcarcontrol;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements ControlsFragment.OnControlFragmentInteraction {


    private final Car mCar = new Car();
    private MazeFragment mMazeFragment;
    private SocketService mSocketService;
    private Boolean mBound = false;

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
        if (mBound) {
            // Call a method from the LocalService.
            // However, if this call were something that might hang, then this request should
            // occur in a separate thread to avoid slowing down the activity performance.
//            int num = mSocketService.getRandomNumber();
            mSocketService.connect();
//            Snackbar.make(fab, "number: " + num, Snackbar.LENGTH_LONG).setAction("Action", null).show();
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

    @SuppressLint("ParcelCreator")
    public class MyReciver extends ResultReceiver{

        public MyReciver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            switch (resultCode){
                case SocketService.SUCCESS:
                    Toast.makeText(MainActivity.this, resultData.getString(SocketService.RESULT_MESSAGE), Toast.LENGTH_SHORT).show();
                    break;
                case SocketService.ERROR:
                    Toast.makeText(MainActivity.this, resultData.getString(SocketService.RESULT_MESSAGE), Toast.LENGTH_SHORT).show();
                    break;
                case SocketService.SERVER_RESPONSE:
                    String response = resultData.getString(SocketService.SERVER_RESPONSE_MESSAGE, null);
                    try{
                        int direction = -1;
                        direction = Integer.parseInt(response);
                        if(direction == ControlsFragment.MOVE){
                            mMazeFragment.draw(mCar.move());
                        }else{
                            mCar.turn(direction);
                        }
                    }catch (NumberFormatException e){
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    Toast.makeText(MainActivity.this, "The process returned an unrecognized result code (" + resultCode + ")", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    }
}
