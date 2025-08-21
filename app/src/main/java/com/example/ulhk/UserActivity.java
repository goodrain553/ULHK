package com.example.ulhk;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private Cursor userEventsCursor;

    private DatabaseHelper databaseHelper;
    private TextView usernameTextView, signatureTextView, emailTextView, labelTextView;
    private ImageView profileImageView;
    private int currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        currentUserId = getIntent().getIntExtra("USER_ID", -1);
        databaseHelper = new DatabaseHelper(this);

        // 初始化视图
        usernameTextView = findViewById(R.id.username);
        signatureTextView = findViewById(R.id.signature);
        profileImageView = findViewById(R.id.profile_image);
        emailTextView = findViewById(R.id.ulhk_email);
        labelTextView = findViewById(R.id.user_label);



        // 获取并展示当前用户的个人信息
        loadUserProfile(currentUserId);

        ImageView btnExitToContact = findViewById(R.id.friend_Esc);

        btnExitToContact.setOnClickListener(v -> {
            // 创建 Intent 跳转到 ContactActivity
            Intent intent = new Intent(UserActivity.this, ContactActivity.class);
            startActivity(intent);

            // 结束当前 Activity
            finish();
        });

        //点击头像后，出现头像用户的主页，显示头像用户的发帖内容
        recyclerView = findViewById(R.id.friendRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userEventsCursor = databaseHelper.getUserAllPosts(currentUserId);
        updateRecyclerView(userEventsCursor);


    }

    private void updateRecyclerView(Cursor userEventsCursor) {
        // 创建并设置适配器
        postAdapter = new PostAdapter(this, userEventsCursor, databaseHelper,currentUserId);
        recyclerView.setAdapter(postAdapter);
    }

    private void loadUserProfile(int FriendId) {
        // 查询数据库，获取当前用户的信息
        Cursor cursor = databaseHelper.getUserById(currentUserId);

        if (cursor != null && cursor.moveToFirst()) {
            String username = cursor.getString(cursor.getColumnIndexOrThrow("Users_name"));
            String signature = cursor.getString(cursor.getColumnIndexOrThrow("Users_signature"));
            byte[] profileImage = cursor.getBlob(cursor.getColumnIndexOrThrow("Users_profile"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("Users_Email"));
            int userLabel = cursor.getInt(cursor.getColumnIndexOrThrow("Users_label")); // 使用 getInt 获取标签值

            // 设置用户信息
            usernameTextView.setText(username);
            signatureTextView.setText(signature);
            emailTextView.setText(email);
            labelTextView.setText(getLabelText(userLabel));
            labelTextView.setBackgroundResource(getLabelBackground(userLabel));

            // 将字节数组转换为 Bitmap 并显示头像
            if (profileImage != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(profileImage, 0, profileImage.length);
                profileImageView.setImageBitmap(bitmap);
            }
        } else {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private String getLabelText(int label) {
        switch (label) {
            case -1:return "no label";
            case 1: return "Sports";
            case 2: return "Music";
            default: return "General";
        }
    }

    private int getLabelBackground(int label) {
        switch (label) {
            case -1: return R.color.gray;
            case 1: return R.drawable.tag_background_1;
            case 2: return R.drawable.tag_background_2;
            default: return R.drawable.tag_background_3;
        }
    }
}
