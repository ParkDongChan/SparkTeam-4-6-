package com.sparktalk.hunandchoo.appointmentapplication;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.sparktalk.hunandchoo.appointmentapplication.model.GpsModel;
import com.sparktalk.hunandchoo.appointmentapplication.model.UserModel;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private EditText id;
    private EditText password;

    private Button login;
    private Button signup;
    private SignInButton button;
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleApiClient mGoogleApiClient;
    private Uri imageUri;

    private static final int RC_SIGN_IN = 10;

    // GPSTracker class
    private GpsModel gps;
    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        /*String background_basic = FirebaseRemoteConfig.getString(getString(R.string.rc_color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(background_basic))  ;
        }*/

        id = (EditText)findViewById(R.id.loginAct_editT_id);
        password = (EditText)findViewById(R.id.loginAct_editT_password);

        login = (Button)findViewById(R.id.loginAct_btn_login);
        signup = (Button)findViewById(R.id.loginAct_btn_signup);
        button = (SignInButton)findViewById(R.id.login_btn_google);

        /*login.setBackgroundColor(Color.parseColor(background_basic));
        signup.setBackgroundColor(Color.parseColor(background_basic));*/


        SharedPreferences sp1 = getSharedPreferences("loginID", 0);
        if (sp1.getString("loginId", "").length() != 0)
            id.setText(sp1.getString("loginId", ""));

        SharedPreferences sp2 = getSharedPreferences("loginPASSWORD", 0);
        if (sp2.getString("loginPassword", "").length() != 0)
            password.setText(sp2.getString("loginPassword", ""));

        //구글로그인관련
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (id.getText().toString().length() == 0 || password.getText().toString().length() == 0)
                    Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 제대로 입력해주십시오.", Toast.LENGTH_SHORT).show();
                else loginEvent();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, 1);
                //startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        // 로그인 성공 시 다음 액티비티로 넘어가는 부분
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                System.out.print("\n리스너부분\n");
                if(user != null){

                    String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        // 구글로그인 경우 때문에 로그인마다 uid, userName 갱신
                        Map<String, Object> uidMap = new HashMap<>();
                        uidMap.put("uid", uid);
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(uidMap);
                        Map<String, Object> nameMap = new HashMap<>();
                        nameMap.put("userName", userName);
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(nameMap);

                    // 지도 관련
                    gps = new GpsModel(LoginActivity.this);
                    // GPS 사용유무 가져오기
                    if (gps.isGetLocation()) {

                        latitude = gps.getLatitude();
                        longitude = gps.getLongitude();
                        // 로그인마다 경위도 갱신
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("latitude").setValue(Double.toString(latitude));
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("longitude").setValue(Double.toString(longitude));

                    } else {
                        // GPS 를 사용할수 없으므로
                    }

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{

                }
            }
        };
    }

    // 이메일 로그인 버튼 누를 시 인증 부분
    void loginEvent(){
        firebaseAuth.signInWithEmailAndPassword(id.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // 실패시
                        if(!task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 제대로 입력해주십시오.", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            SharedPreferences sp1 = getSharedPreferences("loginID", 0);
                            SharedPreferences.Editor editor1 = sp1.edit();
                            editor1.putString("loginId", id.getText().toString());
                            editor1.commit();

                            SharedPreferences sp2 = getSharedPreferences("loginPASSWORD", 0);
                            SharedPreferences.Editor editor2 = sp2.edit();
                            editor2.putString("loginPassword", password.getText().toString());
                            editor2.commit();
                        }
                    }
                });
    }


    // 실질적으로 리스너를 호출하는 부분
    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("Main Activity", "onActivityResult : " + resultCode);
        if (resultCode == RESULT_OK) { // 결과가 OK
            Log.d("Main Activity", "정상적으로 가입 완료");
            this.recreate();
        }

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
            else{

            }
        }
    }

    // 구글 로그인시 토큰 받는 부분
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {

                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
