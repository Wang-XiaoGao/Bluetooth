package wxg.otdr;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2016/11/20.
 */
public class Engineering_Adapter extends BaseAdapter {
    private ViewHolder mViewHolder;
    private LayoutInflater mLayoutInflater;
    private List<String> mList;

    //mTouchItemPosition, used to record position of EditText.
    private int mTouchItemPosition = -1;

    public Engineering_Adapter(Context context, List<String> list) {
        mLayoutInflater = LayoutInflater.from(context);
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.engineering_item, null);
            mViewHolder.mTextView = (TextView) convertView.findViewById(R.id.text_view);
            mViewHolder.mEditText = (EditText) convertView.findViewById(R.id.edit_text);
            mViewHolder.mEditText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    //注意，此处必须使用getTag的方式，不能将position定义为final，写成mTouchItemPosition = position
                    mTouchItemPosition = (Integer) view.getTag();
                    return false;
                }
            });

            // 让ViewHolder持有一个TextWathcer，动态更新position来防治数据错乱；不能将position定义成final直接使用，必须动态更新
            mViewHolder.mTextWatcher = new MyTextWatcher();
            mViewHolder.mEditText.addTextChangedListener(mViewHolder.mTextWatcher);
            mViewHolder.updatePosition(position);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
            //动态更新TextWathcer的position
            mViewHolder.updatePosition(position);
        }

        switch (position){
            case 0:
                mViewHolder.mTextView.setText("压力门限值(M)");
                break;
            case 1:
                mViewHolder.mTextView.setText("持续时间门限T1(0~240min)");
                break;
            case 2:
                mViewHolder.mTextView.setText("持续时间门限T2(30~100S)");
                break;
            case 3:
                mViewHolder.mTextView.setText("发射信号长度(0~1000S)");
                break;
            case 4:
                mViewHolder.mTextView.setText("工作频率设定(20~40khz)");
                break;
            case 5:
                mViewHolder.mTextView.setText("发射信号周期设定(0~100S)");
                break;
        }


        mViewHolder.mEditText.setText(mList.get(position));
        mViewHolder.mEditText.setTag(position);

        if (mTouchItemPosition == position) {
            mViewHolder.mEditText.requestFocus();
            mViewHolder.mEditText.setFocusable(true);
            mViewHolder.mEditText.setSelection(mViewHolder.mEditText.getText().length());
        } else {
            mViewHolder.mEditText.clearFocus();
        }
        return convertView;
    }

    static final class ViewHolder {
        TextView mTextView;
        EditText mEditText;
        MyTextWatcher mTextWatcher;

        //动态更新TextWathcer的position
        public void updatePosition(int position) {
            mTextWatcher.updatePosition(position);
        }
    }

    class MyTextWatcher implements TextWatcher {
        //由于TextWatcher的afterTextChanged中拿不到对应的position值，所以自己创建一个子类
        private int mPosition;

        public void updatePosition(int position) {
            mPosition = position;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mList.set(mPosition, s.toString());
        }
    };
}
