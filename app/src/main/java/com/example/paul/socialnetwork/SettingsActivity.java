package com.example.paul.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
  private StorageReference UserProfileImageRef;
    private EditText userName , userProfName , userStatus , userCountry ,userGender , userRelation , userDOB;
    private Button UpdateAccountSettingsButton;
    private CircleImageView userProfImage;
    private FirebaseAuth mAuth ;
    private DatabaseReference SettingsUserRef;
    private String currentUserId;
    final static int Gallery_Pick =1;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth=FirebaseAuth.getInstance();

         currentUserId=mAuth.getCurrentUser().getUid();
         SettingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
         SettingsUserRef.keepSynced(true);

        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");
   mToolbar=(Toolbar)findViewById(R.id.settings_toolbar);
   setSupportActionBar(mToolbar);
   getSupportActionBar().setTitle("Account Settings");
   getSupportActionBar().setDisplayHomeAsUpEnabled(true);

   userName =(EditText)findViewById(R.id.settings_username);
   userProfName=(EditText)findViewById(R.id.settings_profile_full_name);
   userStatus =(EditText)findViewById(R.id.settings_status);
        userCountry=(EditText)findViewById(R.id.settings_country);
        userGender=(EditText)findViewById(R.id.settings_gender);
        userRelation =(EditText)findViewById(R.id.settings_relationship_status);
       userDOB=(EditText)findViewById(R.id.settings_dob);
       UpdateAccountSettingsButton =(Button)findViewById(R.id.update_account_settings_button);
       userProfImage=(CircleImageView)findViewById(R.id.settings_profile_image);

        loadingBar = new ProgressDialog(this);
      SettingsUserRef.addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              if(dataSnapshot.exists()){
                  final String myProfileImage =dataSnapshot.child("profileimage").getValue().toString();
                  String myUserName =dataSnapshot.child("username").getValue().toString();
                  String myProfileName =dataSnapshot.child("fullname").getValue().toString();
                  String myProfileStatus =dataSnapshot.child("status").getValue().toString();
                  String myDOB =dataSnapshot.child("dob").getValue().toString();
                  String myCountry =dataSnapshot.child("country").getValue().toString();
                  String myGender =dataSnapshot.child("gender").getValue().toString();
                  String myRelationStatus =dataSnapshot.child("relationshipstatus").getValue().toString();

                  Picasso.with(SettingsActivity.this).load(myProfileImage).networkPolicy(NetworkPolicy.OFFLINE)
                          .placeholder(R.drawable.profile)
                          .into(userProfImage, new Callback() {
                              @Override
                              public void onSuccess() {

                              }

                              @Override
                              public void onError() {
                                  Picasso.with(SettingsActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);

                              }
                          });
                  userName.setText(myUserName);
                  userProfName.setText(myProfileName);
                  userStatus.setText(myProfileStatus);
                  userDOB.setText(myDOB);
                  userCountry.setText(myCountry);
                  userGender.setText(myGender);
                  userRelation.setText(myRelationStatus);

              }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {

          }
      });

      UpdateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              ValidateAccountInfo();
          }
      });
           userProfImage.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent galleryIntent = new Intent();
                   galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                   galleryIntent.setType("image/*");
                   startActivityForResult(galleryIntent , Gallery_Pick);
               }
           });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null){
            Uri ImageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1 , 1)
                    .start(this);
        }
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK){

                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait while uploading profile image");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);
                final Uri resultUri =result.getUri();


/*
                final StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                final String downloadUrl =
                                        uri.toString();
                            }
                        });
                    }


                        UsersRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SetupActivity.this, "Image stored", Toast.LENGTH_SHORT).show();
                            } else {
                                String message = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }

                }
            });*/

                StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this , "Profile Image Stored in firebase" ,Toast.LENGTH_SHORT).show();
                            //1st

