package com.example.paul.socialnetwork;

import android.Manifest;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ClickPostActivity extends AppCompatActivity {


    private ImageView PostImage;
    private Button EditPostButton , DeletePostButton;
    private TextView PostDescription;
      Bitmap bitmap;
      private FirebaseAuth mAuth;
    private DatabaseReference ClickPostRef;
   private static final int WRITE_EXTERNAL_STORAGE_CODE =1;
    private String PostKey ,currentUserID ,databaseUserID ,description , image ;

    Button mSaveBtn , mShareBtn , mWallBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth = FirebaseAuth.getInstance();

currentUserID = mAuth.getCurrentUser().getUid();

        PostKey=getIntent().getExtras().get("PostKey").toString();

    ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);
    ClickPostRef.keepSynced(true);




        PostDescription =(TextView)findViewById(R.id.click__post_description);
        EditPostButton =(Button)findViewById(R.id.edit_post_button);
        DeletePostButton =(Button)findViewById(R.id.delete_post_button);
        PostImage =(ImageView)findViewById(R.id.post_image);
       mSaveBtn=findViewById(R.id.saveBtn);
       mShareBtn=findViewById(R.id.shareBtn);
       mWallBtn=findViewById(R.id.wallBtn);


      DeletePostButton.setVisibility(View.INVISIBLE);
      EditPostButton.setVisibility(View.INVISIBLE);
     bitmap=((BitmapDrawable) PostImage.getDrawable()).getBitmap();



      mSaveBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M){
                if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                        PackageManager.PERMISSION_DENIED){
                    String[] permission ={Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permission , WRITE_EXTERNAL_STORAGE_CODE);


                }else{
                    saveImage();

                }


            }else {
                saveImage();
            }
          }
      });

      mShareBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

              shareImage();


          }
      });

      mWallBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
                 setImgWallpaper();
          }
      });

        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if(dataSnapshot.exists()){
                   description = dataSnapshot.child("description").getValue().toString();
                   image = dataSnapshot.child("postimage").getValue().toString();
                   databaseUserID = dataSnapshot.child("uid").getValue().toString();


                   PostDescription.setText(description);
                   Picasso.with(ClickPostActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(PostImage, new Callback() {
                       @Override
                       public void onSuccess() {

                       }

                       @Override
                       public void onError() {

                           Picasso.with(ClickPostActivity.this).load(image).into(PostImage);

                       }
                   });


                   if(currentUserID.equals(databaseUserID)){
                       //i want to put save button here and make it invisible
                       DeletePostButton.setVisibility(View.VISIBLE);
                       EditPostButton.setVisibility(View.VISIBLE);
                   }

                   EditPostButton.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           EditCurrentPost(description);

                       }
                   });
               }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

          DeletePostButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  DeleteCurrentPost();
              }
          });
    }

    private void setImgWallpaper() {
        WallpaperManager myWallManager =WallpaperManager.getInstance(getApplicationContext());
        try{
            myWallManager.setBitmap(bitmap);
            Toast.makeText(this, "Wallpaper set", Toast.LENGTH_SHORT).show();
        }catch (Exception e )
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareImage() {
        try{
            String s =  PostDescription.getText().toString();

            File file = new File(getExternalCacheDir() , "sample.png");

            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG , 100 , fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true , false);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_TEXT ,s );
            intent.putExtra(Intent.EXTRA_STREAM , Uri.fromFile(file));
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent , "share via"));



        }catch (Exception e)
        {
            Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImage() {
        String timeStamp =new SimpleDateFormat("yyyyMMdd_HHmmss"
        , Locale.getDefault()).format(System.currentTimeMillis());

        File path = Environment.getExternalStorageDirectory();

        File dir = new File (path + "/Firebase/");

        dir.mkdirs();


        String imageName = timeStamp + ".PNG";
        File file = new File (dir ,imageName);
        OutputStream out;
        try{
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG ,100 , out);
            out.flush();
            out.close();
            Toast.makeText(this, imageName + "saved to" +dir, Toast.LENGTH_SHORT).show();
        }catch(Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    private void EditCurrentPost(String description) {

        AlertDialog.Builder builder  = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post: ");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this , "Post Updated successfully" ,Toast.LENGTH_SHORT).show();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_red_dark);
    }

    private void DeleteCurrentPost() {

        ClickPostRef.removeValue();
        
        SendUserToMainActivity();

        Toast.makeText(ClickPostActivity.this , "Post Deleted" , Toast.LENGTH_SHORT).show();
    }

    private void SendUserToMainActivity() {

        Intent loginIntent = new Intent(ClickPostActivity.this ,LoginActivity.class );
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case WRITE_EXTERNAL_STORAGE_CODE:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    saveImage();
                }
                else{
                    Toast.makeText(this, "Enable permission to save Image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
