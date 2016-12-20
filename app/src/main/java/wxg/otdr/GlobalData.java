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

    public static boolean bInEngineeringMode = true; // X // As default to be standard mode. 28-Sep-2016.
    public static boolean bWatchDog1_Protection = false; // To prevent button pressed again and agian.
    public final static int iWatchDogTimer1 = 15000; // Timeout gate for 15000 ms.

    // Those parameters are used, when refresh the page of "Device status".
    public static String StrDeviceName = "";
    public static String StrDeviceAdd = "";
    public static String strCurrentBattery = "";
    public static String strMaterialNum = "";
    public static String strVoltage = "";
    public static String strAmplitude = "";
    public static String strFrequency = "";
    public static String strWaveLength = "";
    public static String strWaveCycle = "";
    public static String strPressureGate = "";
    public static String strErrorMessageTimes = "";
    public static String strQueryErrorMessage = "";

    enum BTStatus {BT_Connecting, BT_Connected, BT_Disconnected};
    private static BTStatus eBTStatus = BTStatus.BT_Disconnected;

    public static int[] iQueryBattery = {0x68, 0x01, 0x3C};
    public static int[] iSelfCheck = {0x68, 0x01, 0x69};
    public static int[] iReset = {0x68, 0x01, 0xD2};
    //iSetTime, for date and time need to renew, so just take 0x0 as default.
    public static int[] iSetTime = {0x68, 0x07, 0xC8, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0};
    public static int[] iReadTime = {0x68, 0x01, 0xD7};
    //iSendMessageTimes
    public static int[] iSendMessageTimes = {0x68, 0x01, 0x40};
    //RequestMessageTime, 0x41, 0x42...0x4a, for ten times, 0x41 just as default value, need renew.
    public static int iMaxMessageNum = 10; // Only up to 10 messages could be shown.
    public static int[] iRequestMessageTimes = {0x68, 0x01, 0x41};
    // To send which message want to read. {0x68, 0x01, 0x41}; renew 0x40 to be 0x41 or ...0x4a
    public final static int cSetMessageTimes_Index = 2;

    public static int[] iSettings = {0x68, 0x0C, 0xE1, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00};


    public final static int cCommand_Head = 0x68;
    public final static int cCommand_BatteryRemain = 0x3C;
    public final static int cCommand_Selfcheck = 0x69;
    public final static int cCommand_Reset = 0xD2;
    //public final static int cCommand_SetTime = 0xC8;// It's just for send not receive, not used.
    public final static int cCommand_SendMessageTimes = 0x40;
    public final static int cCommand_SendMessageTimes_First = 0x41;
    public final static int cCommand_SendMessageTimes_Second = 0x42;
    public final static int cCommand_SendMessageTimes_Third = 0x43;
    public final static int cCommand_SendMessageTimes_Fourth = 0x44;
    public final static int cCommand_SendMessageTimes_Fifth = 0x45;
    public final static int cCommand_SendMessageTimes_Sixth = 0x46;
    public final static int cCommand_SendMessageTimes_Seventh = 0x47;
    public final static int cCommand_SendMessageTimes_Eighth = 0x48;
    public final static int cCommand_SendMessageTimes_Ninth = 0x49;
    public final static int cCommand_SendMessageTimes_Last = 0x4a;
    public final static int cCommand_Settings = 0xE1;
    public final static int cCommand_ReadTime = 0xD7;



    // Those below index are defined as pure data, not inclued command type.

    // Index in byte[], e.g. 0x68, 0x01, 0x3C
    public final static int cCommandHead_Index = 0; //0x68, head of every return data.
    public final static int cCommandLength_Index = 1; //0x01, length of return data, including command type.
    public final static int cCommandType_Index = 2; //0x3C, command type.

    public final static int cDataStart_Index = 3; // Used to analyzed data and remove command type.

    // Each byte for return value, in different command type.
    public final static int cBattery_Index = 0;
    // For self-Check items.
    public final static int cVoltage_Index = 0;
    public final static int cPressure_Integer_Index = 1;
    public final static int cPressure_Decimal_Index = 2;
    public final static int cMaterialLow_Index = 3;
    public final static int cMaterialHigh_Index = 4;
    public final static int cFrequency_Integer_Index = 5;
    public final static int cFrequency_Decimal_Index = 6;
    public final static int cWaveLengthLow_Index = 7;
    public final static int cWaveLengthHigh_Index = 8;
    public final static int cCycle_Index = 9;
    public final static int cAmplitude_Integer_Index = 10;
    public final static int cAmplitude_Decimal_Index = 11;
    // To Read Date and Time.{0x68, 0x07, 0xD7, YearLow, month, day, hour, minute, second}
    public final static int cYearLow_Index = 0;
    public final static int cMonth_Index = 1;
    public final static int cDay_Index = 2;
    public final static int cHour_Index = 3;
    public final static int cMinute_Index = 4;
    public final static int cSecond_Index = 5;
    // To set Date and Time. {0x68, 0x07, 0xC8, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0};
    public final static int cSetYearLow_Index = 3;
    public final static int cSetMonth_Index = 4;
    public final static int cSetDay_Index = 5;
    public final static int cSetHour_Index = 6;
    public final static int cSetMinute_Index = 7;
    public final static int cSetSecond_Index = 8;
    // To get times of sending message. {0x68, 0x02, 0x40, 0x0a}, times number is ox0a.
    public final static int cSendTimes_Index = 0;
    // To get Date and time info from returned data.
    public final static int cMessageYearHigh_Index = 0;
    public final static int cMessageYearLow_Index = 1;
    public final static int cMessageMonth_Index = 2;
    public final static int cMessageDay_Index = 3;
    public final static int cMessageHour_Index = 4;
    public final static int cMessageMinute_Index = 5;
    public final static int cMessageSecond_Index = 6;

    public final static int cReset_Index = 0;

    // To set parameters.
    public final static int cPressureGate_Integer_Index = 3;
    public final static int cPressureGate_Decimal_Index= 4;
    public final static int cT2_Duration_Index = 5;
    public final static int cT1_Duration_Index = 6;
    public final static int cAudio_Duration_Index = 7;
    public final static int cMaterial_Settings_Low_Index = 10;
    public final static int cMaterial_Settings_High_Index = 11;


    enum eCommandIndex {eQueryBattery, eSelfCheck, eReset, eReadTime, eSetTime, eSendTimes,
        eRequestMessageTimes, eSettings};

    public static String strTitle = "Log: ";
    public static String strLog = "";



    @Override
    public void onCreate(){
        super.onCreate();

        //Default mode settings.

    }

    public boolean getInEngineeringMode(){
        return bInEngineeringMode;
    }
    public void setEngineeringMode(boolean bStatus){
        bInEngineeringMode = bStatus;
    }

    public BTStatus getBTStatus(){
        return eBTStatus;
    }
    public void setBTStatus(BTStatus eStatus){
        eBTStatus = eStatus;
    }

    public String set02dMode (int iNum){
        String strNum;

        if (iNum < 10){
            strNum = "0" + String.valueOf(iNum);
        }else{
            strNum = String.valueOf(iNum);
        }

        return strNum;
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
            case eQueryBattery:
                bCommand = Int2Byte(iQueryBattery);
                break;
            case eSelfCheck:
                bCommand = Int2Byte(iSelfCheck);
                break;
            case eReset:
                bCommand = Int2Byte(iReset);
                break;
            case eReadTime:
                bCommand = Int2Byte(iReadTime);
                break;
            case eSetTime:
                bCommand = Int2Byte(iSetTime);
                break;
            case eSendTimes:
                bCommand = Int2Byte(iSendMessageTimes);
                break;
            case eRequestMessageTimes:
                bCommand = Int2Byte(iRequestMessageTimes);
                break;
            case eSettings:
                bCommand = Int2Byte(iSettings);
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

        if (bValues == null){
            Log.e(TAG, "bValues is null.");
            return null;
        }else if (bValues[cCommandHead_Index] != cCommand_Head){
            Log.e(TAG, "Command head is not right, no 0X68.");
            return null;
        }

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

    public int getCommandTypeReturn(int[] iReturnValues){
        Log.i(TAG, "AnalyzeData");

        int iCommand = iReturnValues[cCommandType_Index];

        return iCommand;
    }

    // This function only return data, remove Head 0x68, Length and command type.
    // Also for battery reading, this case, int[] only got length = 1;
    public int[] getDataReturn(int[] iReturnValues){
        Log.i(TAG, "AnalyzeData");

        int iLength = iReturnValues[cCommandLength_Index]-1;
        int iAllLength = iReturnValues.length;
        iAllLength = iAllLength - 3; //iAllLength = 0x68+Length+commandType (3 bytes) + iLength

        int[] iValues = new int[iLength];
        if (iLength == iAllLength){
            for (int iCount = 0; iCount < iLength; iCount ++){
                iValues[iCount] = iReturnValues[cDataStart_Index + iCount];
            }
        }else{
            Log.e(TAG, "Length is not match");
            iValues = null;
        }

        return iValues;
    }

    // This function get double type number from String and return as int[0]=Integer, int[1]=Decimal.
    // This function apply to reading Pressure Gate setting from EditText.
    public int[] getDoubleFromString(String strValue){
        int[] iValues = new int[2];

        byte[] bValues = strValue.getBytes(); // Here bValues contains ASCii.
        int iLength = bValues.length;
        if (iLength == 0 ){
            return null;
        }

        int iInteger = 0;
        int iDecimal = 0;
        int iDot = -1;

        // ASCii, "." = 46, 0 = 48 ... 9 = 57.
        for (int iTem = 0; iTem < iLength; iTem++){

            // First execute number verification and find where is the ".";
            // Three case: 1, double, e.g. "3.2"; 2, integer, e.g. "3" or "3." or "3.0";
            // 3, decimal, e.g. "0.2", or ".2".
            if ((bValues[iTem] >= 48 && bValues[iTem] <= 57) | (bValues[iTem] == 46)){
                if (bValues[iTem] == 46){
                    iDot = iTem;
                }else{
                    bValues[iTem] -= 48; // turn it from ASCii to be number.
                }
            }else{
                return null;
            }
        }

        // "." not found, this is a integer.
        if(iDot == -1){
            iDot = iLength;
        }

        // Get position of "."  Now get integer part.
        // e.g. "56.78", iDot = 2, Integer = 5*10(iDot-1 times) + 6*10(iDot-2 times)...
        for (int iTem = 0; iTem < iDot; iTem++){
            iInteger += (bValues[iTem]) * Math.pow(10, (iDot - iTem - 1));
        }

        // Now to get decimal part.
        // e.g. "56.78", iDot = 2, Decimal = 7*10(iDot-1 times) + 8*10(iDot-2 times)...
        for (int iTem = iDot +1; iTem < iLength; iTem++){
            iDecimal += (bValues[iTem]) * Math.pow(10, (iLength -1 - iTem));
        }

        iValues[0] = iInteger;
        iValues[1] = iDecimal;

        return iValues;
    }

    // Only need to check the checksum from returned result.
    // Cancel checksum.
    /*
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
    }*/

}
