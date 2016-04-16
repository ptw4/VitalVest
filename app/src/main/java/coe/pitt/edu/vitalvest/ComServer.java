package coe.pitt.edu.vitalvest;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;

public class ComServer {
    private static String    SERVERIP   = "10.0.2.15"; //server IP set to defaut
    private static final int SERVERPORT = 8080;        //designated port for connections

    private ServerSocket serverSocket;                 //server socket for communication

    public ComServer( ) {
        SERVERIP = getLocalIpAddress();

        Thread server = new Thread( new ServerThread( ) );

        server.start();
    }

    public void ComServerFinalize( ) {
        try {
            serverSocket.close();
        } catch ( Exception e ) {
            Log.e( "Server:  ", "Error Closing the Socket" );
        }
    }

    private String getLocalIpAddress( ) {
        try {
            for( Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces( ); en.hasMoreElements( ); ) {
                NetworkInterface intf = en.nextElement();

                for( Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement( );

                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }

            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return null;
    }

    private class ServerThread implements Runnable {
        public void run( ) {
            try {
                if( SERVERIP != null ) {
                    Log.e( "Server:  ", ( "Listening on IP " + SERVERIP ) );

                    serverSocket = new ServerSocket( SERVERPORT );
                    for( ; ; ) {
                        Socket client = serverSocket.accept( );
                        Log.e( "Server:  ", "Client Connected" );

                        try {
                            BufferedReader in = new BufferedReader( new InputStreamReader( client.getInputStream( ) ) );
                            String line = null;
                            while( ( line = in.readLine( ) ) != null ) {
                                Log.e( "Server:  ", "Couldn't Detect Internet Connection" );
                            }
                        } catch ( Exception e ) {
                            Log.e( "Server:  ", "Connection to Client Dropped" );
                        }
                    }
                } else {
                    Log.e( "Server:  ", "Couldn't Detect Internet Connection" );
                }

            } catch ( Exception e ) {
                Log.e( "Server:  ", "Error in Server Thread" );
            }
        }
    }
}
