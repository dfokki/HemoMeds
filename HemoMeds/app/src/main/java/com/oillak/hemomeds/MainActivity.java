package com.oillak.hemomeds;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.CollationElementIterator;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {



    Button addmedButton;
    //for adding patients to listviews
    ArrayAdapter Aa;
    //for adding taken medications to listviews
    ArrayAdapter Aa2;
    ArrayList<String> holder;
    ArrayList<Patient> PatientList;
    ListView patientListView;
    ListView patient_mainListView;
    Patient selectedPatient;
    TessBaseAPI baseAPI ;
    ImageView imagePreview;

     String recognizedText;
    /*
        String mCurrentPhotoPath;
        File imageF;

        private File createImageFile() throws IOException {
            // Create an image file name
            String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";

            imageF = File.createTempFile(imageFileName,".jpg");
            return imageF;
        }

        private File setUpPhotoFile() throws IOException {

            File f = createImageFile();
            mCurrentPhotoPath = f.getAbsolutePath();

            return f;
        }

        static final int REQUEST_TAKE_PHOTO = 1;

        //käynnistää Kamera Intentin
        private void dispatchTakePictureIntent()
        {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imagePreview.setImageBitmap(imageBitmap);

                try{
                    Bitmap bitmap =
                }

                baseAPI.init(mCurrentPhotoPath,"eng");
                baseAPI.setImage(imageF);
                recognizedText = baseAPI.getUTF8Text();
                baseAPI.end();
                Log.d("BaseAPIResult", recognizedText);
                //imagePreview.setVisibility(View.VISIBLE);
            }
        }

        */
    private static final String LOG_TAG = "Text API";
    private static final int PHOTO_REQUEST = 10;
    private TextView scanResults;
    private Uri imageUri;
    private TessBaseAPI detector;
    private static final int REQUEST_WRITE_PERMISSION = 20;
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    addmedButton.setEnabled(true);
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    File file;

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "picture.jpg");
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, PHOTO_REQUEST);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (imageUri != null) {
            outState.putString(SAVED_INSTANCE_URI, imageUri.toString());
            outState.putString(SAVED_INSTANCE_RESULT, scanResults.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    private void launchMediaScanIntent() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        int targetW = 600;
        int targetH = 600;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(ctx.getContentResolver()
                .openInputStream(uri), null, bmOptions);
    }


  private String scan(){

     /* baseAPI =  new TessBaseAPI();
      baseAPI.init(file.getAbsolutePath().toString()+ "/","eng");
      baseAPI.setImage(file);
      recognizedText = baseAPI.getUTF8Text();

      baseAPI.end();
*/
     recognizedText = "recognizedText";
      Log.d("Scan Result: ", recognizedText);
      return recognizedText;
  }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            takePicture();
            launchMediaScanIntent();
            try {
                Bitmap bitmap = decodeBitmapUri(this, imageUri);

            } catch (Exception e) {
                Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT)
                        .show();
                Log.e(LOG_TAG, e.toString());
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setContentView(R.layout.patient_main);
        baseAPI =  new TessBaseAPI();
        addmedButton = (Button) findViewById(R.id.button);

      if( ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
          addmedButton.setEnabled(false);
          ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
      }

        //Calendar calendar = Calendar.getInstance();
        //SimpleDateFormat sdf = new SimpleDateFormat("/E/d/MMMM/yyyy/HH:mm",Locale.getDefault());
        //String strDate = sdf.format(calendar.getTime());
//
        //String[] values=strDate.split("/",0);

       // for (int i = 0; i < values.length; i++
       //         ){
       //     Log.v("CHECK_DATE", values[i]);
       // }






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
        //SimpleDateFormat sdf = new SimpleDateFormat("E  d MMMM yyyy HH:mm",Locale.getDefault());
        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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
           // String strDate = sdf.format(temp.date);
            String strDate = timeStamp;
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

        takePicture();
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
        input.setText("asdasdasdasdasdasd");
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);
       // TessBaseAPI baseAPI = new TessBaseAPI();
       // baseAPI.init(mCurrentPhotoPath,"eng");
       // baseAPI.setImage(currentImage);
       // recognizedText = baseAPI.getUTF8Text();
       // baseAPI.end();
       // Log.d("BaseAPIResult", recognizedText);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //todo: OCR_Text+input.getText().toString()
                String name  = input.getText().toString();
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
