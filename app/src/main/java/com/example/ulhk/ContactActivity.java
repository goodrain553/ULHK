package com.example.ulhk;

import android.Manifest;
import com.example.ulhk.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import java.util.stream.Collectors;

public class ContactActivity extends AppCompatActivity implements UserAdapter.OnAddFriendListener, FriendsAdapter.OnFriendClickListener {

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerView, onlineRecyclerView, offlineRecyclerView;
    private UserAdapter userAdapter;
    private FriendsAdapter onlineAdapter, offlineAdapter;
    private List<User> onlineFriends = new ArrayList<>();
    private List<User> offlineFriends = new ArrayList<>();
    private List<User> userList = new ArrayList<>();  // 用于存储所有用户
    private ImageView profileImageView;
    private Handler handler;
    private Runnable refreshRunnable;
    private int currentUserId;

    //初始化
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        databaseHelper = new DatabaseHelper(this);
        currentUserId = getCurrentUserId();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_friends);


        // 设置导航栏的点击事件
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_homepage:
                    // Only navigate to MainActivity if it's not already the current activity
                    if (!(ContactActivity.this.getClass().equals(MainActivity.class))) {
                        startActivity(new Intent(ContactActivity.this, MainActivity.class));
                    }
                    return true;

                case R.id.navigation_friends:
                    return true;

                case R.id.navigation_me:
                    if (!(ContactActivity.this.getClass().equals(MeActivity.class))) {
                        startActivity(new Intent(ContactActivity.this, MeActivity.class));
                    }
                    return true;

                default:
                    return false;
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }


        //搜索用户按钮
        ImageButton buttonShowDialog = findViewById(R.id.notification_icon);
        buttonShowDialog.setOnClickListener(v -> showDialog());

        onlineRecyclerView = findViewById(R.id.online_recycler_view);
        offlineRecyclerView = findViewById(R.id.offline_recycler_view);

        onlineRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        offlineRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        onlineAdapter = new FriendsAdapter(this, onlineFriends, this, currentUserId);  // 传递 currentUserId
        offlineAdapter = new FriendsAdapter(this, offlineFriends, this, currentUserId);  // 传递 currentUserId


        onlineRecyclerView.setAdapter(onlineAdapter);
        offlineRecyclerView.setAdapter(offlineAdapter);
        profileImageView = findViewById(R.id.profile_image);

        loadUserProfile(currentUserId);
        // 加载好友数据
        loadFriends(currentUserId);

        // 初始化Handler和Runnable
        handler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadFriends(currentUserId);  // 重新加载好友数据
                onlineAdapter.notifyDataSetChanged();
                offlineAdapter.notifyDataSetChanged();

                // 每隔5秒钟重新执行一次这个Runnable
                handler.postDelayed(this, 5000);  // 5000毫秒 = 5秒
            }
        };

        // 启动定时刷新
        handler.post(refreshRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止定时刷新
        handler.removeCallbacks(refreshRunnable);
    }

    private int getCurrentUserId() {
        int userId;
        SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id",-1);
        return userId;
    }

    private void showDialog() {
        //显示搜索框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View inflate = LayoutInflater.from(ContactActivity.this).inflate(R.layout.search_friends, null);
        builder.setView(inflate);

        EditText text = inflate.findViewById(R.id.text_search);
        Button btn = inflate.findViewById(R.id.btn_search);
        Button btn_can = inflate.findViewById(R.id.btn_cancle);
        recyclerView = inflate.findViewById(R.id.search_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btn.setOnClickListener(v -> {
            String searchText = text.getText().toString().trim();
            searchUsers(searchText);
        });


        AlertDialog dialog = builder.create();
        dialog.show();

        btn_can.setOnClickListener(v -> {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
           refreshMainPage(); // 刷新主页面的方法
        });


    }

    private void searchUsers(String input) {
        // 如果输入为空，则直接返回
        if (input == null || input.trim().isEmpty()) {
            Toast.makeText(this, "No input!", Toast.LENGTH_SHORT).show();
            return;  // 不进行查询
        }

        // 执行查询
        Cursor cursor = databaseHelper.searchUsers(input);

        // 如果查询结果不为空
        if (cursor != null && cursor.getCount() > 0) {
            userList = new ArrayList<>();
            while (cursor.moveToNext()) {

                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("Users_id"));

                // 如果是当前用户的 ID，则跳过
                if (userId == currentUserId) {
                    continue;
                }

                String userName = cursor.getString(cursor.getColumnIndexOrThrow("Users_name"));
                String userEmail = cursor.getString(cursor.getColumnIndexOrThrow("Users_Email"));
                int userLabel = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow("Users_label")));
                String userSignature = cursor.getString(cursor.getColumnIndexOrThrow("Users_signature"));
                byte[] userProfile = cursor.getBlob(cursor.getColumnIndexOrThrow("Users_profile"));
                // 获取在线状态
                int userStatusInt = cursor.getInt(cursor.getColumnIndexOrThrow("Users_status"));
                boolean userStatus = (userStatusInt == 1);  // 如果值为1，则在线，否则离线
                User user = new User(userId, userName, userEmail, userSignature, userProfile, userStatus, userLabel);
                userList.add(user);  // 将用户添加到列表中
            }
            cursor.close();

            // 设置 RecyclerView 的适配器
            if (userAdapter == null) {
                userAdapter = new UserAdapter(this, userList, (friendUserId, holder) -> addFriend(friendUserId, holder),currentUserId);
                recyclerView.setAdapter(userAdapter);
            } else {
                userAdapter.updateUserList(userList);
            }
        } else {
            Toast.makeText(this, "No matching users found", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserProfile(int currentUserId) {
        // 查询数据库，获取当前用户的信息
        Cursor cursor = databaseHelper.getUserById(currentUserId);

        if (cursor != null && cursor.moveToFirst()) {
            byte[] profileImage = cursor.getBlob(cursor.getColumnIndexOrThrow("Users_profile"));

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

    private void addFriend(int friendUserId, UserAdapter.UserViewHolder holder) {
        // 获取当前登录用户的 ID（假设已存储在本地或其他方式获取）
        // 将当前用户添加为该好友的好友
        if (databaseHelper.areUsersFriends(currentUserId, friendUserId)) {
            updateAddFriendButtonAsFriend(holder);  // 更新按钮为“已是好友”
        } else {
            // 如果不是好友，执行添加好友操作
            databaseHelper.addFriend(currentUserId, friendUserId);
            Toast.makeText(this, "Friend added successfully!", Toast.LENGTH_SHORT).show();
            updateAddFriendButtonAsFriend(holder);  // 更新按钮为“已是好友”
        }

    }

    // onAddFriend 回调方法
    public void onAddFriend(int friendUserId, UserAdapter.UserViewHolder holder) {
        addFriend(friendUserId, holder); // 调用添加好友的方法
    }

    // 更新按钮为“已是好友”状态
    private void updateAddFriendButtonAsFriend(UserAdapter.UserViewHolder holder) {
        holder.addFriendButton.setText("Already your friend");
        holder.addFriendButton.setEnabled(false);  // 禁用按钮，防止重复点击
        holder.addFriendButton.setBackgroundColor(holder.itemView.getResources().getColor(R.color.gray));  // 获取颜色
        holder.addFriendButton.setTextColor(holder.itemView.getResources().getColor(R.color.white));  // 设置文字颜色为白色设置文字颜色为白色
    }


    private void loadFriends(int currentUserId) {
        List<User> allFriends = databaseHelper.getFriendsByStatus(currentUserId);

        // 清空现有列表
        onlineFriends.clear();
        offlineFriends.clear();

        // 分类在线和离线好友
        for (User user : allFriends) {
            if (user.getStatus()) {
                onlineFriends.add(user);
            } else {
                offlineFriends.add(user);
            }
        }
        // 更新RecyclerView
        onlineAdapter.notifyDataSetChanged();
        offlineAdapter.notifyDataSetChanged();
    }


    public void onFriendClick(int userId) {
        // 跳转到用户个人主页，并传递用户 ID
        Intent intent = new Intent(ContactActivity.this, UserActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    private void refreshMainPage() {
        // 创建一个新的 Intent 来启动当前 Activity
        recreate();
        // 启动新活动，达到刷新效果
    }

}


