package com.oillak.hemomeds;

import android.Manifest;

import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;

import android.graphics.Matrix;

import android.media.ExifInterface;

import android.net.Uri;
import android.os.Environment;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.EditText;


import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import java.util.Locale;




public class MainActivity extends AppCompatActivity {


    Button scanmed;
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
    TessBaseAPI baseAPI;


    String recognizedText;


    static Uri mImageCaptureUri;

    //http://stackoverflow.com/questions/15432592/get-file-path-of-image-on-android
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            Uri tempUri = getImageUri(getApplicationContext(), photo);

            File finalFile = new File(getRealPathFromURI(tempUri));

            getTextFromPhoto(toGrayscale(finalFile,photo));
        }
    }

    public Bitmap toGrayscale(File _path ,Bitmap bmpOriginal)
    {
// _path = path to the image to be OCRed
        try {


            ExifInterface exif = new ExifInterface(_path.getAbsolutePath());
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            int rotate = 0;

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            if (rotate != 0) {
                int w = bmpOriginal.getWidth();
                int h = bmpOriginal.getHeight();

                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);

                // Rotating Bitmap & convert to ARGB_8888, required by tess
                bmpOriginal = Bitmap.createBitmap(bmpOriginal, 0, 0, w, h, mtx, false);
            }
           return bmpOriginal.copy(Bitmap.Config.ARGB_8888, true);

        }catch (Exception e){};
        return bmpOriginal;
    }

    protected void getTextFromPhoto(Bitmap photo) {

        // http://www.thecodecity.com/2016/09/creating-ocr-android-app-using-tesseract.html
        String DATA_PATH;
        DATA_PATH = Environment.getExternalStorageDirectory() + "/ocrctz/";
        File dir = new File(DATA_PATH + "/tessdata/");
        File tessfile = new File(DATA_PATH + "/tessdata/" + "eng.traineddata");
        if (!tessfile.exists()) {
            Log.d("mylog", "in file doesn't exist");
            dir.mkdirs();
        }

        try {
            AssetManager assetManager = getApplicationContext().getAssets();
            InputStream in = assetManager.open("eng2.traineddata");
            OutputStream out = new FileOutputStream(DATA_PATH + "/tessdata/" + "eng.traineddata");
            byte[] buffer = new byte[1024];
            int read = in.read(buffer);

            while (read != -1) {
                out.write(buffer, 0, read);
                read = in.read(buffer);
            }
        } catch (Exception e) {
            Log.d("mylog", "couldn't copy with the following error : " + e.toString());
        }
//
        try {
            baseAPI.init(DATA_PATH, "eng");
            //treat text as one line
            baseAPI.setPageSegMode(5);
            baseAPI.setImage(photo);
            recognizedText = baseAPI.getUTF8Text();
            baseAPI.end();
            Log.d("BaseAPIResult", recognizedText);
            DialogBuilder("ScannedBatch:",getbatchfromscan());

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public String getbatchfromscan() {

        int i1 = 0;
        int i2 = 0;
        //recognizedText = "daskljadsjkl Batch:      Parsintaonnistunut";
        String Text;
        int batchlength = new String("Batch:").length();


            i1 = recognizedText.indexOf(("B"));
            i2 = recognizedText.indexOf(":");

            if((i2-i1) == (batchlength - 1)) {
                Text = recognizedText.substring(i2 + 1).toString();
                return Text.trim();
            }

        else
        return Text = "No batch number found";
    }

public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }



    private static final String LOG_TAG = "Text API";
    private static final int CAMERA_REQUEST= 10;
    private TextView scanResults;


    private static final int REQUEST_WRITE_PERMISSION = 20;

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


    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        //if (imageUri != null) {
        //    bundle.putString(SAVED_INSTANCE_URI, imageUri.toString());
        //    bundle.putString(SAVED_INSTANCE_RESULT, scanResults.getText().toString());
        //}
        if(selectedPatient == null)
        {
            UpdatePatientList();
        }
        if(selectedPatient != null)
        {
            OnSelected();
        }
        super.onSaveInstanceState(bundle);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);
        setContentView(R.layout.patient_main);
        baseAPI =  new TessBaseAPI();

        addmedButton = (Button) findViewById(R.id.button);
        scanmed = (Button) findViewById(R.id.scan_med);

      if( ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
          addmedButton.setEnabled(false);
          ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
      }

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
 protected void onPause() {
    saveData();

    super.onPause();
}

   protected void saveData(){
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
    }


    @Override
    protected void onResume() {

        super.onResume();

        readData();




    }

    protected void readData() {
        FileInputStream inputStream;
        String fileStream;

        if (PatientList.isEmpty()) {
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
                for (int k = 0; k < size; ) {
                    int addToK = fileStream.indexOf("{", i2);
                    if (addToK == -1)
                        break;
                    addToK = fileStream.indexOf("}", addToK) - addToK + 1;
                    i1 = fileStream.indexOf("\"", i2) + 1;
                    int temp = i1 - 1;
                    i2 = fileStream.indexOf("\"", i1);
                    String name = fileStream.substring(i1, i2);
                    Log.d("tag", "name = " + name);

                    i1 = fileStream.indexOf(("("), i2) + 1;
                    i2 = fileStream.indexOf(")", i1);
                    //temp = i2 - temp + 1;
                    //Log.d("debug", temp);
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
            UpdatePatientList();
        }
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

        Aa2.clear();
        Aa2.addAll(tempList);
        patient_mainListView = (ListView)findViewById(R.id.listView);
        patient_mainListView.setAdapter(Aa2);
        TextView patientName = (TextView) findViewById(R.id.patientName);
        patientName.setText(selectedPatient.FullName);

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

        input.setText("H8866911A ( Default )");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);

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
    }
void DialogBuilder(String title, String defauiltText){
    Log.d("HemoMeds","DialogBuilder");

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(title);

// Set up the input
    final EditText input = new EditText(this);

    input.setText(defauiltText);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
    builder.setView(input);


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

}
   public void OnScanMedClick(View view){


           Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
           cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
           startActivityForResult(cameraIntent, CAMERA_REQUEST);

  }




}
