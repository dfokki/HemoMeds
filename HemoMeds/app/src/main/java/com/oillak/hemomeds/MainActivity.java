package com.oillak.hemomeds;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.CollationElementIterator;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity {

    //for adding patients to listviews
    ArrayAdapter Aa;
    //for adding taken medications to listviews
    ArrayAdapter Aa2;
    ArrayList<String> holder;
    ArrayList<Patient> PatientList;
    ListView patientListView;
    ListView patient_mainListView;
    Patient selectedPatient;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Calendar calendar = Calendar.getInstance();
        //SimpleDateFormat sdf = new SimpleDateFormat("/E/d/MMMM/yyyy/HH:mm",Locale.getDefault());
        //String strDate = sdf.format(calendar.getTime());
//
        //String[] values=strDate.split("/",0);

       // for (int i = 0; i < values.length; i++
       //         ){
       //     Log.v("CHECK_DATE", values[i]);
       // }

        setContentView(R.layout.patient_main);
        patient_mainListView = (ListView)findViewById(R.id.listView);
        if(Aa2 == null)
        Aa2 = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        setContentView(R.layout.activity_main);
        patientListView = (ListView)findViewById(R.id.patientListView);


        //Patient pat = new Patient();
        //pat.AddName("Pate", "Petee");

        holder = new ArrayList<String>();

        PatientList = new ArrayList<Patient>();
        //PatientList.add(pat);
        Aa = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,holder);

        patientListView.setAdapter(Aa);

        selectedPatient = null;



       UpdatePatientList();

    }

    public void UpdatePatientList()
    {
        patientListView = (ListView)findViewById(R.id.patientListView);
        patientListView.setAdapter(Aa);
        holder.clear();
        Aa.clear();

        for (int i = 0; i < PatientList.size(); i++)
        {
            holder.add(PatientList.get(i).FullName);
        }

       // Aa.addAll(holder);
        patientListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3)
            {
                String value = (String)adapter.getItemAtPosition(position);

                for(int i = 0; i < PatientList.size(); i++ )
                {
                    if(PatientList.get(i).FullName == value)
                    {
                        selectedPatient = PatientList.get(i);
                        OnSelected();
                    }

                }

            }
        });
    }
@Override
 protected void onPause()
    {
        String text = new String();

        for (int i = 0; i < PatientList.size(); i++)
        {
            Patient pat = PatientList.get(i);
            text += "{\"" + pat.FullName + "\"(";
            for(int n = 0; n < pat.takenMedication.size(); n++)
            {
                Patient.Medication med = pat.takenMedication.get(n);
                text += "[ \"" + med.Batch + "\"," + Long.toString(med.date.getTime()) + " ]";
            }

            text += ")}\n";
        }

        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput("PatientData", this.MODE_PRIVATE);
            outputStream.write(text.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
super.onPause();
    }

    @Override
    protected void onResume()
    {
        FileInputStream inputStream;
    String fileStream;

        try {
            inputStream = openFileInput("PatientData");
           // inputStream.write(text.getBytes());
            int size = inputStream.available();
            byte[] str = new byte[size];
            inputStream.read(str);
            fileStream = new String(str);
            inputStream.close();
            int i1 = 0;
            int i2 = 0;
            //Parsing
            for(int k = 0; k < size;)
            {
                int addToK  = fileStream.indexOf("{",i2);
                if(addToK == -1)
                    break;
                addToK = fileStream.indexOf("}",addToK)-addToK+1;
                 i1 = fileStream.indexOf("\"",i2) + 1;
                int temp = i1-1;
                 i2 = fileStream.indexOf("\"", i1);
                String name = fileStream.substring(i1, i2);
                Log.d("tag", "name = " + name);

                i1 = fileStream.indexOf(("("), i2) + 1;
                i2 = fileStream.indexOf(")", i1);
                temp = i2-temp+1;
                String medicalData = fileStream.substring(i1, i2);

                Patient pat = new Patient();
                pat.FullName = name;

                for (int i = 0; i < medicalData.length(); ) {
                    i1 = medicalData.indexOf(("["), i2 + 1);
                    i1 = medicalData.indexOf("\"", i1) + 1;
                    i2 = medicalData.indexOf(",", i1);

                    String batch = medicalData.substring(i1, i2 - 1);
                    i1 = i2;
                    i2 = medicalData.indexOf("]", i2);
                    String dateString = medicalData.substring(i1 + 1, i2 - 1);

                    Log.d("tag", "batch = " + batch);
                    long dateLong = Long.parseLong(dateString);
                    Date date = new Date(dateLong);
                    Log.d("tag", "date = " + date);
                    i = i2 + 1;

                    pat.AddMedication(batch);
                    pat.takenMedication.get(pat.takenMedication.size() - 1);
                    Patient.Medication medication = pat.takenMedication.get(pat.takenMedication.size() - 1);

                    medication.date = date;

                }
                Log.d("tag", "medication = " + medicalData);
                PatientList.add(pat);
                //i2 = fileStream.indexOf(")",i2) + 1;
                k += addToK;
                i2 = k;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//TODO: miksi latautuu "ylimääräistä" dataa?

        UpdatePatientList();

        super.onResume();
    }
    @Override
    public void onBackPressed()
    {
        if(selectedPatient != null)
        {
            selectedPatient = null;
            setContentView(R.layout.activity_main);
            UpdatePatientList();
        }
        else
            super.onBackPressed();
    }
    public void OnSelected()
    {
        ArrayList<String> tempList;
        setContentView(R.layout.patient_main);
        tempList = new ArrayList<String>();
        SimpleDateFormat sdf = new SimpleDateFormat("E  d MMMM yyyy HH:mm",Locale.getDefault());

        //sorting
        Collections.sort(selectedPatient.takenMedication, new Comparator<Patient.Medication>() {
            @Override
            public int compare(Patient.Medication o1, Patient.Medication o2) {
                return o2.date.compareTo(o1.date);
            }
        });


        for (int i = 0; i < selectedPatient.takenMedication.size(); i++)
        {
            Patient.Medication temp = selectedPatient.takenMedication.get(i);
            String strDate = sdf.format(temp.date);
            strDate+="\n"+temp.Batch;
            tempList.add(strDate);
        }
        //Aa2 = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_2,tempList);
        Aa2.clear();
        Aa2.addAll(tempList);
        patient_mainListView = (ListView)findViewById(R.id.listView);
        patient_mainListView.setAdapter(Aa2);
        TextView patientName = (TextView) findViewById(R.id.patientName);
        patientName.setText(selectedPatient.FullName);
        //setContentView(R.layout.patient_main);
    }

    public void OnAddPatientClick(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Title");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                if(name.length() > 3 )
                {
                    Patient pat = new Patient();
                    pat.FullName = name;
                    PatientList.add(pat);
                    UpdatePatientList();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    public void AddMed(View view)
    {

        Log.d("HemoMeds","addMed");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Batchnumber");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                if(name.length() > 3 )
                {
                    selectedPatient.AddMedication(name);


                    Aa2.clear();
                    OnSelected();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();


        //Aa2.clear();
        //OnSelected();
    }


}
