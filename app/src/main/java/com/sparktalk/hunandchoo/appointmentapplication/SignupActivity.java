package com.sparktalk.hunandchoo.appointmentapplication;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.sparktalk.hunandchoo.appointmentapplication.model.UserModel;

import java.io.File;


public class SignupActivity extends AppCompatActivity {

    private static final int PICK_FROM_ALBUM = 10;
    private EditText email;
    private EditText phone;
    private EditText name;
    private EditText password;
    private Button signup;
    private ImageView profile;
    private Uri imageUri;
    private FirebaseRemoteConfig firebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //테마 색 설정
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        /*background_basic = mFirebaseRemoteConfig.getString(getString(R.string.rc_color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(background_basic))  ;
        }*/

        profile = (ImageView)findViewById(R.id.signupAct_imgV_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });

        email = (EditText)findViewById(R.id.signupAct_editT_email);
        phone = (EditText)findViewById(R.id.signupAct_editT_phone);
        name = (EditText)findViewById(R.id.signupAct_editT_name);
        password = (EditText)findViewById(R.id.signupAct_editT_password);
        signup = (Button)findViewById(R.id.signupAct_btn_signup);
        //signup.setBackgroundColor(Color.parseColor(background_basic));

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (email.getText().toString() == null || name.getText().toString() == null || password.getText().toString() == null || (password.getText().toString().length() < 6) || phone.getText().toString() == null){
                    Toast.makeText(SignupActivity.this, "대표 사진, 이메일, 전화번호, 이름, 비밀번호를 제대로 입력해주십시오.", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseAuth auth = FirebaseAuth.getInstance();

                auth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                final String uid = task.getResult().getUser().getUid();

                                //이름 담는 부분
                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name.getText().toString()).build();
                                task.getResult().getUser().updateProfile(userProfileChangeRequest);

                                if (imageUri == null)
                                    imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                                            "://" + getResources().getResourcePackageName(R.drawable.person_image_profile)
                                            + '/' + getResources().getResourceTypeName(R.drawable.person_image_profile) + '/' + getResources().getResourceEntryName(R.drawable.person_image_profile) );

                                FirebaseStorage.getInstance().getReference().child("userImages").child(uid).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        //@SuppressWarnings("VisibleForTests")
                                        String imageUrl = task.getResult().getDownloadUrl().toString();

                                        UserModel userModel = new UserModel();
                                        userModel.userName = name.getText().toString();
                                        userModel.phoneNum = phone.getText().toString();
                                        userModel.profileImageUrl = imageUrl;
                                        userModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        userModel.latitude = "0.0";
                                        userModel.longitude = "0.0";
                                        userModel.NumofAllApp = "0";
                                        userModel.NumofLateApp = "0";

                                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent intent = getIntent();
                                                setResult(RESULT_OK, intent);
                                                Toast.makeText(SignupActivity.this, "성공적으로 가입되었습니다.", Toast.LENGTH_SHORT).show();
                                                SignupActivity.this.finish();
                                            }
                                        });
                                    }
                                });
                            }
                        });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK){
            profile.setImageURI(data.getData()); // 가운데 뷰를 바꿈
            imageUri = data.getData(); // 이미지 경로 원본
        }
    }
}
