package com.example.scannersampleandroid;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.aidc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BarcodeReader.BarcodeListener, BarcodeReader.TriggerListener {

    Context _context=this;
    private com.honeywell.aidc.BarcodeReader barcodeReader;
    private AidcManager manager;
    static String TAG="ScannerSample";
    TextView txtData;
    Button btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set lock the orientation
        // otherwise, the onDestory will trigger when orientation changes
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        txtData = (TextView) findViewById(R.id.txtData);
        txtData.setMovementMethod(new ScrollingMovementMethod());

        btnScan=(Button)findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(barcodeReader!=null){
                    Log.i(TAG, "onTriggerEvent");
                    try {
                        // only handle trigger presses
                        // turn on/off aimer, illumination and decoding
                        barcodeReader.aim(true);
                        barcodeReader.light(true);
                        barcodeReader.decode(true);
                    } catch (ScannerNotClaimedException e) {
                        e.printStackTrace();
                        txtData.setText("Scanner not claimed");
                    } catch (ScannerUnavailableException e) {
                        e.printStackTrace();
                        txtData.setText("Scanner unavailable");
                    }
                }
            }
        });

        // create the AidcManager providing a Context and an
        // CreatedCallback implementation.
        Log.i(TAG, "AidcManager.create...");
        AidcManager.create(this, new AidcManager.CreatedCallback() {

                    @Override
                    public void onCreated(AidcManager aidcManager) {
                        Log.i(TAG, "AidcManager.create: onCreated()...");
                        manager = aidcManager;
                        try {
                            Log.i(TAG, "AidcManager.create: onCreated() createBarcodeReader()...");
                            barcodeReader = manager.createBarcodeReader();
                            if (barcodeReader != null) {
                                Log.i(TAG, "barcodeReader created, setting callbacks and properties...");
                                barcodeReader.claim();
                                // register bar code event listener
                                barcodeReader.addBarcodeListener(MainActivity.this);

                                // set the trigger mode to client control
                                try {
                                    barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                                            BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);// BarcodeReader.TRIGGER_CONTROL_MODE_CLIENT_CONTROL);
                                } catch (UnsupportedPropertyException e) {
                                    Toast.makeText(MainActivity.this, "Failed to apply properties", Toast.LENGTH_SHORT).show();
                                }
                                // register trigger state change listener
                                barcodeReader.addTriggerListener(MainActivity.this);

                                Map<String, Object> properties = new HashMap<String, Object>();
                                // Set Symbologies On/Off
                                properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
                                properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
                                properties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
                                properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
                                properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
                                properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
                                properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, false);
                                properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
                                properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
                                properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
                                properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, false);
                                // Set Max Code 39 barcode length
                                properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 10);
                                // Turn on center decoding
                                properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
                                // Disable bad read response, handle in onFailureEvent
                                properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, false);
                                // Apply the settings
                                barcodeReader.setProperties(properties);
                            }else {
                                txtData.setText("Failed to get BarcodeReader");
                            }

                        } catch (InvalidScannerNameException e) {
                            e.printStackTrace();
                            Log.e(TAG, "InvalidScannerNameException: "+e.getMessage());
                            txtData.setText("InvalidScannerNameException: "+e.getMessage());
                        } catch (ScannerUnavailableException e) {
                            Log.e(TAG, "ScannerUnavailableException: "+e.getMessage());
                            txtData.setText("ScannerUnavailableException: "+e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });

    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        if (barcodeReader != null) {
            Log.i(TAG, "claim barcodeReader");
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                Log.e(TAG, "claim barcodeReader failed: "+e.getMessage());
                Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Log.i(TAG, "onResume called without valid barcodeReader!");
        }
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        if (barcodeReader != null) {
            Log.i(TAG, "release barcodeReader");
            // release the scanner claim so we don't get any scanner
            // notifications while paused.
            barcodeReader.release();
        }
        else{
            Log.i(TAG, "onPause called without valid barcodeReader!");
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        if (barcodeReader != null) {
            Log.i(TAG, "onDestroy remving barcodeReader callbacks!");
            // unregister barcode event listener
            barcodeReader.removeBarcodeListener(this);

            // unregister trigger state change listener
            barcodeReader.removeTriggerListener(this);
        }
        else{
            Log.i(TAG, "onDestroy called without valid barcodeReader!");
        }
    }

    @Override
    public void onBarcodeEvent(final BarcodeReadEvent event) {
        Log.i(TAG, "onBarcodeEvent");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StringBuilder sb=new StringBuilder();
                // update UI to reflect the data
                sb.append("Barcode data: \n" +
                        "--------------------------------------------------" +
                        printWithHex(event.getBarcodeData()) + "\n" +
                        "--------------------------------------------------");
                sb.append("Character Set: \n" + event.getCharset() + "\n");
                sb.append("Code ID: \n" + event.getCodeId() + "\n");
                sb.append("AIM ID: \n" + event.getAimId() + "\n");
                sb.append("Timestamp: \n" + event.getTimestamp() + "\n");
                txtData.setText(sb.toString());
            }
        });
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent arg0) {
        Log.i(TAG, "onFailureEvent");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(_context, "No data", Toast.LENGTH_SHORT).show();
                txtData.setText("No data");
            }
        });
    }

    @Override
    public void onTriggerEvent(TriggerStateChangeEvent triggerStateChangeEvent) {
        Log.i(TAG, "onTriggerEvent");
        try {
            // only handle trigger presses
            // turn on/off aimer, illumination and decoding
            barcodeReader.aim(triggerStateChangeEvent.getState());
            barcodeReader.light(triggerStateChangeEvent.getState());
            barcodeReader.decode(triggerStateChangeEvent.getState());
        } catch (ScannerNotClaimedException e) {
            e.printStackTrace();
            Toast.makeText(this, "Scanner is not claimed", Toast.LENGTH_SHORT).show();
        } catch (ScannerUnavailableException e) {
            e.printStackTrace();
            Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    String printWithHex(String sIn){
        StringBuilder sOut=new StringBuilder();
        // Step-1 - Convert ASCII string to char array
        char[] ch = sIn.toCharArray();

        for (char c : ch) {
            if(c<0x20 || c>0x7F) {
                // Step-2 Use %H to format character to Hex
                String hexCode = String.format("<0x%H (%d)>", c, (int)c);
                sOut.append(hexCode);
            }else
                sOut.append(c);
        }
        return  sOut.toString();
    }

}
