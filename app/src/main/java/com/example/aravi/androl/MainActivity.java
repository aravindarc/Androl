package com.example.aravi.androl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class    MainActivity extends AppCompatActivity {
    public static String DEVICE_NAME = "HC-05";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothDevice device;
    private BluetoothSocket socket = null;
    private OutputStream outputStream;
    Button beginButton, sendButton, stopButton, clearButton, voiceButton, tiltButton, manualButton;
    TextView textView;
    EditText editText;
    String deviceAddress;
    boolean deviceConnected = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        beginButton = (Button) findViewById(R.id.button);
        sendButton = (Button) findViewById(R.id.button2);
        stopButton = (Button) findViewById(R.id.button3);
        clearButton = (Button) findViewById(R.id.button4);
        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textView);
        voiceButton = (Button) findViewById(R.id.button5);
        tiltButton = (Button) findViewById(R.id.button6);
        manualButton = (Button) findViewById(R.id.button7);
        setUiEnabled( false );
    }


    public void setUiEnabled( boolean bool )
    {
        beginButton.setEnabled( !bool );
        sendButton.setEnabled( bool );
        stopButton.setEnabled( bool );
        textView.setEnabled( bool );
        editText.setEnabled( bool );
        voiceButton.setEnabled( bool );
        tiltButton.setEnabled( bool );
        manualButton.setEnabled( bool );
    }

    public boolean BTinit()
    {
        boolean found = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if( bluetoothAdapter == null ) {
            Toast.makeText( getApplicationContext(), "No Bluetooth in device", Toast.LENGTH_SHORT).show();
        }
        else
        {
            if( !bluetoothAdapter.isEnabled() ) {
                Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableAdapter, 0);
            }
            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
            if( bondedDevices.isEmpty() ) {
                Toast.makeText( getApplicationContext(), "Please pair robot first!", Toast.LENGTH_SHORT ).show();
            }
            else {
                for( BluetoothDevice iterator : bondedDevices ) {
                    if( iterator.getName().equals( DEVICE_NAME ) ) {
                        device = iterator;
                        deviceAddress = device.getAddress().toString();
                        found = true;
                        break;
                    }
                    if( !found ) {
                        Toast.makeText( getApplicationContext(), "Please pair robot first!", Toast.LENGTH_SHORT ).show();
                    }
                }
            }
        }
        return found;
    }

    public boolean BTconnect()
    {
        boolean connected = true;
        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
        }
        if (connected) {
            try {
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                connected = false;
            }
        }
        return connected;

    }

    public void onClickMethodBegin(View view) {
        if( BTinit() ) {
            if( BTconnect() ) {
                setUiEnabled( true );
                deviceConnected = true;
                textView.append( "\nSerial Channel Enabled!\n");
            }
        }
    }

    public void onClickMethodSend(View view) {
        if( deviceConnected ) {
            String string = editText.getText().toString();
            string = "*"+string+"#";
            try {
                outputStream.write(string.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            textView.append("\nSent command: " + string + "\n");
            editText.setText("");
        }
    }

    public void onClickMethodStop(View view) throws IOException {
        if( deviceConnected ) {
            outputStream.close();
            socket.close();
            setUiEnabled(false);
            deviceConnected = false;
            textView.append("\nConnection Closed!\n");
        }
    }

    public void onClickMethodClear(View view) {
        textView.setText("");
    }

    public void onClickMethodVoice(View view) {
        try {
            socket.close();
        } catch (IOException e) {
            msg("Error");
        }
        if( deviceConnected ) {
            Intent voiceIntent = new Intent(this, VoiceActivity.class);
            voiceIntent.putExtra(DEVICE_NAME, deviceAddress);
            startActivity(voiceIntent);
        }
    }

    public void onClickMethodManual(View view) {
    }

    public void onClickMethodTilt(View view) {
    }

    public void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume()
    {
        setUiEnabled(false);
        super.onResume();
    }
}
