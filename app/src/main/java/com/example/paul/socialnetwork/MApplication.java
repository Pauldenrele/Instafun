package com.example.paul.socialnetwork;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;


public class MApplication extends Application{


        @Override
        public void onCreate() {
                super.onCreate();
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);

                Picasso.Builder builder  = new Picasso.Builder(this);
                builder.downloader(new OkHttpDownloader(this , Integer.MAX_VALUE));
                Picasso built =builder.build();
                built.setIndicatorsEnabled(false);
                built.setLoggingEnabled(true);
                Picasso.setSingletonInstance(built);
        }
}
