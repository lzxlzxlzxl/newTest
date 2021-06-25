package com.android.myandroid.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Util {
    private static final String TAG = Util.class.getSimpleName();
    public static final Executor EXECUTOR = Executors.newCachedThreadPool();        //可缓存的线程池

    public static void mkdirs(String filePath) {
        boolean mk = new File(filePath).mkdirs();
        Log.d(TAG, "mkdirs: " + mk);
    }
    public static final void cpAssertToLocalPath(Context context, String sourceName, String target) {
        //Intrinsics.checkParameterIsNotNull(context, "context");
        //Intrinsics.checkParameterIsNotNull(sourceName, "sourceName");
        //Intrinsics.checkParameterIsNotNull(target, "target");

        try {
            InputStream myInput = null;
            OutputStream myOutput = (OutputStream)(new FileOutputStream(target));
            InputStream is= context.getAssets().open(sourceName);
            //Intrinsics.checkExpressionValueIsNotNull(is, "context.assets.open(sourceName)");
            myInput = is;
            byte[] buffer = new byte[1024];
            for(int length = myInput.read(buffer); length > 0; length = myInput.read(buffer)) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
            myInput.close();
            myOutput.close();
        } catch (IOException e) {
           e.printStackTrace();
        }

    }
}
