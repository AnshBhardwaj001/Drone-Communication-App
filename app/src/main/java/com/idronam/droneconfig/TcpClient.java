package com.idronam.droneconfig;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.dronefleet.mavlink.MavlinkConnection;
import io.dronefleet.mavlink.MavlinkMessage;
import io.dronefleet.mavlink.ardupilotmega.Ahrs;
import io.dronefleet.mavlink.ardupilotmega.Ahrs2;
import io.dronefleet.mavlink.ardupilotmega.EkfStatusReport;
import io.dronefleet.mavlink.ardupilotmega.EscTelemetry1To4;
import io.dronefleet.mavlink.ardupilotmega.Meminfo;
import io.dronefleet.mavlink.ardupilotmega.Simstate;
import io.dronefleet.mavlink.ardupilotmega.Wind;
import io.dronefleet.mavlink.common.Attitude;
import io.dronefleet.mavlink.common.BatteryStatus;
import io.dronefleet.mavlink.common.GlobalPositionInt;
import io.dronefleet.mavlink.common.GpsRawInt;
import io.dronefleet.mavlink.common.LocalPositionNed;
import io.dronefleet.mavlink.common.MissionCurrent;
import io.dronefleet.mavlink.common.NavControllerOutput;
import io.dronefleet.mavlink.common.ParamValue;
import io.dronefleet.mavlink.common.PowerStatus;
import io.dronefleet.mavlink.common.RawImu;
import io.dronefleet.mavlink.common.RcChannels;
import io.dronefleet.mavlink.common.ScaledImu2;
import io.dronefleet.mavlink.common.ScaledImu3;
import io.dronefleet.mavlink.common.ScaledPressure;
import io.dronefleet.mavlink.common.ScaledPressure2;
import io.dronefleet.mavlink.common.ServoOutputRaw;
import io.dronefleet.mavlink.common.SysStatus;
import io.dronefleet.mavlink.common.SystemTime;
import io.dronefleet.mavlink.common.TerrainReport;
import io.dronefleet.mavlink.common.Timesync;
import io.dronefleet.mavlink.common.VfrHud;
import io.dronefleet.mavlink.common.Vibration;
import io.dronefleet.mavlink.minimal.Heartbeat;

public class TcpClient {

//    public static final String SERVER_IP = "172.16.194.191"; //your computer IP address
//    public static final int SERVER_PORT = 14550;
    private String SERVER_IP;
    private int SERVER_PORT;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;

    private ExecutorService executorService;

    private MavlinkConnection mavlinkConnection;
    private Context context;

    public TcpClient(Context context, String SERVER_IP, int SERVER_PORT, OnMessageReceived listener) {
        this.context = context;
        this.SERVER_IP = SERVER_IP;
        this.SERVER_PORT = SERVER_PORT;
        mMessageListener = listener;
        this.executorService = Executors.newSingleThreadExecutor();
    }


