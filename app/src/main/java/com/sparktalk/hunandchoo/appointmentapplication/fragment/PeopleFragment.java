package com.sparktalk.hunandchoo.appointmentapplication.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sparktalk.hunandchoo.appointmentapplication.AccountActivity;
import com.sparktalk.hunandchoo.appointmentapplication.R;
import com.sparktalk.hunandchoo.appointmentapplication.model.UserModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hun on 2018-08-14.
 */

public class PeopleFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people, container, false);

        // 친구목록 리사이클러뷰
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.peopleFrg_recyclerV);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new PeopleFragmentRecyclerViewAdapter());

        // 내 프로필 리사이클러뷰
        RecyclerView recyclerView2 = (RecyclerView) view.findViewById(R.id.peopleFrg_myProfile_recyclerV);
        recyclerView2.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView2.setAdapter(new MyProfileRecyclerViewAdapter());

        // 플로팅 버튼, 아직 미구현
        FloatingActionButton floatingActionButton = (FloatingActionButton)view.findViewById(R.id.peopleFrg_floatingBtn);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 친구목록 갱신
                // /startActivity(new Intent(view.getContext(), SelectFriendActivity.class));
            }
        });

        return view;
    }




    // 친구 리사이클러뷰 어댑터 시작
    class PeopleFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<UserModel> userModels;

        public PeopleFragmentRecyclerViewAdapter(){
            userModels = new ArrayList<>();

            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 접속자 본인 uid

            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                // DB로부터 데이터를 불러오는 부분
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userModels.clear();
                    for(DataSnapshot snapshot :dataSnapshot.getChildren()){

                        UserModel userModel = snapshot.getValue(UserModel.class);

                        // userModel의 uid가 본인의 id면 리스트에 표시 안함
                        if(userModel.uid.equals(myUid)) {
                            continue;
                        }

                        userModels.add(userModel);
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);

            return new CustomViewHolder(view);
        }

        // 실질적인 뷰 화면 설정
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            // 이미지 불러오기
            Glide.with
                    (holder.itemView.getContext())
                    .load(userModels.get(position).profileImageUrl)
                    .apply(new RequestOptions().circleCrop())
                    .into(((CustomViewHolder)holder).imageView);

            ((CustomViewHolder)holder).textView.setText(userModels.get(position).userName);

            // 친구 선택시 프로필 액티비티로 넘어감
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), AccountActivity.class);
                    intent.putExtra("destinationUid", userModels.get(position).uid);
                    startActivity(intent);

                    /*ActivityOptions activityOptions = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                        startActivity(intent, activityOptions.toBundle());
                    }*/
                }
            });

            // 상태메시지 넣어주는 코드
            if(userModels.get(position).comment != null){
                ((CustomViewHolder) holder).textView_comment.setText(userModels.get(position).comment);
            }
        }


        // 바인딩 하는부분
        @Override
        public int getItemCount() {
            return userModels.size();
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView;
            public TextView textView_comment;

            public CustomViewHolder(View view){
                super(view);
                imageView = (ImageView) view.findViewById(R.id.friendItem_imgV);
                textView = (TextView) view.findViewById(R.id.friendItem_textV);
                textView_comment = (TextView)view.findViewById(R.id.friendItem_textV_comment);
            }
        }
    }




    // 본인 프로필 리사이클러뷰 어댑터 시작
    class MyProfileRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<UserModel> userModels;

        public MyProfileRecyclerViewAdapter(){
            userModels = new ArrayList<>();

            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 접속자 본인 uid

            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                // DB로부터 데이터를 불러오는 부분
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userModels.clear();
                    for(DataSnapshot snapshot :dataSnapshot.getChildren()){

                        UserModel userModel = snapshot.getValue(UserModel.class);

                        // userModel의 uid가 본인의 id면 리스트에 표시 안함
                        if(!userModel.uid.equals(myUid)) {
                            continue;
                        }

                        // 구글로그인의 경우 저장되지 않는 정보를 저장
                        if(userModel.NumofAllApp == null){
                            FirebaseDatabase.getInstance().getReference().child("users").child(userModel.uid).child("NumofAllApp").setValue("0");
                        }
                        if(userModel.NumofLateApp == null){
                            FirebaseDatabase.getInstance().getReference().child("users").child(userModel.uid).child("NumofLateApp").setValue("0");
                        }
                        if(userModel.latitude == null){
                            FirebaseDatabase.getInstance().getReference().child("users").child(userModel.uid).child("NumofAllApp").setValue("36.0");
                        }
                        if(userModel.longitude == null){
                            FirebaseDatabase.getInstance().getReference().child("users").child(userModel.uid).child("NumofLateApp").setValue("126.0");
                        }

                        userModels.add(userModel);
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);

            return new CustomViewHolder(view);
        }

        // 실질적인 뷰 화면 설정
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            // 이미지 불러오기
            Glide.with
                    (holder.itemView.getContext())
                    .load(userModels.get(position).profileImageUrl)
                    .apply(new RequestOptions().circleCrop())
                    .into(((CustomViewHolder)holder).imageView);

            ((CustomViewHolder)holder).textView.setText(userModels.get(position).userName);

            // 선택시 프로필 액티비티로 넘어감
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), AccountActivity.class);
                    intent.putExtra("destinationUid", userModels.get(position).uid);
                    startActivity(intent);

                    /*ActivityOptions activityOptions = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                        startActivity(intent, activityOptions.toBundle());
                    }*/
                }
            });

            // 상태메시지 넣어주는 코드
            if(userModels.get(position).comment != null){
                ((CustomViewHolder) holder).textView_comment.setText(userModels.get(position).comment);
            }
        }


        // 바인딩 하는부분
        @Override
        public int getItemCount() {
            return userModels.size();
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView;
            public TextView textView_comment;

            public CustomViewHolder(View view){
                super(view);
                imageView = (ImageView) view.findViewById(R.id.friendItem_imgV);
                textView = (TextView) view.findViewById(R.id.friendItem_textV);
                textView_comment = (TextView)view.findViewById(R.id.friendItem_textV_comment);
            }
        }
    }


}
