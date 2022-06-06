package com.baseflow.permissionhandler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;

import java.util.List;

import com.baseflow.permissionhandler.FakeActivity;

import android.util.Log;

import java.util.ArrayList;

final class MethodCallHandlerImpl implements MethodChannel.MethodCallHandler {
    private final Context applicationContext;
    private final AppSettingsManager appSettingsManager;
    private final PermissionManager permissionManager;
    private final ServiceManager serviceManager;

    MethodCallHandlerImpl(
            Context applicationContext,
            AppSettingsManager appSettingsManager,
            PermissionManager permissionManager,
            ServiceManager serviceManager) {
        this.applicationContext = applicationContext;
        this.appSettingsManager = appSettingsManager;
        this.permissionManager = permissionManager;
        this.serviceManager = serviceManager;
    }

    @Nullable
    private Activity activity;

    public void setActivity(@Nullable Activity activity) {
      this.activity = activity;
    }

    static List<MethodCallHandlerImpl> handlers = new ArrayList<MethodCallHandlerImpl>();
    static List<MethodCall> calls = new ArrayList<MethodCall>();
    static List<Result> results = new ArrayList<Result>();
    public static void handle(int index, FakeActivity activity) {
        MethodCallHandlerImpl handler = handlers.get(index);
        handler.setActivity(activity);
        handler.onMethodCall(calls.get(index), results.get(index));
        handlers.remove(index);
        calls.remove(index);
        results.remove(index);
    }

  @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull final Result result)
    {
        Log.d(PermissionConstants.LOG_TAG , "im on MethodCall handler");
        if(activity == null) {
            Log.d(PermissionConstants.LOG_TAG , "activity is null");
            Intent intent = new Intent(applicationContext, FakeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            handlers.add(this);
            calls.add(call);
            results.add(result);
            intent.putExtra("HANDLER_INDEX", handlers.size() - 1);
            intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            applicationContext.startActivity(intent);
        } else {
            switch (call.method) {
                case "checkServiceStatus": {
                    @PermissionConstants.PermissionGroup final int permission = Integer.parseInt(call.arguments.toString());
                    serviceManager.checkServiceStatus(
                            permission,
                            applicationContext,
                            result::success,
                            (String errorCode, String errorDescription) -> result.error(
                                    errorCode,
                                    errorDescription,
                                    null));

                    break;
                }
                case "checkPermissionStatus": {
                    @PermissionConstants.PermissionGroup final int permission = Integer.parseInt(call.arguments.toString());
                    permissionManager.checkPermissionStatus(
                            permission,
                            applicationContext,
                            result::success);
                    break;
                }
                case "requestPermissions":
                    final List<Integer> permissions = call.arguments();
                    permissionManager.requestPermissions(
                            permissions,
                            activity,
                            result::success,
                            (String errorCode, String errorDescription) -> result.error(
                                    errorCode,
                                    errorDescription,
                                    null));

                    break;
                case "shouldShowRequestPermissionRationale": {
                    @PermissionConstants.PermissionGroup final int permission = Integer.parseInt(call.arguments.toString());
                    permissionManager.shouldShowRequestPermissionRationale(
                            permission,
                            activity,
                            result::success,
                            (String errorCode, String errorDescription) -> result.error(
                                    errorCode,
                                    errorDescription,
                                    null));

                    break;
                }
                case "openAppSettings":
                    appSettingsManager.openAppSettings(
                            applicationContext,
                            result::success,
                            (String errorCode, String errorDescription) -> result.error(
                                    errorCode,
                                    errorDescription,
                                    null));

                    break;
                default:
                    result.notImplemented();
                    break;
            }
        }
    }
}
