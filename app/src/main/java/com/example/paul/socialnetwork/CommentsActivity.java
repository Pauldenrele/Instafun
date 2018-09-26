package com.example.paul.socialnetwork;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {
    private RecyclerView CommentsList;
    private ImageButton PostCommentButton;
    private EditText CommentInputText;
     private DatabaseReference UsersRef,PostsRef;
     private FirebaseAuth mAuth;

    private String Post_Key , current_User_Id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Post_Key=getIntent().getExtras().get("PostKey").toString();
       UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
       PostsRef=FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");
        CommentsList=(RecyclerView)findViewById(R.id.comment_lists);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);
   mAuth=FirebaseAuth.getInstance();
   current_User_Id=mAuth.getCurrentUser().getUid();
        CommentInputText=(EditText)findViewById(R.id.comment_input);
        PostCommentButton=(ImageButton)findViewById(R.id.post_comment_btn);

        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           UsersRef.child(current_User_Id).addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   if(dataSnapshot.exists()){
                       String userName = dataSnapshot.child("username").getValue().toString();
                    ValidateComment(userName);
                    CommentInputText.setText("");
                   }

               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Comments , CommentViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Comments, CommentViewHolder>(

                        Comments.class,
                        R.layout.all_comments_layout,
                        CommentViewHolder.class,
                        PostsRef
                ) {
                    @Override
                    protected void populateViewHolder(CommentViewHolder viewHolder, Comments model, int position) {
                         viewHolder.setUsername(model.getUsername());
                        viewHolder.setComment(model.getComment());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setTime(model.getTime());
                    }
                };
        CommentsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentViewHolder extends  RecyclerView.ViewHolder{
        View mView;

        public CommentViewHolder(View itemView) {
            super(itemView);
            mView=itemView;

        }
        public void setUsername(String username) {
            TextView myUserName = (TextView)mView.findViewById(R.id.comment_username);
            myUserName.setText( "@" +username +" ");

        }
        public void setComment(String comments) {
            TextView myComment = (TextView)mView.findViewById(R.id.comment_text);
            myComment.setText(comments);



        }
        public void setDate(String date) {
            TextView myUserName = (TextView)mView.findViewById(R.id.comment_date);
            myUserName.setText(" Date: " + date);


        }
        public void setTime(String time) {
            TextView myTime = (TextView)mView.findViewById(R.id.comment_time);
            myTime.setText(" Time: " +time);


        }
    }

    private void ValidateComment(String userName) {
        String commentText = CommentInputText.getText().toString();

        if(TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show();

        }else{
            Calendar calFordDate =Calendar.getInstance();
            SimpleDateFormat currentDate =new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime =Calendar.getInstance();
            SimpleDateFormat currentTime =new SimpleDateFormat("HH:mm");

            final String saveCurrentTime = currentTime.format(calFordTime.getTime());

            final  String RandomKey = saveCurrentDate + saveCurrentTime +current_User_Id;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid", current_User_Id);
            commentsMap.put("comments", commentText);
            commentsMap.put("date", saveCurrentDate);
            commentsMap.put("time", saveCurrentTime);
            commentsMap.put("username", userName);
          PostsRef.child(RandomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
              @Override
              public void onComplete(@NonNull Task task) {
                  if(task.isSuccessful()){
                     // Toast.makeText(CommentsActivity.this, "Commented successfully", Toast.LENGTH_SHORT).show();
                  }else{
                      Toast.makeText(CommentsActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                  }
              }
          });



        }
    }
}
