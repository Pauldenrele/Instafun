package com.example.paul.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
 private EditText UserName , FullName , CountryName;
 private Button SaveUserInformationButton;
 private CircleImageView ProfileImage;
  private ProgressDialog loadingBar;
 private FirebaseAuth mAuth;
 private DatabaseReference UsersRef;
 private StorageReference UserProfileImageRef;
    private String downloadURL;

 String currentUserId;
 final static int Gallery_Pick =1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
          UsersRef.keepSynced(true);
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");



        UserName=(EditText)findViewById(R.id.setup_username);
        FullName=(EditText)findViewById(R.id.setup_full_name);
        CountryName=(EditText)findViewById(R.id.setup_country_name);
         SaveUserInformationButton=(Button)findViewById(R.id.setup_information_button);
         ProfileImage=(CircleImageView)findViewById(R.id.setup_profile_image);
         loadingBar = new ProgressDialog(this);


         SaveUserInformationButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 SaveAccountSetupInformation();
             }
         });
          ProfileImage.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Intent galleryIntent = new Intent();
                  galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                  galleryIntent.setType("image/*");
                  startActivityForResult(galleryIntent , Gallery_Pick);
              }
          });
          UsersRef.addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                  if(dataSnapshot.exists()){

                      if(dataSnapshot.hasChild("profileimage")) {
                         final String image = dataSnapshot.child("profileimage").getValue().toString();
                      //   Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(ProfileImage);
                          Picasso.with(SetupActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).into(ProfileImage, new Callback() {
                              @Override
                              public void onSuccess() {

                              }

                              @Override
                              public void onError() {
                                  Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(ProfileImage);
                              }
                          });
                      }
                      else{
                          Toast.makeText(SetupActivity.this , "Please select profile image first" , Toast.LENGTH_SHORT).show();
                      }
                  }

              }

              @Override
              public void onCancelled(@NonNull DatabaseError databaseError) {

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
                              Toast.makeText(SetupActivity.this , "Profile Image Stored in firebase" ,Toast.LENGTH_SHORT).show();
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
                                          Intent selfIntent = new Intent(SetupActivity.this , SetupActivity.class);
                                          startActivity(selfIntent);
                                          Toast.makeText(SetupActivity.this , "Profile Image Stored" , Toast.LENGTH_SHORT).show();
                                          loadingBar.dismiss();
                                          Uri downloadUri = task.getResult();
                                          UsersRef.child("profileimage").setValue(downloadUri.toString());


                                      } else {
                                          String message = task.getException().getMessage();
                                          Toast.makeText(SetupActivity.this , "Error occurred " +message , Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SetupActivity.this , "Error post can't be Cropped" , Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void SaveAccountSetupInformation() {

        String username =UserName.getText().toString();
        String fullname= FullName.getText().toString();
        String country =CountryName.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this , "Please enter your username" , Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(fullname)){
            Toast.makeText(this , "Please enter your fullname" , Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(country)){
            Toast.makeText(this , "Please enter your country" , Toast.LENGTH_SHORT).show();
        }
        else{

            loadingBar.setTitle("Saving your information");
            loadingBar.setMessage("Please wait while we are creating your account");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            HashMap userMap = new HashMap();

            userMap.put("username" ,username);
            userMap.put("fullname" ,fullname);
            userMap.put("country" ,country);
            userMap.put("status" ,"Yayy my first cool app");
            userMap.put("gender" ,"none");
            userMap.put("dob" ,"none");
            userMap.put("relationshipstatus" ,"none");
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){

                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this , "Your account have been created" , Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    } else{

                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this , "Error occurred "  +message, Toast.LENGTH_SHORT).show();
                       loadingBar.dismiss();
                    }
                }
            });

        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent =new Intent(SetupActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}