/*
                              UserProfileImageRef.child("profileimage").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()

                              {
                                  @Override
                                  public void onSuccess(Uri uri) {

                                      Intent selfIntent = new Intent(SetupActivity.this , SetupActivity.class);
                                      startActivity(selfIntent);
                                      Uri downloadUrl = uri;

                                      UsersRef.child("profileimage").setValue(uri);
                                      Toast.makeText(getBaseContext(), "Upload success! URL - " + downloadUrl.toString() , Toast.LENGTH_SHORT).show();
                                      loadingBar.dismiss();


                                  }
                              });*/


                            //2nd
                            final StorageReference ref = UserProfileImageRef.child("profileimage");
                            Task<UploadTask.TaskSnapshot> uploadTask = ref.putFile(resultUri);



                            Task<Uri> urlTask = task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }

                                    // Continue with the task to get the download URL
                                    return ref.getDownloadUrl();
                                }
                            })
                                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful()) {
                                                Intent selfIntent = new Intent(SettingsActivity.this , SettingsActivity.class);
                                                startActivity(selfIntent);
                                                Toast.makeText(SettingsActivity.this , "Profile Image Stored" , Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                                Uri downloadUri = task.getResult();
                                                SettingsUserRef.child("profileimage").setValue(downloadUri.toString());


                                            } else {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SettingsActivity.this , "Error occurred " +message , Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                                // Handle failures
                                                // ...
                                            }
                                        }
                                    });
                            //3rd

                            /*   downloadURL = task.getResult().getStorage().getDownloadUrl().toString();
                              UsersRef.child("profileimage").setValue(downloadURL)
                                      .addOnCompleteListener(new OnCompleteListener<Void>() {
                                          @Override
                                          public void onComplete(@NonNull Task<Void> task) {
                                              if(task.isSuccessful()){

                                                  Intent selfIntent = new Intent(SetupActivity.this , SetupActivity.class);
                                                  startActivity(selfIntent);
                                                  Toast.makeText(SetupActivity.this , "Profile Image Stored" , Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                              }else{
                                                  String message = task.getException().getMessage();
                                                  Toast.makeText(SetupActivity.this , "Error occurred " +message , Toast.LENGTH_SHORT).show();
                                              loadingBar.dismiss();
                                              }

                                          }
                                      });*/
                        }
                    }
                });

            }
            else {
                Toast.makeText(SettingsActivity.this , "Error post can't be Cropped" , Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void ValidateAccountInfo() {

        String username = userName.getText().toString();
        String profilename =userProfName.getText().toString();
        String status = userStatus.getText().toString();
        String dob =userDOB.getText().toString();
        String country = userCountry.getText().toString();
        String gender =userGender.getText().toString();
        String relation = userRelation.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this , "Please enter your username " , Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(profilename)){
            Toast.makeText(this, "Please enter your profile name", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(status)){
            Toast.makeText(this, "Please enter your status", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(dob)){
            Toast.makeText(this, "Please enter your date of birth", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(country)){
            Toast.makeText(this, "Please enter your country name", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(gender)){
            Toast.makeText(this, "Please enter your gender ", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(status)){
        Toast.makeText(this, "Please enter your status ", Toast.LENGTH_SHORT).show();
    }

        else if (TextUtils.isEmpty(relation)){
            Toast.makeText(this, "Please enter your relationship", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait while uploading profile image");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            UpdateAccountInfo(username , profilename , dob , country , gender , relation,status);
        }

    }

    private void UpdateAccountInfo(String username, String profilename, String dob, String country, String gender, String relation , String status) {
        HashMap userMap = new HashMap();
        userMap.put("username" , username);
        userMap.put("fullname" , profilename);
        userMap.put("status" , status);
        userMap.put("dob" , dob);
        userMap.put("country" , country);
        userMap.put("gender" , gender);
        userMap.put("relationshipstatus" , relation);
        SettingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Account Settings Updated", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(SettingsActivity.this, "Error occurred while updating your profile", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    private void SendUserToMainActivity(){
        Intent setupIntent = new Intent(SettingsActivity.this ,MainActivity.class );
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}
