package wxg.otdr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2016/11/20.
 */
public class Engineering_Adapter extends BaseAdapter {
    private ViewHolder mViewHolder;
    private LayoutInflater mLayoutInflater;
    private List<String> mList;

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
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        if (position <= 9) {
            mViewHolder.mTextView.setText("0" + (position));
        } else {
            mViewHolder.mTextView.setText("" + (position));
        }
        mViewHolder.mEditText.setText(mList.get(position));
        return convertView;
    }

    static final class ViewHolder {
        TextView mTextView;
        EditText mEditText;
    }
}