    public void run() {
        mRun = true;
        executorService.execute(() -> {
            try {
                // Connect to the server
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                Log.d("TCP Client", "Trying to Connect");
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(serverAddr, SERVER_PORT), 5000);
//                Socket socket = new Socket(serverAddr, SERVER_PORT);

                // Setup MAVLink connection
                mavlinkConnection = MavlinkConnection.create(socket.getInputStream(), socket.getOutputStream());

                // Listen for MAVLink messages from the server
                while (mRun && !socket.isClosed()) {
                    try {
                        MavlinkMessage mavlinkMessage = mavlinkConnection.next();
                        if (mavlinkMessage != null) {
                            mMessageListener.messageReceived(mavlinkMessage);
                            // Handle MAVLink messages
//                            if (mavlinkMessage.getPayload() instanceof GpsRawInt) {
//                                GpsRawInt gpsRawInt = (GpsRawInt) mavlinkMessage.getPayload();
////                                Log.d("GPS RAW INT", "Received GPS Raw Int: " + gpsRawInt);
//
//                                // Extract GPS data
//                                BigInteger timeUsec = gpsRawInt.timeUsec();
//                                int fixType = gpsRawInt.fixType().value();
//                                int lat = gpsRawInt.lat(); // latitude in 1E7 degrees
//                                int lon = gpsRawInt.lon(); // longitude in 1E7 degrees
//                                int alt = gpsRawInt.alt(); // altitude in millimeters
//                                int eph = gpsRawInt.eph(); // GPS HDOP
//                                int epv = gpsRawInt.epv(); // GPS VDOP
//                                int vel = gpsRawInt.vel(); // GPS ground speed in cm/s
//                                int cog = gpsRawInt.cog(); // course over ground in 1E2 degrees
//                                int satellitesVisible = gpsRawInt.satellitesVisible(); // number of satellites visible
//
//                                // Log the extracted GPS data
//                                Log.d("GPS Data", "timeUsec=" + timeUsec + ", fixType=" + fixType + ", lat=" + lat + ", lon=" + lon + ", alt=" + alt + ", eph=" + eph + ", epv=" + epv + ", vel=" + vel + ", cog=" + cog + ", satellitesVisible=" + satellitesVisible);
//                            }
//                            if(mavlinkMessage.getPayload() instanceof Ahrs){
//                                Ahrs msg = (Ahrs) mavlinkMessage.getPayload();
//                                Log.d("Ahrs Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof Ahrs2){
//                                Ahrs2 msg = (Ahrs2) mavlinkMessage.getPayload();
//                                Log.d("Ahrs2 Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof Attitude){
//                                Attitude msg = (Attitude) mavlinkMessage.getPayload();
//                                Log.d("Attitude Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof BatteryStatus){
//                                BatteryStatus msg = (BatteryStatus) mavlinkMessage.getPayload();
//                                Log.d("BatteryStatus Data : ", msg.toString());
////                                List<Integer> voltages = msg.voltages();
//                                Log.d("Voltages : ", ""+((msg.voltages().get(0) / 1000)));
//                            }
//                            if(mavlinkMessage.getPayload() instanceof EkfStatusReport){
//                                EkfStatusReport msg = (EkfStatusReport) mavlinkMessage.getPayload();
//                                Log.d("EkfStatusReport Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof EscTelemetry1To4){
//                                EscTelemetry1To4 msg = (EscTelemetry1To4) mavlinkMessage.getPayload();
//                                Log.d("EscTelemetry1To4 Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof GlobalPositionInt){
//                                GlobalPositionInt msg = (GlobalPositionInt) mavlinkMessage.getPayload();
//                                Log.d("GlobalPositionInt Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof Heartbeat){
//                                Heartbeat msg = (Heartbeat) mavlinkMessage.getPayload();
//                                Log.d("Heartbeat Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof LocalPositionNed){
//                                LocalPositionNed msg = (LocalPositionNed) mavlinkMessage.getPayload();
//                                Log.d("LocalPositionNed Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof Meminfo){
//                                Meminfo msg = (Meminfo) mavlinkMessage.getPayload();
//                                Log.d("Meminfo Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof MissionCurrent){
//                                MissionCurrent msg = (MissionCurrent) mavlinkMessage.getPayload();
//                                Log.d("MissionCurrent Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof NavControllerOutput){
//                                NavControllerOutput msg = (NavControllerOutput) mavlinkMessage.getPayload();
//                                Log.d("NavControllerOutput Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof ParamValue){
//                                ParamValue msg = (ParamValue) mavlinkMessage.getPayload();
//                                Log.d("ParamValue Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof PowerStatus){
//                                PowerStatus msg = (PowerStatus) mavlinkMessage.getPayload();
//                                Log.d("PowerStatus Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof RawImu){
//                                RawImu msg = (RawImu) mavlinkMessage.getPayload();
//                                Log.d("RawImu Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof RcChannels){
//                                RcChannels msg = (RcChannels) mavlinkMessage.getPayload();
//                                Log.d("RcChannels Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof ScaledImu2){
//                                ScaledImu2 msg = (ScaledImu2) mavlinkMessage.getPayload();
//                                Log.d("ScaledImu2 Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof ScaledImu3){
//                                ScaledImu3 msg = (ScaledImu3) mavlinkMessage.getPayload();
//                                Log.d("ScaledImu3 Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof ScaledPressure){
//                                ScaledPressure msg = (ScaledPressure) mavlinkMessage.getPayload();
//                                Log.d("ScaledPressure Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof ScaledPressure2){
//                                ScaledPressure2 msg = (ScaledPressure2) mavlinkMessage.getPayload();
//                                Log.d("ScaledPressure2 Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof ServoOutputRaw){
//                                ServoOutputRaw msg = (ServoOutputRaw) mavlinkMessage.getPayload();
//                                Log.d("ServoOutputRaw Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof Simstate){
//                                Simstate msg = (Simstate) mavlinkMessage.getPayload();
//                                Log.d("Simstate Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof SysStatus){
//                                SysStatus msg = (SysStatus) mavlinkMessage.getPayload();
//                                Log.d("SysStatus Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof SystemTime){
//                                SystemTime msg = (SystemTime) mavlinkMessage.getPayload();
//                                Log.d("SystemTime Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof TerrainReport){
//                                TerrainReport msg = (TerrainReport) mavlinkMessage.getPayload();
//                                Log.d("TerrainReport Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof Timesync){
//                                Timesync msg = (Timesync) mavlinkMessage.getPayload();
//                                Log.d("Timesync Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof VfrHud){
//                                VfrHud msg = (VfrHud) mavlinkMessage.getPayload();
//                                Log.d("VfrHud Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof Vibration){
//                                Vibration msg = (Vibration) mavlinkMessage.getPayload();
//                                Log.d("Vibration Data : ", msg.toString());
//                            }
//                            if(mavlinkMessage.getPayload() instanceof Wind){
//                                Wind msg = (Wind) mavlinkMessage.getPayload();
//                                Log.d("Wind Data : ", msg.toString());
//                            }
                        }
                    } catch (IOException e) {
                        Log.e("TCP", "Error reading MAVLink message: ", e);
                        showToast("Unable to Connect to Server");
                    }
                }
                Log.d("TCP Client", "Connection closed");
                showToast("Unable to Connect to Server");
            } catch (Exception e) {
                Log.e("TCP", "Error in TCP client: ", e);
                showToast("Unable to Connect to Server");            }
        });
    }




