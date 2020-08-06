package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class SignInActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    PhoneAuthCredential credential;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    FirebaseUser user;
    String mVerificationId,code;
    Button Send,Verify;
    EditText Phone,Code;
    ProgressBar progressBar;
    Toolbar toolbar;
    DatabaseReference rootdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAuth=FirebaseAuth.getInstance();
        progressBar=findViewById(R.id.progress_login);
        Phone=findViewById(R.id.login_phone_e);
        Send=findViewById(R.id.send_verification_code_b);
        Code=findViewById(R.id.code_login_e);
        Verify=findViewById(R.id.verify_code_b);
        toolbar=findViewById(R.id.app_bar_layout_login);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatApp");
        rootdb= FirebaseDatabase.getInstance().getReference();
        Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send();
            }
        });
        Verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verify();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:


                break;
        }
        return true;
    }

    void verify(){
        code=Code.getText().toString().trim();
        if(TextUtils.isEmpty(code)){
            Code.setError("Verification Code required");
            Code.requestFocus();
            return;
        }
        credential=PhoneAuthProvider.getCredential(mVerificationId,code);
        signInWithPhoneAuthCredential(credential);
    }

    void send(){
        String phoneno=Phone.getText().toString().trim();
        if(TextUtils.isEmpty(phoneno)){
            Phone.setError("Phone no. required");
            Phone.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneno,60, TimeUnit.SECONDS,this,callback);
    }



    OnVerificationStateChangedCallbacks callback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override

        public void onVerificationCompleted(PhoneAuthCredential credentiall) {


            signInWithPhoneAuthCredential(credentiall);
        }



        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.


            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                // ...
            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                // ...
            }

            // Show a message and update the UI
            // ...
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                @NonNull PhoneAuthProvider.ForceResendingToken token) {
            progressBar.setVisibility(View.INVISIBLE);
            mVerificationId = verificationId;
            mResendToken = token;
            Code.setVisibility(View.VISIBLE);
            Verify.setVisibility(View.VISIBLE);
            Phone.setVisibility(View.INVISIBLE);
            Send.setVisibility(View.INVISIBLE);


        }
    };

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.INVISIBLE);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            Toast.makeText(SignInActivity.this,"SignIn Successful",Toast.LENGTH_LONG).show();
                            user = task.getResult().getUser();

                            send(ProfileUpdate.class);
                            rootdb.child("ContactLog").child(user.getPhoneNumber()).setValue(user.getUid());
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(SignInActivity.this,"The verification code entered was invalid",Toast.LENGTH_LONG).show();
                            }else {
                                Toast.makeText(SignInActivity.this,task.getException().toString(),Toast.LENGTH_LONG).show();
                            }
                            Code.setVisibility(View.GONE);
                            Verify.setVisibility(View.GONE);
                            Phone.setVisibility(View.VISIBLE);
                            Send.setVisibility(View.VISIBLE);

                        }
                    }
                });
    }

    void send(Class c){
        Intent i=new Intent(SignInActivity.this,c);
        if(c==ProfileUpdate.class)i.putExtra("uid",user.getUid());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }
}