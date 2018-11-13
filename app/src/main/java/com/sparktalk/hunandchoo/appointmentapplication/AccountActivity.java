package com.sparktalk.hunandchoo.appointmentapplication;

import android.*;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.sparktalk.hunandchoo.appointmentapplication.model.AppointmentModel;
import com.sparktalk.hunandchoo.appointmentapplication.model.ChatModel;
import com.sparktalk.hunandchoo.appointmentapplication.model.GpsModel;
import com.sparktalk.hunandchoo.appointmentapplication.model.ResultModel;
import com.sparktalk.hunandchoo.appointmentapplication.model.UserModel;


import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class AccountActivity extends AppCompatActivity {

    String destinationUid; // 피플 프래그먼트에서 넘어온 uid정보

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        destinationUid = getIntent().getStringExtra("destinationUid");

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.account_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(AccountActivity.this));
        recyclerView.setAdapter(new AccountRecyclerViewAdapter());

    }


    class AccountRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<UserModel> accountModels = new ArrayList<>();

        public AccountRecyclerViewAdapter(){
            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 접속자 본인 uid

            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                // DB로부터 데이터를 불러오는 부분
                public void onDataChange(DataSnapshot dataSnapshot) {
                    accountModels.clear();
                    for(DataSnapshot snapshot :dataSnapshot.getChildren()){

                        UserModel userModel = snapshot.getValue(UserModel.class);

                        // 해당 uid의 정보만 받아오기
                        if(!userModel.uid.equals(destinationUid)) {
                            continue;
                        }

                        accountModels.add(userModel);
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);

            return new CustomViewholder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            // 이미지
            Glide.with(holder.itemView.getContext())
                    .load(accountModels.get(position).profileImageUrl)
                    .apply(new RequestOptions().circleCrop())
                    .into(((CustomViewholder)holder).imageView);
            // 상태메시지
            if(accountModels.get(position).comment != null){
                ((CustomViewholder)holder).textView_comment.setText(accountModels.get(position).comment);
            }
            // 유저이름
            ((CustomViewholder)holder).textView_userName.setText(accountModels.get(position).userName);
            // 약속 지킴 정보들
            if(accountModels.get(position).NumofAllApp.equals("0")){
                ((CustomViewholder)holder).textView_totalAppNum.setText("0회");
                ((CustomViewholder)holder).textView_rating.setText("0%");
            }
            else {
                ((CustomViewholder)holder).textView_totalAppNum.setText(accountModels.get(position).NumofAllApp);
                float temp1 = Float.parseFloat(accountModels.get(position).NumofAllApp);
                float temp2 = Float.parseFloat(accountModels.get(position).NumofLateApp);
                String temp3 = String.format("%.2f", 100*(temp1-temp2)/temp1);
                ((CustomViewholder)holder).textView_rating.setText(temp3 + "%");
            }
        }

        @Override
        public int getItemCount() {
            return accountModels.size();
        }

        private class CustomViewholder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView_comment;
            public TextView textView_userName;
            public TextView textView_totalAppNum;
            public TextView textView_rating;

            public CustomViewholder(View view) {
                super(view);
                imageView = (ImageView) view.findViewById(R.id.accountitem_imageview);
                textView_comment = (TextView)view.findViewById(R.id.accountitem_textview_comment);
                textView_userName = (TextView)view.findViewById(R.id.accountitem_textview_userName);
                textView_totalAppNum = (TextView)view.findViewById(R.id.account_totalN_textview);
                textView_rating = (TextView)view.findViewById(R.id.account_rating_textview);
            }
        }
    }


    @Override
    public void onBackPressed() {
        finish();
    }
}

