package gt.edu.usac.ingenieria.ia.smartcarcontrol;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketService extends Service {

    // ACTIONS
    private static final int CONNECT = 1;
    private static final int DISCONNECT = 2;
    private static final int SEND = 3;

    // KEYS
    public static final String RESULT_MESSAGE = "gt.edu.usac.ingenieria.ia.RESULT_MESSAGE";
    public static final String SERVER_RESPONSE_MESSAGE = "gt.edu.usac.ingenieria.ia.SERVER_RESPONSE_MESSAGE";

    // RESULT CODES
    public static final int SUCCESS = 0;
    public static final int ERROR = -1;
    public static final int SERVER_RESPONSE = 1;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    //Client result reciver
    private ResultReceiver mResultReciver = null;
    private Handler mSendServiceHandler;
    private Socket mSocket;
    private BufferedReader mInputSocketReader;
    private PrintWriter mOutputSocketWriter;

    /**
     * Sets the Client's ResultReciver that will be use to update its Activity/Fragment
     * @param myReciver the receiver
     */
    public void setResultReceiver(ResultReceiver myReciver) {
        this.mResultReciver = myReciver;
    }

    /**
     * Issues a message to start a connection
     */
    public void connect(){
        Message.obtain(mSendServiceHandler, CONNECT).sendToTarget();
    }

    /**
     * Tries to connect to the server through a socket.
     */
    private void handleConnect(){
        String serverIp = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_server_ip_key), "192.168.43.1");
        int port;
        try{
            port = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_server_port_key), "9898"));
        }catch (NumberFormatException e){
            port = 9898;
        }

        Bundle result;

        try {
            mSocket = new Socket();
            mSocket.connect(new InetSocketAddress(serverIp, port), 7000);

            // register the inputstream
            mInputSocketReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

            // register the outpustStream
            mOutputSocketWriter = new PrintWriter(mSocket.getOutputStream(), true);

            // start worker thread to listen the socket
            ReciveWorkerThread mReciveWorkerThread = new ReciveWorkerThread();
            mReciveWorkerThread.start();

            // send connection succesful message
            result = new Bundle();
            result.putString(RESULT_MESSAGE, "connected :D!");
            if(mResultReciver != null)
                mResultReciver.send(SUCCESS, result);

        } catch (IOException e) {
            result = new Bundle();
            result.putString(RESULT_MESSAGE, e.getMessage());
            if(mResultReciver != null)
                mResultReciver.send(ERROR, result);
        }

    }

    /**
     * Issues a message to close the socket's connection.
     */
    public void closeConnection(){
        Message.obtain(mSendServiceHandler, DISCONNECT).sendToTarget();
    }

    /**
     * Closes the socket connection.
     */
    private void handleCloseConnection(){
        int resultCode = SUCCESS;
        String message = "";
        Bundle result = new Bundle();
        if(mSocket != null && !mSocket.isClosed()){
            try {
                mSocket.close();
                message = "Connection is closed.";
            } catch (IOException e) {
                resultCode = ERROR;
                message = e.getMessage();
            }
        }
        result.putString(RESULT_MESSAGE, message);
        mResultReciver.send(resultCode, result);
    }

    /**
     * Issues a message to send a message to the server.
     * @param command command number. See the server's implementation for a fult set of valid command numbers
     */
    public void sendMessage(int command){
        Message.obtain(mSendServiceHandler, SEND, command, 0).sendToTarget();
    }

    /**
     * Sends a message through the socket
     * @param command a command
     */
    private void handleSendMessage(int command){
        if(mSocket != null && mSocket.isConnected()){
            mOutputSocketWriter.println(String.valueOf(command));
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        // Return this instance of LocalService so clients can call public methods
        SocketService getService() {
            return SocketService.this;
        }

    }

    /**
     * Class that handles messages to perform the different accions.
     */
    public class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            if(mResultReciver == null)
                return;

            switch (msg.what){
                case CONNECT:
                    handleConnect();
                    break;
                case DISCONNECT:
                    handleCloseConnection();
                    break;
                case SEND:
                    handleSendMessage(msg.arg1);
                    break;
                default:
                    Bundle result = new Bundle();
                    result.putString(RESULT_MESSAGE, "Action not recognized :(");
                    mResultReciver.send(-1, result);
                    break;
            }
        }

    }

    /**
     * Worker thread that litens for income data on Socket InputStream ({@link #mInputSocketReader})
     */
    public class ReciveWorkerThread extends Thread{
        @Override
        public void run() {
            String message;
            Bundle result;
            while (mSocket.isConnected()){
                try {
                    if((message = mInputSocketReader.readLine()) != null && mResultReciver != null){
                        result = new Bundle();
                        result.putString(SERVER_RESPONSE_MESSAGE, message);
                        mResultReciver.send(SERVER_RESPONSE, result);
                    }
                } catch (IOException e) {
                    if(mResultReciver != null){
                        result = new Bundle();
                        result.putString(RESULT_MESSAGE, e.getMessage());
                        mResultReciver.send(ERROR, result);
                    }
                }
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Start worker thread
        HandlerThread thread = new HandlerThread("Socket Thread");
        thread.start();
        mSendServiceHandler = new ServiceHandler(thread.getLooper());

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {

            mResultReciver = null;

            if(mSocket != null)
                mSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
