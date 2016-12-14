package wxg.otdr;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private final static String TAG = MainActivity.class.getSimpleName();

    private static MainActivity Myinstance;
    public static GlobalData mGlobalData = null;

    private static BluetoothService mBluetoothService = null;
    private BluetoothReceiver mBroadcastReceiver = null;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Title and item number for tab control.
    public String TabTitle_Device_Status;
    public String TabTitle_System_Info;
    public String TabTitle_Engineering_Mode;

    public int iTabCount = 3;
    public static boolean bIsInEngineeringMode = false;

    public static final int iDeviceStatus = 0;
    public static final int iSystemInfo = 1;
    public static final int iEngineeringMode = 2;

    private static final int READ_REQUEST_CODE = 42;

    private static TabLayout.Tab mEngineeringTab; // To add and remove for Standard & Engineering mode.
    private static TabLayout.Tab mSystemInfoTab;
    private static TabLayout.Tab mDeviceStatusTab;
    private static TabLayout mtabLayout;


    private static EditText mDateEdit;
    private static EditText mTimeEdit;
    private static Calendar mCalendar; // Read system time through Calendar
    private static int mYear;
    private static int mMonth;
    private static int mDay;
    private static int mHour;
    private static int mMinute;
    private static int mSecond;
    private static boolean mbDrawListVew = false;

    private static EditText mVersionEdit;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    //private ViewPager mViewPager;
    private CustomerViewPager mViewPager;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // To offer system service in BluetoothService.
        Myinstance = this;
        mBroadcastReceiver = new BluetoothReceiver();
        mBluetoothService = new BluetoothService();
        mGlobalData = new GlobalData();


        // To get mListView, need to find it's container...
        //View ContainerView = findViewById(R.id.container);
        //LayoutInflater inflater = LayoutInflater.from(this);
        //View rootView = inflater.inflate(R.layout.fragment_engineering_mode, (ViewGroup) ContainerView);
        //mListView = (ListView) rootView.findViewById(R.id.Engineering_listView);

        Log.i("MainActivity::OnCreate", "MainActivity::onCreate");

        /*
        BrocastReceive should be register and unregister.
        If register at OnResume and unregister at OnDestroy, leak warning will be reported
        when app switch more than once between other apps, since OnResume has been called several times.
        By WXG 24-Jan-2016
        */
        // Stop to receive Bluetooth broadcast from system
        /*mBTReceive = new BluetoothReceiver();
        IntentFilter intent = new IntentFilter();
        //ACTION_STATE_CHANGED only means the local bluetooth available, not connected done.
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //intent.addAction(BluetoothDevice.ACTION_FOUND);
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intent.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        //intent.addAction(BluetoothDevice.);
        registerReceiver(mBTReceive, intent);*/

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        TabTitle_Device_Status = getString(R.string.TabTitle_DeviceStauts);
        TabTitle_System_Info = getString(R.string.TabTitle_SystemInfo);
        TabTitle_Engineering_Mode = getString(R.string.TabTitle_EngineeringMode);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (CustomerViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.isShown();

        mtabLayout = (TabLayout) findViewById(R.id.tabs);
        mtabLayout.setupWithViewPager(mViewPager);
        mtabLayout.setTabsFromPagerAdapter(mSectionsPagerAdapter);

        // This app set standard mode to be default mode.

        mEngineeringTab = mtabLayout.getTabAt(iEngineeringMode);
        // For development, default mode change to be engineering mode.
        //mtabLayout.removeTabAt(iEngineeringMode);
        //iTabCount--;
        //mSectionsPagerAdapter.notifyDataSetChanged();
        //bIsInEngineeringMode = false;
        bIsInEngineeringMode = true;
        // For debug, don't know why this have to be true, or else tab view could not be scrolled.
        mGlobalData.setEngineeringMode(true);

        //Email function will be deferred. By WXG 12-Jan-2016
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_FEEDBACK_AVAILABLE);
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothService.ACTION_DEBUG_DATA);

        //Just for debug, to receive info from system
        //intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        registerReceiver(mBroadcastReceiver, intentFilter);


                // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();



    }

    public static MainActivity getInstance() {
        // TODO Auto-generated method stub
        // To offer system service in BluetoothService.
        return Myinstance;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e(this.getString(R.string.Log_Info), "MainActivity::onResume");


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mBroadcastReceiver);

        mBroadcastReceiver = null;
        mBluetoothService.startActionClose(this.getApplicationContext());
        mBluetoothService = null;
        mGlobalData = null;

        Log.e(this.getString(R.string.Log_Info), "MainActivity::onDestroy");
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

        //final GlobalData Data = (GlobalData) getApplication();

        boolean bInEngineeringMode = mGlobalData.getInEngineeringMode();

        // To Be delete
        int iTemCount = 0;
        //Make a switch and avoid trigger same action again.
        if (id == R.id.Standard_Mode & bIsInEngineeringMode) {
            // For debug, don't know why this have to be true, or else tab view could not be scrolled.
            mGlobalData.setEngineeringMode(true);
            bIsInEngineeringMode = false;

            //Return to OTDR fragment and then hide tablayout.
            if (mtabLayout.getTabCount() == iTabCount) {
                mtabLayout.removeTabAt(iEngineeringMode);
                iTabCount--;
                mSectionsPagerAdapter.notifyDataSetChanged();
                //ToDo
                iTemCount = mSectionsPagerAdapter.getCount();
                //mtabLayout.getTabCount();
            } else {
                Log.e("onOptionsItemSelected", "Wrong Tab number.");
                //// TODO: 2016/10/7

            }
// & !bInEngineeringMode
        } else if (id == R.id.Engineering_Mode & !bIsInEngineeringMode) {
            // For debug, don't know why this have to be true, or else tab view could not be scrolled.
            mGlobalData.setEngineeringMode(true);
            bIsInEngineeringMode = true;

            mtabLayout.addTab(mEngineeringTab, iEngineeringMode);
            iTabCount++;
            mSectionsPagerAdapter.notifyDataSetChanged();
            //mtabLayout.setVisibility(View.VISIBLE);
            iTemCount = mSectionsPagerAdapter.getCount();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://wxg.otdr/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://wxg.otdr/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            //Original code
            /*View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));*/

            int iSectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);

            Log.i("PlaceholderFragment", "Fragment Creation");
            View rootView = null;

            // If input soft windows could not be hidden after switch frame, need to hide manually.
            /*
            InputMethodManager InputManger = (InputMethodManager) getContext().getSystemService(
                    Context.INPUT_METHOD_SERVICE);

            EditText edit = (EditText) MainActivity.getInstance().findViewById(R.id.edit_text);
            if (edit != null){
                InputManger.hideSoftInputFromWindow(edit.getWindowToken(), 0);
            }else{
                Log.e(TAG, "EditText edit is null.");
            }
            */


            switch (iSectionNumber) {
                case MainActivity.iDeviceStatus:
                    rootView = ReadDeviceStatus(inflater, container);
                    break;
                case MainActivity.iSystemInfo:
                    rootView = ShowSystemInfo(inflater, container);
                    break;
                case MainActivity.iEngineeringMode:
                    rootView = EnterEngineeringMode(inflater, container);
                    break;
                default:
                    Log.e("PlaceholderFragment", "Unexpected tab position.");
            }
            return rootView;
        }

        public View ReadDeviceStatus(LayoutInflater inflater, ViewGroup container) {
            Log.i("PlaceholderFragment", "Draw DeviceStatus");
            View rootView = inflater.inflate(R.layout.fragment_device_status, container, false);

            mDateEdit = (EditText) rootView.findViewById(R.id.id_Edit_Date);
            mTimeEdit = (EditText) rootView.findViewById(R.id.id_Edit_Time);
            mCalendar = Calendar.getInstance();
            mYear = mCalendar.get(Calendar.YEAR);
            //JANUARY = 0; month from 0~11.
            mMonth = mCalendar.get(Calendar.MONTH) + 1;
            mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
            mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
            mMinute = mCalendar.get(Calendar.MINUTE);
            mSecond = mCalendar.get(Calendar.SECOND);

            String strDate = String.format(mYear + "-" + mGlobalData.set02dMode(mMonth)
                    + "-" + mGlobalData.set02dMode(mDay));
            if (mDateEdit != null){
                mDateEdit.setText(strDate);
            }else{
                Log.e(TAG, "mDateEdit is null.");
            }


            String strTime = String.format(mGlobalData.set02dMode(mHour) + ":" +
                    mGlobalData.set02dMode(mMinute) + ":" + mGlobalData.set02dMode(mSecond));
            if (mTimeEdit != null){
                mTimeEdit.setText(strTime);
            }else{
                Log.e(TAG, "mTimeEdit is null.");
            }

            if (mDateEdit == null){
                Log.e(TAG, "mDateEdit is null.");
            }

            mDateEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DatePickerDialog(getContext(),
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year,
                                                      int month, int day) {
                                    // TODO Auto-generated method stub
                                    mYear = year;
                                    //JANUARY = 0; month from 0~11.
                                    mMonth = month + 1;
                                    mDay = day;

                                    String srtDate = String.format(mYear + "-" + mGlobalData.set02dMode(mMonth)
                                            + "-" + mGlobalData.set02dMode(mDay));
                                    mDateEdit.setText(srtDate);
                                }
                            }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                            mCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            });

            mTimeEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new TimePickerDialog(getContext(),
                            new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hour, int minute) {
                                    // TODO Auto-generated method stub
                                    mHour = hour;
                                    mMinute = minute;
                                    mSecond = 0;

                                    String srtTime = String.format(mGlobalData.set02dMode(mHour) + ":" +
                                            mGlobalData.set02dMode(mMinute) + ":" + mGlobalData.set02dMode(mSecond));
                                    mTimeEdit.setText(srtTime);
                                }
                            }, mCalendar.get(Calendar.HOUR_OF_DAY),
                            mCalendar.get(Calendar.MINUTE), true).show();
                }
            });

            return rootView;
        }

        public View ShowSystemInfo(LayoutInflater inflater, ViewGroup container) {
            Log.i("PlaceholderFragment", "ShowSystemInfo");
            View rootView = inflater.inflate(R.layout.fragment_system_info, container, false);

            mVersionEdit = (EditText) rootView.findViewById(R.id.VersionInfo);

            String pName = "wxg.otdr";

            try {
                PackageManager pm = getContext().getPackageManager();

                PackageInfo pinfo = pm.getPackageInfo(pName, PackageManager.GET_CONFIGURATIONS);
                String strVersion = "版本：" + pinfo.versionName;
                //String versionName = String.format("%d", pinfo.versionCode);
                if (mVersionEdit != null){
                    mVersionEdit.setText(strVersion);
                }else{
                    Log.e(TAG, "mVersion is null.");
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
            return rootView;
        }

        public View EnterEngineeringMode(LayoutInflater inflater, ViewGroup container) {
            Log.i("PlaceholderFragment", "Draw Engineering Mode");
            View rootView = inflater.inflate(R.layout.fragment_engineering_mode, container, false);

            return rootView;
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return iTabCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case iDeviceStatus:
                    return TabTitle_Device_Status;
                case iSystemInfo:
                    return TabTitle_System_Info;
                case iEngineeringMode:
                    return TabTitle_Engineering_Mode;
            }
            return null;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        Log.i(TAG, "onActivityResult");
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    String deviceAddress = resultData.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.i(TAG, "REQUEST_SELECT_DEVICE: " + deviceAddress);
                    //Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    //((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                    // ToDo add device name.
                    mBluetoothService.startActionConnection(this.getApplicationContext(), null, deviceAddress);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, getResources().getText(R.string.BT_TurnOn), Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, getResources().getText(R.string.BT_Not_TurnOn), Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    public void OpenBluetoothSettings(View v) {
        //startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
        int iBTTem = mBluetoothService.getBTConnectStatus();

        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onClick - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else {
            // ToDo not implement disconnect now.
            if (iBTTem == BluetoothProfile.STATE_DISCONNECTED){
                Log.d(TAG, "Current bluetooth service status is: " + iBTTem);
                Log.i(TAG, "onClick - BT will be connected.");
                //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices

                Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
            }
            else if ((iBTTem == BluetoothProfile.STATE_CONNECTING)||(iBTTem == BluetoothProfile.STATE_CONNECTED)){
                //Disconnect button pressed
                Log.d(TAG, "Current bluetooth service status is: " + iBTTem);
                Log.i(TAG, "onClick - BT will be disconnected.");
                mBluetoothService.startActionDisconnect(this.getApplicationContext());
            }
        }

    }

    public void OpenFile(View v) {
        Log.i("MainActivity", "OpenFile");

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        //Set intent Action property.
        intent.setType("*/*");
        //Set category of file.
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        //intent.putExtra("Title", "选择文件");

        try {
            //startActivity(intent);
            startActivityForResult(intent, READ_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "没有正确打开文件管理器", Toast.LENGTH_SHORT).show();
        }

    }

    public void QueryBattery(View v) {
        Log.i(TAG, "Call QueryBattery.");
        boolean bCheck = false;
        byte[] bCommand = mGlobalData.getCommand(GlobalData.eCommandIndex.eQueryBattery);
//TEST_TX_SERVICE_UUID,RX_SERVICE_UUID //TEST_TX_CHAR_UUID, RX_CHAR_UUID
        bCheck = SendCommand(v, BluetoothService.RX_SERVICE_UUID, BluetoothService.RX_CHAR_UUID, bCommand);

        if (!bCheck){
            Log.e(TAG, "QueryBattery: Send Command failed.");
        }
    }


    public void StartSelfCheck(View v) {
        Log.i(TAG, "Call StartSelfCheck.");
        boolean bCheck = false;

        byte[] bCommand = mGlobalData.getCommand(GlobalData.eCommandIndex.eSelfCheck);

        bCheck = SendCommand(v, BluetoothService.RX_SERVICE_UUID, BluetoothService.RX_CHAR_UUID, bCommand);

        if (!bCheck){
            Log.e(TAG, "StartSelfCheck: Send Command failed.");
        }

    }

    public void onButtonRequestStatus(View v) {
        Log.i(TAG, "Call onButtonRequestStatus.");
        boolean bCheck = false;
//TEST_TX_SERVICE_UUID,RX_SERVICE_UUID//TEST_TX_CHAR_UUID, RX_CHAR_UUID
        // Check whether previous query on-going. There is a watch dog 1 to monitor, timeout gate is 3000ms.
        if (!GlobalData.bWatchDog1_Protection){
            byte[] bCommand = mGlobalData.getCommand(GlobalData.eCommandIndex.eSendTimes);
            bCheck = SendCommand(v, BluetoothService.RX_SERVICE_UUID, BluetoothService.RX_CHAR_UUID, bCommand);

            if (!bCheck){
                Log.e(TAG, "onButtonRequestStatus: Send Command failed.");
            }else{
                // WatchDog1 enter active mode. Note: need to activate WatchDog1 after command sent.
                GlobalData.bWatchDog1_Protection = true;
                CountDownTimer WatchDog1 = new CountDownTimer(GlobalData.iWatchDogTimer1, GlobalData.iWatchDogTimer1-50) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }
                    @Override
                    public void onFinish() {
                        // If after iWatchDogTimer1 ms, WatchDog1 still be active, something wrong.
                        if (GlobalData.bWatchDog1_Protection){
                            Log.e(TAG, "WatchDog1: Update BT send message status timeout.");
                            GlobalData.bWatchDog1_Protection = false;
                            BluetoothService.strBTStatus = "";
                            BluetoothService.miSendTimes = 0;
                            MainActivity.getInstance().ShowInfo2User(
                                    getResources().getText(R.string.WatchDog_Timeout).toString(),
                                    Toast.LENGTH_SHORT);
                        }

                    }
                };

                WatchDog1.start();
            }
        }else{
            Log.e(TAG, "onButtonRequestStatus: Already on-going.");
            Toast.makeText(this, getResources().getText(R.string.WatchDog_Ongoing),
                    Toast.LENGTH_SHORT).show();
        }

    }

    public void onButtonReset(View v) {
        Log.i(TAG, "Call onButtonReset.");
        boolean bCheck = false;

        int iBTTem = mBluetoothService.getBTConnectStatus();

        byte[] bCommand = mGlobalData.getCommand(GlobalData.eCommandIndex.eReset);
        int[] iValues = mGlobalData.getIntReturn(bCommand);

        Log.e(TAG, "onButtonReset, Data to send: " + mGlobalData.Int2String(iValues));
        if (iBTTem == BluetoothProfile.STATE_CONNECTED){
            //send data to service
//TEST_TX_SERVICE_UUID,RX_SERVICE_UUID
            if (mBluetoothService != null) {
                bCheck = mBluetoothService.AssignGATTService(BluetoothService.RX_SERVICE_UUID);
                if (!bCheck){
                    Log.e(TAG, "StartSelfCheck: mBluetoothService is null.");
                    return;
                }
//TEST_TX_CHAR_UUID, RX_CHAR_UUID
                bCheck = mBluetoothService.AssignGATTCharacteristics(BluetoothService.RX_CHAR_UUID);
                if (!bCheck){
                    Log.e(TAG, "StartSelfCheck: Characteristics is null.");
                    return;
                }

                bCheck = mBluetoothService.writeRXCharacteristic(bCommand);
                if (!bCheck){
                    Log.e(TAG, "StartSelfCheck: mBluetoothService is null.");
                    //Intent intent = new Intent(BluetoothService.ACTION_DATA_AVAILABLE);
                    //byte[] bValues = {0x9, 0x9};
                    //intent.putExtra(BluetoothService.EXTRA_DATA, bValues);
                    //sendBroadcast(intent);
                    Toast.makeText(this, getResources().getText(R.string.Command_Send_Failed),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

            }else{
                Log.e(TAG, "Exception: mBluetoothService is null.");
            }
        }else{
            Log.e(TAG, "Bluetooth not connected yet, could not read battery info.");

            Toast.makeText(v.getContext(),
                    "Bluetooth not connected yet, could not read battery info:",
                    Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void onButtonSettings(View v) {
        Log.i(TAG, "Call onButtonSettings.");

        boolean bCheck = false;
        byte[] bCommand = mGlobalData.getCommand(GlobalData.eCommandIndex.eSettings);

        EditText PressureGate = (EditText) findViewById(R.id.id_Pressure_Gate_Value);


        bCheck = SendCommand(v, BluetoothService.RX_SERVICE_UUID, BluetoothService.RX_CHAR_UUID, bCommand);

        if (!bCheck) {
            Log.e(TAG, "onButtonReadTime: Send Command failed.");
        }
    }


    // Try onButtonReadTime.
    public void onButtonReadTime(View v) {
        Log.i(TAG, "Call onButtonReadTime.");
        boolean bCheck = false;
        byte[] bCommand = mGlobalData.getCommand(GlobalData.eCommandIndex.eReadTime);

        bCheck = SendCommand(v, BluetoothService.RX_SERVICE_UUID, BluetoothService.RX_CHAR_UUID, bCommand);

        if (!bCheck) {
            Log.e(TAG, "onButtonReadTime: Send Command failed.");
        }

    }

    // Try onButtonSetTime.
    public void onButtonSetTime(View v) {
        Log.i(TAG, "Call onButtonSetTime.");
        boolean bCheck = false;

        // Only take lower part of year, e.g. 2016-->16.
        GlobalData.iSetTime[GlobalData.cSetYearLow_Index] =  mYear - 2000;
        GlobalData.iSetTime[GlobalData.cSetMonth_Index] =  mMonth;
        GlobalData.iSetTime[GlobalData.cSetDay_Index] =  mDay;
        GlobalData.iSetTime[GlobalData.cSetHour_Index] =  mHour;
        GlobalData.iSetTime[GlobalData.cSetMinute_Index] =  mMinute;
        GlobalData.iSetTime[GlobalData.cSetSecond_Index] =  mSecond;

        byte[] bCommand = mGlobalData.getCommand(GlobalData.eCommandIndex.eSetTime);

        bCheck = SendCommand(v, BluetoothService.RX_SERVICE_UUID, BluetoothService.RX_CHAR_UUID, bCommand);

        if (!bCheck){
            Log.e(TAG, "onButtonSetTime: Send Command failed.");
        }

    }

    // Try QueryLatestVersion.
    public void QueryLatestVersion(View v) {
        BluetoothService.startActionConnection(this.getApplicationContext(), null, null);

    }

    public void ShowInfo2User(String strInfo, int iShowTime){
        Toast.makeText(this.getApplicationContext(),
                strInfo,
                iShowTime).show();
    }

    public boolean SendCommand(View v, UUID ServiceUUID, UUID CharUUID, byte[] bCommand){
        int iBTTem = BluetoothService.getBTConnectStatus();
        boolean bCheck = false;

        if (iBTTem == BluetoothProfile.STATE_CONNECTED){
            //eSelfCheck, eQueryBatter
            int[] iValues = mGlobalData.getIntReturn(bCommand);

            Log.i(TAG, "Data to send: " + mGlobalData.Int2String(iValues));
//BluetoothService.TEST_TX_SERVICE_UUID,RX_SERVICE_UUID//ServiceUUID
            if (mBluetoothService != null) {
                bCheck = mBluetoothService.AssignGATTService(ServiceUUID);
                if (!bCheck){
                    Log.e(TAG, "mBluetoothService is null.");
                    return false;
                }
//BluetoothService.TEST_TX_CHAR_UUID, RX_CHAR_UUID//CharUUID
                bCheck = mBluetoothService.AssignGATTCharacteristics(CharUUID);
                if (!bCheck){
                    Log.e(TAG, "Characteristics is null.");
                    return false;
                }

                bCheck = mBluetoothService.writeRXCharacteristic(bCommand);
                if (!bCheck){
                    Log.e(TAG, "QueryBattery: writeRXCharacteristic failed.");
                    //Intent intent = new Intent(BluetoothService.ACTION_DATA_AVAILABLE);
                    //byte[] bValues = {0x9, 0x9};
                    //intent.putExtra(BluetoothService.EXTRA_DATA, bValues);
                    //sendBroadcast(intent);
                    Toast.makeText(this, getResources().getText(R.string.Command_Send_Failed),
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

            }else{
                Log.e(TAG, "Exception: mBluetoothService is null.");
            }
        }else{
            Log.e(TAG, "Bluetooth not connected yet, could not read battery info.");

            Toast.makeText(getBaseContext(),
                    "蓝牙设备还未连接!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


}

