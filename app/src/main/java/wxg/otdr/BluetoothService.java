package wxg.otdr;

import android.app.Service;
import android.app.ActivityManager;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;

import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * This BluetoothService is designed to communicate and get data from OTDR device.
 * By WXG 25-Jan-2016
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class BluetoothService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_Disconnect = "BluetoothService.action.Disconnect";
    private static final String ACTION_Connection = "BluetoothService.action.Connection";
    private static final String ACTION_QueryBTMessages = "BluetoothService.action.ACTION_QueryBTMessages";
    private static final String ACTION_Init = "BluetoothService.action.Init";
    private static final String ACTION_Close = "BluetoothService.action.Close";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "wxg.otdr.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "wxg.otdr.extra.PARAM2";

    public static final String mStrDeviceName = "Device Name";
    public static final String mStrDeviceAdd = "Device Address";


    private final static String TAG = BluetoothService.class.getSimpleName();

    private static BluetoothManager mBluetoothManager = null;
    private static BluetoothAdapter mBluetoothAdapter = null;
    private static String mBluetoothDeviceAddress = "";
    private static BluetoothDevice mBluetoothDevice = null;
    private static BluetoothGatt mBluetoothGatt = null;

    // To be used as Service/Characteristics to manipulate.
    private static BluetoothGattService mBluetoothGattService = null;
    private static BluetoothGattCharacteristic mBluetoothGattCharacteristic = null;

    private List<BluetoothGattService> mListService = null;
    private List<BluetoothGattCharacteristic> mListChar = null;
    private List<BluetoothGattDescriptor> mListDesc = null;



    public final static String ACTION_GATT_CONNECTED =
            "wxg.otdr.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "wxg.otdr.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "wxg.otdr.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_FEEDBACK_AVAILABLE =
            "wxg.otdr.ACTION_FEEDBACK_AVAILABLE";
    public final static String ACTION_DATA_AVAILABLE =
            "wxg.otdr.ACTION_DATA_AVAILABLE";
    public final static String ACTION_DEBUG_DATA =
            "wxg.otdr.ACTION_DEBUG_DATA";
    public final static String EXTRA_DATA =
            "wxg.otdr.EXTRA_DATA";
    public final static String DEVICE_DOES_NOT_SUPPORT_UART =
            "wxg.otdr.DEVICE_DOES_NOT_SUPPORT_UART";
    public static final String RETURN_COMMAND = "wxg.otdr.extra.COMMAND";
    public static final String RETURN_DATA = "wxg.otdr.extra.DATA";
    public static final String DEBUG_COMMAND = "wxg.otdr.extra.DEBUG.DATA";


    public static final UUID TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");

    public static UUID RX_SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    public static UUID RX_CHAR_UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    public static UUID TX_CHAR_UUID = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");

    // Set a pre-compile conditions.
    public static boolean bInDebug = false;

    // For debug.
    public static final UUID TEST_TX_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID TEST_TX_CHAR_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public static final UUID TEST_Name_SERVICE_UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    public static final UUID TEST_Name_CHAR_UUID = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");

    private static GlobalData mGlobalData = null;

    // Special action for request BT status, max ten times.
    public static int miSendTimes = 0;
    public final static int MaxReadTimes = 3;// For specific id, read at most three times.
    public static int miReReadTimes = 0; // Reread times, important counting.
    public static byte[] bStatusCommand; // To record command for request BT status;
    public static boolean bQueryBTStatus = false;
    private static CountDownTimer WatchDog2 = null;
    public static String strBTStatus = "";

    private static int mConnectionState = BluetoothProfile.STATE_DISCONNECTED;

    //enum BTStatus {BT_Connecting, BT_Connected, BT_Disconnected};
   // private static BTStatus eBTStatus = BTStatus.BT_Disconnected;



    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;

            mGlobalData = new GlobalData();

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;

                // Update Global stauts.
                //final GlobalData Data = (GlobalData) getApplication();
                //Data.setBTStatus(GlobalData.BTStatus.BT_Connected);
                String StrDeviceName = gatt.getDevice().getName();
                String StrDeviceAdd = gatt.getDevice().getAddress();

                mConnectionState = BluetoothProfile.STATE_CONNECTED;
                broadcastUpdate(intentAction, StrDeviceName, StrDeviceAdd);
                //broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                if (mBluetoothGatt.discoverServices()){
                    Log.d(TAG, "Attempting to start service discovery:");
                }else{
                    Log.e(TAG, "Failed to start service discovery.");
                }
                if (mBluetoothGatt == null){
                    Log.e(TAG, "mBluetoothGatt is null.");
                }
                Log.i(TAG, "Attempting to start service discovery:");

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = BluetoothProfile.STATE_DISCONNECTED;

                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "onServicesDiscovered::mBluetoothGatt = " + mBluetoothGatt);

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                // Todo should be open.
                enableTXNotification();

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "onCharacteristicRead");
                broadcastUpdate(ACTION_FEEDBACK_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            Log.i(TAG, "onCharacteristicChanged");
        }
    };

    private void broadcastUpdate(final String action) {
        Log.i(TAG, "broadcastUpdate: final String action");

        final Intent intent = new Intent(action);
        //sendBroadcast(intent);
        MainActivity.getInstance().sendBroadcast(intent);

    }

    private void broadcastUpdate(final String action, String StrDeviceName, String StrDeviceAdd) {

        String StrLog = "broadcastUpdate:" + StrDeviceName + "; " + StrDeviceAdd;
        Log.i(TAG, StrLog);

        final Intent intent = new Intent(action);
        intent.putExtra(mStrDeviceName, StrDeviceName);
        intent.putExtra(mStrDeviceAdd, StrDeviceAdd);

        //sendBroadcast(intent);
        MainActivity.getInstance().sendBroadcast(intent);

    }


    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // just for debug, show in UI.
        byte[] bDebugValues = characteristic.getValue();
        GlobalData gData = new GlobalData();
        String strNewLog = "";
        int[] iDebugValues = gData.Byte2Int(bDebugValues);
        if (iDebugValues != null){
            for (int iTem = 0; iTem < iDebugValues.length; iTem ++){
                //String str = String.valueOf(txValue[iTem]);
                strNewLog += String.format("%02x ", iDebugValues[iTem]);
            }
        }
        //strNewLog += ";";
        Intent intent_Debug = new Intent(ACTION_DEBUG_DATA);
        intent_Debug.putExtra(DEBUG_COMMAND, strNewLog + ".");
        MainActivity.getInstance().sendBroadcast(intent_Debug);

        // This debug is for log saved in file. Collect all log in logcat.
        Log.d(TAG, strNewLog);

        if (TX_CHAR_UUID.equals(characteristic.getUuid())) {
            Log.d(TAG, "Received data modified: " + strNewLog);
            //GlobalData gData = new GlobalData();
            int iCommand_Type = 0;
            int[] iDatas_Return = null;
            byte[] bValues = characteristic.getValue();

            if (bValues != null){
                int[] iValues = gData.Byte2Int(bValues);

                if (iValues != null){
                    iCommand_Type = gData.getCommandTypeReturn(iValues);
                    // Be careful, every return except for BT status query, should use int[].
                    iDatas_Return = gData.getDataReturn(iValues);

                    if (iCommand_Type >= GlobalData.cCommand_SendMessageTimes &&
                            iCommand_Type <= GlobalData.cCommand_SendMessageTimes_Last){
                        // This case is special for query BT status, number of send times.

                        if (!BTMessage_CommandType(iCommand_Type, iDatas_Return)){
                            Log.e(TAG, "Analyze SendMessagetimes failed.");
                        }
                        return; // Message broadcast action executed in function BTMessage_CommandType.
                    }else{
                        // This is normal cases to send UI update to BluetoothReceiver.
                        intent.putExtra(RETURN_COMMAND, iCommand_Type);

                        if (iDatas_Return == null){
                            Log.d(TAG, "iData_Return is null.");
                            return;
                        }else{
                            intent.putExtra(RETURN_DATA, iDatas_Return);
                        }
                    }

                }else{
                    Log.d(TAG, "iValue is null.");
                    return;
                }
            }else{
                Log.d(TAG, "bValue is null.");
                return;
            }

        }

        //sendBroadcast(intent);
        MainActivity.getInstance().sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        Log.w(TAG, "mBluetoothGatt closed");
        mBluetoothDeviceAddress = "";
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public static int getBTConnectStatus(){
        if (mBluetoothManager == null || mBluetoothDevice == null) {
            Log.w(TAG, "mBluetoothManager or mBluetoothDevice not initialized");
            mConnectionState = BluetoothProfile.STATE_DISCONNECTED;

        }else{
            mConnectionState = mBluetoothManager.getConnectionState(mBluetoothDevice, BluetoothProfile.GATT);
        }

        //BluetoothProfile.STATE_CONNECTED, STATE_CONNECTING, STATE_DISCONNECTED, STATE_DISCONNECTING
        return mConnectionState;
    }

    public BluetoothService() {
        super("BluetoothService");

        if (!initialize()){
            Log.e(TAG, "Unable to initialize Bluetooth.");
        }

        if(!bInDebug){
            RX_SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
            RX_CHAR_UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
            TX_CHAR_UUID = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");
        }else{
            RX_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
            RX_CHAR_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
            TX_CHAR_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
        };
    }

    // int[] iValues only contain data, not include head0x68, length and command type.
    public boolean BTMessage_CommandType(int iCommandTpye, int[] iValues){
        boolean bCheck = false;

        if (iCommandTpye == GlobalData.cCommand_SendMessageTimes){

            // To avoid BT device send more than one message number to trigger the count loop.
            if (GlobalData.bFirst_ReceiveMessageNum){
                GlobalData.bFirst_ReceiveMessageNum = false;
                Log.i("TAG", "Receive SendMessageTimes for first times.");

            }else{
                Log.e("TAG", "Receive SendMessageTimes more than once, not handle.");
                return false;
            }

            GlobalData.iExpectCommandType_ErrorMessage = GlobalData.cCommand_SendMessageTimes;
            // Receive times of message and then call to send xx times to get messages.
            miSendTimes = iValues[GlobalData.cSendTimes_Index];

            // Send to update message number.
            Intent intent = new Intent(ACTION_DATA_AVAILABLE);
            intent.putExtra(RETURN_COMMAND, GlobalData.cCommand_SendMessageTimes);
            intent.putExtra(RETURN_DATA, miSendTimes);
            MainActivity.getInstance().sendBroadcast(intent);

            strBTStatus = ""; // A new query start. So clear strBTStatus.
            intent = new Intent(ACTION_DATA_AVAILABLE);
            intent.putExtra(RETURN_COMMAND, GlobalData.cCommand_SendMessageTimes_First);
            intent.putExtra(RETURN_DATA, strBTStatus);
            MainActivity.getInstance().sendBroadcast(intent);

            if (miSendTimes != 0){
                if (miSendTimes > GlobalData.iMaxMessageNum){
                    miSendTimes = GlobalData.iMaxMessageNum; // Only up to 10 messages could be shown.
                }
                // In this case, number of error message is not zero. Need to query and show.
                GlobalData.iExpectCommandType_ErrorMessage = GlobalData.cCommand_SendMessageTimes_First;
                ActionQueryBTMessages(getBaseContext(), GlobalData.iExpectCommandType_ErrorMessage); // to next thread to handle.
                //Looper.prepare();
                //handleActionQueryBTMessages(iExpectCommandType_ErrorMessage);
                //Looper.loop();
                //ActivateCounter(iExpectCommandType_ErrorMessage);
                miSendTimes --; // Query has taken one step.
            }else{
                // return error message number is zero. Not need to query.
                GlobalData.bWatchDog1_Protection = false; // Query BT send messages finish, end WatchDog1.
                MainActivity.getInstance().WatchDog1.cancel();
                GlobalData.bFirst_ReceiveMessageNum = true;
                GlobalData.iExpectCommandType_ErrorMessage = 0; // Reset expected Command Tpye.
                strBTStatus = "";
                BluetoothService.miSendTimes = 0;
                GlobalData.miReSendCount = 0;
                GlobalData.bCommand_Waiting = null;
            }
            bCheck = true;
        }else if (iCommandTpye == GlobalData.iExpectCommandType_ErrorMessage){
            //[GlobalData.cCommand_SendMessageTimes_First, GlobalData.cCommand_SendMessageTimes_Last]
            //int[] iData_Return = mGlobalData.getDataReturn(iValues);
            // In Java, int could not <<8, better *100.
            int iYear = iValues[GlobalData.cMessageYearHigh_Index]*100 +
                    iValues[GlobalData.cMessageYearLow_Index];
            int iMonth = iValues[GlobalData.cMessageMonth_Index];
            int iDay = iValues[GlobalData.cMessageDay_Index];
            int iHour = iValues[GlobalData.cMessageHour_Index];
            int iMinute = iValues[GlobalData.cMessageMinute_Index];
            int iSecond = iValues[GlobalData.cMessageSecond_Index];

            String strDate = String.format(iYear + "-" + mGlobalData.set02dMode(iMonth)
                    + "-" + mGlobalData.set02dMode(iDay));

            String strTime = String.format(mGlobalData.set02dMode(iHour) + ":" +
                    mGlobalData.set02dMode(iMinute) + ":" + mGlobalData.set02dMode(iSecond));

            String strTem = strDate + "   " + strTime + "\n";
            strBTStatus += strTem;
            strTem = "";

            // Clear pre-condition for re-read loop, max re-read 3 times.
            GlobalData.miReReadTimes = 0;
            GlobalData.bCommand_Waiting = null;
            /*if (WatchDog2 != null){
                WatchDog2.cancel();
                WatchDog2 = null;
                Log.d(TAG, "Get expected status message, cancel WatchDog2.");
            }*/

            if ( miSendTimes >= 1){
                // >0, still need continue to query.
                GlobalData.iExpectCommandType_ErrorMessage ++;
                ActionQueryBTMessages(getBaseContext(), GlobalData.iExpectCommandType_ErrorMessage); // to next thread to handle.

                miSendTimes --;

                bCheck = true;
            }else if(miSendTimes == 0){
                // All query has finished.
                // Two cases：1， no error message, miSendTimes = 0 and strBTStatus = "".
                // 2, miSendTimes != 0 ; strBTStatus will be updated to UI.
                GlobalData.bWatchDog1_Protection = false; // Query BT send messages finish, end WatchDog1.
                GlobalData.bFirst_ReceiveMessageNum = true;

                Intent intent2 = new Intent(ACTION_DATA_AVAILABLE);
                intent2.putExtra(RETURN_COMMAND, GlobalData.cCommand_SendMessageTimes_First);
                intent2.putExtra(RETURN_DATA, strBTStatus);
                //sendBroadcast(intent);
                MainActivity.getInstance().sendBroadcast(intent2);

                MainActivity.getInstance().WatchDog1.cancel();
                GlobalData.iExpectCommandType_ErrorMessage = 0; // Reset expected Command Tpye. Important for protection logic.
                BluetoothService.miSendTimes = 0;
                GlobalData.miReSendCount = 0;
                strBTStatus = "";
                bCheck = true;
            }

        }else{
            Log.d(TAG, "Unexpected ID send up data, probably one ID send up data more than one time.");
        }

        return bCheck;
    }


    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) MainActivity.getInstance().getSystemService(Context.BLUETOOTH_SERVICE);

            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    public static void  startActionInit(Context context) {
        Intent intent = new Intent(context, BluetoothService.class);
        intent.setAction(ACTION_Init);
        context.startService(intent);
    }

    public static void  startActionClose(Context context) {
        Intent intent = new Intent(context, BluetoothService.class);
        intent.setAction(ACTION_Close);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void  startActionDisconnect(Context context) {

        // To avoid quick operation in more thread to cause lost frame, I change it to static call.
        /*Intent intent = new Intent(context, BluetoothService.class);
        //intent.setAction(ACTION_Disconnect);

        //context.startService(intent);

        */
        handleActionDisconnect();
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionConnection(Context context, String DeviceName, String Address) {
        Intent intent = new Intent(context, BluetoothService.class);
        intent.setAction(ACTION_Connection);
        intent.putExtra(EXTRA_PARAM1, DeviceName);
        intent.putExtra(EXTRA_PARAM2, Address);
        context.startService(intent);
    }

    public static void ActionQueryBTMessages(Context context, int iPara) {
        Intent intent = new Intent(context, BluetoothService.class);
        intent.setAction(ACTION_QueryBTMessages);
        intent.putExtra(EXTRA_PARAM1, iPara);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_Disconnect.equals(action)) {
                handleActionDisconnect();
            } else if (ACTION_Connection.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionConnection(param1, param2);
            }else if (ACTION_QueryBTMessages.equals(action)){
                int iDefault = 10;
                int iParam = intent.getIntExtra(EXTRA_PARAM1, iDefault);

                handleActionQueryBTMessages(iParam);
            }else if (ACTION_Init.equals(action)) {
                initialize();
            }else if (ACTION_Close.equals(action)) {
                close();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private static void handleActionDisconnect() {
        // TODO: Handle action handleActionDisconnect
        Log.i(TAG, "handleActionDisconnect");

        if (mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (mBluetoothGatt == null) {
            Log.w(TAG, "mBluetoothGatt not initialized");
            return;
        }

        // Important, to avoid multiple Gatt.
        //mBluetoothGatt.disconnect();
        mBluetoothGatt.close();

        mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
        Log.i(TAG, "Close GATT server.");
        Intent intent = new Intent(ACTION_GATT_DISCONNECTED);
        MainActivity.getInstance().sendBroadcast(intent);


        mBluetoothDeviceAddress = "";
        mBluetoothDevice = null;

    }

    /**
     * Handle action Connection in the provided background thread with the provided
     * parameters.
     */
    private void handleActionConnection(String DeviceName, String Address) {
        // TODO: Handle action Connection.
        //throw new UnsupportedOperationException("Not yet implemented");
        Log.i(TAG, "handleActionConnection");

        if (mBluetoothAdapter == null || Address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return;
        }

        // Previously connected device.  Try to reconnect.
        Log.d(TAG, "Check whether old connection existed.");
        if (mBluetoothDeviceAddress != null && Address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = BluetoothProfile.STATE_CONNECTING;
                return;
            }
        }

        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(Address);
        if (mBluetoothDevice == null) {
            Log.d(TAG, "Device not found.  Unable to connect.");
            return;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothGatt = mBluetoothDevice.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = Address;
        mConnectionState = BluetoothProfile.STATE_CONNECTING;
    }


    // A special action to query BT messages, max for ten times.
    private void handleActionQueryBTMessages(int iCommandType){

        boolean bCheck = false;

        int iBTTem = getBTConnectStatus();
        GlobalData mGlobalData = new GlobalData();

        bStatusCommand = mGlobalData.getCommand(GlobalData.eCommandIndex.eRequestMessageTimes);
        int[] iValues = mGlobalData.getIntReturn(bStatusCommand);// {0x68, 0x01, 0x41}
        // {0x68, 0x01, 0x41}...{0x68, 0x01, 0x4a}
        iValues[GlobalData.cSetMessageTimes_Index] = iCommandType;
        bStatusCommand = mGlobalData.Int2Byte(iValues);
        Log.i(TAG, "Data to send: " + mGlobalData.Int2String(iValues));
        bCheck = writeRXCharacteristic(bStatusCommand);

        // Reset counter for resend.
        // Used at MainActivity, WatchDog1, ontick 1000 ms to check whether need resend.
        GlobalData.miReSendCount = 0;

        if (!bCheck) {
            Log.e(TAG, "Query BT status, DATA send failed: " + mGlobalData.Int2String(iValues));
        }

        // This thread only query every ID for one time.
        // Now record info to check for more 3 times in MainActivity WatchDog1.
        GlobalData.bCommand_Waiting = bStatusCommand;
        GlobalData.miReReadTimes = GlobalData.MaxReadTimes;
        GlobalData.miIntervalSecond = Calendar.getInstance().get(Calendar.SECOND);
        Log.d(TAG, "BT Status Query. TimeStamp in Service: " + String.format("%d", GlobalData.miIntervalSecond));
/*
        for (int iTem = 0; iTem < iTimes; iTem++){

            bCommand = mGlobalData.Int2Byte(iValues);

            Log.e(TAG, "Data to send: " + mGlobalData.Int2String(iValues));
            bCheck = writeRXCharacteristic(bCommand);
            try {
                Thread.currentThread().sleep(1000);//阻断1000ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String strLog = "Send Query status message" + String.format("%02d", iTem) +
                    ": writeRXCharacteristic failed.";

            if (!bCheck){
                Log.e(TAG, strLog);

                return;
            }else{

            }

        }*/
    }

    public boolean AssignGATTService(UUID UID){
        Log.d(TAG, "call AssignGATTService.");
        boolean bReturn = false;

        if (mBluetoothGatt != null) {
            mBluetoothGattService = mBluetoothGatt.getService(UID);
            if (mBluetoothGattService != null) {
                Log.d(TAG, "AssignGATTService::service found: " + UID.toString());
                //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
                bReturn = true;
            }else{
                Log.d(TAG, "AssignGATTService::service found: " + UID.toString());
                bReturn = false;
            }
        }else{
            Log.d(TAG, "mBluetoothGatt is not connected");

            bReturn = false;
        }

        return bReturn;
    }

    public boolean AssignGATTCharacteristics(UUID UID){
        Log.d(TAG, "call AssignGATTCharacteristics.");
        boolean bReturn = false;

        if (mBluetoothGattService != null) {
            mBluetoothGattCharacteristic = mBluetoothGattService.getCharacteristic(UID);
            if (mBluetoothGattCharacteristic != null) {
                Log.d(TAG, "AssignGATTService::Characteristic found: " + UID.toString());
                //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
                bReturn = true;
            }else{
                Log.d(TAG, "AssignGATTService::Characteristic not found: " + UID.toString());
                //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
                bReturn = false;
            }
        }else{
            Log.d(TAG, "mBluetoothGatt is not connected");

            bReturn = false;
        }

        return bReturn;
    }


    /**
     * Enable TXNotification
     *
     * @return
     */
    public void enableTXNotification()
    {
        Log.i(TAG, "call enableTXNotification.");

        String str1 = "enableTXNotification: ";
        String str2 = "failed";

        if (mBluetoothGatt == null) {
            Log.e(TAG, "enableTXNotification::mBluetoothGatt is null.");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            broadcastUpdate(ACTION_FEEDBACK_AVAILABLE, str1, str2);
            return;
        }

        // To collect all services info, for debug.
        // Closed.
        /*
        if (getAllServicesInfo() == true){
            Log.d(TAG, "call getAllServicesInfo successfully.");
        }else{
            Log.d(TAG, "call getAllServicesInfo failed.");
        }
        */

        BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        if (RxService == null) {
            Log.d(TAG, "enableTXNotification::Rx service not found!");

            broadcastUpdate(ACTION_FEEDBACK_AVAILABLE, str1, str2);
            return;
        }

        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            Log.d(TAG, "Tx charateristic not found!");

            broadcastUpdate(ACTION_FEEDBACK_AVAILABLE, str1, str2);
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar, true);

        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
        Log.i(TAG, "call enableTXNotification executed.");

        str2 = "successfully";
        broadcastUpdate(ACTION_FEEDBACK_AVAILABLE, str1, str2);

    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     */
    public boolean readCharacteristic() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return false;
        }

        if (mBluetoothGattCharacteristic == null) {
            Log.e(TAG, "mBluetoothGattCharacteristic is null");
            return false;
        }

        if (mBluetoothGatt.readCharacteristic(mBluetoothGattCharacteristic)){
            Log.d(TAG, "readCharacteristic successfully:" + mBluetoothGattCharacteristic.getUuid().toString());
            return true;
        }else {
            Log.e(TAG, "readCharacteristic failed:" + mBluetoothGattCharacteristic.getUuid().toString());
            return false;
        }

    }

    public boolean writeRXCharacteristic(byte[] strValue)
    {
        Log.d(TAG, "writeRXCharacteristic: " + strValue);

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return false;
        }else{
            Log.d(TAG, "mBluetoothAdapter is: " + mBluetoothAdapter.toString());
            Log.d(TAG, "mBluetoothGatt is: " + mBluetoothGatt.toString());
        }

        int iBTStatus = getBTConnectStatus();

        if (iBTStatus != BluetoothProfile.STATE_CONNECTED){
            Log.d(TAG, "BTConnectStatus is not connected.");
        }

        if (mBluetoothGattCharacteristic == null) {
            Log.e(TAG, "mBluetoothGattCharacteristic is null");
            return false;
        }else{
            Log.d(TAG, "mBluetoothGattCharacteristic is: " + mBluetoothGattCharacteristic.getUuid().toString());
        }

        mBluetoothGattCharacteristic.setValue(strValue);

        if (mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic)){
            Log.d(TAG, "writeRXCharacteristic successfully:" + mBluetoothGattCharacteristic.getUuid().toString());

            return true;
        }else {
            Log.e(TAG, "writeRXCharacteristic failed:" + mBluetoothGattCharacteristic.getUuid().toString());
            return false;
        }

    }



    // This function is called to collected all services and corresponding Characteristics.
    // Then output to a file name "A_GATTServiceInfo.txt".
    // This function is for debug and scan BLE devices.
    public boolean getAllServicesInfo(){

        mListService = mBluetoothGatt.getServices();
        int iServiceNum = 0;
        int iCharNum = 0;
        int iDescNum = 0;
        int iTemService = 0;
        int iTemChar = 0;
        int iTemDesc = 0;

        if (mListService == null){
            Log.d(TAG, "listService is null");
        }else{
            iServiceNum = mListService.size();
            mListService.get(0).getCharacteristics();
        }


        BluetoothGattService temGATTService = null;
        BluetoothGattCharacteristic temGATTChara = null;
        BluetoothGattDescriptor temGATTDesc = null;

        String strFileName = "A_GATTServiceInfo.txt";
        String strLog = "Dump all Gatt Service info as below: \n";

        File newxmlfile = new File(Environment.getExternalStorageDirectory(), strFileName);
        try{
            if(!newxmlfile.exists())
                newxmlfile.createNewFile();
        }catch(IOException e){
            Log.e("IOException", "exception in createNewFile() method");
        }
        //we have to bind the new file with a FileOutputStream
        FileOutputStream fileos = null;
        try{
            fileos = new FileOutputStream(newxmlfile);
        }catch(FileNotFoundException e){
            Log.e("FileNotFoundException", "can't create FileOutputStream");
        }

        XmlSerializer serializer = Xml.newSerializer();

        try {
            //we set the FileOutputStream as output for the serializer, using UTF-8 encoding
            serializer.setOutput(fileos, "UTF-8");
            //Write <?xml declaration with encoding (if encoding not null) and standalone flag (if standalone not null)
            serializer.startDocument(null, Boolean.valueOf(true));
            //set indentation option
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            //start a tag called "root"
            serializer.startTag(null, "Gatt Service info");


            List<XMLItem> ServiceList = new ArrayList<XMLItem>();


            for (iTemService = 0; iTemService < iServiceNum; iTemService++){
                temGATTService = mListService.get(iTemService);
                String ServiceeUUID = temGATTService.getUuid().toString();
                XMLItem ServiceItem = new XMLItem("Service" + iTemService, ServiceeUUID);

                serializer.startTag(null, ServiceItem.getName());
                serializer.text(ServiceItem.getData());
                serializer.text("\n\t\t");


                // Serial Characteristics info.
                mListChar = temGATTService.getCharacteristics();
                iCharNum = mListChar.size();

                for(iTemChar = 0; iTemChar < iCharNum; iTemChar++){
                    temGATTChara = mListChar.get(iTemChar);
                    String CharUUID = temGATTChara.getUuid().toString();
                    XMLItem CharItem = new XMLItem("Characteristcs" + iTemChar, CharUUID);
                    serializer.startTag(null, CharItem.getName());
                    serializer.text(CharItem.getData());
                    serializer.endTag(null, CharItem.getName());
                    serializer.text("\n\t\t\t\t");

                    String CharProperties = String.valueOf(temGATTChara.getProperties());
                    XMLItem CharPropItem = new XMLItem("Char " + iTemChar + "Properties", CharProperties);
                    serializer.startTag(null, CharPropItem.getName());
                    serializer.text(CharPropItem.getData());
                    serializer.endTag(null, CharPropItem.getName());
                    serializer.text("\n\t\t\t\t");

                    String CharWritetype = String.valueOf(temGATTChara.getWriteType());
                    XMLItem CharWritetypeItem = new XMLItem("Char " + iTemChar + "Write Type", CharWritetype);
                    serializer.startTag(null, CharWritetypeItem.getName());
                    serializer.text(CharWritetypeItem.getData());
                    serializer.endTag(null, CharWritetypeItem.getName());
                    serializer.text("\n");

                    mListDesc = temGATTChara.getDescriptors();
                    iDescNum = mListDesc.size();
                    for(iTemDesc = 0; iTemDesc < iDescNum; iTemDesc++) {
                        temGATTDesc = mListDesc.get(iTemDesc);
                        String DescUUID = temGATTDesc.getUuid().toString();
                        XMLItem DescItem = new XMLItem("DescItem" + iTemDesc, DescUUID);
                        serializer.startTag(null, DescItem.getName());
                        serializer.text(DescItem.getData());
                        serializer.endTag(null, DescItem.getName());
                        serializer.text("\n\t\t\t\t\t\t");
                    }

                }
                serializer.endTag(null, ServiceItem.getName());
            }

            serializer.endTag(null, "Gatt Service info");
            serializer.endDocument();
            //write xml data into the FileOutputStream
            serializer.flush();
            //finally we close the file stream
            fileos.close();
        } catch (Exception e) {
            Log.e("Exception", "error occurred while creating xml file");
        }
        return true;
    }

    public static class XMLItem {
        //id
        private String mName;
        //Data
        private String mData;
        public XMLItem(String Name, String Data) {
            mName = Name;
            mData = Data;
        }
        public String getName(){
            return mName;
        }
        public String getData (){
            return mData;
        }
        public void setName(String sName){
            mName = sName;
        }
        public void setData (String sData){
            mData = sData;
        }

    }

}
