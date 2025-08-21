package com.example.ulhk;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private Cursor userEventsCursor;

    private DatabaseHelper databaseHelper;
    private TextView usernameTextView, signatureTextView, emailTextView, labelTextView,postView,meView;
    private ImageView profileImageView;
    private ActivityResultLauncher<Intent> editUserLauncher;
    private View underlinePosts,underlineMy;
    private int currentUserId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        // 初始化视图
        usernameTextView = findViewById(R.id.username);
        signatureTextView = findViewById(R.id.signature);
        profileImageView = findViewById(R.id.profile_image);
        emailTextView = findViewById(R.id.ulhk_email);
        labelTextView = findViewById(R.id.user_label);

        currentUserId = getCurrentUserId();  // 你可以根据自己的逻辑获取当前用户的 ID
        // 获取数据库实例
        databaseHelper = new DatabaseHelper(this);

        // 获取并展示当前用户的个人信息
        loadUserProfile(currentUserId);

        // 设置底部导航栏
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_me);

        // 设置导航栏监听器
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_homepage:
                    // 跳转到主页
                    if (!(MeActivity.this.getClass().equals(MainActivity.class))) {
                        startActivity(new Intent(MeActivity.this, MainActivity.class));
                    }
                    return true;

                case R.id.navigation_friends:
                    // 跳转到联系人界面
                    if (!(MeActivity.this.getClass().equals(ContactActivity.class))) {
                        startActivity(new Intent(MeActivity.this, ContactActivity.class));
                    }
                    return true;

                case R.id.navigation_me:
                    // 当前已在个人主页，无需跳转
                    return true;

                default:
                    return false;
            }
        });

        //实现个人主页 user发布的活动的post刷新
        recyclerView = findViewById(R.id.joinRecyclerView);
        postView = findViewById(R.id.posts);
        meView=findViewById(R.id.my);

        underlinePosts = findViewById(R.id.underline_posts);
        underlineMy = findViewById(R.id.underline_my);
        databaseHelper = new DatabaseHelper(this);

        // 设置 RecyclerView 布局管理器
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        displayUserPosts();

        //点击my后出现加入的活动
        meView.setOnClickListener(v -> {
            // 假设 currentUserId 是当前登录用户的 ID

            underlineMy.setVisibility(View.VISIBLE);  // 显示my下划线
            underlinePosts.setVisibility(View.GONE);  // 隐藏posts下划线

            int currentUserId = getCurrentUserId();  // 获取当前用户 ID，可能是从 SharedPreferences 或其他地方获得
            // 查询用户已加入的活动
            userEventsCursor = databaseHelper.getUserJoinedEvents(currentUserId);

            // 更新 RecyclerView 的适配器

            updateRecyclerView(userEventsCursor);
        });

        //展示该用户发布的活动
        postView.setOnClickListener(v -> {

            underlinePosts.setVisibility(View.VISIBLE);  // 显示下划线
            underlineMy.setVisibility(View.GONE);
            displayUserPosts();
        });

        // 查询用户已加入的事件
        // 获取 ImageView 控件
        ImageView usernameIcon = findViewById(R.id.username_icon);

        // 注册 ActivityResultLauncher
        editUserLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                // 如果返回RESULT_OK，则刷新页面
                recreate(); // 或者执行其他刷新逻辑
            } else {
                Toast.makeText(MeActivity.this, "Cancel edit！", Toast.LENGTH_SHORT).show();
            }
        });


        usernameIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MeActivity.this, EditMeActivity.class);
            editUserLauncher.launch(intent); // 使用 launcher 启动
        });

    }

    // 新增函数，用于默认显示用户发布的所有帖子
    private void displayUserPosts() {
        int currentUserId = getCurrentUserId();  // 获取当前用户 ID
        // 查询用户发布的所有帖子
        Cursor userPostsCursor = databaseHelper.getUserAllPosts(currentUserId);
        // 更新 RecyclerView 的适配器
        updateRecyclerView(userPostsCursor);
    }

    private void updateRecyclerView(Cursor userEventsCursor) {
        // 创建并设置适配器
        postAdapter = new PostAdapter(this, userEventsCursor, databaseHelper,currentUserId);
        recyclerView.setAdapter(postAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("com.example.ulhk.UPDATE_EVENTS");
        registerReceiver(eventsReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(eventsReceiver);
    }

    private final BroadcastReceiver eventsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Cursor newCursor = databaseHelper.getAllEvents(); // 获取最新数据
            postAdapter.updateCursor(newCursor); // 更新数据源
        }
    };




    private int getCurrentUserId() {
        int userId;
        SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id",-1);
        return userId;
    }

    private void loadUserProfile(int userId) {
        // 查询数据库，获取当前用户的信息
        Cursor cursor = databaseHelper.getUserById(userId);

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
            profileImageView.setImageResource(R.drawable.icon_user_default);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private String getLabelText(int label) {
        switch (label) {
            case -1:return "selete your label";
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userEventsCursor != null) {
            userEventsCursor.close();
        }
    }
}
