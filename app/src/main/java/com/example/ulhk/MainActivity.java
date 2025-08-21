package com.example.ulhk;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private DatabaseHelper databaseHelper;
    private Cursor cursor;
    private int currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 初始化 DBHelper 和获取数据
        //databaseHelper = new DatabaseHelper(this);
        databaseHelper = DatabaseHelper.getInstance(MainActivity.this);


        Bundle bundle = getIntent().getExtras();
        Cursor cursor_userinfo = null;
        String user_name = "default";
        int userId = -1;
        if(bundle!=null){
            user_name = bundle.getString("username");
            cursor_userinfo = databaseHelper.getUserInfoByUserName(user_name);

            if (cursor_userinfo != null && cursor_userinfo.moveToFirst()) {
                // 获取user_id列的索引
                int userIdIndex = cursor_userinfo.getColumnIndex("Users_id");
                // 检查列索引是否有效
                if (userIdIndex != -1) {
                    // 获取user_id的值
                    userId = cursor_userinfo.getInt(userIdIndex);
                    SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("user_id", userId);
                    editor.putString("user_name",user_name);
                    editor.apply();
                }
                // 关闭cursor
                cursor_userinfo.close();
            }
        }else{
            SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
            userId = sharedPreferences.getInt("user_id",-1);
            user_name = sharedPreferences.getString("user_name","default");
        }



        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_homepage);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_homepage:
                    return true;

                case R.id.navigation_friends:
                    if (!(MainActivity.this.getClass().equals(ContactActivity.class))) {
                        startActivity(new Intent(MainActivity.this, ContactActivity.class));
                    }
                    return true;

                case R.id.navigation_me:
                    if (!(MainActivity.this.getClass().equals(MeActivity.class))) {
                        startActivity(new Intent(MainActivity.this, MeActivity.class));
                    }
                    return true;

                default:
                    return false;
            }
        });

        recyclerView = findViewById(R.id.postsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化 DBHelper 和获取数据
        cursor = databaseHelper.getAllEvents();

     //  databaseHelper.insertTestUsers();  //测试用户插入

     //  databaseHelper.insertTestEvents();  //测试帖子插入

        // 设置适配器
        postAdapter = new PostAdapter(this, cursor,databaseHelper,userId);
        // bing the userid and the adapter
        //postAdapter.setUserId(userId);
        recyclerView.setAdapter(postAdapter);

        registerReceiver(eventsReceiver, new IntentFilter("com.example.ulhk.UPDATE_EVENTS"));

        ImageButton btnNavigate = findViewById(R.id.publish_button);

        // 设置按钮点击事件
        btnNavigate.setOnClickListener(v -> {
            // 创建跳转的 Intent
            Intent intent = new Intent(MainActivity.this, postActivity.class);
            // 启动 SecondActivity
            startActivity(intent);
        });

        currentUserId = getCurrentUserId();

        EditText searchEditText = findViewById(R.id.search_bar);
        ImageButton searchButton = findViewById(R.id.search_button);

        ImageView cultureView=findViewById(R.id.icon1ImageView);
        ImageView campingView=findViewById(R.id.icon2ImageView);
        ImageView entertainView=findViewById(R.id.icon3ImageView);
        ImageView sportsView=findViewById(R.id.icon4ImageView);
        ImageView volView=findViewById(R.id.icon5ImageView);
        ImageView exView=findViewById(R.id.icon6ImageView);
        ImageView workView=findViewById(R.id.icon7ImageView);
        ImageView allView=findViewById(R.id.icon8ImageView);

        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString();
            // Hide the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            }
            postAdapter.search(query);
        });



        cultureView.setOnClickListener(v -> {
            postAdapter.filterByCategory("Culture");
        });

        campingView.setOnClickListener(v -> {
            postAdapter.filterByCategory("Camping");
        });

        entertainView.setOnClickListener(v -> {
            postAdapter.filterByCategory("Entertaining");
        });


        sportsView.setOnClickListener(v -> {
            postAdapter.filterByCategory("Sports");
        });

        volView.setOnClickListener(v -> {
            postAdapter.filterByCategory("Volunteer");
        });

        exView.setOnClickListener(v -> {
            postAdapter.filterByCategory("Exchanges");
        });

        workView.setOnClickListener(v -> {
            postAdapter.filterByCategory("Workshops");
        });


        allView.setOnClickListener(v -> {
            postAdapter.resetFilter();
        });

    }



    private int getCurrentUserId() {
        int userId;
        SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id",-1);
        return userId;
    }


    private BroadcastReceiver eventsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取新的数据并更新适配器
            Cursor newCursor = databaseHelper.getAllEvents();
            postAdapter.updateCursor(newCursor);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在 Activity 销毁时关闭 Cursor
        if (cursor != null) {
            cursor.close();
        }
        // clear preference when the application ends
        SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次返回 MainActivity 时重新加载数据
        if (postAdapter != null) {
            Cursor newCursor = databaseHelper.getAllEvents();
            postAdapter.updateCursor(newCursor);
        }
    }

}
