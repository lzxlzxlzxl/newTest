package com.android.myandroid.adapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.myandroid.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class DevAdapter extends RecyclerView.Adapter<DevAdapter.VH> {
    private static final String TAG = DevAdapter.class.getSimpleName();
    private final List<BluetoothDevice> mDevices = new ArrayList<>();
    private final Listener mListener;

    public DevAdapter(Listener listener) {
        mListener = listener;
        addBound();
    }

    private void addBound() {
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (bondedDevices != null)
            mDevices.addAll(bondedDevices);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dev, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VH holder, int position) {
        BluetoothDevice dev = mDevices.get(position);
        String name = dev.getName();
        String address = dev.getAddress();
        int bondState = dev.getBondState();
        String address_state = String.format("%s (%s)", address, bondState == 10 ? "未配对" : "配对");
        holder.name.setText(name+"  "+address_state);
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public void add(BluetoothDevice dev) {
        if (mDevices.contains(dev))
            return;
        if (dev.getName()==null){
            mDevices.remove(dev);
        }
        else {
            mDevices.add(dev);
            notifyDataSetChanged();
        }
    }

    public void reScan() {
        mDevices.clear();
        addBound();
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        if (!bt.isDiscovering())
            bt.startDiscovery();
        notifyDataSetChanged();
    }

    class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView name;

        VH(final View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            name = itemView.findViewById(R.id.name);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Log.d(TAG, "onClick, getAdapterPosition=" + pos);
            if (pos >= 0 && pos < mDevices.size())
                mListener.onItemClick(mDevices.get(pos));
        }
    }

    public interface Listener {
        void onItemClick(BluetoothDevice dev);
    }
}
