/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.hellojni;

import android.app.Activity;
import android.widget.TextView;
import android.widget.Button;
import android.os.Bundle;
import android.view.View;
import android.app.DownloadManager;
import android.os.Build;
import android.os.Environment;
import android.net.Uri;
import android.content.Context;
import android.content.Intent;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import android.content.res.AssetManager;

public class HelloJni extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Button button;
        button = new Button(this);
        button.setText("Press me");
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                try {
                    System.loadLibrary("hello-mother");
                    button.setText(stringFromJNI2());
                } catch (UnsatisfiedLinkError e) {
                    button.setText("hello-mother not found");
//                    String url = "url you want to download";
//                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
//                    request.setDescription("Some descrition");
//                    request.setTitle("Some title");
//                    // in order for this if to run, you must use the android 3.2 to compile your app
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                        request.allowScanningByMediaScanner();
//                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//                    }
//                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "name-of-the-file.ext");
//
//                    // get download service and enqueue file
//                    DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//                    manager.enqueue(request);

                    final String apkFile = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/Offloadme.apk";
                    String className = "com.khaledalanezi.offloadme.SimpleCalculator";
                    String methodToInvoke = "add";

                    final File optimizedDexOutputPath = getDir("outdex", 0);

                    DexClassLoader dLoader = new DexClassLoader(apkFile,optimizedDexOutputPath.getAbsolutePath(),
                            null,ClassLoader.getSystemClassLoader().getParent());

                    try {
                        Class<?> loadedClass = dLoader.loadClass(className);
                        Object obj = (Object)loadedClass.newInstance();
                        int x =5;
                        int y=6;
                        Method m = loadedClass.getMethod(methodToInvoke, int.class, int.class);
                        int z = (Integer) m.invoke(obj, y, x);
                        System.out.println("The sum of "+x+" and "+"y="+z);

                    } catch (ClassNotFoundException ee) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InstantiationException ee) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalAccessException ee) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (NoSuchMethodException ee) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalArgumentException ee) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InvocationTargetException ee) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
        setContentView(button);
    }

    /* A native method that is implemented by the
     * 'hello-jni' native library, which is packaged
     * with this application.
     */
    public native String  stringFromJNI();
    public native String  stringFromJNI2();

    /* This is another native method declaration that is *not*
     * implemented by 'hello-jni'. This is simply to show that
     * you can declare as many native methods in your Java code
     * as you want, their implementation is searched in the
     * currently loaded native libraries only the first time
     * you call them.
     *
     * Trying to call this function will result in a
     * java.lang.UnsatisfiedLinkError exception !
     */
    public native String  unimplementedStringFromJNI();

    /* this is used to load the 'hello-jni' library on application
     * startup. The library has already been unpacked into
     * /data/data/com.example.hellojni/lib/libhello-jni.so at
     * installation time by the package manager.
     */
    static {
        System.loadLibrary("hello-jni");
    }
}
