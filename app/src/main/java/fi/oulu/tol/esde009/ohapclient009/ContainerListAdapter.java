package fi.oulu.tol.esde009.ohapclient009;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.opimobi.ohap.Container;
import com.opimobi.ohap.Device;
import com.opimobi.ohap.Item;

/**
 * Created by bel on 01.04.16.
 */
public class ContainerListAdapter implements ListAdapter {

    private static final String DEBUG_TAG = "ContainerListAdapter";
    private Container container;

    public ContainerListAdapter(Container container) {
        this.container = container;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        Log.d(DEBUG_TAG, "getCount()");
        return container.getItemCount();
    }

    @Override
    public Object getItem(int position) {
        Log.d(DEBUG_TAG, "getItem()");
        return container.getItemByIndex(position);
    }

    @Override
    public long getItemId(int position) {
        Log.d(DEBUG_TAG, "getItemId()");
        return container.getItemByIndex(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        Log.d(DEBUG_TAG, "hasStableIds()");
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(DEBUG_TAG, "getView()" + position);

        ViewHolder viewHolder;

        if(convertView == null){
            Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            /*
            * Create instance of internal class and find views by existing IDs from row_item.xml
            * and take parameters of that existing views
            * */
            viewHolder = new ViewHolder();
            viewHolder.iv_image = (ImageView)convertView.findViewById(R.id.iv_image);
            viewHolder.layout_row = (LinearLayout)convertView.findViewById(R.id.layout_row);
            viewHolder.tv_deviceName = (TextView)convertView.findViewById(R.id.tv_deviceName);
            viewHolder.tv_deviceDescription = (TextView) convertView.findViewById(R.id.tv_deviceDescription);
            viewHolder.tv_contents = (TextView) convertView.findViewById(R.id.tv_contents);
            convertView.setTag(viewHolder);
        }
        else
            viewHolder = (ViewHolder)convertView.getTag();

        Item whatItem = container.getItemByIndex(position);
        if (whatItem instanceof Device)
        {
            if(((Device) whatItem).getType() == Device.Type.ACTUATOR)
                viewHolder.layout_row.setBackgroundColor(Color.GREEN);
            else
                viewHolder.layout_row.setBackgroundColor(Color.GRAY);

            switch(((Device) whatItem).getCategory())
            {
                case Device.LIGHT :
                viewHolder.iv_image.setImageResource(R.mipmap.oc_light);
                    break;
                case Device.JEALOUSE :
                    viewHolder.iv_image.setImageResource(R.mipmap.oc_jealouse);
                    break;
                case Device.HEATING :
                    viewHolder.iv_image.setImageResource(R.mipmap.ic_heating);
                    break;
            }
        }
        else
        {
            Log.d(DEBUG_TAG, "getView() Container : " + ((Container)whatItem).getItemCount() );
            viewHolder.tv_contents.setText(((Container) whatItem).getItemCount() + "");
        }

        String deviceName = container.getItemByIndex(position).getName();
        String deviceDescription = container.getItemByIndex(position).getDescription();

        viewHolder.tv_deviceName.setText(deviceName);
        viewHolder.tv_deviceDescription.setText(deviceDescription);

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(DEBUG_TAG, "getItemViewType()");
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        Log.d(DEBUG_TAG, "getViewTypeCount()");
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    private static class ViewHolder {
        public LinearLayout layout_row;
        public ImageView iv_image;
        public TextView tv_deviceName;
        public TextView tv_deviceDescription;
        public TextView tv_contents;
    }
}
