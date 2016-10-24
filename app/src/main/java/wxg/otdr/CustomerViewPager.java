package wxg.otdr;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Administrator on 2016/1/14.
 */

public class CustomerViewPager extends ViewPager {

    public CustomerViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {

        final GlobalData Data = (GlobalData) getContext().getApplicationContext();
        boolean bInStandardMode = Data.getInEngineeringMode();
        if (bInStandardMode) {
            return super.onTouchEvent(arg0);
        } else {
            return false;
        }
    }
}

