package com.idronam.droneconfig;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.dronefleet.mavlink.MavlinkConnection;
import io.dronefleet.mavlink.MavlinkMessage;
import io.dronefleet.mavlink.common.BatteryStatus;
import io.dronefleet.mavlink.common.GlobalPositionInt;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private EditText serverIpET;
    private EditText servserPortET;
    private Button connectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        serverIpET = findViewById(R.id.serverIpEditText);
        servserPortET = findViewById(R.id.serverPortEditText);
        connectBtn = findViewById(R.id.connectBtn);

        connectBtn.setOnClickListener(v -> {
            String serverIp = serverIpET.getText().toString();
            String serverPort = servserPortET.getText().toString();

            if(!isValidIPAddress(serverIp) || serverPort.length() == 0){
                Toast.makeText(MainActivity.this, "Please enter valid server ip and port", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Connect Btn clicked");
            try {
                // Start the TCP client
//            String serverIp = " 192.168.100.83";
//            int serverPort = 14550;
                Context context = getApplicationContext();
                TcpClient tcpClient = new TcpClient(context , serverIp , Integer.parseInt(serverPort), new TcpClient.OnMessageReceived() {
                    @Override
                    public void messageReceived(MavlinkMessage message) {
                        handleMessage(message);
                    }
                });
                tcpClient.run();

                Log.d(TAG, "Started the TCP client");
            } catch (Exception e) {
                Log.e(TAG, "Error starting TCP client: ", e);
            }
        });
    }

    private void handleMessage(MavlinkMessage message) {
//        Log.d(TAG ,"Main Activity Message recieved : "+ message);

        if(message.getPayload() instanceof BatteryStatus){
            BatteryStatus msg = (BatteryStatus) message.getPayload();
            Log.d("BatteryStatus Data : ", msg.toString());
//                                List<Integer> voltages = msg.voltages();
            Log.d("Voltages : ", ""+((msg.voltages().get(0) / 1000)));
        }
        if(message.getPayload() instanceof GlobalPositionInt){
            GlobalPositionInt msg = (GlobalPositionInt) message.getPayload();
            Log.d("GlobalPositionInt Data : ", msg.toString());
        }
    }

    private boolean isValidIPAddress(String ip) {
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(ipPattern);
    }
}