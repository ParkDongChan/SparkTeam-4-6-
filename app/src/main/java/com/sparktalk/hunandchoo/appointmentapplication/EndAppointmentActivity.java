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

public class EndAppointmentActivity extends AppCompatActivity {

    // 메시지 관련
    Map<String, UserModel> users = new HashMap<>();
    String destinationRoom;
    String uid;
    EditText editText;
    int peopleCount = 0;

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm"); // 시간 포맷

    private RecyclerView recyclerView;
    private RecyclerView recyclerView1;
    private RecyclerView recyclerView2;

    List<ChatModel.Comment> comments = new ArrayList<>(); // comment를 담아주는 리스트 정의
    AppointmentModel appointment = new AppointmentModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_appointment);

        destinationRoom = getIntent().getStringExtra("destination");
        String penalty = getIntent().getStringExtra("penalty");
        String how_money = getIntent().getStringExtra("how_money");

        // 정보들 가져오기
        FirebaseDatabase.getInstance().getReference().child("appointments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    AppointmentModel appointmentModel = snapshot.getValue(AppointmentModel.class);

                    if (appointmentModel.appointmentId.equals(destinationRoom)) {
                        appointment = appointmentModel;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // 결과창 관련
        recyclerView1 = (RecyclerView)findViewById(R.id.end_recyclerV1);
        recyclerView1.setAdapter(new ResultRecyclerViewAdapter1());
        recyclerView1.setLayoutManager(new LinearLayoutManager(EndAppointmentActivity.this));

        recyclerView2 = (RecyclerView)findViewById(R.id.end_recyclerV2);
        recyclerView2.setAdapter(new ResultRecyclerViewAdapter2());
        recyclerView2.setLayoutManager(new LinearLayoutManager(EndAppointmentActivity.this));

        TextView re_text1 = (TextView)findViewById(R.id.result_text1);
        TextView re_text2 = (TextView)findViewById(R.id.result_text2);
        TextView re_text3 = (TextView)findViewById(R.id.result_text3);
        TextView re_text4 = (TextView)findViewById(R.id.result_text4);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 1000);

        if(penalty == null){
            re_text2.setText("벌칙을 정하지 않았습니다!");
        }
        else if(penalty.equals("money")){
            re_text2.setText("현금!");
            re_text3.setText(how_money);
            re_text3.setVisibility(View.VISIBLE);
            re_text4.setVisibility(View.VISIBLE);
        }
        else if(penalty.equals("food")){
            re_text2.setText("밥사주기!");
        }
        else if(penalty.equals("giftycon")){
            re_text2.setText("기프티콘!");
        }
        else if(penalty.equals("hit")){
            re_text2.setText("딱밤맞기!");
        }


        // 메시지 관련
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        editText = (EditText)findViewById(R.id.end_groupMessageAct_editT);
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    users.put(item.getKey(), item.getValue(UserModel.class));
                }
                init();

                recyclerView = (RecyclerView)findViewById(R.id.end_groupMessageAct_recyclerV);
                recyclerView.setAdapter(new GroupMessageRecyclerViewAdapter());
                recyclerView.setLayoutManager(new LinearLayoutManager(EndAppointmentActivity.this));

                //PagerSnapHelper snapHepler = new PagerSnapHelper();
                //snapHepler.attachToRecyclerView(recyclerView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    void init(){
        Button button = (Button) findViewById(R.id.end_groupMessageAct_Btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatModel.Comment comment = new ChatModel.Comment();
                comment.uid = uid;
                comment.message = editText.getText().toString();
                comment.timestamp = ServerValue.TIMESTAMP;
                FirebaseDatabase.getInstance().getReference().child("appointments").child(destinationRoom).child("chat").child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        FirebaseDatabase.getInstance().getReference().child("appointments").child(destinationRoom).child("chat").child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Map<String, Boolean> map = (Map<String, Boolean>) dataSnapshot.getValue();

                                for(String item : map.keySet()){
                                    if(item.equals(uid)){
                                        continue;
                                    }
                                    //sendGcm(users.get(item).pushToken);
                                }
                                editText.setText("");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        });
    }

    /*
    // 푸시알람 관련 함수
    void sendGcm(String pushToken){
        Gson gson = new Gson();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = pushToken;
        // notificaiton은 백그라운드 푸시용
        notificationModel.notification.title = userName;
        notificationModel.notification.text = editText.getText().toString();
        // data는 포그라운드 푸시용
        //notificationModel.data.title = userName;
        //notificationModel.data.text = editText.getText().toString();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"),gson.toJson(notificationModel));

        Request request = new Request.Builder().header("Content-Type", "application/json")
                .addHeader("Authorization", "key=AIzaSyAGENprlasiAEsnuxr6iNNSL0A63YdMhm0")
                .url("https://gcm-http.googleapis.com/gcm/send")
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }
*/

    class GroupMessageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public GroupMessageRecyclerViewAdapter(){
            getMessageList();

        }

        // 메시지, 즉 comments를 DB로부터 받아오는 함수
        void getMessageList(){
            databaseReference = FirebaseDatabase.getInstance().getReference().child("appointments").child(destinationRoom).child("chat").child("comments");
            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    comments.clear();

                    Map<String, Object> readUsersMap = new HashMap<>();

                    // 리사이클러 뷰에 DB안에 있는 comments들을 전부 불러와서 넣어주는 방식
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        String key = item.getKey();
                        ChatModel.Comment comment_origin = item.getValue(ChatModel.Comment.class);
                        ChatModel.Comment comment_modify = item.getValue(ChatModel.Comment.class);

                        comment_modify.readUsers.put(uid, true);

                        readUsersMap.put(key, comment_modify);
                        comments.add(comment_origin);
                    }

                    if(comments.size() == 0){
                        return;
                    }

                    if (!comments.get(comments.size() - 1).readUsers.containsKey(uid)) {

                        FirebaseDatabase.getInstance().getReference().child("appointments").child(destinationRoom).child("chat").child("comments")
                                .updateChildren(readUsersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                notifyDataSetChanged(); // 서버의 데이터 갱신 기능
                                recyclerView.scrollToPosition(comments.size() - 1); // 뷰에 띄울 때 마지막 메시지가 맨 밑으로 오도록 하는 부분
                            }
                        });
                    }else{
                        notifyDataSetChanged(); // 서버의 데이터 갱신 기능
                        recyclerView.scrollToPosition(comments.size() - 1); // 뷰에 띄울 때 마지막 메시지가 맨 밑으로 오도록 하는 부분
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);

            return new GroupMessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            GroupMessageViewHolder messageViewHolder = ((GroupMessageViewHolder)holder);

            if(comments.get(position).uid.equals(uid)){ // 본인인 경우
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.rightbubble);
                messageViewHolder.textView_message.setTextSize(20);
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE);
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT);
                //setReadCounter(position, messageViewHolder.textView_readCounter_left);
            }else // 타인의 것인 경우
            {   // 상대방은 프로필 이미지부터 불러와야하므로, Glide 사용
                Glide.with(holder.itemView.getContext())
                        .load(users.get(comments.get(position).uid).profileImageUrl)
                        .apply(new RequestOptions().circleCrop())
                        .into(messageViewHolder.imageView_profile);
                messageViewHolder.textView_name.setText(users.get(comments.get(position).uid).userName);
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.leftbubble);
                messageViewHolder.textView_message.setTextSize(20);
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT);
                //setReadCounter(position, messageViewHolder.textView_readCounter_right);

            }
            // 시간
            long unixTime = (long) comments.get(position).timestamp; // DB로부터 timestamp 값을 저장받는 변수
            Date date = new Date(unixTime); // date 형식에 재저장
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date); // time 변수에 unix포맷에서 일반적 포맷으로 변환된 시간 문자열 저장
            messageViewHolder.textView_timestamp.setText(time); // 출력


        }

        // DB에 읽은 수를 물어보는 함수
        void setReadCounter(final int position, final TextView textView){
            // 서버에 부담을 줄이기 위해 특정 조건에만 물어보도록 함
            if(peopleCount == 0){ // 읽은 사람이 하나도 없는 경우
                // users 안의 사람 수를 받아오는 부분
                FirebaseDatabase.getInstance().getReference().child("appointments").child(destinationRoom).child("chat").child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Boolean> users = (Map<String, Boolean>) dataSnapshot.getValue(); // 우선 users 라는 맵에 uid와 true값을 받아왔음
                        peopleCount = users.size();

                        int count = peopleCount - comments.get(position).readUsers.size(); // count 변수에 읽지 않은 유저 수 받아옴
                        if(count > 0 ) {
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(String.valueOf(count));
                        }else{
                            textView.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }else {
                int count = peopleCount - comments.get(position).readUsers.size(); // count 변수에 읽지 않은 유저 수 받아옴
                if(count > 0 ) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(String.valueOf(count));
                }else{
                    textView.setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class GroupMessageViewHolder extends RecyclerView.ViewHolder {

            public TextView textView_message;
            public TextView textView_name;
            public ImageView imageView_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            public TextView textView_timestamp;
            public TextView textView_readCounter_left;
            public TextView textView_readCounter_right;


            public GroupMessageViewHolder(View view) {
                super(view);
                textView_message = (TextView)view.findViewById(R.id.messageItem_textV_message);
                textView_name = (TextView)view.findViewById(R.id.messageItem_textV_name);
                imageView_profile = (ImageView)view.findViewById(R.id.messageItem_imageV_profile);
                linearLayout_destination = (LinearLayout)view.findViewById(R.id.messageItem_linearLayout_destination);
                linearLayout_main = (LinearLayout)view.findViewById(R.id.messageItem_linearLayout_main);
                textView_timestamp = (TextView)view.findViewById(R.id.messageItem_textV_timeStamp);
                textView_readCounter_left = (TextView)view.findViewById(R.id.messageItem_textV_readCounter_L);
                textView_readCounter_right = (TextView)view.findViewById(R.id.messageItem_textV_readCounter_R);

            }
        }
    }

    // 메시지 액티비티에서 뒤로가기를 누른 경우에 대한 기능 설정 시작
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //if(valueEventListener != null){
        //    databaseReference.removeEventListener(valueEventListener);
        //}
        finish();
        //overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }


    class ResultRecyclerViewAdapter1 extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<UserModel> resultModels1 = new ArrayList<>();
        private ResultModel first = new ResultModel();

        public ResultRecyclerViewAdapter1(){


            FirebaseDatabase.getInstance().getReference().child("appointments")
                    .child(destinationRoom).child("result").child("first").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    first.first = dataSnapshot.getValue().toString();
                    //System.out.println(first.first);

                    notifyDataSetChanged();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                // DB로부터 데이터를 불러오는 부분
                public void onDataChange(DataSnapshot dataSnapshot) {
                    resultModels1.clear();
                    for(DataSnapshot snapshot :dataSnapshot.getChildren()){

                        UserModel userModel = snapshot.getValue(UserModel.class);

                        if(!userModel.uid.equals(first.first)) {
                            //System.out.println(userModel.uid);
                            continue;
                        }
                        resultModels1.add(userModel);
                    }
                    //System.out.println(resultModels1);
                    notifyDataSetChanged();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);

            return new CustomViewholder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            Glide.with(holder.itemView.getContext())
                    .load(resultModels1.get(position).profileImageUrl)
                    .apply(new RequestOptions().circleCrop())
                    .into(((CustomViewholder)holder).imageView_end1);

            ((CustomViewholder)holder).textView_userName_end1.setText(resultModels1.get(position).userName);
        }

        @Override
        public int getItemCount() {
            return resultModels1.size();
        }

        private class CustomViewholder extends RecyclerView.ViewHolder {
            public ImageView imageView_end1;
            public TextView textView_userName_end1;

            public CustomViewholder(View view) {
                super(view);
                imageView_end1 = (ImageView) view.findViewById(R.id.resultItem_imgV);
                textView_userName_end1 = (TextView)view.findViewById(R.id.resultItem_textV);
            }
        }
    }

    class ResultRecyclerViewAdapter2 extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<UserModel> resultModels2 = new ArrayList<>();
        private ResultModel late = new ResultModel();

        public ResultRecyclerViewAdapter2() {

            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                // DB로부터 데이터를 불러오는 부분
                public void onDataChange(DataSnapshot dataSnapshot) {
                    resultModels2.clear();
                    System.out.println("&&&&&&&&&&&&&&");
                    System.out.println(late.late.size());
                    for(DataSnapshot snapshot :dataSnapshot.getChildren()){

                        final UserModel userModel = snapshot.getValue(UserModel.class);

                        FirebaseDatabase.getInstance().getReference().child("appointments").child(destinationRoom).child("result").child("late")
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    // DB로부터 데이터를 불러오는 부분
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        for(DataSnapshot snapshot :dataSnapshot.getChildren()){
                                            //late.late.add(snapshot.getValue().toString());
                                            //System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                                            //System.out.println(snapshot.getValue().toString());

                                            if(userModel.uid.equals(snapshot.getValue().toString())) {
                                                //System.out.println(userModel.uid);
                                                resultModels2.add(userModel);
                                            }
                                        }
                                        //System.out.println(resultModels1);
                                        notifyDataSetChanged();
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                    }
                    //System.out.println(resultModels1);
                    notifyDataSetChanged();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        }
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);

            return new CustomViewholder2(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            Glide.with(holder.itemView.getContext())
                    .load(resultModels2.get(position).profileImageUrl)
                    .apply(new RequestOptions().circleCrop())
                    .into(((CustomViewholder2)holder).imageView_end2);

            ((CustomViewholder2)holder).textView_userName_end2.setText(resultModels2.get(position).userName);
        }

        @Override
        public int getItemCount() {
            return resultModels2.size();
        }

        private class CustomViewholder2 extends RecyclerView.ViewHolder {
            public ImageView imageView_end2;
            public TextView textView_userName_end2;

            public CustomViewholder2(View view) {
                super(view);
                imageView_end2 = (ImageView) view.findViewById(R.id.resultItem_imgV);
                textView_userName_end2 = (TextView)view.findViewById(R.id.resultItem_textV);
            }
        }
    }

}


