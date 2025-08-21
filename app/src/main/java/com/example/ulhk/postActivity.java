package com.example.ulhk;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class postActivity extends AppCompatActivity {
    private ImageButton selectLocationButton;
    private TextView displayLocation;

    private ImageButton selectTypeButton;
    private TextView displayType;

    private ImageButton selectNumberButton;
    private TextView displayNumber;

    private ImageButton selectTimeButton;
    private TextView displayTime;

    private Button sendButton;
    private EditText posttitle, postcontent;

    private DatabaseHelper databaseHelper;
    private int currentUserId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        ImageButton close = findViewById(R.id.close_button);

        close.setOnClickListener(v -> {
            Intent intent = new Intent(postActivity.this, MainActivity.class);
            startActivity(intent);
        });

        currentUserId = getCurrentUserId();
        //选择地点
        selectLocationButton = findViewById(R.id.SelectedLocation);
        displayLocation = findViewById(R.id.displaySelectedLocation);

        // 可供选择的地点列表
        final String[] locations = {"Hong Kong Coliseum", "Mong Kok Stadium", "Kowloon Bay Sports Ground","Yuen Long Sports Centre","Shatin Sports Ground"};

        // 设置按钮点击事件
        selectLocationButton.setOnClickListener(v -> showDialog("Select Location", locations, displayLocation));

        //选择类型
        selectTypeButton = findViewById(R.id.SelectedType);
        displayType = findViewById(R.id.displaySelectedType);

        // 可供选择的类型列表
        final String[] types = {"Culture", "Camping", "Entertainment","Sports","Volunteer","Exchanges","Workshops","Others"};

        // 设置按钮点击事件
        selectTypeButton.setOnClickListener(v -> showDialog("Select Type", types, displayType));

        //输入人数
        selectNumberButton = findViewById(R.id.SelectedNumber);
        displayNumber = findViewById(R.id.displaySelectedMembers);

        // 可供选择的地点列表
        selectNumberButton.setOnClickListener(v -> showInputDialog("Input Total Number", displayNumber));

        displayTime = findViewById(R.id.displaySelectedTime);
        selectTimeButton = findViewById(R.id.SelectedTime);

        selectTimeButton.setOnClickListener(v -> showDatePicker());


        //发帖按键
        sendButton = findViewById(R.id.send_button);
        posttitle=findViewById(R.id.title_text_view);
        postcontent=findViewById(R.id.thoughts_edit_text);
        sendButton.setOnClickListener(v -> submitEvent());

    }

    //用于选择地点和类型
    private void showDialog(String title, String[] items, TextView displayTextView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        // 设置列表及点击事件
        builder.setItems(items, (dialog, which) -> {
            String selectedItem = items[which];
            displayTextView.setText(selectedItem);
            Toast.makeText(postActivity.this, "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
        });

        // 显示对话框
        builder.create().show();
    }

    private int getCurrentUserId() {
        int userId;
        SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id",-1);
        return userId;
    }


    // 输入框对话框函数（用于输入活动人数）
    private void showInputDialog(String title, TextView displayTextView) {
        // 创建一个 EditText 输入框
        final EditText input = new EditText(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setView(input); // 将输入框添加到对话框中

        // 设置"确定"按钮
        builder.setPositiveButton("OK", (dialog, which) -> {
            String userInput = input.getText().toString().trim();
            if (!userInput.isEmpty()) {
                displayTextView.setText(userInput);
                Toast.makeText(postActivity.this, "Entered: " + userInput, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(postActivity.this, "Input cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // 设置"取消"按钮
        builder.setNegativeButton("Cancel", null);

        // 显示对话框
        builder.create().show();
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    // 设置选择的日期
                    calendar.set(year, monthOfYear, dayOfMonth);
                    showTimePicker(calendar); // 选择日期后弹出时间选择
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // 时间选择函数
    private void showTimePicker(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute1) -> {
                    // 设置选择的时间
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute1);

                    // 格式化日期时间显示
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String formattedDateTime = sdf.format(calendar.getTime());

                    // 更新TextView显示日期时间
                    displayTime.setText(formattedDateTime);
                    Toast.makeText(postActivity.this, "Selected: " + formattedDateTime, Toast.LENGTH_SHORT).show();
                },
                hour, minute, true);

        timePickerDialog.show();
    }

    private void submitEvent() {
        // Get the values from the UI components
        String title=posttitle.getText().toString();
        String content=postcontent.getText().toString();
        String location = displayLocation.getText().toString();
        String type = displayType.getText().toString();
        String time = displayTime.getText().toString();
        String numberStr = displayNumber.getText().toString();

        if (location.isEmpty() || type.isEmpty() || time.isEmpty() || numberStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert number to integer
        int number = Integer.parseInt(numberStr);

        String posttime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Create DBHelper instance and insert event
        databaseHelper = new DatabaseHelper(this);
        databaseHelper.insertEvent(title,currentUserId, location, type,number,time, content,posttime);
        //public void insertEvent(String eventsTitle, String eventsVenue, String eventsType, int eventsNumber,String eventsTime, String eventsDescription) {

        Toast.makeText(this, "Event Submitted Successfully!", Toast.LENGTH_SHORT).show();


        // Optionally, clear the fields after submission
        displayLocation.setText("");
        displayType.setText("");
        displayNumber.setText("");
        displayTime.setText("");

        // Send broadcast to MainActivity to refresh data
        Intent intent = new Intent("com.example.ulhk.UPDATE_EVENTS");
        sendBroadcast(intent);  // Send the broadcast to refresh data in MainActivity

        // Optionally, you can finish this activity and return to MainActivity
        finish();
    }




}
