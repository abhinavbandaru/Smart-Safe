package com.abhinavbandaru.smartsafe;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class HomePage extends AppCompatActivity {
    Button googleSignOutButton;
    Button panicButton;
    Button stopPanicButton;
    GoogleSignInClient mGoogleSignInClient;
    TextView googleName;
    TextView googleEmail;
    AudioManager audioManager;
    MediaPlayer mediaPlayer;
    String emergencyContact;
    private AlertDialog dialog;
    String truePass, falsePass;
    int cnt = 0;
    private Handler handler;
    private LocationManager locationManager;
    private String latitude, longitude;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        googleName = findViewById(R.id.google_name);
        googleEmail = findViewById(R.id.google_email);
        panicButton = findViewById(R.id.panic_button);
        emergencyContact = "9912311177";
        truePass = "true";
        falsePass = "false";
        panicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                panicRunnable.run();
            }
        });
        handler = new Handler();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        stopPanicButton = findViewById(R.id.stop_panic_button);
        stopPanicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDia();
            }
        });
        googleSignOutButton = findViewById(R.id.sign_out_button);
        googleSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    // ...
                    case R.id.sign_out_button:
                        signOut();
                        break;
                    // ...
                }
            }
        });
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();
            googleName.setText(personName);
            googleEmail.setText(personEmail);
        }
    }

    private void showDia() {
        AlertDialog.Builder alert;
        alert = new AlertDialog.Builder(HomePage.this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.password_check, null);
        alert.setView(view);
        alert.setCancelable(true);
        dialog = alert.create();
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(HomePage.this, "Signed Out", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private Runnable panicRunnable = new Runnable() {
        @Override
        public void run() {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
            mediaPlayer = MediaPlayer.create(HomePage.this, R.raw.siren);
            mediaPlayer.start();
            if (ActivityCompat.checkSelfPermission(HomePage.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.checkSelfPermission(HomePage.this, Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null && cnt % 10 == 0) {
                double lat = locationGPS.getLatitude();
                double longi = locationGPS.getLongitude();
                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);
                sendLocationSMS(locationGPS);
            }
            cnt++;
            handler.postDelayed(panicRunnable, 9000);
        }
    };

    public void sendLocationSMS(Location currentLocation) {
        SmsManager smsManager = SmsManager.getDefault();
        StringBuffer smsBody = new StringBuffer();
        smsBody.append("In Danger, please help. Current Location: \n");
        smsBody.append("http://maps.google.com?q=");
        smsBody.append(currentLocation.getLatitude());
        smsBody.append(",");
        smsBody.append(currentLocation.getLongitude());
        smsManager.sendTextMessage(emergencyContact, null, smsBody.toString(), null, null);
        Toast.makeText(HomePage.this, "Message sent", Toast.LENGTH_SHORT).show();
    }

    private Runnable silentPanic = new Runnable() {
        @Override
        public void run() {
            if (ActivityCompat.checkSelfPermission(HomePage.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.checkSelfPermission(HomePage.this, Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null && cnt%30 == 0) {
                double lat = locationGPS.getLatitude();
                double longi = locationGPS.getLongitude();
                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);
                sendLocationSMS(locationGPS);
            }
            cnt++;
            handler.postDelayed(panicRunnable, 1000);
        }
    };

    public void loginbtnclk(View view){
        EditText password = dialog.findViewById(R.id.passcode);
        String pass = password.getText().toString();
        password.setText("");
        if (pass.equals(truePass)) {
            handler.removeCallbacks(panicRunnable);
            mediaPlayer.stop();
            cnt=0;
            Toast.makeText(HomePage.this, "true pass!", Toast.LENGTH_SHORT).show();
        } else if (pass.equals(falsePass)) {
            handler.removeCallbacks(panicRunnable);
            mediaPlayer.stop();
            cnt=0;
            Toast.makeText(HomePage.this, "false pass!", Toast.LENGTH_SHORT).show();
            silentPanic.run();
        } else {
            Toast.makeText(HomePage.this, "Wrong Password", Toast.LENGTH_SHORT).show();
        }
        dialog.dismiss();
    }
    public void cancelbtnclk(View view){
        dialog.dismiss();
    }

    public void enterphno(View view){
        EditText phno = findViewById(R.id.phno);
        emergencyContact = phno.getText().toString();
        Toast.makeText(HomePage.this, "Ph No: " + emergencyContact, Toast.LENGTH_LONG).show();
    }
}
