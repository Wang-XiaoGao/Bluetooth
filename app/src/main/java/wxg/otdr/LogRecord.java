package wxg.otdr;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.system.OsConstants;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Administrator on 2016/12/27.
 */
public class LogRecord extends Thread {
    private final static String TAG = LogRecord.class.getSimpleName();
    private Handler mHandler;

    public LogRecord(Handler handler){
        super();
        mHandler = handler;

    }


    public void run(){



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
            long lUsableSpace = newLogFolder.getUsableSpace();
            String strTemp = String.valueOf(lUsableSpace);
            Log.i(TAG, "lUsableSpace is " + strTemp);

            if (lUsableSpace <= GlobalData.lLogFileVolume) {
                GlobalData.bLogFileMode = false;
                Message message=new Message();
                message.what=1;
                mHandler.sendMessage(message);

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
                    Log.i(TAG, "Existed Log File Volume is: " + strTemp);
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
        FileOutputStream FileOut = null;
        FileInputStream FileIn = null;

        try{
            FileIn = new FileInputStream(newLogFile);
            int iFileVolume = FileIn.available();
            Log.e(TAG, "Read Log file volume is: " + String.valueOf(iFileVolume));
            if (iFileVolume>0){
                byte[] bytesTemp = new byte[iFileVolume];
                FileIn.read(bytesTemp);
                GlobalData.strPreLogFile = bytesTemp.toString();
            }
            FileIn.close();
        }catch(IOException e){
            Log.e(TAG, "can't create FileInputStream");
        }


        try {
            FileOut = new FileOutputStream(newLogFile);
            //Process process = Runtime.getRuntime().exec("logcat -d");
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log=new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
            }
            Log.d(TAG, "Process reading end.");

            if(FileOut != null){
                FileOut.write((GlobalData.strPreLogFile + log.toString()).getBytes());
                GlobalData.strPreLogFile = "";
                FileOut.close();
            }

        } catch (FileNotFoundException e) {
            Log.e("FileNotFoundException", "can't create FileOutputStream");
        } catch(IOException e){
            Log.e(TAG, "can't write FileOutputStream");
        }
    }

}
