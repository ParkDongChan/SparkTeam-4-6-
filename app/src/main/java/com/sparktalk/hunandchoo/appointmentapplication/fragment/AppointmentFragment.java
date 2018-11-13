package com.sparktalk.hunandchoo.appointmentapplication.fragment;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
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
import com.bumptech.glide.load.engine.bitmap_recycle.IntegerArrayAdapter;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sparktalk.hunandchoo.appointmentapplication.EachAppointmentActivity;
import com.sparktalk.hunandchoo.appointmentapplication.EndAppointmentActivity;
import com.sparktalk.hunandchoo.appointmentapplication.MakeAppointmentActivity;
import com.sparktalk.hunandchoo.appointmentapplication.R;
import com.sparktalk.hunandchoo.appointmentapplication.model.AppointmentModel;
import com.sparktalk.hunandchoo.appointmentapplication.model.ChatModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Hun on 2018-08-15.
 */

public class AppointmentFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_appointment, container, false);

        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.appointmentFrg_recyclerV);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new AppointmentRecyclerViewAdapter());

        RecyclerView recyclerView2 = (RecyclerView)view.findViewById(R.id.appointmentFrg_recyclerV_ForEnd);
        recyclerView2.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView2.setAdapter(new AppointmentRecyclerViewForEndAdapter());

        // 플로팅 버튼 구현
        FloatingActionButton floatingActionButton = (FloatingActionButton)view.findViewById(R.id.appointmentFrg_floatingBtn);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), MakeAppointmentActivity.class));
            }
        });

        return view;
    }

    // 약속방 목록을 DB로부터 받아오는 어댑터 설정 시작
    class AppointmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        //private List<ChatModel> chatModels = new ArrayList<>();
        private List<String> keys =  new ArrayList<>();
        private String uid;
        private List<AppointmentModel> appointmentModels = new ArrayList<>();

        // DB로부터 데이터를 받아오는 부분
        public AppointmentRecyclerViewAdapter() {

            FirebaseDatabase.getInstance().getReference().child("appointments").addValueEventListener(new ValueEventListener() {
                @Override
                // DB로부터 데이터를 불러오는 부분
                public void onDataChange(DataSnapshot dataSnapshot) {
                    appointmentModels.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        AppointmentModel appointmentModel = snapshot.getValue(AppointmentModel.class);

                        if (appointmentModel.appointmentUsersUid.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            if(appointmentModel.status.equals("end")) {
                                continue;
                            }
                        appointmentModels.add(appointmentModel);}
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            // 유저의 정보 받는부분
            /*uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    chatModels.clear();
                    for(DataSnapshot item : dataSnapshot.getChildren()) {
                        chatModels.add(item.getValue(ChatModel.class));
                        keys.add(item.getKey());
                    }
                    notifyDataSetChanged(); // 새로고침
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });*/
        }

        // DB로부터 받아온 데이터를 리사이클러뷰에 보여주도록 설정하는 부분
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);

            return new CustomViewHolder(view);
        }

        // 실질적인 뷰 화면 설정
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

            String time = appointmentModels.get(position).appointmentTime.substring(2, appointmentModels.get(position).appointmentTime.length() - 3);

            ((CustomViewHolder)holder).textView_title.setText(appointmentModels.get(position).appointmentName);
            ((CustomViewHolder)holder).textView_people.setText(appointmentModels.get(position).appointmentUsers);
            ((CustomViewHolder)holder).textView_time.setText(time);
            ((CustomViewHolder)holder).textView_location.setText(appointmentModels.get(position).location);
            ((CustomViewHolder)holder).textView_Dday.setText("D-" + cal_dday(appointmentModels.get(position).appointmentTime));

            if (cal_dday(appointmentModels.get(position).appointmentTime).equals("0"))
                ((CustomViewHolder)holder).textView_Dday.setText("D-day");


            // 약속 선택시 각각의 약속방으로 넘어가는 부분
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String status = appointmentModels.get(position).status.toString();
                    Intent intent = null;
                    //System.out.println(status);

                    if (status.equals("prog")) {
                        intent = new Intent(getActivity(), EachAppointmentActivity.class);
                        intent.putExtra("destination", appointmentModels.get(position).appointmentId);
                        intent.putExtra("late_Uids", appointmentModels.get(position).appointmentUsersUid);
                        startActivity(intent);
                    } else if (status.equals("end")) {
                        intent = new Intent(getActivity(), EndAppointmentActivity.class);
                        intent.putExtra("destination", appointmentModels.get(position).appointmentId);
                        intent.putExtra("penalty", appointmentModels.get(position).appointmentPenalty);
                        intent.putExtra("how_money", appointmentModels.get(position).how_money);
                        startActivity(intent);

                        }

                    /*
                    Intent intent = new Intent(getActivity(), EachAppointmentActivity.class);
                    intent.putExtra("destination", appointmentModels.get(position).appointmentId);
                    startActivity(intent);
                    */

                    /*ActivityOptions activityOptions = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                        startActivity(intent, activityOptions.toBundle());
                    }*/
                }
            });
        }

        public String cal_dday(String appointment_time) {

            String now_date = appointment_time.substring(0, 10);
            //Log.e("TAG", appointment_time.substring(0, 10));
            Calendar c = Calendar.getInstance();

            String date = Integer.toString(c.get(Calendar.YEAR));

            date += "-";

            if (c.get(Calendar.MONTH) + 1 < 10)
                date += "0" + Integer.toString(c.get(Calendar.MONTH) + 1);
            else date += Integer.toString(c.get(Calendar.MONTH) + 1);

            date += "-";

            if (c.get(Calendar.DATE) < 10)
                date += "0" + Integer.toString(c.get(Calendar.DATE));
            else date += Integer.toString(c.get(Calendar.DATE));

            System.out.println(date);
            System.out.println(now_date);

            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date beginDate = formatter.parse(date);
                Date endDate = formatter.parse(now_date);

                // 시간차이를 시간,분,초를 곱한 값으로 나누면 하루 단위가 나옴
                long diff = endDate.getTime() - beginDate.getTime();
                long diffDays = diff / (24 * 60 * 60 * 1000);

                System.out.println("날짜차이=" + diffDays);

                return Long.toString(diffDays);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return "0";
        }

        @Override
        public int getItemCount() {
            return appointmentModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            //public ImageView imageView;
            public TextView textView_title;
            public TextView textView_people;
            public TextView textView_time;
            public TextView textView_location;
            public TextView textView_Dday;

            public CustomViewHolder(View view) {
                super(view);

                //imageView = (ImageView)view.findViewById(R.id.appointmentItem_imgV);
                textView_title = (TextView)view.findViewById(R.id.appointmentItem_textV_title);
                textView_people = (TextView)view.findViewById(R.id.appointmentItem_textV_people);
                textView_time = (TextView)view.findViewById(R.id.appointmentItem_textV_time);
                textView_location = (TextView)view.findViewById(R.id.appointmentItem_textV_location);
                textView_Dday = (TextView)view.findViewById(R.id.appointmentItem_textV_dday);
            }
        }
    }

    class AppointmentRecyclerViewForEndAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        //private List<ChatModel> chatModels = new ArrayList<>();
        private List<String> keys =  new ArrayList<>();
        private String uid;
        private List<AppointmentModel> appointmentModels = new ArrayList<>();

        // DB로부터 데이터를 받아오는 부분
        public AppointmentRecyclerViewForEndAdapter() {

            FirebaseDatabase.getInstance().getReference().child("appointments").addValueEventListener(new ValueEventListener() {
                @Override
                // DB로부터 데이터를 불러오는 부분
                public void onDataChange(DataSnapshot dataSnapshot) {
                    appointmentModels.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        AppointmentModel appointmentModel = snapshot.getValue(AppointmentModel.class);

                        if (appointmentModel.appointmentUsersUid.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            if(!appointmentModel.status.equals("end")) {
                                continue;
                            }
                            appointmentModels.add(appointmentModel);
                        }
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        // DB로부터 받아온 데이터를 리사이클러뷰에 보여주도록 설정하는 부분
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);

            return new CustomViewHolder(view);
        }

        // 실질적인 뷰 화면 설정
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

            String time = appointmentModels.get(position).appointmentTime.substring(2, appointmentModels.get(position).appointmentTime.length() - 3);

            ((CustomViewHolder)holder).textView_title.setText(appointmentModels.get(position).appointmentName);
            ((CustomViewHolder)holder).textView_people.setText(appointmentModels.get(position).appointmentUsers);
            ((CustomViewHolder)holder).textView_time.setText(time);
            ((CustomViewHolder)holder).textView_location.setText(appointmentModels.get(position).location);
            ((CustomViewHolder)holder).textView_Dday.setText(" ");

            if (cal_dday(appointmentModels.get(position).appointmentTime).equals("0"))
                ((CustomViewHolder)holder).textView_Dday.setText("D-day");

            // 약속 선택시 각각의 약속방으로 넘어가는 부분
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String status = appointmentModels.get(position).status.toString();
                    Intent intent = null;
                    //System.out.println(status);

                    if (status.equals("prog")) {
                        intent = new Intent(getActivity(), EachAppointmentActivity.class);
                        intent.putExtra("destination", appointmentModels.get(position).appointmentId);
                        intent.putExtra("late_Uids", appointmentModels.get(position).appointmentUsersUid);
                        startActivity(intent);
                    } else if (status.equals("end")) {
                        intent = new Intent(getActivity(), EndAppointmentActivity.class);
                        intent.putExtra("destination", appointmentModels.get(position).appointmentId);
                        intent.putExtra("penalty", appointmentModels.get(position).appointmentPenalty);
                        intent.putExtra("how_money", appointmentModels.get(position).how_money);
                        startActivity(intent);

                    }

                    /*
                    Intent intent = new Intent(getActivity(), EachAppointmentActivity.class);
                    intent.putExtra("destination", appointmentModels.get(position).appointmentId);
                    startActivity(intent);
                    */

                    /*ActivityOptions activityOptions = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                        startActivity(intent, activityOptions.toBundle());
                    }*/
                }
            });
        }

        public String cal_dday(String appointment_time) {

            String now_date = appointment_time.substring(0, 10);
            //Log.e("TAG", appointment_time.substring(0, 10));
            Calendar c = Calendar.getInstance();

            String date = Integer.toString(c.get(Calendar.YEAR));

            date += "-";

            if (c.get(Calendar.MONTH) + 1 < 10)
                date += "0" + Integer.toString(c.get(Calendar.MONTH) + 1);
            else date += Integer.toString(c.get(Calendar.MONTH) + 1);

            date += "-";

            if (c.get(Calendar.DATE) < 10)
                date += "0" + Integer.toString(c.get(Calendar.DATE));
            else date += Integer.toString(c.get(Calendar.DATE));

            System.out.println(date);
            System.out.println(now_date);

            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date beginDate = formatter.parse(date);
                Date endDate = formatter.parse(now_date);

                // 시간차이를 시간,분,초를 곱한 값으로 나누면 하루 단위가 나옴
                long diff = endDate.getTime() - beginDate.getTime();
                long diffDays = diff / (24 * 60 * 60 * 1000);

                System.out.println("날짜차이=" + diffDays);

                return Long.toString(diffDays);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return "0";
        }

        @Override
        public int getItemCount() {
            return appointmentModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            //public ImageView imageView;
            public TextView textView_title;
            public TextView textView_people;
            public TextView textView_time;
            public TextView textView_location;
            public TextView textView_Dday;

            public CustomViewHolder(View view) {
                super(view);

                //imageView = (ImageView)view.findViewById(R.id.appointmentItem_imgV);
                textView_title = (TextView)view.findViewById(R.id.appointmentItem_textV_title);
                textView_people = (TextView)view.findViewById(R.id.appointmentItem_textV_people);
                textView_time = (TextView)view.findViewById(R.id.appointmentItem_textV_time);
                textView_location = (TextView)view.findViewById(R.id.appointmentItem_textV_location);
                textView_Dday = (TextView)view.findViewById(R.id.appointmentItem_textV_dday);
            }
        }
    }
}
