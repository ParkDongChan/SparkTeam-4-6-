package com.sparktalk.hunandchoo.appointmentapplication;

import android.*;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
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
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
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


import net.daum.mf.map.api.CalloutBalloonAdapter;
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

public class EachAppointmentActivity extends AppCompatActivity {

    private String EVENT_DATE_TIME;
    private String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private LinearLayout linear_layout;
    private TextView tv_days, tv_hour, tv_minute, tv_second;
    private Handler handler = new Handler();
    private Runnable runnable;

    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;

    // GPSTracker class
    private GpsModel gps;
    double latitude, longitude;

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

    MapView mapView;

    List<ChatModel.Comment> comments = new ArrayList<>(); // comments를 담아주는 리스트 정의
    AppointmentModel appointment = new AppointmentModel();

    String[] ids;
    Integer i;
    MapPOIItem[] marker;

    String late_Uids;
    String uidHasFirst = "0";
    private Button button;

    Integer distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_each_appointment);

        destinationRoom = getIntent().getStringExtra("destination");
        late_Uids = getIntent().getStringExtra("late_Uids");


        // 정보들 가져오기

        FirebaseDatabase.getInstance().getReference().child("appointments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    AppointmentModel appointmentModel = snapshot.getValue(AppointmentModel.class);

                    if (appointmentModel.appointmentId.equals(destinationRoom)){
                        appointment = appointmentModel;
                        mapPresent();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*FirebaseDatabase.getInstance().getReference().child("appointments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    AppointmentModel appointmentModel = snapshot.getValue(AppointmentModel.class);

                    if (appointmentModel.appointmentId.equals(destinationRoom))
                    {
                        appointment = appointmentModel;
                        mapPresent();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        // 타이머 관련
        linear_layout = (LinearLayout) findViewById(R.id.eachAppointmentAct_linearLayout);
        tv_days = (TextView) findViewById(R.id.eachAppointmentAct_textV_days);
        tv_hour = (TextView) findViewById(R.id.eachAppointmentAct_textV_hours);
        tv_minute = (TextView) findViewById(R.id.eachAppointmentAct_textV_minutes);
        tv_second = (TextView) findViewById(R.id.eachAppointmentAct_textV_seconds);
        countDownStart();

        // 지도 관련
        gps = new GpsModel(EachAppointmentActivity.this);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("latitude").setValue(Double.toString(latitude));
            FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("longitude").setValue(Double.toString(longitude));

            Toast.makeText(
                    getApplicationContext(),
                    "당신의 위치 - \n위도: " + latitude + "\n경도: " + longitude,
                    Toast.LENGTH_LONG).show();
        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }


        callPermission();  // 권한 요청을 해야 함

        // 메시지 관련
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        editText = (EditText)findViewById(R.id.groupMessageAct_editT);
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    users.put(item.getKey(), item.getValue(UserModel.class));
                }
                init();

                recyclerView = (RecyclerView)findViewById(R.id.groupMessageAct_recyclerV);
                recyclerView.setAdapter(new GroupMessageRecyclerViewAdapter());
                recyclerView.setLayoutManager(new LinearLayoutManager(EachAppointmentActivity.this));

                //PagerSnapHelper snapHepler = new PagerSnapHelper();
                //snapHepler.attachToRecyclerView(recyclerView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        show_arrive();

        // 약속 종료 버튼 관련, 1등만 약속종료를 할 수 있도록 하는 부분
        button = (Button) findViewById(R.id.button_appointment_end);

        // first를 DB에서 읽어옴
        final ResultModel first = new ResultModel();
        FirebaseDatabase.getInstance().getReference().child("appointments")
                .child(destinationRoom).child("result").child("first").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // first 값이 현재 유저의 uid와 동일하면 약속 종료 버튼을 활성화
                if(dataSnapshot.getValue() != null){
                    first.first = dataSnapshot.getValue().toString();
                    if(uid.equals(first.first)){
                        button.setEnabled(true);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                end_show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    private void countDownStart() {
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    handler.postDelayed(this, 1000);
                    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
                    Date event_date = dateFormat.parse(appointment.appointmentTime);
                    Date current_date = new Date();
                    if (!current_date.after(event_date)) {
                        long diff = event_date.getTime() - current_date.getTime();
                        long Days = diff / (24 * 60 * 60 * 1000);
                        long Hours = diff / (60 * 60 * 1000) % 24;
                        long Minutes = diff / (60 * 1000) % 60;
                        long Seconds = diff / 1000 % 60;

                        if(diff == 1){
                            end_show_for_timer();
                            //finish();
                            //when_end();
                        }
                        //
                        tv_days.setText(String.format("%02d", Days));
                        tv_hour.setText(String.format("%02d", Hours));
                        tv_minute.setText(String.format("%02d", Minutes));
                        tv_second.setText(String.format("%02d", Seconds));

                    } else {
                        end_show_for_timer();
                        //finish();
                        //when_end();
                        linear_layout.setVisibility(View.VISIBLE);
                        handler.removeCallbacks(runnable);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 0);
    }

    private void mapPresent() {
        mapView = new MapView(this);
        mapView.setDaumMapApiKey("170124d1d8b75dc107d80943297c2f52");

        LinearLayout container = (LinearLayout) findViewById(R.id.map_view);
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(Double.parseDouble(appointment.latitude), Double.parseDouble(appointment.longitude)), 6, true);

        final MapPOIItem marker_appointment = new MapPOIItem();
        marker_appointment.setItemName("약속 장소");
        marker_appointment.setTag(0);
        marker_appointment.setMapPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(appointment.latitude), Double.parseDouble(appointment.longitude)));
        marker_appointment.setMarkerType(MapPOIItem.MarkerType.RedPin); // 기본으로 제공하는 RedPin 마커 모양.
        mapView.addPOIItem(marker_appointment);

        ids = appointment.appointmentUsersUid.split(" ");

        marker = new MapPOIItem[ids.length];

        FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            // DB로부터 데이터를 불러오는 부분
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    UserModel userModel = snapshot.getValue(UserModel.class);

                    for (i = 0; i < ids.length; i++) {
                        if (userModel.uid.equals(ids[i])) {
                            marker[i] = new MapPOIItem();

                            distance = (int)calDistance(Double.parseDouble(appointment.latitude), Double.parseDouble(appointment.longitude), Double.parseDouble(userModel.latitude), Double.parseDouble(userModel.longitude));

                            marker[i].setItemName(userModel.userName + " 위치 " + String.format("%,d", distance) + " 남음");
                            marker[i].setTag(0);
                            marker[i].setMapPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(userModel.latitude), Double.parseDouble(userModel.longitude)));
                            marker[i].setMarkerType(MapPOIItem.MarkerType.CustomImage);

                            if (distance <= 100) {
                                marker[i].setCustomImageResourceId(R.drawable.arrive);
                            }

                            else {
                                marker[i].setCustomImageResourceId(R.drawable.run);
                            }

                            marker[i].setCustomCalloutBalloon(new CustomCalloutBalloonAdapter().getCalloutBalloon(marker[i]));
                            marker[i].setCustomImageAutoscale(false); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
                            marker[i].setCustomImageAnchor(0.5f, 0.5f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
                            mapView.addPOIItem(marker[i]);

                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());

        container.addView(mapView);
    }

    public double calDistance(double lati1, double long1, double lati2, double long2){

        double theta, dist;
        theta = long1 - long2;
        dist = Math.sin(deg2rad(lati1)) * Math.sin(deg2rad(lati2)) + Math.cos(deg2rad(lati1))
                * Math.cos(deg2rad(lati2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;    // 단위 mile 에서 km 변환.
        dist = dist * 1000.0;      // 단위  km 에서 m 로 변환

        return dist;
    }

    // 주어진 도(degree) 값을 라디언으로 변환
    private double deg2rad(double deg){
        return (double)(deg * Math.PI / (double)180d);
    }

    // 주어진 라디언(radian) 값을 도(degree) 값으로 변환
    private double rad2deg(double rad){
        return (double)(rad * (double)180d / Math.PI);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessFineLocation = true;

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }

    // 전화번호 권한 요청
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }

    // 도착한 경우를 처리하는 부분. 등수관리 여기서 함
    void show_arrive() {

        FirebaseDatabase.getInstance().getReference().child("appointments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    AppointmentModel appointmentModel = snapshot.getValue(AppointmentModel.class);

                    if (appointmentModel.appointmentId.equals(destinationRoom))
                    {
                        if ((int)calDistance(Double.parseDouble(appointmentModel.latitude), Double.parseDouble(appointmentModel.longitude),
                                latitude, longitude) <= 100){

                            /* 도착한 순간에 등수를 관리하는 부분, 구조는 도착하게 되면 도착한 유저의 Uid를 DB에서 삭제하여 late에 들어가지 않도록 하는 구조.
                               1등의 경우만 first로 처리하기 위한 과정을 거침*/
                            String late_checker = appointmentModel.late_checker;
                            int temp = Integer.parseInt(late_checker) + 1;

                            // 1등인 경우
                            if(temp == 1){
                                ResultModel resultModel = new ResultModel();
                                resultModel.first = uid;
                                uidHasFirst = uid;
                                // 우선 DB의 first에 1등한 유저 uid를 넣어줌
                                FirebaseDatabase.getInstance().getReference().child("appointments").child(destinationRoom)
                                        .child("result").child("first").setValue(resultModel.first);

                                // 다음 도착한 유저의 uid를 약속자 uid 목록에서 삭제해줌
                                String userUids = appointmentModel.uidsForCheck;
                                String addUids = uid;
                                userUids = userUids.replace(addUids, "");
                                // 해당 유저의 uid 삭제한 목록으로 다시 업데이트
                                Map<String, Object> stringObjectMap = new HashMap<>();
                                stringObjectMap.put("uidsForCheck", userUids);
                                FirebaseDatabase.getInstance().getReference().child("appointments").child(destinationRoom).updateChildren(stringObjectMap);
                                // 1등 처리가 끝났으므로 증가시킨 late_checker 다시 업데이트
                                String temp2 = String.valueOf(temp);
                                FirebaseDatabase.getInstance().getReference().child("appointments").child(destinationRoom)
                                        .child("late_checker").setValue(temp2);
                            }
                            else{ // 1등 이외 경우
                                // 도착한 유저의 uid를 삭제 후 재 업데이트까지만 해줌
                                String userUids = appointmentModel.uidsForCheck;
                                String addUids = uid;
                                userUids = userUids.replace(addUids, "");
                                Map<String, Object> stringObjectMap = new HashMap<>();
                                stringObjectMap.put("uidsForCheck", userUids);
                                FirebaseDatabase.getInstance().getReference().child("appointments").child(destinationRoom).updateChildren(stringObjectMap);
                            }

                            arrive_ontime();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    // 도착 알림 다이얼로그
    void arrive_ontime(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("도착!");
        builder.setMessage("정시에 도착하셨습니다");
        builder.setPositiveButton("만세!",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }

    void when_end(){
        // 약속방의 스태이터스 - 종료시 end로
        Map<String, Object> status = new HashMap<>();
        status.put("status", "end");
        FirebaseDatabase.getInstance().getReference().child("appointments").child(destinationRoom).updateChildren(status);

        // 종료 전까지 도착 못한사람, 즉 늦은 사람들에 대한 처리
        // 1. 우선 uidsForCheck를 불러와서 늦은사람을 late로 분류해줌
        // 2. 그 후 늦은 사람에 대해서 users의 late 횟수를 업데이트 해줌
        FirebaseDatabase.getInstance().getReference().child("appointments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    AppointmentModel appointmentModel = snapshot.getValue(AppointmentModel.class);

                    if (appointmentModel.appointmentId.equals(destinationRoom))
                    {
                        // 1. late로 분류하는 부분
                        String temp3 = appointmentModel.uidsForCheck;
                        System.out.println("temp 3 : " + temp3);
                        final String[] split = temp3.split("\\s");
                        for(int k=0; k < split.length; k++) {
                            if(split[k].equals(uid)){
                                continue;
                            }
                            System.out.println(split[k]);
                            FirebaseDatabase.getInstance().getReference().child("appointments").child(destinationRoom)
                                    .child("result").child("late").push().setValue(split[k]);

                            //2. 지각자 uid의 late 수를 업데이트 하기위해 한번 읽어와서 처리하는 부분
                            final int m = k;
                                FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for(DataSnapshot snapshot :dataSnapshot.getChildren()){

                                            UserModel userModel = snapshot.getValue(UserModel.class);

                                            if(userModel.uid.equals(split[m])) {
                                                int temp = Integer.parseInt(userModel.NumofLateApp);
                                                String temp2 = String.valueOf(temp + 1);
                                                FirebaseDatabase.getInstance().getReference().child("users").child(split[m]).child("NumofLateApp").setValue(temp2);
                                                break;
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });

                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
    }

    void end_show(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("약속 종료");
        builder.setMessage("약속을 정말로 종료하시겠어요?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        when_end();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    void end_show_for_timer(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("약속 종료");
        builder.setMessage("약속시간이 다되었습니다! 약속이 종료됩니다");
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        when_end();
                    }
                });
        builder.show();
    }

    void init(){
        Button button = (Button) findViewById(R.id.groupMessageAct_Btn);
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

    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.item_custom_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            ((ImageView) mCalloutBalloon.findViewById(R.id.customBallonItem_imgV)).setImageResource(R.drawable.bono);
            ((TextView) mCalloutBalloon.findViewById(R.id.customBallonItem_textV_name)).setText(poiItem.getItemName().split(" ")[0]);
            ((TextView) mCalloutBalloon.findViewById(R.id.customBallonItem_textV_distance)).setText(poiItem.getItemName().split(" ")[2] + "m");
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }
    }

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
                messageViewHolder.textView_message.setTextSize(25);
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
                messageViewHolder.textView_message.setTextSize(25);
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
}