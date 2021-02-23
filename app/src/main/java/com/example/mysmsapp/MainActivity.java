package com.example.mysmsapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";


    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        allowSMSPermissions();
        Button button = findViewById(R.id.button);
        setReceiver();
        button.setOnClickListener(view -> sendMessage());
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void allowSMSPermissions() {
        requestPermissions(new String[]{
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_LOGS,
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.w("before", "Logcat save");

            File file = new File(getExternalCacheDir() + "/mylog" + System.currentTimeMillis() + ".txt");
            try {
                if (!file.exists()) {
                    Files.createFile(file.toPath());
                }
                String cmd = "logcat -f " + file.getAbsolutePath()+" -s MainActivity" ;
                Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    private void sendMessage() {
        List<SimInfo> simInfo = readSimInfo(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose SIM");
        String[] sims = null;
        if (simInfo.size() > 1) {
            sims = new String[]{simInfo.get(0).carrierDisplayName + "-" + simInfo.get(0).subscriptionId, simInfo.get(1).carrierDisplayName + "-" + simInfo.get(1).subscriptionId};
        } else {
            sims = new String[]{simInfo.get(0).carrierDisplayName + "-" + simInfo.get(0).subscriptionId};
        }

        String smsText = ((AppCompatEditText) findViewById(R.id.smsEditText)).getText().toString();
        String mobileNumber = ((AppCompatEditText) findViewById(R.id.mobileEditText)).getText().toString();

        builder.setItems(sims, (dialog, which) -> {
            try {
                PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                        new Intent(SENT), 0);

                PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                        new Intent(DELIVERED), 0);
                SmsManager smsManager;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    Log.d(TAG,"Using Subscription id "+simInfo.get(which).subscriptionId);
                    Log.d(TAG,"Default Subscription id "+ SmsManager.getDefault().getSubscriptionId());
                    smsManager = SmsManager.getSmsManagerForSubscriptionId(simInfo.get(which).subscriptionId);
                } else {
                    Log.d(TAG,"Using Default subscription");
                    smsManager = SmsManager.getDefault();
                }
                for (String key : smsManager.getCarrierConfigValues().keySet()) {
                    Log.d(TAG, "init sms manager---> " + key + " = \"" + smsManager.getCarrierConfigValues().get(key) + "\"");
                }
                smsManager.sendTextMessage(mobileNumber, null, smsText, sentPI, deliveredPI);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setReceiver() {
        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                for (String key : arg1.getExtras().keySet()) {
                    Log.d(TAG, "onReceive SENT BR ---> " + key + " = \"" + arg1.getExtras().get(key) + "\"");
                }
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "ACTION SENT - SMS sent");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "ACTION SENT - Generic failure");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "ACTION SENT - No service");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "ACTION SENT - Null PDU");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "ACTION SENT - Radio off");
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                for (String key : arg1.getExtras().keySet()) {
                    Log.d(TAG, "onReceive DEL BR ---> " + key + " = \"" + arg1.getExtras().get(key) + "\"");
                }
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "ACTION DELIVERED - SMS delivered");
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "ACTION DELIVERED - SMS not delivered");
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

    }

    @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
    public List<SimInfo> readSimInfo(Context context) {
        List<SimInfo> simInfos = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            final SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            final List<SubscriptionInfo> activeSubscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();

            if (activeSubscriptionInfoList == null)
                return simInfos;
            for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
                SimInfo simInfo = SimInfo.convertSubscriptionInfoToSimInfo(subscriptionInfo);
                Log.d(TAG, "Reading Sim Info --> " + simInfo.toString());
                simInfos.add(simInfo);
            }
        } else {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            SimInfo simInfo = new SimInfo();
            simInfo.carrierName = tm.getNetworkOperatorName();
            simInfo.carrierDisplayName = tm.getSimOperatorName();
            try {
                simInfo.mcc = Integer.parseInt(tm.getNetworkOperator().substring(0, 3));
            } catch (Exception ignored) {
                simInfo.mcc = -1;
            }
            try {
                simInfo.mnc = Integer.parseInt(tm.getNetworkOperator().substring(3));
            } catch (Exception ignored) {
                simInfo.mnc = -1;
            }
            simInfo.slotIndex = 0;
            simInfo.subscriptionId = 1;
            simInfo.number = tm.getLine1Number();
            simInfos.add(simInfo);
        }
        return simInfos;
    }
}