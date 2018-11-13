package com.sparktalk.hunandchoo.appointmentapplication;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.sparktalk.hunandchoo.appointmentapplication.fragment.AccountFragment;
import com.sparktalk.hunandchoo.appointmentapplication.fragment.AppointmentFragment;
import com.sparktalk.hunandchoo.appointmentapplication.fragment.PeopleFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.mainAct_bottomNavigationV);

        // 시작하면 친구목록 프래그먼트부터 시작
        getFragmentManager().beginTransaction().replace(R.id.mainAct_frameLayout, new PeopleFragment()).commit();

        // 바텀 네비게이션 터치 시 프래그먼트로 이동하는 부분
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_people: // 친구목록 버튼 누른 경우
                        getFragmentManager().beginTransaction().replace(R.id.mainAct_frameLayout, new PeopleFragment()).commit();
                        return true;

                    case R.id.action_chat:  // 약속방 버튼 누른 경우
                        getFragmentManager().beginTransaction().replace(R.id.mainAct_frameLayout, new AppointmentFragment()).commit();
                        return true;

                    case R.id.action_account: // 나의계정 버튼 누른 경우
                        getFragmentManager().beginTransaction().replace(R.id.mainAct_frameLayout, new AccountFragment()).commit();
                        return true;
                }

                return false;
            }
        });

        //passPushTokenToServer();
    }

    // 이부분 푸쉬알람 관련 기능으로 일단 무시
    // 토큰 생성
    /*void passPushTokenToServer(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();
        // 파이어베이스에선 해쉬맵으로밖에 토큰을 못넘겨줌
        Map<String, Object> map = new HashMap<>();
        map.put("pushToken", token);

        // 실제 받는 명령
        FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map);
    }*/

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
