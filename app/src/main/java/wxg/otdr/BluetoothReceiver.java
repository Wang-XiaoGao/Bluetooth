package wxg.otdr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;


/**
 * This Receiver only support BLE.
 * Created by Administrator on 2016/10/11.
 */
public class BluetoothReceiver extends BroadcastReceiver {
    private final static String TAG = BluetoothReceiver.class.getSimpleName();


    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Log.i(TAG, "BluetoothReceiver::onReceive");



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
        EditText mDateText = (EditText) MainActivity.getInstance().findViewById(R.id.id_Edit_Date);
        EditText mTimeText = (EditText) MainActivity.getInstance().findViewById(R.id.id_Edit_Time);


        // Todo, this test view is for debug.
        TextView mDebugTextVew = (TextView) MainActivity.getInstance().findViewById(R.id.id_DebugText_View);



        //*********************//
        if (strAction.equals(BluetoothService.ACTION_GATT_CONNECTED)) {
            Log.d(TAG, "ACTION_GATT_CONNECTED");

            // if it's not for connection action, the below two string may not be initiated.
            final String StrDeviceName = intent.getStringExtra(BluetoothService.mStrDeviceName);
            final String StrDeviceAdd = intent.getStringExtra(BluetoothService.mStrDeviceAdd);
            String StrShowText = StrDeviceName + "\n" + StrDeviceAdd;

            Log.i(TAG, StrShowText);
            mTextVew.setText(StrShowText);
            mImageBT.setBackgroundResource(R.drawable.bluetooth56);
            mImageDeviceCheck.setBackgroundResource(R.drawable.devicecheck56);
            mImageBattery.setBackgroundResource(R.drawable.battery_56px_green);

            GlobalData.strNewLog = "Connected";

            GlobalData.strLog = GlobalData.strNewLog + GlobalData.strLog;
            String strTem = GlobalData.strTitle + GlobalData.strLog;

            mDebugTextVew.setText(strTem);


            //mTextVew.notify();
        }else if (strAction.equals(BluetoothService.ACTION_GATT_DISCONNECTED)){

            Log.d(TAG, "Bluetooth Disconnected");

            // Tobe deleted.
            GlobalData.strNewLog = null;
            GlobalData.strLog = null;

            String strTem = GlobalData.strTitle + "Clear.";

            mDebugTextVew.setText(strTem);


            mTextVew.setText(R.string.Bluetooth_Not_Connected);
            mImageBT.setBackgroundResource(R.drawable.bluetooth56_gray);
            mImageDeviceCheck.setBackgroundResource(R.drawable.devicecheck56gray);
            mImageBattery.setBackgroundResource(R.drawable.battery_56px_gray);

        }else if (strAction.equals(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED)) {

            Log.e(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
            //BluetoothService.startActionenableTXNotification(MainActivity.getInstance().getBaseContext());

        }else if (strAction.equals(BluetoothService.ACTION_FEEDBACK_AVAILABLE)) {
            Log.d(TAG, "ACTION_FEEDBACK_AVAILABLE");
            //final byte[] txValue = intent.getByteArrayExtra(BluetoothService.EXTRA_DATA);



        }else if (strAction.equals(BluetoothService.ACTION_DATA_AVAILABLE)){
            Log.d(TAG, "ACTION_DATA_AVAILABLE");
            //final String StrEXTRA_DATA = intent.getStringExtra(BluetoothService.EXTRA_DATA);
            int iDefault = 0;


            int iCommandType = intent.getIntExtra(BluetoothService.RETURN_COMMAND, iDefault);
            int[] iReturnData = intent.getIntArrayExtra(BluetoothService.RETURN_DATA);
            if (iReturnData == null){
                Log.e(TAG, "iReturnData is null.");
                return;
            }

            switch (iCommandType){
                case GlobalData.cCommand_BatteryRemain:
                    mBatteryValue.setText(String.format("%d", iReturnData[GlobalData.cBattery_Index]));
                    break;
                case GlobalData.cCommand_Selfcheck:
                    if (iReturnData.length < GlobalData.cMaterialHigh_Index + 1){
                        Log.e(TAG, "iReturnData is less than expected.");
                        return;
                    }
                    mVoltageView.setText(String.format("%d", iReturnData[GlobalData.cVoltage_Index]));
                    String strPressure = String.format("%d", iReturnData[GlobalData.cPressure_Integer_Index]);
                    strPressure += ".";
                    strPressure += String.format("%d", iReturnData[GlobalData.cPressure_Decimal_Index]);
                    mPressureView.setText(strPressure);

                    int iMaterialHigh = iReturnData[GlobalData.cMaterialHigh_Index];
                    int iMaterialLow = iReturnData[GlobalData.cMaterialLow_Index];
                    int iTem = iMaterialHigh<<8 + iMaterialLow;

                    mMaterialView.setText(String.format("%d", iTem));

                    break;

                case GlobalData.cCommand_ReadTime:
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

                case GlobalData.cCommand_Reset:

                    break;
                default:
                    break;

            }

            // To do, delete, just for debug.
            GlobalData.strNewLog = "Command_Type:";
            GlobalData.strNewLog += String.format("0x%02x; Data: ", iCommandType);

            if (iReturnData != null){
                for (int iTem = 0; iTem < iReturnData.length; iTem ++){
                    //String str = String.valueOf(txValue[iTem]);
                    GlobalData.strNewLog += String.format("%02x ", iReturnData[iTem]);
                }
            }
            GlobalData.strNewLog += "。";

            GlobalData.strLog = GlobalData.strNewLog + GlobalData.strLog;
            String strTem = GlobalData.strTitle + GlobalData.strLog;

            mDebugTextVew.setText(strTem);


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
