package com.android.myandroid.client;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.myandroid.App;
import com.android.myandroid.R;
import com.android.myandroid.util.Comment;
import com.android.myandroid.util.Util;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.hprt.lib.mt800.HPRTPrinterHelper;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BluetoothManagerActivity extends AppCompatActivity implements Base.Listener {


    private TextView device_name;
    private LinearLayout CancelPair;
    private LinearLayout send_photo_ll;
    private LinearLayout printer;
    private LinearLayout SendFile;
    private BTBroadcastReceiver receiver;
    private BluetoothDevice mDev;
    private BtClient mClient=new BtClient(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_manager);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");


        mDev=bundle.getParcelable("dev");

        receiver = new BTBroadcastReceiver(mHandler);
        registerReceiver(receiver, makeFilters());
        init();
        init2();

        if (mClient.isConnected(mDev)) {
            App.toast("已经连接了", 0);
        }else {
            mClient.connect(mDev);
            App.toast("正在连接...", 0);
        }
    }

    //初始化
    private void init() {
        device_name = (TextView) findViewById(R.id.device_name);
        device_name.setText(mDev.getName());
        CancelPair = (LinearLayout) findViewById(R.id.CancelPair);
        CancelPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unpairDevice();
            }
        });
        send_photo_ll = (LinearLayout) findViewById(R.id.send_photo_ll);
        send_photo_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPhoto();
            }
        });
        SendFile=(LinearLayout)findViewById(R.id.SendFile);
        SendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFile();
            }
        });
    }

    //打印按钮事件初始化
    public void init2(){
        printer=(LinearLayout)findViewById(R.id.printer);
        printer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder stringBuilder=new StringBuilder();
                //printLabel();
                File file=Utils.getApp().getExternalCacheDir();
                String path=stringBuilder.append(String.valueOf(file != null ? file.getAbsolutePath() : null)).append("/test.pdf").toString();
                if(!FileUtils.isFileExists(path)){
                   Context context=BluetoothManagerActivity.this.getBaseContext();
                    //Intrinsics.checkExpressionValueIsNotNull(context ,"baseContext");
                    Util.cpAssertToLocalPath(context, "test.pdf", path);
                }
                Bitmap bitmap = HPRTPrinterHelper.INSTANCE.pdfToImage(new File(path), 0, 2336);
                final boolean result = HPRTPrinterHelper.INSTANCE.printBitmap(bitmap);
                bitmap.recycle();
                BluetoothManagerActivity.this.runOnUiThread((Runnable)(new Runnable() {
                    public final void run() {
                        ToastUtils.showShort("数据下发结果:" + result, new Object[0]);
                    }
                }));
            }
        });
    }


    private void sendFile() {
        if(mDev!=null){
            //选择文件  打开文件选择器
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            this.startActivityForResult(intent, 1);
        }else {
            App.toast("请连接设备...", 0);
        }
    }

    private void sendPhoto() {
        if(mDev!=null){
            //选择图片    打开图库
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, 0);
        }else {
            App.toast("请连接设备...", 0);
        }
    }

    /**
     * 广播拦截
     *
     * @return IntentFilter
     */
    public  IntentFilter makeFilters() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//开关监听
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);//查询
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//查询结束
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//绑定状况
        return intentFilter;
    }
    /**
     * 解除配对
     */
    public void unpairDevice() {
        Method removeBondMethod = null;
        try {
            removeBondMethod = mDev.getClass().getMethod("removeBond");
            removeBondMethod.invoke(mDev);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Handler mHandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Comment.BOND){
                if ((int)msg.obj == BluetoothDevice.BOND_NONE){
                    App.toast("已取消配对",0);
                }
            }
            //switch (msg.what) {
                //case PRINTER_COMMAND_ERROR:
                    //Toast.makeText(BluetoothManagerActivity.this, "请选择正确的打印机指令", Toast.LENGTH_SHORT).show();
                    //break;
                //case CONN_PRINTER:
                    //Toast.makeText(BluetoothManagerActivity.this, "请先连接打印机", Toast.LENGTH_SHORT).show();
                   // break;
           // }
        }
    };
    /**
     * 页面跳转处理
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("111", String.valueOf(data.getData()));

        if(requestCode == 0){
            if (resultCode == this.RESULT_OK){
                sendFile(data);
            }
        }else if(requestCode==1) {
            if (resultCode == this.RESULT_OK){
                sendFile(data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendFile(Intent data) {
        Uri uri = data.getData();
        if (mClient.isConnected(null)) {
            if (!new File(getPath(uri)).isFile())
                App.toast("文件无效", 0);
            else
                mClient.sendFile(getPath(uri));
        } else
            App.toast("没有连接", 0);
    }

    //获取文件的路径
    @SuppressLint("NewApi")
    public  String getPath(final Uri uri)
    {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(BluetoothManagerActivity.this, uri))
        {
            // ExternalStorageProvider内部存储文件提供者
            if (isExternalStorageDocument(uri))
            {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type))
                {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];

                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri))
            {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(BluetoothManagerActivity.this, contentUri, null, null);
                //return getRealPathFromURI(contentUri);

            }
            // MediaProvider
            else if (isMediaDocument(uri))
            {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type))
                {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                } else if ("video".equals(type))
                {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

                } else if ("audio".equals(type))
                {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(BluetoothManagerActivity.this, contentUri, selection, selectionArgs);
                //return getRealPathFromURI(contentUri);

            }


        }
        // MediaStore (and general)
        else if (uri.getScheme( ).compareTo( "content" ) == 0 )            //用这个就无法发送除了图片以外的文件"content".equalsIgnoreCase(uri.getScheme())
        {

            //方法问题
            return getDataColumn(BluetoothManagerActivity.this, uri, null, null);
            //return getRealPathFromURI(uri);

        }
        // File
        else if (uri.getScheme( ).compareTo( "file" ) == 0)
        {
            return uri.getPath();

        }
        return null;
    }
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs)
    {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try
        {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst())
            {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);

            }

        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }

        }
        return null;

    }

    //内部存储空间
    public static boolean isExternalStorageDocument(Uri uri)
    {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());

    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */

    //下载的文件
    public static boolean isDownloadsDocument(Uri uri)
    {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());

    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    //媒体文件
    public static boolean isMediaDocument(Uri uri)
    {
        return "com.android.providers.media.documents".equals(uri.getAuthority());

    }

    //通信的通知
    @Override
    public void socketNotify(int state, Object obj) {
        if (isDestroyed())
            return;
        String msg = null;
        switch (state) {
            case Base.Listener.CONNECTED:
                BluetoothDevice dev = (BluetoothDevice) obj;
                msg = String.format("与%s(%s)连接成功", dev.getName(), dev.getAddress());
                break;
            case Base.Listener.DISCONNECTED:
                msg = "连接断开";
                break;
            case Base.Listener.MSG:
                msg = String.format("%s", obj);
                break;
        }
        App.toast(msg, 0);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mClient.unListener();
        mClient.close();
        unregisterReceiver(receiver);
    }
}
