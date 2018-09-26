package com.example.paul.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private Button LoginButton;
    private ImageView googleSignInButton;
    private EditText UserEmail, UserPassword;
    private TextView NeedNewAccountLink;
      private FirebaseAuth mAuth;
      private ProgressDialog loadingBar;
      private static final int RC_SIGN_IN=1;
      private GoogleApiClient mGoogleSignInClient;
      private static final String TAG ="LoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

       mAuth=FirebaseAuth.getInstance();
        LoginButton = (Button)findViewById(R.id.login);
        UserEmail=(EditText)findViewById(R.id.login_email);
        UserPassword=(EditText)findViewById(R.id.login_password);
        NeedNewAccountLink  =(TextView)findViewById(R.id.textView);
        loadingBar = new ProgressDialog(this);
        googleSignInButton =(ImageView)findViewById(R.id.google_signin_button);


        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               SendUserToRegisterActivity();
            }
        });
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient =new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                             Toast.makeText(LoginActivity.this , "Connection to google sign in failed!" , Toast.LENGTH_SHORT).show();
                    }
                })
              .addApi(Auth.GOOGLE_SIGN_IN_API , gso)
                .build();

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


    }
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent( mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            loadingBar.setTitle("Google Sign In");
            loadingBar.setMessage("Please wait  while we are logging you in through your google account...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if(result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Toast.makeText(this , "Authenticating" ,Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this , "Can't authenticate" , Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }

        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            SendUserToMainActivity();
                            Log.d(TAG, "signInWithCredential:success");
                            loadingBar.dismiss();


                        } else {
                         String message = task.getException().toString();

                            Toast.makeText(LoginActivity.this , "Not authenticated : " +message , Toast.LENGTH_SHORT).show();
                            SendUserToLoginActivity();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                             loadingBar.dismiss();
                        }


                    }
                });
    }

    private void SendUserToLoginActivity() {
        Intent setupIntent =new Intent(LoginActivity.this , LoginActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();


        if(currentUser!=null){
            SendUserToMainActivity();

        }
    }

    private void AllowUserToLogin() {
        String email=UserEmail.getText().toString();
        String password =UserPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
           Toast.makeText(this, "Please Enter your email", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this , "Please enter your password " ,Toast.LENGTH_SHORT).show();
        }
        else{

            loadingBar.setTitle("Login");
            loadingBar.setMessage("Please wait ...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email , password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                     if(task.isSuccessful()){
                         SendUserToMainActivity();
                         Toast.makeText(LoginActivity.this , "You are logged in successfully" , Toast.LENGTH_SHORT).show();

                     }
                     else {
                         String message =task.getException().getMessage();
                         Toast.makeText(LoginActivity.this , "Error occurred " +message , Toast.LENGTH_SHORT).show();
                     }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent setupIntent =new Intent(LoginActivity.this , MainActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToRegisterActivity(){
        Intent registerActivity= new Intent(LoginActivity.this , RegisterActivity.class);
        startActivity(registerActivity);
    }
}



