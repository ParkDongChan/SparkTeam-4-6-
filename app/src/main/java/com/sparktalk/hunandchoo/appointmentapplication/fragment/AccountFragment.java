package com.sparktalk.hunandchoo.appointmentapplication.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sparktalk.hunandchoo.appointmentapplication.EachAppointmentActivity;
import com.sparktalk.hunandchoo.appointmentapplication.EndAppointmentActivity;
import com.sparktalk.hunandchoo.appointmentapplication.LoginActivity;
import com.sparktalk.hunandchoo.appointmentapplication.MakeAppointmentActivity;
import com.sparktalk.hunandchoo.appointmentapplication.R;
import com.sparktalk.hunandchoo.appointmentapplication.model.AppointmentModel;
import com.sparktalk.hunandchoo.appointmentapplication.model.UserModel;

import java.util.ArrayList;
import java.util.List;

public class AccountFragment extends Fragment{

    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.accountfrag_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new AccountRecyclerViewAdapter());

        Button button = (Button)view.findViewById(R.id.account_button_logout);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                getActivity().finish();
                startActivity(new Intent(getActivity(), LoginActivity.class));

            }
        });

        return view;
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

                        // 본인의 uid만 받아오기
                        if(!userModel.uid.equals(myUid)) {
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

        @SuppressLint("SetTextI18n")
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
        public int getItemCount() { return accountModels.size(); }

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


}
