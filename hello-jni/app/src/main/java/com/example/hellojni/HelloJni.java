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
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.content.Context;
import android.util.Log;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;

public class HelloJni extends Activity
{
    private static final String SECONDARY_DEX_NAME = "sdkbox-dex.apk";
    private static final String CLASS_TO_LOAD = "com.sdkbox.plugin.SDKBox";

    // Buffer size for file copying.  While 8kb is used in this sample, you
    // may want to tweak it based on actual size of the secondary dex file involved.
    private static final int BUF_SIZE = 8 * 1024;

    private Button mCopyButton = null;
    private Button mLoadButton = null;
    private Button mExeButton = null;
    private Button mEnumerateButton = null;

    private DexClassLoader mCL = null;
    private File mDexInternalStoragePath = null;

    private ProgressDialog mProgressDialog = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mCopyButton = (Button) findViewById(R.id.button_copy);
        mLoadButton = (Button) findViewById(R.id.button_load);
        mExeButton = (Button) findViewById(R.id.button_exe);
        mEnumerateButton = (Button) findViewById(R.id.button_enumerate);

        mCopyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                copyJar();
            }
        });

        mEnumerateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                enumerateFiles();
            }
        });

        mLoadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loadJar();
            }
        });

        mExeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                exeJar();
            }
        });
    }

    public void copyJar()
    {
        // Before the secondary dex file can be processed by the DexClassLoader,
        // it has to be first copied from asset resource to a storage location.
        mDexInternalStoragePath = new File(getDir("dex", Context.MODE_PRIVATE), SECONDARY_DEX_NAME);

        if (!mDexInternalStoragePath.exists()) {
            mProgressDialog = ProgressDialog.show(this,
                    getResources().getString(R.string.diag_title),
                    getResources().getString(R.string.diag_message), true, false);
            // Perform the file copying in an AsyncTask.
            (new PrepareDexTask()).execute(mDexInternalStoragePath);
        } else {
//            mToastButton.setEnabled(true);
        }
    }

    public void loadJar() {
        // Internal storage where the DexClassLoader writes the optimized dex file to.
        final File optimizedDexOutputPath = getDir("outdex", Context.MODE_PRIVATE);

        // Initialize the class loader with the secondary dex file.
        mCL = new DexClassLoader(mDexInternalStoragePath.getAbsolutePath(),
                optimizedDexOutputPath.getAbsolutePath(),
                null,
                getClassLoader());
    }

    public void enumerateFiles() {
        String path = mDexInternalStoragePath.getAbsolutePath();
        try {
            DexFile dx = DexFile.loadDex(path,
                    File.createTempFile("opt", "dex", getCacheDir()).getPath(),
                    0);

            // Print all classes in the DexFile
            for (Enumeration<String> classNames = dx.entries(); classNames.hasMoreElements(); ) {
                String className = classNames.nextElement();
                System.out.println("class: " + className);
            }
        } catch (IOException e) {
            Log.w("Hi", "Error opening " + path, e);
        }
    }

    public void exeJar()
    {
        Class clazz = null;
        try {
            Class<?> classToLoad = mCL.loadClass(CLASS_TO_LOAD);

            Method[] array = classToLoad.getMethods();
            for (int i=0; i<array.length; i++)
            {
                Method m = array[i];
                Log.i("Testing", m.getName());
            }
//            Method method = classToLoad.getMethod("configure", )
//            efi = (EffectsInterface) clazz.newInstance();
//            AdColony.configure(this, "version:2.1,store:google", "app185a7e71e1714831a49ec7", "ads");

        } catch (Exception e) {
            Log.w("Hi", "Error loading class " + CLASS_TO_LOAD, e);
            e.printStackTrace();
        }
    }


    // File I/O code to copy the secondary dex file from asset resource to internal storage.
    private boolean prepareDex(File dexInternalStoragePath) {
        BufferedInputStream bis = null;
        OutputStream dexWriter = null;

        try {
            bis = new BufferedInputStream(getAssets().open(SECONDARY_DEX_NAME));
            dexWriter = new BufferedOutputStream(new FileOutputStream(dexInternalStoragePath));
            byte[] buf = new byte[BUF_SIZE];
            int len;
            while((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
                dexWriter.write(buf, 0, len);
            }
            dexWriter.close();
            bis.close();
            return true;
        } catch (IOException e) {
            if (dexWriter != null) {
                try {
                    dexWriter.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            return false;
        }
    }

    private class PrepareDexTask extends AsyncTask<File, Void, Boolean> {

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (mProgressDialog != null) mProgressDialog.cancel();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (mProgressDialog != null) mProgressDialog.cancel();
        }

        @Override
        protected Boolean doInBackground(File... dexInternalStoragePaths) {
            prepareDex(dexInternalStoragePaths[0]);
            return null;
        }
    }
}
