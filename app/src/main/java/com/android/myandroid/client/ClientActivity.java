package com.android.myandroid.client;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.myandroid.App;
import com.android.myandroid.R;
import com.android.myandroid.activity.NameActivity;
import com.android.myandroid.adapter.DevAdapter;
import com.android.myandroid.util.BtReceiver;
import com.android.myandroid.util.Comment;

import static android.bluetooth.BluetoothClass.Device.Major.IMAGING;

public class ClientActivity extends AppCompatActivity implements DevAdapter.Listener, BtReceiver.Listener {

    private final DevAdapter mDevAdapter = new DevAdapter(this);
    private Switch btnSearch;
    private TextView mMobileName;
    private ImageView rotate_img;
    private BtReceiver mBtReceiver;
    private Animation rotate;
    private BluetoothDevice mBluetoothDevice;

    //bluetooth
    private BTBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        receiver = new BTBroadcastReceiver(mHandler);
        registerReceiver(receiver, makeFilters());


        btnSearch = findViewById(R.id.btnSearch);
        mMobileName = findViewById(R.id.mobilename);
        rotate_img = findViewById(R.id.rotate_img);

        mMobileName.setText(BluetoothAdapter.getDefaultAdapter().getName() == null ? "未知设备" : BluetoothAdapter.getDefaultAdapter().getName());

        rotate = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);

        RecyclerView rv = findViewById(R.id.rv_bt);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(mDevAdapter);

        btnSearch.setChecked(true);

        //刷新
        findViewById(R.id.swipe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSearch.setChecked(true);
                rotate_img.startAnimation(rotate);
                mDevAdapter.reScan();
            }
        });

        mBtReceiver = new BtReceiver(this, this);//注册蓝牙广播
        BluetoothAdapter.getDefaultAdapter().startDiscovery();

        //修改手机名称
        findViewById(R.id.name_ll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ClientActivity.this, NameActivity.class);
                intent.putExtra("name",BluetoothAdapter.getDefaultAdapter().getName());
                startActivity(intent);
            }
        });


        //蓝牙开关
        findViewById(R.id.switch_ll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    boolean enable = BluetoothAdapter.getDefaultAdapter().enable(); //直接打开
                    btnSearch.setChecked(true);
                    mDevAdapter.reScan();
                    rotate_img.startAnimation(rotate);
                    if (!enable) {  //申请权限打开失败
                        startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
                    }
                } else {
                    BluetoothAdapter.getDefaultAdapter().disable();
                    mDevAdapter.reScan();
                    btnSearch.setChecked(false);
                }
            }
        });
    }
    @Override
    public void onItemClick(BluetoothDevice dev) {
        mBluetoothDevice=dev;
        connectBound();
    }

    /**
     * 发起配对
     */
    public  void connectBound() {
        if (mBluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mBluetoothDevice.createBond();      //配对
            }
        }else {
            Bundle bundle = new Bundle();
            Intent intent=new Intent(ClientActivity.this,BluetoothManagerActivity.class);
            bundle.putParcelable("dev",mBluetoothDevice);
            intent.putExtra("bundle",bundle);
            startActivity(intent);
        }
    }

    @Override
    public void foundDev(BluetoothDevice dev) {
        if(dev.getBluetoothClass().getMajorDeviceClass() == IMAGING) {
            mDevAdapter.add(dev);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBtReceiver);
        unregisterReceiver(receiver);
    }
    @SuppressLint("HandlerLeak")
    private Handler mHandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Comment.BOND:
                    switch ((int)msg.obj) {
                        case BluetoothDevice.BOND_BONDING://正在配对
                            App.toast("正在配对", 0);
                            break;
//                        case BluetoothDevice.BOND_BONDED://配对结束
//                            Bundle bundle = new Bundle();
//                            Intent intent=new Intent(ClientActivity.this,BluetoothManagerActivity.class);
//                            bundle.putParcelable("dev",mBluetoothDevice);
//                            intent.putExtra("bundle",bundle);
//                            startActivity(intent);
//                            break;
                        case BluetoothDevice.BOND_NONE://取消配对/未配对
                            break;
                    }
                    break;

            }
        }
    };


    public  IntentFilter makeFilters() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//开关监听
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);//查询
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//查询结束
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//绑定状况
        return intentFilter;
    }
}
