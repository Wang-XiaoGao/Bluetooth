package wxg.otdr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Date;


/**
 * This Receiver only support BLE.
 * Created by Administrator on 2016/10/11.
 */
public class BluetoothReceiver extends BroadcastReceiver {
    private final static String TAG = BluetoothReceiver.class.getSimpleName();
    private static TextView mDebugTextVew = null;
    // Just for debug, since debug textview in Tab0, if you change to Tab2, setText will collapse.
    private static int iTabSelection = -1;

    public BluetoothReceiver(){
        String strFileName = "A_BlueToothApp_Log.txt";

        String filePath = "/A_BlueToothApp/";

        File newLogFolder = new File(Environment.getExternalStorageDirectory(), filePath);
        boolean bCheck = false;
        if(!newLogFolder.exists()){
            bCheck = newLogFolder.mkdir();
            if (!bCheck){
                Log.e(TAG, "Create log folder failed.");
            }else
                Log.i(TAG, "Create log folder successfully.");
        }


        File newLogFile = new File(newLogFolder.getPath(), strFileName);
        // First to check whether disk space is enough for 5Mb (GlobalData.lLogFileVolume);
        // Second to decide whether GlobalData.bLogFileMode
        try{
            long lUsableSpace = newLogFile.getUsableSpace();
            String strTemp = String.valueOf(lUsableSpace);
            Log.i(TAG, "lUsableSpace is " + strTemp);

            if (lUsableSpace <= GlobalData.lLogFileVolume) {
                GlobalData.bLogFileMode = false;
                MainActivity.getInstance().ShowInfo2User(
                        "系统剩余空间不足！APK运行信息无法记录！", Toast.LENGTH_LONG);
                Log.e(TAG, "系统剩余空间不足！APK运行信息无法记录！");
            }else{
                if (!newLogFile.exists()) {
                    GlobalData.bLogFileMode = true;
                    bCheck = newLogFile.createNewFile();
                    if (!bCheck) {
                        Log.e(TAG, "Create log file failed.");
                    } else
                        Log.i(TAG, "Create log file successfully.");
                } else {
                    long lFileVolume = newLogFile.length();

                    strTemp = String.valueOf(lFileVolume);
                    Log.i(TAG, "lFileVolume is " + strTemp);
                    // If Log file is bigger than GlobalData.lLogFileVolume (5 Mb), renew.
                    if (lFileVolume > GlobalData.lLogFileVolume) {
                        bCheck = newLogFile.delete();
                        if (!bCheck) {
                            Log.e(TAG, "Delete log file failed.");
                        } else
                            Log.i(TAG, "Delete log file successfully.");

                        bCheck = newLogFile.createNewFile();
                        if (!bCheck) {
                            Log.e(TAG, "Create log file failed.");
                        } else
                            Log.i(TAG, "Create log file successfully.");
                    }


                }
            }
        }catch(IOException e){
            Log.e("IOException", "exception in createNewFile() method");
        }

        //we have to bind the new file with a FileOutputStream
        FileOutputStream fileos = null;
        try{
            fileos = new FileOutputStream(newLogFile);
        }catch(FileNotFoundException e){
            Log.e("FileNotFoundException", "can't create FileOutputStream");
        }

        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log=new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
            }

