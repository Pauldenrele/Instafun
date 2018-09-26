package com.example.paul.socialnetwork;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {


    private RecyclerView myFriendsList;
    private DatabaseReference UsersRef , FriendsRef;
    private FirebaseAuth mAuth;
    private String online_user_id;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

   myFriendsList=(RecyclerView)findViewById(R.id.friend_list);
   UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
   FriendsRef=FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
   mAuth=FirebaseAuth.getInstance();
   online_user_id= mAuth.getCurrentUser().getUid();


        myFriendsList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendsList.setLayoutManager(linearLayoutManager);
    
        DisplayAllFriends();

    }

    private void DisplayAllFriends() {

        FirebaseRecyclerAdapter<Friends , FriendsViewHolder> firebaseRecyclerAdapter=
                new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                        Friends.class,
                        R.layout.all_user_display_layout,
                        FriendsViewHolder.class,
                        FriendsRef
                ) {
                    @Override
                    protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {

                        viewHolder.setDate(model.getDate());
                   final String userIDS =getRef(position).getKey();



                   UsersRef.child(userIDS).addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           if(dataSnapshot.exists()){
                               final String userName =dataSnapshot.child("fullname").getValue().toString();
                               final String profileImage=dataSnapshot.child("profileimage").getValue().toString();

                               viewHolder.setFullname(userName);
                               viewHolder.setProfileimage(getApplicationContext() , profileImage);

                           }
                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError databaseError) {

                       }
                   });
                    }
                };
        myFriendsList.setAdapter(firebaseRecyclerAdapter);
    }


    public  static  class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView=itemView;

        }
        public void setProfileimage(final Context ctx , final String profileimage){
            final CircleImageView myImage = (CircleImageView)mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).networkPolicy(NetworkPolicy.OFFLINE)
                    .into(myImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myImage);
                        }
                    });

        }
        public void setFullname(String fullname) {
            TextView myName = (TextView)mView.findViewById(R.id.all_user_profile_full_name);
            myName.setText(fullname);
        }
        public void setDate(String date) {
            TextView friendsDate = (TextView)mView.findViewById(R.id.all_user_profile_full_name);
            friendsDate.setText("Friends since: " + date);
        }
    }
}
