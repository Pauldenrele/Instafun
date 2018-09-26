package com.example.paul.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.images.internal.ImageUtils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class PostActivity extends AppCompatActivity {
  private  String saveCurrentDate;
    private String saveCurrentTime;
    private String postRandomName;
    private Task<Uri> downloadUrl;
    private String current_user_id;
    private Toolbar mToolbar;
    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;
    private static  final int Gallery_Pick=1;
  private Uri ImageUri;
  private FirebaseAuth mAuth;
  private StorageReference PostImagesRefrence;
  private ProgressDialog loadingBar;
  private DatabaseReference UsersRef ,PostsRef;
  private String Description;
  private long countPosts = 0;

    HashMap postsMap =new HashMap();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mAuth=FirebaseAuth.getInstance();
        current_user_id=mAuth.getCurrentUser().getUid();
        PostImagesRefrence= FirebaseStorage.getInstance().getReference().child("Post Images");
      UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        SelectPostImage=(ImageButton)findViewById(R.id.select_post_image);
        UpdatePostButton = (Button)findViewById(R.id.update_post_button);
       PostDescription =(EditText)findViewById(R.id.post_description);
     loadingBar =new ProgressDialog(this);



        mToolbar = (Toolbar)findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

      SelectPostImage.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              OpenGallery();
          }
      });
      UpdatePostButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              ValidatePostInfo();
          }
      });

    }

    private void ValidatePostInfo() {
        Description = PostDescription.getText().toString();

        if(ImageUri==null){
            Toast.makeText(this , "PLease select post Image..." , Toast.LENGTH_SHORT).show();

        }
        if(TextUtils.isEmpty(Description)){
            Toast.makeText(this , "Please add a Caption" ,Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Add new Post");
            loadingBar.setMessage("Please wait while updating new post");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            StoringImageToFirebaseStorage();

        }
    }

    private void StoringImageToFirebaseStorage() {

        Calendar calFordDate =Calendar.getInstance();
        SimpleDateFormat currentDate =new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());


        Calendar calFordTime =Calendar.getInstance();
        SimpleDateFormat currentTime =new SimpleDateFormat("HH:mm:ss");

        saveCurrentTime = currentTime.format(calFordTime.getTime());

        postRandomName=saveCurrentDate + saveCurrentTime;
        final StorageReference filePath =PostImagesRefrence.child(ImageUri.getLastPathSegment() + postRandomName +".jpg");
        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){




                    downloadUrl=task.getResult().getStorage().getDownloadUrl();
                    final StorageReference ref = filePath;
               Task<UploadTask.TaskSnapshot> uploadTask = ref.putFile(ImageUri);

                    //downloadUrl=ref.getDownloadUrl();


                    downloadUrl = task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return ref.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {


                                HashMap postsMap =new HashMap();
                                postsMap.put("postimage" , downloadUrl);
                                Intent selfIntent = new Intent(PostActivity.this , MainActivity.class);
                                startActivity(selfIntent);
                                Toast.makeText(PostActivity.this , "Post Image Stored" , Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Uri downloadUrl = task.getResult();
                                PostsRef.child(current_user_id + postRandomName).child("postimage").setValue(downloadUrl.toString());


                            } else {
                                String message = task.getException().getMessage();
                                Toast.makeText(PostActivity.this , "Error occurred " +message , Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                // Handle failures
                                // ...
                            }
                        }
                    });

                    SavingPostInformationToDatabase();
                    Toast.makeText(PostActivity.this ,"Image Uploaded successfully " , Toast.LENGTH_SHORT).show();
                }
                else {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this , "Error occurred" + message , Toast.LENGTH_SHORT).show();
                }
            }
        });




          }



    private void SavingPostInformationToDatabase() {


        PostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
               countPosts =dataSnapshot.getChildrenCount();
                }else{
                   countPosts=0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
      UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


              if (dataSnapshot.exists()){
               final    String userFullName = dataSnapshot.child("fullname").getValue().toString();
            final  String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

              HashMap postsMap = new HashMap();
              postsMap.put("uid", current_user_id);
              postsMap.put("date", saveCurrentDate);
              postsMap.put("time", saveCurrentTime);
              postsMap.put("description", Description);
              postsMap.put("profileimage", userProfileImage);
              postsMap.put("fullname", userFullName);
              postsMap.put("counter", countPosts);

              PostsRef.child(current_user_id + postRandomName).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                  @Override
                  public void onComplete(@NonNull Task task) {
                      if (task.isSuccessful()) {
                          SendUserToMainActvity();
                          Toast.makeText(PostActivity.this, "New Post Updated Successfully", Toast.LENGTH_SHORT).show();
                          loadingBar.dismiss();
                      } else {

                          Toast.makeText(PostActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                          loadingBar.dismiss();

                      }
                  }
              });

          }

          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {

          }
      });

    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent , Gallery_Pick);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null){
            ImageUri= data.getData();
            SelectPostImage.setImageURI(ImageUri);




    }}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id = item.getItemId();

        if(id==android.R.id.home){
            SendUserToMainActvity();

        }
        return super.onOptionsItemSelected(item);

    }

    private void SendUserToMainActvity() {

        Intent mainIntent =new Intent(PostActivity.this , MainActivity.class);
        startActivity(mainIntent);


    }
}