            fileos.write(log.toString().getBytes());

        } catch (IOException e) {
        }


    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Log.i(TAG, "BluetoothReceiver::onReceive");

        // Just for debug, since debug textview in Tab0, if you change to Tab2, setText will collapse.
        iTabSelection = MainActivity.mtabLayout.getSelectedTabPosition();

        String strAction = intent.getAction();

        final Intent mIntent = intent;

        TextView mTextVew = (TextView) MainActivity.getInstance().findViewById(R.id.id_MakeSure_Connection);
        ImageButton mImageBT = (ImageButton) MainActivity.getInstance().findViewById(R.id.imageViewBT);
        ImageButton mImageDeviceCheck = (ImageButton) MainActivity.getInstance().findViewById(R.id.id_SelfCheck_Image);
        ImageButton mImageBattery = (ImageButton) MainActivity.getInstance().findViewById(R.id.id_Battery_Image);
        TextView mBatteryValue = (TextView) MainActivity.getInstance().findViewById(R.id.id_Battery_Remain_Value);
        TextView mMaterialView = (TextView) MainActivity.getInstance().findViewById(R.id.id_MaterialNum_Value);
        TextView mVoltageView = (TextView) MainActivity.getInstance().findViewById(R.id.id_Voltage_Value);
        TextView mPressureView = (TextView) MainActivity.getInstance().findViewById(R.id.id_Pressure_Value);
        TextView mFrequencyText = (TextView) MainActivity.getInstance().findViewById(R.id.id_Frequency_Value);
        TextView mWaveLengthText = (TextView) MainActivity.getInstance().findViewById(R.id.id_WaveLength_Value);
        TextView mCycleText = (TextView) MainActivity.getInstance().findViewById(R.id.id_Cycle_Value);
        TextView mAmplitudeText = (TextView) MainActivity.getInstance().findViewById(R.id.id_Amplitude_Value);
        EditText mDateText = (EditText) MainActivity.getInstance().findViewById(R.id.id_Edit_Date);
        EditText mTimeText = (EditText) MainActivity.getInstance().findViewById(R.id.id_Edit_Time);
        EditText mSendTimesText = (EditText) MainActivity.getInstance().findViewById(R.id.id_SendTimes_Message);
        EditText mMessagesText = (EditText) MainActivity.getInstance().findViewById(R.id.id_MessageNumberText_Value);




        // Todo, this test view is for debug.
        mDebugTextVew = (TextView) MainActivity.getInstance().findViewById(R.id.id_DebugText_View);



        //*********************//
        if (strAction.equals(BluetoothService.ACTION_GATT_CONNECTED)) {
            Log.d(TAG, "ACTION_GATT_CONNECTED");

            // if it's not for connection action, the below two string may not be initiated.
            GlobalData.StrDeviceName = intent.getStringExtra(BluetoothService.mStrDeviceName);
            GlobalData.StrDeviceAdd = intent.getStringExtra(BluetoothService.mStrDeviceAdd);
            String StrShowText = GlobalData.StrDeviceName + "\n" + GlobalData.StrDeviceAdd;

            Log.i(TAG, StrShowText);


            GlobalData.strLog = "BT Connected.";
            String strTemp = GlobalData.strTitle + GlobalData.strLog;

            // Just for debug, since debug textview in Tab0, if you change to Tab2, setText will collapse.
            if (iTabSelection == 0){
                mTextVew.setText(StrShowText);
                mImageBT.setBackgroundResource(R.drawable.bluetooth56);
                mImageDeviceCheck.setBackgroundResource(R.drawable.devicecheck56);
                mImageBattery.setBackgroundResource(R.drawable.battery_56px_green);

                mDebugTextVew.setText(strTemp);
            }



            //mTextVew.notify();
        }else if (strAction.equals(BluetoothService.ACTION_GATT_DISCONNECTED)){

            Log.d(TAG, "Bluetooth Disconnected");

            // Tobe deleted.
            GlobalData.strLog = "";
            GlobalData.StrDeviceName = "";
            GlobalData.StrDeviceAdd = "";

            String strTem = GlobalData.strTitle + "Clear.";

            // Just for debug, since debug textview in Tab0, if you change to Tab2, setText will collapse.
            if (iTabSelection == 0){
                mDebugTextVew.setText(strTem);
                mTextVew.setText(R.string.Bluetooth_Not_Connected);
                mImageBT.setBackgroundResource(R.drawable.bluetooth56_gray);
                mImageDeviceCheck.setBackgroundResource(R.drawable.devicecheck56gray);
                mImageBattery.setBackgroundResource(R.drawable.battery_56px_gray);
            }

        }else if (strAction.equals(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED)) {

            Log.e(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
            //BluetoothService.startActionenableTXNotification(MainActivity.getInstance().getBaseContext());

        }else if (strAction.equals(BluetoothService.ACTION_FEEDBACK_AVAILABLE)) {
            Log.d(TAG, "ACTION_FEEDBACK_AVAILABLE");
            //final byte[] txValue = intent.getByteArrayExtra(BluetoothService.EXTRA_DATA);

        }else if (strAction.equals(BluetoothService.ACTION_DEBUG_DATA)){
            Log.d(TAG, "ACTION_DEBUG_DATA");
            String StrDebugData = intent.getStringExtra(BluetoothService.DEBUG_COMMAND);
            GlobalData.strLog = StrDebugData + GlobalData.strLog;
            String strTem = GlobalData.strTitle + GlobalData.strLog;

            // Just for debug, since debug textview in Tab0, if you change to Tab2, setText will collapse.
            int iTabSelection = MainActivity.mtabLayout.getSelectedTabPosition();
            if (iTabSelection == 0){
                mDebugTextVew.setText(strTem);
            }



        }else if (strAction.equals(BluetoothService.ACTION_DATA_AVAILABLE)){
            Log.d(TAG, "ACTION_DATA_AVAILABLE");
            //final String StrEXTRA_DATA = intent.getStringExtra(BluetoothService.EXTRA_DATA);
            int iDefault = 0;

            int iCommandType = intent.getIntExtra(BluetoothService.RETURN_COMMAND, iDefault);
            int[] iReturnData  = null;
            String strReturnData = null;

            switch (iCommandType){
                case GlobalData.cCommand_BatteryRemain:
                    iReturnData= intent.getIntArrayExtra(BluetoothService.RETURN_DATA);
                    if (iReturnData == null){
                        Log.e(TAG, "iReturnData is null.");
                        return;
                    }

                    GlobalData.strCurrentBattery = String.format("%d", iReturnData[GlobalData.cBattery_Index]);

                    mBatteryValue.setText(GlobalData.strCurrentBattery);
                    break;
                case GlobalData.cCommand_Selfcheck:
                    iReturnData= intent.getIntArrayExtra(BluetoothService.RETURN_DATA);
                    if (iReturnData == null){
                        Log.e(TAG, "iReturnData is null.");
                        return;
                    }

                    if (iReturnData.length < GlobalData.cAmplitude_Decimal_Index + 1){
                        Log.e(TAG, "iReturnData is less than expected.");
                        return;
                    }

                    GlobalData.strVoltage = String.format("%d", iReturnData[GlobalData.cVoltage_Index]);
                    mVoltageView.setText(GlobalData.strVoltage);
                    GlobalData.strPressureGate = String.format("%d", iReturnData[GlobalData.cPressure_Integer_Index]);
                    GlobalData.strPressureGate += ".";
                    GlobalData.strPressureGate += String.format("%d", iReturnData[GlobalData.cPressure_Decimal_Index]);
                    mPressureView.setText(GlobalData.strPressureGate);

                    int iMaterialHigh = iReturnData[GlobalData.cMaterialHigh_Index];
                    int iMaterialLow = iReturnData[GlobalData.cMaterialLow_Index];
                    int iTem = iMaterialHigh*256 + iMaterialLow;
                    GlobalData.strMaterialNum = String.format("%d", iTem);
                    mMaterialView.setText(GlobalData.strMaterialNum);

                    GlobalData.strFrequency = String.format("%d", iReturnData[GlobalData.cFrequency_Integer_Index]);
                    GlobalData.strFrequency += ".";
                    GlobalData.strFrequency += String.format("%d", iReturnData[GlobalData.cFrequency_Decimal_Index]);
                    mFrequencyText.setText(GlobalData.strFrequency);

                    int iWaveLengthHigh = iReturnData[GlobalData.cWaveLengthHigh_Index];
                    int iWaveLengthLow = iReturnData[GlobalData.cWaveLengthLow_Index];
                    iTem = iWaveLengthHigh*256 + iWaveLengthLow;
                    GlobalData.strWaveLength = String.format("%d", iTem);
                    mWaveLengthText.setText(GlobalData.strWaveLength);

                    iTem = iReturnData[GlobalData.cCycle_Index];
                    GlobalData.strWaveCycle = String.format("%d", iTem);
                    mCycleText.setText(GlobalData.strWaveCycle);

                    GlobalData.strAmplitude = String.format("%d", iReturnData[GlobalData.cAmplitude_Integer_Index]);
                    GlobalData.strAmplitude += ".";
                    GlobalData.strAmplitude += String.format("%d", iReturnData[GlobalData.cAmplitude_Decimal_Index]);
                    mAmplitudeText.setText(GlobalData.strAmplitude);
                    break;

                case GlobalData.cCommand_ReadTime:
                    iReturnData= intent.getIntArrayExtra(BluetoothService.RETURN_DATA);
                    if (iReturnData == null){
                        Log.e(TAG, "iReturnData is null.");
                        return;
                    }

                    String strDate = String.format("%d", 20); //2016, high part.
                    strDate += String.format("%d", iReturnData[GlobalData.cYearLow_Index]);
                    strDate += "-";
                    strDate += String.format("%d", iReturnData[GlobalData.cMonth_Index]);
                    strDate += "-";
                    strDate += String.format("%d", iReturnData[GlobalData.cDay_Index]);
                    mDateText.setText(strDate);

                    String strTime = String.format("%d", iReturnData[GlobalData.cHour_Index]);
                    strTime += ":";
                    strTime += String.format("%d", iReturnData[GlobalData.cMinute_Index]);
                    strTime += ":";
                    strTime += String.format("%d", iReturnData[GlobalData.cSecond_Index]);
                    mTimeText.setText(strTime);

                    break;

                case GlobalData.cCommand_SendMessageTimes:
                    int iTimes = intent.getIntExtra(BluetoothService.RETURN_DATA, 0);
                    if (iTimes < 0){
                        Log.e(TAG, "strReturnData is null.");
                        return;
                    }else{
                        GlobalData.strErrorMessageTimes = String.format("%d", iTimes);
                    }

                    if (mMessagesText != null){
                        mMessagesText.setText(GlobalData.strErrorMessageTimes);
                    }else{
                        Log.e(TAG, "mMessagesText is null");
                    }

                    break;

                case GlobalData.cCommand_SendMessageTimes_First:
                    GlobalData.strQueryErrorMessage = intent.getStringExtra(BluetoothService.RETURN_DATA);
                    if (GlobalData.strQueryErrorMessage == ""){
                        Log.e(TAG, "GlobalData.strQueryErrorMessage is null.");
                        return;
                    }

                    if (mSendTimesText != null){
                        mSendTimesText.setText(GlobalData.strQueryErrorMessage);
                    }else{
                        Log.e(TAG, "mSendTimesText is null");
                    }

                    break;

                case GlobalData.cCommand_Settings:
                        MainActivity.getInstance().ShowInfo2User(
                                "参数设置成功", Toast.LENGTH_SHORT);
                    break;

                case GlobalData.cCommand_Reset:
                    MainActivity.getInstance().ShowInfo2User(
                            "复位成功", Toast.LENGTH_SHORT);

                    break;
                default:
                    break;

            }

            // Todo, show all log;


            /*
            final byte[] txValue = intent.getByteArrayExtra(BluetoothService.EXTRA_DATA);
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        String text = new String(txValue, "UTF-8");
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        listAdapter.add("[" + currentDateTimeString + "] RX: " + text);
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
            });*/
        }else if (strAction.equals(BluetoothService.DEVICE_DOES_NOT_SUPPORT_UART)){
            //showMessage("Device doesn't support UART. Disconnecting");
            //mService.disconnect();
            Log.e(TAG, "DEVICE_DOES_NOT_SUPPORT_UART");

        }else {
            Log.e(TAG, "Wrong intnet: " + strAction);
        }


    }

}
