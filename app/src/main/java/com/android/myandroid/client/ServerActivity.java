package com.android.myandroid.client;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.myandroid.App;
import com.android.myandroid.R;

import java.io.File;

public class ServerActivity extends AppCompatActivity implements Base.Listener {

    private TextView mTips;
    private BtServer mServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        mTips = findViewById(R.id.tv_tips);
        mServer = new BtServer(this);


        //发送图片
        findViewById(R.id.send_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPhoto();
            }
        });
        //发送文件
        findViewById(R.id.send_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFile();
            }
        });
    }

    private void sendFile() {
        //选择文件
        if(mServer.isConnected(null)){
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            this.startActivityForResult(intent, 1);
        }else {
            App.toast("没有连接", 0);
        }


    }

    private void sendPhoto() {
        //选择图片
        if(mServer.isConnected(null)){
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, 0);
        }else {
            App.toast("没有连接", 0);
        }
    }

    @Override
    public void socketNotify(int state, Object obj) {
        if (isDestroyed())
            return;
        String msg = null;
        switch (state) {
            case Base.Listener.CONNECTED:
                BluetoothDevice dev = (BluetoothDevice) obj;
                msg = String.format("与%s(%s)连接成功", dev.getName(), dev.getAddress());
                mTips.setText(msg);
                break;
            case Base.Listener.DISCONNECTED:
                mServer.listen();
                msg = "连接断开,正在重新监听...";
                mTips.setText(msg);
                break;
            case Base.Listener.MSG:
                msg = String.format("%s", obj);
                mTips.setText(msg);
                break;
        }
        App.toast(msg, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mServer.unListener();
        mServer.close();
    }
    /**
     * 页面跳转处理
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("111",getPath(data.getData()));
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
        if (mServer.isConnected(null)) {
            if (!new File(getPath(uri)).isFile())
                App.toast("文件无效", 0);
            else
                mServer.sendFile(getPath(uri));
        } else
            App.toast("没有连接", 0);
    }


    @SuppressLint("NewApi")
    public  String getPath(final Uri uri)
    {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(ServerActivity.this, uri))
        {
            // ExternalStorageProvider
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

                return getDataColumn(ServerActivity.this, contentUri, null, null);

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

                return getDataColumn(ServerActivity.this, contentUri, selection, selectionArgs);

            }

        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme()))
        {
            return getDataColumn(ServerActivity.this, uri, null, null);

        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme()))
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
    public static boolean isExternalStorageDocument(Uri uri)
    {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());

    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */

    public static boolean isDownloadsDocument(Uri uri)
    {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());

    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */

    public static boolean isMediaDocument(Uri uri)
    {
        return "com.android.providers.media.documents".equals(uri.getAuthority());

    }
}
