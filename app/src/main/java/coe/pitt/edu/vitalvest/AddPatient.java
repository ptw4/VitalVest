package coe.pitt.edu.vitalvest;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class AddPatient extends AppCompatActivity {

    EditText nameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);
        nameText = (EditText) findViewById(R.id.editText);
    }

    public void addPatient(View view) {
        String pName = nameText.getText().toString();
        Intent retIntent = new Intent();
        retIntent.putExtra("name", pName);
        setResult(Activity.RESULT_OK, retIntent);
        finish();
    }

}
