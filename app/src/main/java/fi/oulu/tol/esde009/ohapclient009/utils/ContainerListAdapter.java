package fi.oulu.tol.esde009.ohapclient009.utils;

import android.content.Context;
import android.database.DataSetObservable;
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
import com.opimobi.ohap.EventSource;
import com.opimobi.ohap.Item;

import fi.oulu.tol.esde009.ohapclient009.R;
import fi.oulu.tol.esde009.ohapclient009.network.CentralUnitConnection;

/**
 * Created by bel on 01.04.16.
 */
public class ContainerListAdapter implements ListAdapter, EventSource.Listener<Container, Item> {

    private static final String DEBUG_TAG = "Debug_ListAdapter";
    private Container container;
    private DataSetObservable dataSetObservable;

    public ContainerListAdapter(Container container) {
        Log.d(DEBUG_TAG, "ContainerListAdapter()");
        if(container != null) {
            this.container = container;

            Log.d(DEBUG_TAG, "container id : " + container.getId());
            dataSetObservable = new DataSetObservable();
            /*
            *  A way to inform the ContainerListAdapter when the Container has changed,
            *  and further the ListView that the ContainerListAdapter has changed
            * */
            container.itemAddedEventSource.addListener(this);
            container.itemRemovedEventSource.addListener(this);
//            ((CentralUnitConnection) container).initializeEventListener(this);
        }
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
        Log.d(DEBUG_TAG, "registerDataSetObserver()");
        /*
        * DataSetObservable contains a list of registered observer that can be used when notifying on changes in the observed object.
        * */
        dataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        Log.d(DEBUG_TAG, "unregisterDataSetObserver()");
        if(observer != null)
            dataSetObservable.unregisterObserver(observer);

        //container.itemAddedEventSource.removeListener(this);
    }

    @Override
    public int getCount() {
        Log.d(DEBUG_TAG, "getCount() count is : " + container.getItemCount());
        return container.getItemCount();
    }

    @Override
    public Object getItem(int position) {
        //Log.d(DEBUG_TAG, "getItem()");
        return container.getItemByIndex(position);
    }

    @Override
    public long getItemId(int position) {
        //Log.d(DEBUG_TAG, "getItemId()");
        return container.getItemByIndex(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        //Log.d(DEBUG_TAG, "hasStableIds()");
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

        }

        String deviceName = container.getItemByIndex(position).getName();
        String deviceDescription = container.getItemByIndex(position).getDescription();

        viewHolder.tv_deviceName.setText(deviceName);
        viewHolder.tv_deviceDescription.setText(deviceDescription);

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        //Log.d(DEBUG_TAG, "getItemViewType()");
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        //Log.d(DEBUG_TAG, "getViewTypeCount()");
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }


//  The method called when an item added or removed from the Container
    @Override
    public void onEvent(Container container, Item item) {
        Log.d(DEBUG_TAG, "onEvent()");
        Log.d(DEBUG_TAG, "Container : " + container.getName() + " Id : " + container.getId());
        Log.d(DEBUG_TAG, "Item : " + item.getId());

        dataSetObservable.notifyChanged();
        Log.d(DEBUG_TAG, "dataSetObservable.notifyChanged()");
    }

    private static class ViewHolder {
        public LinearLayout layout_row;
        public TextView tv_deviceName;
        public TextView tv_deviceDescription;
        public TextView tv_contents;
    }
}
