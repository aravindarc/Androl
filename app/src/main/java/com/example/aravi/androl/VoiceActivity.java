package com.example.aravi.androl;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.OutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class VoiceActivity extends ActionBarActivity {

    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String deviceAddress;
    private EditText editText;
    private TextView textView2;
    private Button micButton;
    private ProgressDialog progress;
    private Button clearButton;
    private BluetoothSocket socket = null;
    private BluetoothDevice device = null;
    private OutputStream outputStream;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private boolean connected = true;
    private View b;
    private OutputStream output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        deviceAddress = newint.getStringExtra( MainActivity.DEVICE_NAME );
        setContentView(R.layout.activity_voice);

        textView2 = (TextView) findViewById(R.id.textView2);
        editText = (EditText) findViewById(R.id.editText3);
        micButton = (Button) findViewById(R.id.button8);
        clearButton = (Button) findViewById(R.id.button10);
        b = findViewById(R.id.button10);

        micButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick( View v ) {
                speechPrompt();
            }
        });
        new BTconnect().execute();
    }

    private class BTconnect extends AsyncTask<Void, Void, Void>
    {
        private boolean connected = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(VoiceActivity.this, "Connecting...", "Please wait");
        }

        @Override
        protected Void doInBackground(Void... devices)
        {
            try
            {
                if(socket == null) {
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                    socket = device.createInsecureRfcommSocketToServiceRecord(PORT_UUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    socket.connect();
                    output = socket.getOutputStream();
                }
            } catch (IOException e) {
                connected = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            if(!connected) {
                msg("Connection Failed. Check if it is a SPP!");
                finish();
            }
            else {
                msg("Connected.");
                connected = true;
            }
            progress.dismiss();
        }
    }
    private void speechPrompt()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra( RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM );
        intent.putExtra( RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault() );
        intent.putExtra( RecognizerIntent.EXTRA_PROMPT, "Say something" );
        try {
            startActivityForResult( intent, REQ_CODE_SPEECH_INPUT );
        } catch ( ActivityNotFoundException e ) {
            Toast.makeText( getApplicationContext(), "Speech input not supported in device!", Toast.LENGTH_SHORT ).show();
        }
    }

    @Override
    protected  void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );

        switch ( requestCode ) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    editText.setText(result.get(0));
                }
                break;
            }
        }
    }

    public void onClickMethodVoiceSend(View view) {
        if( connected ) {
            String string = editText.getText().toString();
            string = "*" + string + "#";
            try {
                output.write(string.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            b.setVisibility(view.VISIBLE);
            textView2.append("\nSent command: " + string + "\n");
            editText.setText("");
        }
        else
            msg("Failed!");
    }

    public void onClickMethodVoiceClear() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                msg("Error");
            }
        }
        finish();
    }

    public void msg( String s )
    {
        Toast.makeText( getApplicationContext(), s, Toast.LENGTH_SHORT ).show();
    }

    @Override
    public void onBackPressed() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                msg("Error");
            }
        }
        finish();
    }

}

