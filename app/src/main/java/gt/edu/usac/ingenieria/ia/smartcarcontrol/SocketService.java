package gt.edu.usac.ingenieria.ia.smartcarcontrol;

import android.app.Service;
import android.content.Intent;
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

/**
 * It is a bound service which handles the comunication with a
 * web socket server. The services needs a {@link ResultReceiver}
 * to deliver the result to the client.
 *
 * Each result has an action code, result code and result message.
 * @see SocketService#setResultReceiver(ResultReceiver)
 * @see SocketService#sendResponseToClient(int, int, String)
 */
public class SocketService extends Service {

    // ACTIONS
    public static final int CONNECT = 1;
    public static final int DISCONNECT = 2;
    public static final int SEND = 3;
    public static final int SERVER_RESPONSE = 4;

    // KEYS
    public static final String RESULT_MESSAGE = "gt.edu.usac.ingenieria.ia.RESULT_MESSAGE";
    public static final String ACTION = "gt.edu.usac.ingenieria.ia.ACTION";

    // RESULT CODES
    public static final int SUCCESS = 0;
    public static final int ERROR = -1;
    public static final int ERROR_SOCKET_NOT_CONNECTED = -2;

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
            sendResponseToClient(CONNECT, SUCCESS, "connected :D!");

        } catch (IOException e) {
            sendResponseToClient(CONNECT, ERROR, e.getMessage());
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
        if(mSocket != null && !mSocket.isClosed()){
            try {
                mSocket.close();
                message = "Connection is closed.";
            } catch (IOException e) {
                resultCode = ERROR;
                message = e.getMessage();
            }
        }
        sendResponseToClient(DISCONNECT, resultCode, message);
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
        }else{
            sendResponseToClient(SEND, ERROR_SOCKET_NOT_CONNECTED, "Phone is not connected, please stablish a connection firt and try again.");
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
                    if((message = mInputSocketReader.readLine()) != null){
                        sendResponseToClient(SERVER_RESPONSE, SUCCESS, message);
                    }
                } catch (IOException e) {
                    sendResponseToClient(SERVER_RESPONSE, ERROR, e.getMessage());
                }
            }
        }
    }

    /**
     * Sends a response to the client through a {@link ResultReceiver}
     * @param action the action that was handled. {@link #CONNECT} | {@link #DISCONNECT} | {@link #SEND} | {@link #SERVER_RESPONSE}
     * @param resultCode the result code for the handled action {@link #ERROR} | {@link #ERROR_SOCKET_NOT_CONNECTED}
     * @param resultMessage the result message of the action
     */
    private void sendResponseToClient(int action, int resultCode, String resultMessage){
        if(mResultReciver != null){
            Bundle result = new Bundle();
            result.putInt(ACTION, action);
            result.putString(RESULT_MESSAGE, resultMessage);
            mResultReciver.send(resultCode, result);
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
