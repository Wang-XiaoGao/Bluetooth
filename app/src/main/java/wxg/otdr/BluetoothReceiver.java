package wxg.otdr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
        final String StrDeviceName = intent.getStringExtra(BluetoothService.mStrDeviceName);
        final String StrDeviceAdd = intent.getStringExtra(BluetoothService.mStrDeviceAdd);
        String StrShowText = StrDeviceName + "\n" + StrDeviceAdd;

        TextView mTextVew = (TextView) MainActivity.getInstance().findViewById(R.id.id_MakeSure_Connection);
        ImageButton mImageBT = (ImageButton) MainActivity.getInstance().findViewById(R.id.imageViewBT);
        ImageButton mImageDeviceCheck = (ImageButton) MainActivity.getInstance().findViewById(R.id.id_SelfCheck_Image);
        ImageButton mImageBattery = (ImageButton) MainActivity.getInstance().findViewById(R.id.id_Battery_Image);


        //*********************//
        if (strAction.equals(BluetoothService.ACTION_GATT_CONNECTED)) {
            Log.d(TAG, "ACTION_GATT_CONNECTED");
            Log.i(TAG, StrShowText);
            mTextVew.setText(StrShowText);
            mImageBT.setBackgroundResource(R.drawable.bluetooth56);
            mImageDeviceCheck.setBackgroundResource(R.drawable.devicecheck56);
            mImageBattery.setBackgroundResource(R.drawable.battery_56px_green);
            //mTextVew.notify();
        }else if (strAction.equals(BluetoothService.ACTION_GATT_DISCONNECTED)){

            Log.d(TAG, "Bluetooth Disconnected");
            Log.i(TAG, StrShowText);
            mTextVew.setText(R.string.Bluetooth_Not_Connected);
            mImageBT.setBackgroundResource(R.drawable.bluetooth56_gray);
            mImageDeviceCheck.setBackgroundResource(R.drawable.devicecheck56gray);
            mImageBattery.setBackgroundResource(R.drawable.battery_56px_gray);

        }else if (strAction.equals(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED)) {

            Log.e(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
            //BluetoothService.startActionenableTXNotification(MainActivity.getInstance().getBaseContext());

        }else if (strAction.equals(BluetoothService.ACTION_DATA_AVAILABLE)) {
            Log.e(TAG, "ACTION_DATA_AVAILABLE");/*
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