//    public void run() {
//
//        mRun = true;
//
//        executorService.execute(() -> {
//        try {
//            //here you must put your computer's IP address.
//            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
//
//            Log.d("TCP Client", "C: Connecting...");
//
//            //create a socket to make the connection with the server
//            Socket socket = new Socket(serverAddr, SERVER_PORT);
//            Log.d("TCP Client", "C: Connecting... 2");
//
//            try {
//                Log.d("TCP","trying");
//
//                //sends the message to the server
//                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
//
//                //receives the message which the server sends back
//                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//                mavlinkConnection = MavlinkConnection.create(socket.getInputStream(),socket.getOutputStream());
//
//                //in this while the client listens for the messages sent by the server
//                while (mRun) {
//
//                    mServerMessage = mBufferIn.readLine();
//
//                    if (mServerMessage != null && mMessageListener != null) {
//                            //call the method messageReceived from MyActivity class
//                            mMessageListener.messageReceived(mServerMessage);
////                            Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");
//                            MavlinkMessage mavlinkMessage = mavlinkConnection.next();
//                            if (mavlinkMessage != null) {
////                                Log.d("Mavlink",mavlinkMessage.toString());
//
////                                if(mavlinkMessage.getPayload() instanceof Heartbeat){
////                                    MavlinkMessage<Heartbeat> heartbeatMessage = (MavlinkMessage<Heartbeat>)mavlinkMessage;
////                                    Log.v("Heartbeat Message : ", heartbeatMessage.toString());
////                                    Log.d("Heartbeat Message Payload : ", heartbeatMessage.getPayload().toString());
////                                }
//                                if (mavlinkMessage.getPayload() instanceof GpsRawInt) {
//                                    GpsRawInt gpsRawInt = (GpsRawInt) mavlinkMessage.getPayload();
//                                    Log.d("GPS RAW INT : ", "Received GPS Raw Int: " + gpsRawInt);
//                                }
//
//
//                            }
//
//
//                    }
//
//                }
//
//                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");
//
//            } catch (Exception e) {
//
//                Log.e("TCP", "S: Error", e);
//
//            } finally {
//                //the socket must be closed. It is not possible to reconnect to this socket
//                // after it is closed, which means a new socket instance has to be created.
//                Log.e("finally : ","closing the socket");
//                socket.close();
//            }
//
//        } catch (Exception e) {
//
//            Log.e("TCP", "C: Error"+e);
//
//        }
//        });
//    }

    public interface OnMessageReceived {
        public void messageReceived(MavlinkMessage message);
    }

    private void showToast(final String message) {
        // To show a toast from a background thread, use the context and post to the UI thread
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

}
