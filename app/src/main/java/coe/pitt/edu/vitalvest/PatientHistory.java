package coe.pitt.edu.vitalvest;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PatientHistory extends AppCompatActivity {

    TextView text;
    ArrayList<String> sessions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
            sessions = extras.getStringArrayList("sessions");
        }
        text = (TextView) findViewById(R.id.text);
        for(String entry : sessions)
        {
//            String[] s = entry.split(",");
//            text.append(s[0] + "\t" + s[1] + "\n");
            text.append(entry);
        }

        setContentView(R.layout.activity_patient_history);
    }


    public void toMain() {
        finish();
    }
}