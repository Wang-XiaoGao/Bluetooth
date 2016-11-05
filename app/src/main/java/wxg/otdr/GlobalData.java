package wxg.otdr;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.InputStream;

/**
 * Created by Administrator on 2016/1/15.
 */
public class GlobalData extends Application{
    public static final String TAG = GlobalData.class.getSimpleName();

    private static boolean bInEngineeringMode = true; // X // As default to be standard mode. 28-Sep-2016

    enum BTStatus {BT_Connecting, BT_Connected, BT_Disconnected};
    private static BTStatus eBTStatus = BTStatus.BT_Disconnected;

    public static int[] iQueryBatter = {0x68, 0x01, 0x3C, 0xA5};
    public static int[] iSelfCheck = {0x68, 0x02, 0x69, 0x06, 0xD9};

    private final static int cCommand_Index = 0;
    private final static int cDataLenth_Index = 1;

    enum eCommandIndex {eQueryBatter, eSelfCheck};

    public static String strLog = null;
    public static String strLogRead = null;

    @Override
    public void onCreate(){
        super.onCreate();

        //Default mode settings.

    }

    public boolean getInEngineeringMode(){
        return this.bInEngineeringMode;
    }
    public void setEngineeringMode(boolean bStatus){
        this.bInEngineeringMode = bStatus;
    }

    public BTStatus getBTStatus(){
        return this.eBTStatus;
    }
    public void setBTStatus(BTStatus eStatus){
        this.eBTStatus = eStatus;
    }

    public String Char2String(char[] chars){

        int iCount = chars.length;
        String str = "";
        for (int iTem = 0; iTem < iCount; iTem++){
            char c = chars[iTem];
            String strTem = String.valueOf(c);
            str = str + strTem;
        }
        return str;
    }

    public String Char2Int2String(char[] chars){

        int iCount = chars.length;
        String str = "";
        for (int iTem = 0; iTem < iCount; iTem++){
            int iChar = (int)chars[iTem];
            String strTem = String.valueOf(iChar);
            str = str + ";" + strTem;
        }
        return str;
    }

    public String Int2String(int[] IntSerial){

        int iCount = IntSerial.length;
        String str = "";
        for (int iTem = 0; iTem < iCount; iTem++){

            String strTem = String.valueOf(IntSerial[iTem]);
            str = str + String.format(strTem, "%02x") + ";";
        }
        return str;
    }

    public String Int2StringLog(int[] IntSerial){

        int iCount = IntSerial.length;
        String str = "";
        for (int iTem = 0; iTem < iCount; iTem++){

            String strTem = String.valueOf(IntSerial[iTem]);
            str = str + ";" + strTem;
        }
        return str;
    }

    public byte[] getCommand(eCommandIndex eIndex){

        byte[] bCommand = null;
        switch (eIndex){
            case eQueryBatter:
                bCommand = Int2Byte(iQueryBatter);
                break;
            case eSelfCheck:
                bCommand = Int2Byte(iSelfCheck);
                break;
            default:
                break;

        }
        return bCommand;
    }

    // The correct command code should be [0, 255];
    // If [0, 127], it's belong to byte [-128, 127], no need to convert.
    // if [128, 255], it's needed to convert.
    public byte[] Int2Byte(int[] iValues){

        byte[] bValues = new byte[iValues.length];
        byte bValue = 0;

        for (int iCount = 0; iCount < iValues.length; iCount ++){

            if (iValues[iCount] < 0 ){
                Log.e(TAG, "Command value < 0, error!");
                return null;
            } else if (iValues[iCount] <= 127){

                bValue = ((byte) iValues[iCount]);
            }else if (iValues[iCount] >= 128 || iValues[iCount] <= 255 ){
                bValue = ((byte) iValues[iCount]);
            }else if (iValues[iCount] >= 256){
                Log.e(TAG, "Command value > 256, error!");
                return null;
            }
            bValues[iCount] = bValue;
        }

        return bValues;

    }

    public int[] getIntReturn(byte[] bValues){

        int[] iValues = null;
        iValues = Byte2Int(bValues);

        return iValues;
    }

    // The correct command code should be [0, 255];
    // byte [-128, 127]; Int[-2,147,483,648 to 2,147,483,647].
    // So if byteValue smaller than 0, it's needed to convert.
    public int[] Byte2Int(byte[] bValues){

        int[] iValues = new int[bValues.length];
        int iValue = 0;

        for (int iCount = 0; iCount < bValues.length; iCount ++){

            if (bValues[iCount] < 0 ){
                iValue = ((int) (bValues[iCount] & 0xFF));
            } else if (iValues[iCount] >= 0){
                iValue = ((int) bValues[iCount]);
            }

            // All Command code and return value is non-Zero, which is transferred by unsigned-8.
            if (iValue >= 0){
                iValues[iCount] = iValue;
            }else{
                Log.e(TAG, "Int value is smaller than 0.");
                return null;
            }

        }
        return iValues;
    }

    // Only need to check the checksum from returned result.
    public boolean Checksum(byte[] bValues){

        Log.i(TAG, "Checksum");
        boolean bCheck = false;

        int[] iValues = Byte2Int(bValues);
        int iCheckSumLeft = 0;
        int iCheckSumRight = iValues[iValues.length - 1]; // Take the last byte, the check sum.

        // Take all but the last byte.
        for (int iCount = 0; iCount < (iValues.length - 1); iCount ++) {
            iCheckSumLeft += iValues[iCount];
        }

        iCheckSumLeft = iCheckSumLeft & 0x000000FF;

        if (iCheckSumRight == iCheckSumLeft ){
            bCheck = true;
            Log.d(TAG, "Checksum is right.");
        }else{
            bCheck = false;
            Log.d(TAG, "Checksum is wrong.");
        }
        return bCheck;
    }

    public int[] getDataReturn(int[] iReturnValues){
        Log.i(TAG, "AnalyzeData");

        int iLenth = iReturnValues[cDataLenth_Index];

        int[] iValues = new int[iLenth];

        for (int iCount = 0; iCount < iLenth; iCount ++){
            iValues[iCount] = iReturnValues[cDataLenth_Index + iCount];
        }

        return iValues;
    }

    public int getCommandTypeReturn(int[] iReturnValues){
        Log.i(TAG, "AnalyzeData");

        int iCommand = iReturnValues[cCommand_Index];

        return iCommand;
    }
}
