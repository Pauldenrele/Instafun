package com.example.paul.socialnetwork;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;
import io.opencensus.tags.Tag;

public class MainActivity extends AppCompatActivity {


    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private android.support.v7.widget.Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef,LikesRef;
    private CircleImageView NavProfileImage;
    private TextView NavProfileUsername;
    String currentUserID;
    private ImageButton AddNewPostButton;
    private FirebaseFirestore db;
  Boolean LikeChecker=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // if (!FirebaseApp.getApps(this).isEmpty())
// FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // PostsRef.keepSynced(true);
     /*  FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();*/

        //db.setFirestoreSettings(settings);



      /*  db.collection("cities").whereEqualTo("state", "CA")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                        @Nullable FirebaseFirestoreException e) {

                        String TAG = null;
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        for (DocumentChange change : querySnapshot.getDocumentChanges()) {



                            if (change.getType() == DocumentChange.Type.ADDED) {
                                Log.d(TAG, "New city:" + change.getDocument().getData());
                            }

                            String source = querySnapshot.getMetadata().isFromCache() ?
                                    "local cache" : "server";
                            Log.d(TAG ,"Data fetched from " + source);
                        }

                    }
                });*/


        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        PostsRef.keepSynced(true);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UsersRef.keepSynced(true);
        LikesRef=FirebaseDatabase.getInstance().getReference().child("Likes");
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);


        postList = (RecyclerView) findViewById(R.id.all_user_post_list);
        postList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavProfileUsername = (TextView) navView.findViewById(R.id.nav_user_full_name);


        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    if (dataSnapshot.hasChild("fullname")) {

                        String fullName = dataSnapshot.child("fullname").getValue().toString();
                        NavProfileUsername.setText(fullName);
                    }
                    if (dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();


                        Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                    } else {
                        Toast.makeText(MainActivity.this, "Profile name doesn't exist...", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        AddNewPostButton = (ImageButton) findViewById(R.id.add_new_post_button);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }
        });

        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPostActivity();


            }
        });

        DisplayAllUsersPost();


    }

    private void DisplayAllUsersPost() {

        Query SortPostsInDescendingOrder =PostsRef.orderByChild("counter");


        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(

                Posts.class,
                R.layout.all_post_layout,
                PostsViewHolder.class,
               SortPostsInDescendingOrder
        ) {
            @Override
            protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position) {


                final String PostKey = getRef(position).getKey();

                viewHolder.setFullname(model.getFullname());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());
                viewHolder.setPostimage(getApplicationContext(), model.getPostimage());
           viewHolder.setLikeButtonStatus(PostKey);
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey", PostKey);
                        startActivity(clickPostIntent);
                    }
                });

                viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent = new Intent(MainActivity.this, CommentsActivity.class);
                        commentsIntent.putExtra("PostKey", PostKey);
                        startActivity(commentsIntent);
                    }
                });
                viewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
            LikeChecker=true;

            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   if(LikeChecker.equals(true)){
                       if(dataSnapshot.child(PostKey).hasChild(currentUserID)){
                           LikesRef.child(PostKey).child(currentUserID).removeValue();
                           LikeChecker=false;
                       }else{
                           LikesRef.child(PostKey).child(currentUserID).setValue(true);
                           LikeChecker=false;
                       }
                   }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
                    }
                });

            }
        };
        postList.setAdapter(firebaseRecyclerAdapter);

    }


    public static class PostsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageButton LikePostButton , CommentPostButton;
          TextView DisplayNoOfLikes;
          int countLikes;
          String currentUserId;
          DatabaseReference LikesRef;
        public PostsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            LikePostButton =(ImageButton)mView.findViewById(R.id.like_button);
            CommentPostButton=(ImageButton)mView.findViewById(R.id.comment_button);
            DisplayNoOfLikes =(TextView)mView.findViewById(R.id.display_no_of_likes);

            LikesRef=FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();


        }

        public  void setLikeButtonStatus(final  String PostKey){
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(PostKey).hasChild(currentUserId)){
                        countLikes=(int)dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.ic_favorite_black_24dp);
                        DisplayNoOfLikes.setText(Integer.toString(countLikes) + (" Likes"));
                    }else {
                        countLikes=(int)dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.ic_dislike_black_24dp);
                        DisplayNoOfLikes.setText(Integer.toString(countLikes) + (" Likes"));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void setFullname(String fullname) {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(final Context ctx, final String profileimage) {
            final CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            //Picasso.with(ctx).load(profileimage).into(image);

            Picasso.with(ctx).load(profileimage).networkPolicy(NetworkPolicy.OFFLINE).into(image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(profileimage).into(image);


                }
            });
        }

        public void setTime(String time) {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText("  " + time);
        }

        public void setDate(String date) {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText("  " + date);
        }

        public void setDescription(String description) {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(final Context ctx, final String postimage) {
            final ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            //  Picasso.with(ctx).load(postimage).into(PostImage);

            Picasso.with(ctx).load(postimage).networkPolicy(NetworkPolicy.OFFLINE).into(PostImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(postimage).into(PostImage);
                }
            });

        }

    }

    private void SendUserToPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();


        if (currentUser == null) {
            SendUserToLoginActivity();

        } else {
            CheckUserExistence();

        }

    }

    private void CheckUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(current_user_id)) {
                    SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();

    }

    private void SendUserToLoginActivity() {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }


        return super.onOptionsItemSelected(item);

    }


    private void UserMenuSelector(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_post:
                SendUserToPostActivity();
                break;

            case R.id.nav_profile:
               SendUserToProfileActivity();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
              SendUserToFriendsActivity();
                break;
            case R.id.nav_find_friends:
               SendUserToFindfriendsActivity();
                break;
            case R.id.nav_setting:
                SendUserToSettingsActivity();
                break;
            case R.id.nav_Logout:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
        }
    }

    private void SendUserToFriendsActivity() {

        Intent loginIntent = new Intent(MainActivity.this, FriendsActivity.class);

        startActivity(loginIntent);
    }

    private void SendUserToSettingsActivity() {

        Intent loginIntent = new Intent(MainActivity.this, SettingsActivity.class);

        startActivity(loginIntent);


    }

    private void SendUserToFindfriendsActivity() {

        Intent loginIntent = new Intent(MainActivity.this, FindFriendsActivity.class);

        startActivity(loginIntent);


    }

    private void SendUserToProfileActivity() {

        Intent loginIntent = new Intent(MainActivity.this, ProfileActivity.class);

        startActivity(loginIntent);
    }
}
