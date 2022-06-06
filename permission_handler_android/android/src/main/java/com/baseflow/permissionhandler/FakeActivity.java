package com.baseflow.permissionhandler;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;

import android.app.Activity;

import io.flutter.plugin.common.PluginRegistry;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;

import android.util.Log;



public class FakeActivity extends Activity implements PermissionManager.ActivityRegistry, PermissionManager.PermissionRegistry {

    private MethodChannel methodChannel;
    private MethodCallHandlerImpl methodCallHandler;

    private List<PluginRegistry.RequestPermissionsResultListener> _handlers = new ArrayList<PluginRegistry.RequestPermissionsResultListener>();

    public void onCreate(Bundle savedInstanceState) {
        Log.d("fake", "fake activity Created");
        super.onCreate(savedInstanceState);
        int idx = getIntent().getIntExtra("HANDLER_INDEX", -1);
        MethodCallHandlerImpl.handle(idx, this);
    }
    public void addListener(PluginRegistry.ActivityResultListener handler) {

    }

    public void addListener(PluginRegistry.RequestPermissionsResultListener handler) {
        _handlers.add(handler);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("fake", "got request permissions result");
        for (PluginRegistry.RequestPermissionsResultListener handler : _handlers) {
            handler.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        finish();
    }

}