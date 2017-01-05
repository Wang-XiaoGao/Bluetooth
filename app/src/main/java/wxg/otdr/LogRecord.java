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
import java.io.FileWriter;
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

            if (lUsableSpace <= GlobalData.lLogFile_MaxVolume) {
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
                    // If Log file is bigger than GlobalData.lLogFile_MaxVolume (10 Mb), renew.
                    if (lFileVolume > GlobalData.lLogFile_MaxVolume) {
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
        FileInputStream FileIn = null;
        long iFileVolume = 0;

        try{
            FileIn = new FileInputStream(newLogFile);
            iFileVolume = FileIn.available();
            Log.e(TAG, "Read Log file volume is: " + String.valueOf(iFileVolume));
            FileIn.close();
        }catch(IOException e){
            Log.e(TAG, "can't create FileInputStream");
        }

        iFileVolume = GlobalData.lLogFile_MaxVolume - iFileVolume;
        try{
            // Open a file writer，the second parameter means to write with append mode.
            FileWriter writer = new FileWriter(newLogFile, true);
        // Run a loop, continue to read, unless the file reach max volume.
            while (iFileVolume > 0) {
                //Process process = Runtime.getRuntime().exec("logcat -d");
                Process process = Runtime.getRuntime().exec("logcat -d");
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                StringBuilder log = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    log.append(line);
                }
                Log.d(TAG, "Process reading end.");
                iFileVolume = iFileVolume - log.length();

                writer.write(log.toString().toCharArray());
            }

            writer.close();
        } catch (FileNotFoundException e) {
                Log.e("FileNotFoundException", "can't create FileOutputStream");
        } catch (IOException e) {
                Log.e(TAG, "can't create FileWriter");
        }

    }

}
