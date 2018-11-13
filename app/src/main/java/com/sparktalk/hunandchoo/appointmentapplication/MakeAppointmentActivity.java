package com.sparktalk.hunandchoo.appointmentapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sparktalk.hunandchoo.appointmentapplication.fragment.PeopleFragment;
import com.sparktalk.hunandchoo.appointmentapplication.model.AppointmentModel;
import com.sparktalk.hunandchoo.appointmentapplication.model.ChatModel;
import com.sparktalk.hunandchoo.appointmentapplication.model.ResultModel;
import com.sparktalk.hunandchoo.appointmentapplication.model.UserModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MakeAppointmentActivity extends AppCompatActivity implements TimePicker.OnTimeChangedListener {

    EditText title;
    Button locationSelect;
    Button result;
    Button select;
    DatePicker datePicker;
    TimePicker timePicker;

    String date;
    String dateTime;

    String latiLongi;

    UserModel userModel = new UserModel();
    ChatModel chatModel = new ChatModel();
    AppointmentModel appointmentModel = new AppointmentModel();
    ResultModel resultModel = new ResultModel();
    String userNames;
    String userUids;

    Button money, food, giftycon, hit;
    String penalty = null;
    String how_money_str = "0";
    int how_money_int = 0;
    String what_food;
    Calendar c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_appointment);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.makeAppointmentAct_recyclerV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MakeAppointmentActivityRecyclerViewAdapter());

        title = (EditText)findViewById(R.id.makeAppointmentAct_editT_title);
        locationSelect = (Button)findViewById(R.id.makeAppointmentAct_selectLocationBtn);
        result = (Button)findViewById(R.id.makeAppointmentAct_resultBtn);
        select = (Button)findViewById(R.id.makeAppointmentAct_selectBtn);

        money = (Button)findViewById(R.id.makeAppointmentAct_money);
        food = (Button)findViewById(R.id.makeAppointmentAct_food);
        giftycon = (Button)findViewById(R.id.makeAppointmentAct_giftycon);
        hit = (Button)findViewById(R.id.makeAppointmentAct_hit);

        datePicker = (DatePicker) findViewById(R.id.makeAppointmentAct_datePicker);
        timePicker = (TimePicker) findViewById(R.id.makeAppointmentAct_timePicker);

        date = Integer.toString(datePicker.getYear());

        date += "-";

        if (datePicker.getMonth() + 1 < 10)
            date += "0" + Integer.toString(datePicker.getMonth() + 1);
        else date += Integer.toString(datePicker.getMonth() + 1);

        date += "-";

        if (datePicker.getDayOfMonth() < 10)
            date += "0" + Integer.toString(datePicker.getDayOfMonth());
        else date += Integer.toString(datePicker.getDayOfMonth());

        datePicker.init(datePicker.getYear(),
                datePicker.getMonth(),
                datePicker.getDayOfMonth(),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                        // TODO Auto-generated method stub

                        date = Integer.toString(year);

                        date += "-";

                        if (monthOfYear + 1 < 10)
                            date += "0" + Integer.toString(monthOfYear + 1);
                        else date += Integer.toString(monthOfYear + 1);

                        date += "-";

                        if (dayOfMonth < 10)
                            date += "0" + Integer.toString(dayOfMonth);
                        else date += Integer.toString(dayOfMonth);
                    }
                });

        c = Calendar.getInstance();

        if (c.get(Calendar.HOUR_OF_DAY) < 10)
            dateTime = "0" + Integer.toString(c.get(Calendar.HOUR_OF_DAY));
        else dateTime = Integer.toString(c.get(Calendar.HOUR_OF_DAY));

        dateTime += ":";

        if (c.get(Calendar.MINUTE) < 10)
            dateTime += "0" + Integer.toString(c.get(Calendar.MINUTE));
        else dateTime += Integer.toString(c.get(Calendar.MINUTE));

        dateTime += ":00";

        timePicker.setOnTimeChangedListener(this);

        locationSelect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LocationSelectActivity.class);
                startActivity(intent);
                //startActivityForResult(intent, 1);
            }
        });

        onResume();

        money.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                moneyDialog(view.getContext());
                penalty = "money";
                //how_money_int = Integer.parseInt(how_money_str);
            }
        });

        food.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                penalty = "food";
                //foodDialog(view.getContext());
            }
        });

        giftycon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                penalty = "gitfycon";
                //foodDialog(view.getContext());
            }
        });

        hit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                penalty = "hit";
                //foodDialog(view.getContext());
            }
        });



        select.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                SharedPreferences sp = getSharedPreferences("locationInfo", 0);
                latiLongi = sp.getString("location", "");

                if (title.getText().toString() == null || title.getText().toString().length() == 0)
                    Toast.makeText(getApplicationContext(), "약속명을 입력해 주십시오", Toast.LENGTH_LONG).show();

                else if (penalty == null)
                    Toast.makeText(getApplicationContext(), "벌칙을 입력해 주십시오", Toast.LENGTH_LONG).show();

                else
                {
                    appointmentModel.appointmentName = title.getText().toString();
                    appointmentModel.appointmentTime = date + " " + dateTime;
                    appointmentModel.location = latiLongi.split("#")[0];
                    appointmentModel.latitude = latiLongi.split("#")[1];
                    appointmentModel.longitude = latiLongi.split("#")[2];
                    appointmentModel.appointmentUsers = userNames;
                    appointmentModel.appointmentUsersUid = userUids;
                    appointmentModel.uidsForCheck = userUids;
                    appointmentModel.appointmentPenalty = penalty;
                    appointmentModel.status = "prog";
                    appointmentModel.how_money = how_money_str;
                    appointmentModel.late_checker = "0";

                    // 본인 추가 부분, 현재 약속을 만드는 본인의 NumofAllApp 수는 약속방 생성 버튼 클릭 시 올라가게 되어있으나,
                    // 참여자는 체크박스를 클릭할 때마다 올라간다. 그래서 체크박스 클릭하고 뒤로가기를 하는 것을 반복하면 NumofAllApp가 계속 증가하는 버그 가능......
                    final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    chatModel.users.put(myUid, true);
                    FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot snapshot :dataSnapshot.getChildren()){
                                UserModel userModel = snapshot.getValue(UserModel.class);
                                if(userModel.uid.equals(myUid)){
                                    int temp = Integer.parseInt(userModel.NumofAllApp);
                                    String temp2 = String.valueOf(temp + 1);
                                    FirebaseDatabase.getInstance().getReference().child("users").child(myUid).child("NumofAllApp").setValue(temp2);
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    }); //여기까지 본인 NumofAllApp 추가 하는 부분


                    DatabaseReference pushedPostRef = FirebaseDatabase.getInstance().getReference().child("appointments").push();
                    String postId = pushedPostRef.getKey();

                    appointmentModel.appointmentId = postId;

                    FirebaseDatabase.getInstance().getReference().child("appointments").child(postId).setValue(appointmentModel);
                    FirebaseDatabase.getInstance().getReference().child("appointments").child(postId).child("chat").setValue(chatModel);

                    finish();
                }
            }
        });
    }

    @Override
    public void onResume()
    {  // After a pause OR at startup

        super.onResume();

        SharedPreferences sp = getSharedPreferences("locationInfo", 0);
        latiLongi = sp.getString("location", "");

        TextView showLocation = (TextView)findViewById(R.id.makeAppointmentAct_showLocation);
        showLocation.setText(latiLongi.split("#")[0]);
    }

    void moneyDialog(Context context){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_money, null);
        view.setBackground(getDrawable(R.drawable.recycler_background));

        final EditText editText = (EditText) view.findViewById(R.id.dialog_money_text_input);
        builder.setView(view).setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                how_money_str = editText.getText().toString();

            }
        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.show();
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {

        if (hourOfDay < 10)
            dateTime = "0" + Integer.toString(hourOfDay);
        else dateTime = Integer.toString(hourOfDay);

        dateTime += ":";

        if (minute < 10)
            dateTime += "0" + Integer.toString(minute);
        else dateTime += Integer.toString(minute);

        dateTime += ":00";
    }

    // 피플 뷰 어댑터
    class MakeAppointmentActivityRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<UserModel> userModels;

        public MakeAppointmentActivityRecyclerViewAdapter(){
            userModels = new ArrayList<>();

            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 접속자 본인 uid

            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                // DB로부터 데이터를 불러오는 부분
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userModels.clear();
                    for(DataSnapshot snapshot :dataSnapshot.getChildren()){

                        UserModel userModel = snapshot.getValue(UserModel.class);

                        if(userModel.NumofAllApp == null){
                            FirebaseDatabase.getInstance().getReference().child("users").child(userModel.uid).child("NumofAllApp").setValue("0");
                        }
                        if(userModel.NumofLateApp == null){
                            FirebaseDatabase.getInstance().getReference().child("users").child(userModel.uid).child("NumofLateApp").setValue("0");
                        }

                        // userModel의 uid가 본인의 id면 리스트에 표시 안함
                        if(userModel.uid.equals(myUid)) {
                            if(userNames == null && userUids == null){
                                userNames = userModel.userName;
                                userUids = myUid;
                            }
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_select, parent, false);

            return new CustomViewHolder(view);
        }

        // 실질적인 뷰 화면 설정
        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            // 이미지 불러오기

            Glide.with
                    (holder.itemView.getContext())
                    .load(userModels.get(position).profileImageUrl)
                    .apply(new RequestOptions().circleCrop())
                    .into(((CustomViewHolder)holder).imageView);

            ((CustomViewHolder)holder).textView.setText(userModels.get(position).userName);

            ((CustomViewHolder)holder).checkBox.setOnCheckedChangeListener(null); // null 초기화 해줘야함

            // 체크박스 부분, 체크한 경우 app모델의 uid와 name에 추가함과 함께, user모델의 NumAllApp 수에도 영향을 주게 함
            // DB의 users 정보 수정 과정 : users 데이터 읽기 -> int로 변환 후 1 더하고 다시 string으로 변환 -> 다시 그 string 값을 DB에 쓰기
            ((CustomViewHolder)holder).checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    //((CustomViewHolder)holder).checkBox.isChecked()
                    if(b){
                        chatModel.users.put(userModels.get(position).uid, true);
                        userUids += (" " + userModels.get(position).uid);
                        userNames += (", " + userModels.get(position).userName);
                        System.out.println("결과: " + userUids + " " + userNames);

                        // 여기부터 NumAllApp의 수에 영향 주는 부분.
                        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for(DataSnapshot snapshot :dataSnapshot.getChildren()){
                                            UserModel userModel = snapshot.getValue(UserModel.class);
                                            if(userModel.uid.equals(userModels.get(position).uid)){
                                                int temp = Integer.parseInt(userModel.NumofAllApp);
                                                String temp2 = String.valueOf(temp + 1);
                                                FirebaseDatabase.getInstance().getReference().child("users").child(userModels.get(position).uid)
                                                        .child("NumofAllApp").setValue(temp2);
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                    }
                    else{
                        String addUids = " " + userModels.get(position).uid;
                        userUids = userUids.replace(addUids, "");
                        String addNames = ", " + userModels.get(position).userName;
                        userNames = userNames.replace(addNames, "");
                        System.out.println("빙과: " + userUids + " " + userNames);

                        // 여기부터 NumAllApp의 수에 영향 주는 부분.
                        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot snapshot :dataSnapshot.getChildren()){
                                    UserModel userModel = snapshot.getValue(UserModel.class);
                                    if(userModel.uid.equals(userModels.get(position).uid)){
                                        int temp = Integer.parseInt(userModel.NumofAllApp);
                                        String temp2 = String.valueOf(temp - 1);
                                        FirebaseDatabase.getInstance().getReference().child("users").child(userModels.get(position).uid)
                                                .child("NumofAllApp").setValue(temp2);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                }
            });

            /*
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    chatModel.users.put(userModels.get(position).uid, true);
                    //resultModel.users.put(userModels.get(position).uid, true);

                    userUids += " " + userModels.get(position).uid;
                    userNames += ", " + userModels.get(position).userName;
                }
            });
            */
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
            public CheckBox checkBox;

            public CustomViewHolder(View view){
                super(view);
                imageView = (ImageView) view.findViewById(R.id.friendItem_imgV);
                textView = (TextView) view.findViewById(R.id.friendItem_textV);
                textView_comment = (TextView)view.findViewById(R.id.friendItem_textV_comment);
                checkBox = (CheckBox)view.findViewById(R.id.frienditem_checkbox);
            }
        }
    }
}
