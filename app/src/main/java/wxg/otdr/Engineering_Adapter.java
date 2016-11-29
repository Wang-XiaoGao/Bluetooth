package wxg.otdr;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
    private static List<String> mList;

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

    /**
     * EditText竖直方向是否可以滚动
     * @param editText 需要判断的EditText
     * @return true：可以滚动 false：不可以滚动
     */
    private boolean canVerticalScroll(EditText editText) {
        //滚动的距离
        int scrollY = editText.getScrollY();
        //控件内容的总高度
        int scrollRange = editText.getLayout().getHeight();
        //控件实际显示的高度
        int scrollExtent = editText.getHeight() - editText.getCompoundPaddingTop() -editText.getCompoundPaddingBottom();
        //控件内容总高度与实际显示高度的差值
        int scrollDifference = scrollRange - scrollExtent;

        if(scrollDifference == 0) {
            return false;
        }

        return (scrollY > 0) || (scrollY < scrollDifference - 1);
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
                    //Note: here use getTag()，position could not defined as final，mTouchItemPosition = position
                    mTouchItemPosition = (Integer) view.getTag();

                    //触摸的是EditText并且当前EditText可以滚动则将事件交给EditText处理；否则将事件交由其父类处理
                    if ((view.getId() == R.id.edit_text && canVerticalScroll((EditText)view))) {
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                        }
                    }
                    return false;
                }
            });


            // Let's ViewHolder get a TextWathcer，update position to avoid edit mix up.
            mViewHolder.mTextWatcher = new MyTextWatcher();
            mViewHolder.mEditText.addTextChangedListener(mViewHolder.mTextWatcher);
            mViewHolder.updatePosition(position);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();

            mViewHolder.updatePosition(position);
        }

        switch (position){
            case 0:
                mViewHolder.mTextView.setText("压力门限值\n(M)");
                mViewHolder.mEditText.setInputType(EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case 1:
                mViewHolder.mTextView.setText("持续时间门限T1\n(0~240min)");
                mViewHolder.mEditText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);

                break;
            case 2:
                mViewHolder.mTextView.setText("持续时间门限T2\n(30~100S)");
                mViewHolder.mEditText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                break;
            case 3:
                mViewHolder.mTextView.setText("发射信号长度\n(0~1000S)");
                mViewHolder.mEditText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                break;
            case 4:
                mViewHolder.mTextView.setText("工作频率设定\n(20~40khz)");
                mViewHolder.mEditText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                break;
            case 5:
                mViewHolder.mTextView.setText("发射信号周期设定\n(0~100S)");
                mViewHolder.mEditText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
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

        public void updatePosition(int position) {
            mTextWatcher.updatePosition(position);
        }
    }

    class MyTextWatcher implements TextWatcher {
        //afterTextChanged use position value
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
