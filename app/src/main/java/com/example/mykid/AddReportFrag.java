package com.example.mykid;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class AddReportFrag extends Fragment implements FetchAddressTask.OnTaskCompleted, View.OnClickListener {

    private TextView dateInputTxtView,timeInputTxtView,locationInputTxtView,actErrorMsg,dateErrorMsg,timeErrorMsg,reporterErrorMsg;
    private EditText actNameEditTxt,reporterNameEditTxt;
    private Button addDateBtn,addTimeBtn,locationBtn,completeBtn,removeImgBtn;
    private ImageButton imageBtn, clearLocationBtn;
    private ImageView imageView;

    private String activityName,location,date,time,reporter,result,uriStr;

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private FusedLocationProviderClient mFusedLocationClient;

    private UUID id;
    private File photoFile;
    private Intent captureImageIntent;
    private static final int REQUEST_PHOTO = 1;
    private Uri uri;
    ReportViewModel reportViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_add_report, container, false);
        dateInputTxtView = view.findViewById(R.id.dateInputTxtView);
        timeInputTxtView = view.findViewById(R.id.timeInputTxtView);
        actNameEditTxt= view.findViewById(R.id.actNameEditTxt);
        reporterNameEditTxt=view.findViewById(R.id.reporterNameEditTxt);
        locationInputTxtView=view.findViewById(R.id.locationInputTxtView);
        actErrorMsg=view.findViewById(R.id.actErrorMsg);
        dateErrorMsg=view.findViewById(R.id.dateErrorMsg);
        timeErrorMsg=view.findViewById(R.id.timeErrorMsg);
        reporterErrorMsg=view.findViewById(R.id.reporterErrorMsg);
        reportViewModel= new ViewModelProvider(this).get(ReportViewModel.class);
        addDateBtn = view.findViewById(R.id.dateBtn);
        addTimeBtn = view.findViewById(R.id.timeBtn);
        locationBtn = view.findViewById(R.id.locationBtn);
        completeBtn=view.findViewById(R.id.completeBtn);
        removeImgBtn=view.findViewById(R.id.removeImgBtn);
        clearLocationBtn = view.findViewById(R.id.clearLocationBtn);
        imageView = view.findViewById(R.id.imageView);
        imageBtn = view.findViewById(R.id.imageBtn);
        removeImgBtn.setOnClickListener(this);
        clearLocationBtn.setOnClickListener(this);
        addDateBtn.setOnClickListener(this);
        addTimeBtn.setOnClickListener(this);
        locationBtn.setOnClickListener(this);
        completeBtn.setOnClickListener(this);
        imageBtn.setOnClickListener(this);

        removeImgBtn.setVisibility(View.GONE);

        if(savedInstanceState != null){
            date=savedInstanceState.getString("date");
            time=savedInstanceState.getString("time");
            result=savedInstanceState.getString("location");
            if(savedInstanceState.getString("Uri")!=null){
                uri= Uri.parse(savedInstanceState.getString("Uri"));
                Picasso.get().load(uri).into(imageView);
            }
            dateInputTxtView.setText(date);
            timeInputTxtView.setText(time);
            locationInputTxtView.setText(result);

            if(uri!=null){
                removeImgBtn.setVisibility(View.VISIBLE);
            }
        }

        getParentFragmentManager().setFragmentResultListener("location", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                result = bundle.getString("location");
                locationInputTxtView.setText(result);
            }
        });

        // Initialize the FusedLocationClient.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        //create the instance of file object
        photoFile = getPhotoFile();
        PackageManager pm = getContext().getPackageManager();

        //create the camera services
        captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhoto = photoFile != null &&
                captureImageIntent.resolveActivity(pm) != null;
        imageBtn.setEnabled(canTakePhoto);

        return view;
    }

    public void onClick(View view) {
        DialogFragment newFragment;
        switch (view.getId()){
            case R.id.dateBtn:
                newFragment = new DatePickerFragment();
                newFragment.show(getChildFragmentManager(), "datePickerAddFrag");
                break;

            case R.id.timeBtn:
                newFragment = new TimePickerFragment();
                newFragment.show(getChildFragmentManager(),"timePickerAddFrag");
                break;

            case R.id.locationBtn:
                getLocation(); //get user current location
                break;

            case R.id.clearLocationBtn:
                locationInputTxtView.setText("");
                break;

            case R.id.imageBtn:
                    uri = FileProvider.getUriForFile(getActivity(), "com.example.mykid.fileprovider", photoFile);

                    //start launch the camera service with file path
                    captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                    //permission
                    //Check the return value from captureImageIntent

                    List<ResolveInfo> cameraActivities =
                            getActivity().getPackageManager().queryIntentActivities(captureImageIntent,
                                    PackageManager.MATCH_DEFAULT_ONLY);
                    //solve each activity
                    for (ResolveInfo activity : cameraActivities) {
                        getActivity().grantUriPermission(activity.activityInfo.packageName,
                                uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }

                    //start the camera services
                    startActivityForResult(captureImageIntent, REQUEST_PHOTO);
                break;

            case R.id.removeImgBtn:
                imageView.setImageBitmap(null);
                uri=null;
                removeImgBtn.setVisibility(View.GONE);
                break;

            case R.id.completeBtn:
                activityName=actNameEditTxt.getText().toString();
                location=locationInputTxtView.getText().toString();
                date=dateInputTxtView.getText().toString();
                time=timeInputTxtView.getText().toString();
                reporter=reporterNameEditTxt.getText().toString();
                if(activityName.isEmpty()){
                    actErrorMsg.setVisibility(View.VISIBLE);
                }
                else {
                    actErrorMsg.setVisibility(View.INVISIBLE);
                }
                if(date.isEmpty()){
                    dateErrorMsg.setVisibility(View.VISIBLE);
                }
                else {
                    dateErrorMsg.setVisibility(View.INVISIBLE);
                }
                if(time.isEmpty()){
                    timeErrorMsg.setVisibility(View.VISIBLE);
                }
                else {
                    timeErrorMsg.setVisibility(View.INVISIBLE);
                }
                if(reporter.isEmpty()){
                    reporterErrorMsg.setVisibility(View.VISIBLE);
                }
                else {
                    reporterErrorMsg.setVisibility(View.INVISIBLE);
                }
                if(location.isEmpty()){
                    location=null;
                }
                String attached;
                if(uri==null || imageView.getDrawable() == null){
                    uriStr=null;
                    attached = "No image taken";
                }else{
                    uriStr=uri.toString();
                    attached = "Attached";
                }

                if(!activityName.isEmpty() && !date.isEmpty() &&!time.isEmpty() && !reporter.isEmpty()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.AlertDialogCustom);
                    builder.setTitle("Confirmation")
                            .setMessage("Are you sure you want to add an report with these details?\n" +
                                    "\n" +
                                    "Activity Name : " + activityName + "\n" +
                                    "\n" +
                                    "Location : " + location + "\n" +
                                    "\n" +
                                    "Date : " + date + "\n" +
                                    "\n" +
                                    "Time : " + time + "\n" +
                                    "\n" +
                                    "Reporter : " + reporter +"\n" +
                                    "\n" +
                                    "Image : " + attached)
                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Report report= new Report(activityName,location,date,time,reporter, uriStr);
                                    reportViewModel.insert(report);
                                    actNameEditTxt.setText("");
                                    reporterNameEditTxt.setText("");
                                    getActivity().onBackPressed();
                                }
                            })
                            .setNegativeButton("Cancel",null)
                            .show();
                }

            default:
                break;
        }
    }

    public void processDatePickerResult(int year, int month, int day) {
        String month_string = Integer.toString(month + 1); // bc start from 0
        String day_string = Integer.toString(day);
        String year_string = Integer.toString(year);
        date = (day_string + "/" + month_string + "/" + year_string);

        dateInputTxtView.setText(date);
    }

    public void processTimePickerResult(int hourOfDay, int minute) {
         time = (String.format("%02d:%02d",hourOfDay , minute));
        timeInputTxtView.setText(time);
    }

    //standard code
    public void getLocation() { //check for the ACCESS_FINE_LOCATION permission.
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(
                    new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                new FetchAddressTask(getActivity(), AddReportFrag.this).execute((location));
                            }
                        }
                    });
        }
    }

    @Override
    //request permission, then now chk permission result with this function
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // If the permission is granted, get the location, otherwise, show a Toast
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation(); // after permit can get location
                } else {
                    Toast.makeText(getActivity(),
                            R.string.location_permission_denied,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onTaskCompleted(String result) {
        ((MainActivity)getActivity()).openMap(result, null, "true"); //open google map
    }

    //setup methods to get file name and file location
    public String getPhotoFileName() {
        String fileName="";
        id = UUID.randomUUID();

        fileName = "IMG_" + id.toString() + ".jpg";
        Log.d("FILE", fileName);
        return fileName;
    }

    //get the file path
    public File getPhotoFile() {
        //construct the file object
        File fileDir = getActivity().getFilesDir();
        return new File(fileDir, getPhotoFileName());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        //other implementation

        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == REQUEST_PHOTO) {
            //retrieve back the file from file system
            uri = FileProvider.getUriForFile(getContext(), "com.example.mykid.fileprovider", photoFile);

            getContext().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updatePhotoView();
        }
    }

    private void updatePhotoView() {
        if (photoFile == null || !photoFile.exists()) {
            imageView.setImageDrawable(null);
        }
        else
        {
            Bitmap bitmap = PictureUtils.getScaledBitmap(photoFile.getPath(), getActivity());
            imageView.setImageBitmap(bitmap);
            removeImgBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("date",date);
        outState.putString("time",time);
        outState.putString("location",result);
        if(uri!=null){
            outState.putString("Uri",uri.toString());
        }
    }
}