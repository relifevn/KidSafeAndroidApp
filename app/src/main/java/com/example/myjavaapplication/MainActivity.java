package com.example.myjavaapplication;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Button;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import android.os.Build;
import android.content.DialogInterface;
import android.telephony.SmsManager;
import android.widget.TextView;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.SEND_SMS;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;
import java.net.URISyntaxException;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonCall, buttonSendSMS, buttonConnectSocket, buttonEmitTest;
    private TextView textConnectionStatus;

    private static final int PERMISSION_REQUEST_CODE = 200;
    private View view;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("https://nhom-khkt-hiep-phuoc.herokuapp.com/");
        } catch (URISyntaxException e) {
//            e.printStackTrace();
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "Error - IO socket failed", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        /* Call Handler */
        buttonCall = (Button) findViewById(R.id.buttonCall);
        buttonCall.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:0387358924"));

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{CALL_PHONE},
                        PERMISSION_REQUEST_CODE);
                }
                startActivity(callIntent);
            }
        });

        /* Send SMS Handler */
        buttonSendSMS = (Button) findViewById(R.id.buttonSendSMS);
        buttonSendSMS.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{SEND_SMS},
                        PERMISSION_REQUEST_CODE);
                }
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage("0387358924", null, "Hello There!", null, null);
            }
        });

        /* Permission Handler */
        Button check_permission = (Button) findViewById(R.id.check_permission);
        Button request_permission = (Button) findViewById(R.id.request_permission);
        check_permission.setOnClickListener(this);
        request_permission.setOnClickListener(this);

        /* Socket handler */
        buttonConnectSocket = (Button) findViewById(R.id.buttonConnectSocket);
        buttonConnectSocket.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Snackbar.make(view, "Start to connect to Socket.", Snackbar.LENGTH_LONG).show();
                mSocket.connect();
            }
        });

        textConnectionStatus = (TextView) findViewById(R.id.textConnectionStatus);

        buttonEmitTest = (Button) findViewById(R.id.buttonEmitTest);
        buttonEmitTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("key", "school_1");
                    mSocket.emit("select-school", obj);
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                            "Test", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } catch (JSONException e) {
//                    e.printStackTrace();
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                            "Error - Emit test - Extract JSON failed", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });

        mSocket.on("connection", new Emitter.Listener() {
            @Override public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        JSONObject data = (JSONObject) args[0];
                        textConnectionStatus.setText("connected");
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "Connected", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                });
            }
        });

        mSocket.on("vehicles-result", new Emitter.Listener() {
            @Override public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        JSONObject data = (JSONObject) args[0];
                        textConnectionStatus.setText("received");
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:0387358924"));

                        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{CALL_PHONE},
                                    PERMISSION_REQUEST_CODE);
                        }
                        startActivity(callIntent);
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                                "Received Test", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                });
            }
        });

        mSocket.on("call", new Emitter.Listener() {
            @Override public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        JSONObject data = (JSONObject) args[0];
                        try {
                            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                                    "Received Call", Snackbar.LENGTH_LONG);
                            snackbar.show();

                            String phoneNumber = data.getString("phoneNumber");

                            textConnectionStatus.setText("received");
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:" + phoneNumber));

                            if (ContextCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{CALL_PHONE},
                                        PERMISSION_REQUEST_CODE);
                            }
                            startActivity(callIntent);


                        } catch (JSONException e) {
//                            e.printStackTrace();
                            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                                    "Error - Received Call - Extract JSON failed", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                    }
                });
            }
        });

        mSocket.on("sendSMS", new Emitter.Listener() {
            @Override public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        JSONObject data = (JSONObject) args[0];
                        try {
                            String phoneNumber = data.getString("phoneNumber");
                            String message = data.getString("message");

                            if (ContextCompat.checkSelfPermission(MainActivity.this,
                                    SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{SEND_SMS},
                                        PERMISSION_REQUEST_CODE);
                            }
//                            SmsManager smsManager = SmsManager.getDefault();
//                            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

                            SMSUtils.sendSMS(getApplicationContext(), phoneNumber, message);

                            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                                    "Send SMS to " + phoneNumber + " - with message " + message, Snackbar.LENGTH_LONG);
                            snackbar.show();
                        } catch (JSONException e) {
//                            e.printStackTrace();
                            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                                    "Error - Received SendSMS - Extract JSON failed", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }

                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        view = v;
        int id = v.getId();
        switch (id) {
            case R.id.check_permission:
                if (checkPermission()) {
                    Snackbar.make(view, "Permission already granted.", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(view, "Please request permission.", Snackbar.LENGTH_LONG).show();
                }
                break;
            case R.id.request_permission:
                if (!checkPermission()) {
                    requestPermission();
                } else {
                    Snackbar.make(view, "Permission already granted.", Snackbar.LENGTH_LONG).show();
                }
                break;
        }

    }

    private boolean checkPermission() {
        int result_CALL_PHONE = ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE);
        int result_SEND_SMS = ContextCompat.checkSelfPermission(getApplicationContext(), SEND_SMS);

        return result_CALL_PHONE == PackageManager.PERMISSION_GRANTED
                && result_SEND_SMS == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CALL_PHONE, SEND_SMS}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean sendSMSAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean callAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;



                    if (sendSMSAccepted && callAccepted)
                        Snackbar.make(view, "Permission Granted, Now you can call phone & send sms.",
                                Snackbar.LENGTH_LONG).show();
                    else {
                        Snackbar.make(view, "Permission Denied, Now you can't call phone or send sms.",
                                Snackbar.LENGTH_LONG).show();
                        if (!sendSMSAccepted) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (shouldShowRequestPermissionRationale(SEND_SMS)) {
                                    showMessageOKCancel("You need to allow access to send sms",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{SEND_SMS},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                }
                            }
                        }
                        if (!callAccepted) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (shouldShowRequestPermissionRationale(CALL_PHONE)) {
                                    showMessageOKCancel("You need to allow access to send sms",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CALL_PHONE},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                }
                            }
                        }


                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
//        mSocket.off("new message", onNewMessage);
    }

}
