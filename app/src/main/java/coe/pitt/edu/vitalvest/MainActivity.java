package coe.pitt.edu.vitalvest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter BA;
    private DBHelper DB;
    private int count;
    private boolean inSession = false;
    private boolean isConnected = false;
    private ListView patients;
    static final int ADD_PATIENT = 1;

    BluetoothSocket socket;
    BluetoothDevice myDevice;
    OutputStream out;
    InputStream in;
    Thread work;
    byte[] readBuffer;
    int bufferPosition;
    volatile boolean stopWorker;
    int currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        count = 1;

        // DBHelper is a control interface for SQLite DB's
        DB = new DBHelper(this);
        //DB.insertPatient("Default", "None");

        // set up contact list and make buttons of the contacts
        setListView();
        patients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override // switching to display contact on selection
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                currentUser = arg2 + 1;
                Toast.makeText(getApplicationContext(), "Patient " + currentUser + " chosen", Toast.LENGTH_LONG).show();

            }
        });
    }

    public void setListView() {
        ArrayList<String> patientList = DB.getAllPatients();
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, patientList);
        patients = (ListView)findViewById(R.id.listView);
        patients.setAdapter(arrayAdapter);
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

    public void addPatient(View view) {
        Intent intent = new Intent(this, AddPatient.class);
        startActivityForResult(intent, ADD_PATIENT);
    }

    public void deletePatient(View view) {
        DB.deletePatient(currentUser);
        setListView();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        System.out.println("Request Code: " + requestCode);
        System.out.println("Result Code: " + resultCode);
        if (requestCode == ADD_PATIENT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra("name");
                DB.insertPatient(name, myDevice.getName());
                setListView();

            }
        }
    }

    public void viewHistory(View view) {
        Bundle dataBundle = new Bundle();
        ArrayList<String> strings = DB.getAllSessions(currentUser);
        dataBundle.putStringArrayList("sessions", strings);

        Intent intent = new Intent(getApplicationContext(), PatientHistory.class);

        intent.putExtras(dataBundle);
        startActivity(intent);
    }

    void findBT()
    {
        BA = BluetoothAdapter.getDefaultAdapter();
        if(BA == null)
        {
            Toast.makeText(getApplicationContext(), "No bluetooth adapter available", Toast.LENGTH_LONG).show();
        }

        if(!BA.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                System.err.println(device.getName());
                if(device.getName().equals("HC-05")) {
                    myDevice = device;
                    isConnected = true;
                    Toast.makeText(getApplicationContext(), "Bluetooth Device Found", Toast.LENGTH_LONG).show();
                    break;
                }
                else
                {
                    isConnected = false;
                    Toast.makeText(getApplicationContext(), "Bluetooth Device Not Found", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        socket = myDevice.createRfcommSocketToServiceRecord(uuid);
        socket.connect();
        out = socket.getOutputStream();
        in = socket.getInputStream();

        Toast.makeText(getApplicationContext(), "Bluetooth Coms Opened", Toast.LENGTH_LONG).show();

    }

    public void startBT(View view) throws IOException {
        if(!isConnected) {
            try {
                findBT();
                openBT();
            } catch (IOException ex)
            {

            }
        }
        else
        {
            closeBT();
            Toast.makeText(getApplicationContext(), "Device disconnected", Toast.LENGTH_LONG).show();
        }
    }

    void beginListenForData(final int type)
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        bufferPosition = 0;
        readBuffer = new byte[4096];
        work = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = in.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            in.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[bufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    bufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            if (type == 1) {
                                                String[] results = data.split(",");
                                                Toast.makeText(getApplicationContext(), "BPI: " + results[0] + ",\n "
                                                        + "Temp: " + results[1], Toast.LENGTH_LONG).show();
                                                Double v1 = Double.parseDouble(results[0]);
                                                Double v2 = Double.parseDouble(results[1]);
                                                DB.insertSession(currentUser, v1, v2);
                                            }
                                            else{
                                                System.out.println("Output: " + data);
                                                String[] results = data.split(",");
                                                Toast.makeText(getApplicationContext(), "Pulse Rate: "
                                                        + results[0] + "\n", Toast.LENGTH_LONG).show();
                                                Toast.makeText(getApplicationContext(), "Temperature: "
                                                        + results[1] + "\n", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[bufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        work.start();
    }

    public void newSession(View view) throws IOException
    {
        if(!inSession) {
            String msg = "h";
            out.write(msg.getBytes());
            Toast.makeText(getApplicationContext(), "Session started", Toast.LENGTH_LONG).show();
            beginListenForData(1);
        }
        else
        {
            stopWorker = true;
            inSession = false;
            Toast.makeText(getApplicationContext(), "Session stopped", Toast.LENGTH_LONG).show();
        }

    }

    public void showVitals(View view) throws IOException
    {
        if(!inSession)
        {
            //        String msg = "h";
            //        msg += "\n";
            //        out.write(msg.getBytes());
            Toast.makeText(getApplicationContext(), "Session started", Toast.LENGTH_LONG).show();
            beginListenForData(2);
        }
        else
        {
            stopWorker = true;
            Toast.makeText(getApplicationContext(), "Session stopped", Toast.LENGTH_LONG).show();
         }

    }

    void closeBT() throws IOException
    {
        stopWorker = true;
        out.close();
        in.close();
        socket.close();
        Toast.makeText(getApplicationContext(), "Bluetooth Coms Closed", Toast.LENGTH_LONG).show();
    }


}
