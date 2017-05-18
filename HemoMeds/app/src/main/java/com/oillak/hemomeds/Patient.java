package com.oillak.hemomeds;
import android.util.Log;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
/**
 * Created by Jesse Kallio on 18.4.2017.
 */

public class Patient
{
    public ArrayList<Medication> takenMedication;
    public String FullName;


    public Patient()
    {
        takenMedication = new ArrayList<Medication>();
    }

   public void AddName( String firstName, String lastName)
   {

        FullName = firstName + " " + lastName;
   }

    public void AddMedication(String batchName)
    {
        Medication med = new Medication();
        med.Batch = batchName;
        med.date = Calendar.getInstance().getTime();

        takenMedication.add(med);
        Log.d("HemoMeds","addMed");

    }

    class Medication
    {
        public String Batch;
        public Date date;
    }

}


