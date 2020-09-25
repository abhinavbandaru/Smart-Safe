package com.abhinavbandaru.smartsafe;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
    private Handler handler;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mediaPlayer = MediaPlayer.create(this, R.raw.siren);
        audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        googleName = findViewById(R.id.google_name);
        googleEmail = findViewById(R.id.google_email);
        panicButton = findViewById(R.id.panic_button);
        emergencyContact = "9989300201";
        panicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                panicRunnable.run();
            }
        });
        stopPanicButton = findViewById(R.id.stop_panic_button);
        stopPanicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.removeCallbacks(panicRunnable);
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
            mediaPlayer.start();
            Intent smsIntent = new Intent(Intent.ACTION_VIEW);

            smsIntent.setDataAndType(Uri.parse("smsto:"),"vnd.android-dir/mms-sms");
            smsIntent.putExtra("address"  , new String (emergencyContact));
            smsIntent.putExtra("sms_body"  , "Test ");

            try {
                startActivity(smsIntent);
                finish();
                Log.i("Finished sending SMS...", "");
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(HomePage.this,
                        "SMS faild, please try again later.", Toast.LENGTH_SHORT).show();
            }
            handler.postDelayed(panicRunnable,9000);
        }
    };
}
