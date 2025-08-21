package com.example.ulhk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class EditMeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1; // 请求码

    private EditText editName, editEmail, editSignature;
    private Spinner editLabel;
    private ImageView addImageButton;
    private Button sendButton;
    private ImageButton closeButton;
    private DatabaseHelper databaseHelper; // 数据库对象
    private int currentUserId;
    private int selectedLabelValue;
    private int profileImageResId; // 存储头像资源 ID

    Uri selectedImageUri;// 存储头像资源 uri
    byte[] imageBytes ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // 初始化控件
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editSignature = findViewById(R.id.editSignature);
        editLabel = findViewById(R.id.spinnerTags);
        addImageButton = findViewById(R.id.add_image_button);
        sendButton = findViewById(R.id.send_button);
        closeButton = findViewById(R.id.close_button);

        currentUserId = getCurrentUserId();  // 获取当前用户 ID

        profileImageResId = R.drawable.icon_user_default; // 默认头像

        // 打开数据库
        databaseHelper = new DatabaseHelper(this);

//        databaseHelper.getUserProfileById();
        initLabelSpinner();

        // 加载当前用户信息
        loadUserProfile();

        // 点击关闭按钮关闭页面
        closeButton.setOnClickListener(v -> {
            finish();
        });


        // 点击保存按钮更新信息
        sendButton.setOnClickListener(v -> updateUserProfile());

        // 点击头像按钮更换头像
        addImageButton.setOnClickListener(v -> {
            openGallery();
        });
    }

    /**
     * 打开相册选择图片
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                // 将选择的图片显示在 addImageButton 上
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                addImageButton.setImageBitmap(bitmap);
                // 将选择的图片转换为二进制数据并存储在数据库中
                imageBytes = getBytesFromBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }


    /**
     * 从 Uri 获取图片资源 ID
     * @param uri 图片 Uri
     * @return 图片资源 ID
     */
    private int getImageResIdFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media._ID};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            cursor.close();
            return id;
        }
        return -1;
    }

    /**
     * 初始化标签 Spinner
     */
    private void initLabelSpinner() {
        String[] labels = {"Sports", "Music", "General"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                labels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editLabel.setAdapter(adapter);

        editLabel.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedLabelValue = position + 1; // 直接将索引值 +1 返回
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedLabelValue = 0;
            }
        });
    }

    private int getCurrentUserId() {
        int userId;
        SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);
        return userId;
    }

    /**
     * 加载当前用户信息
     */
    private void loadUserProfile() {
        Cursor cursor = databaseHelper.getUserById(currentUserId);
        if (cursor != null && cursor.moveToFirst()) {

            String username = cursor.getString(cursor.getColumnIndexOrThrow("Users_name"));
            String signature = cursor.getString(cursor.getColumnIndexOrThrow("Users_signature"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("Users_Email"));
            int label = cursor.getInt(cursor.getColumnIndexOrThrow("Users_label"));

            // 设置用户信息
            editName.setText(username);
            editEmail.setText(email);
            editSignature.setText(signature);
            switch (label) {
                case 1:
                    editLabel.setSelection(0); // "Sports"
                    break;
                case 2:
                    editLabel.setSelection(1); // "Music"
                    break;
                case 3:
                    editLabel.setSelection(2); // "General"
                    break;
                default:
                    editLabel.setSelection(0); // 默认选择 "Sports"
                    break;
            }
        }
        cursor.close();
    }

    /**
     * 更新用户信息
     */
    private void updateUserProfile() {
        String name = editName.getText().toString();
        String email = editEmail.getText().toString();
        String signature = editSignature.getText().toString();

        // 更新数据库
        boolean isUpdated = databaseHelper.editUser(currentUserId, name, email, selectedLabelValue, signature, imageBytes);

        if (isUpdated) {
            setResult(RESULT_OK);
            finish(); // 返回上一页
        } else {
            Toast.makeText(this, "No Edit", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close(); // 关闭数据库
        }
    }
}
