package wxg.otdr;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2016/1/15.
 */
public class GlobalData extends Application{

    private static boolean bInEngineeringMode = true; // X // As default to be standard mode. 28-Sep-2016

    enum BTStatus {BT_Connecting, BT_Connected, BT_Disconnected};
    private static BTStatus eBTStatus = BTStatus.BT_Disconnected;





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


}
