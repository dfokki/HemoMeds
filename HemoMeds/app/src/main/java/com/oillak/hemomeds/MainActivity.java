package com.oillak.hemomeds;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatePicker datePicker = (DatePicker)findViewById(R.id.datePicker);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("/E/d/MMMM/yyyy/HH:mm",Locale.getDefault());
        String strDate = sdf.format(calendar.getTime());

        String[] values=strDate.split("/",0);

        for (int i = 0; i < values.length; i++
                ){
            Log.v("CHECK_DATE", values[i]);
        }

    }


}
