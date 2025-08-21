package com.example.ulhk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ulhk.R;

import java.io.ObjectInputStream;
import java.text.BreakIterator;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private Context context;
    private Cursor cursor;
    private DatabaseHelper databaseHelper;
    int userId;  //需要绑定登录ID


    public PostAdapter(Context context, Cursor cursor, DatabaseHelper databaseHelper, int userId) {
        this.context = context;
        this.cursor = cursor;
        this.databaseHelper = databaseHelper;
        this.userId= userId;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 用于加载 item 布局
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @SuppressLint("Range")
    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            // 从 Cursor 中提取数据并填充到视图中
            @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex("Events_title"));
            @SuppressLint("Range") String venue = cursor.getString(cursor.getColumnIndex("Events_venue"));
            @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex("Events_type"));
            @SuppressLint("Range") int number = cursor.getInt(cursor.getColumnIndex("Events_number"));
            @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("Events_time"));
            @SuppressLint("Range") int eventId = cursor.getInt(cursor.getColumnIndex("Events_id"));
            @SuppressLint("Range") String postTime = cursor.getString(cursor.getColumnIndex("Events_posttime"));

            @SuppressLint("Range") int organizerId = cursor.getInt(cursor.getColumnIndex("Users_id"));
            Cursor userCursor = databaseHelper.getUserInfoById(organizerId); // 通过用户ID查询用户信息
            String organizerName = "";
            byte[] avatarBlob = null; // 存储头像的 BLOB 数据
            if (userCursor != null && userCursor.moveToFirst()) {
                organizerName = userCursor.getString(userCursor.getColumnIndex("Users_name"));
                avatarBlob = userCursor.getBlob(userCursor.getColumnIndex("Users_profile")); // 获取头像 BLOB
                userCursor.close();
            }
//            @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex("Events_description"));

            // 设置 TextView 的内容
            holder.titleTextView.setText(title);
            holder.venueTextView.setText("Location: "+venue);
            holder.typeTextView.setText(type);
            holder.numberTextView.setText(String.valueOf("Total Members: "+number));
            holder.timeTextView.setText("Date&Time: "+time);
            holder.posttimeTextView.setText(postTime);
//            holder.descriptionTextView.setText(description);

            holder.organizerNameTextView.setText(organizerName);
            // 设置头像（如果存在头像数据）
            if (avatarBlob != null) {
                // 将 BLOB 数据转换为 Bitmap
                Bitmap avatarBitmap = BitmapFactory.decodeByteArray(avatarBlob, 0, avatarBlob.length);
                holder.organizerAvatarImageView.setImageBitmap(avatarBitmap); // 设置到 ImageView
            } else {
                // 如果没有头像，使用默认头像
                holder.organizerAvatarImageView.setImageResource(R.drawable.icon_user_default);
            }


            // 获取当前用户的参与状态
            Cursor statusCursor = databaseHelper.getUserEventStatus(userId, eventId);
            boolean isJoined = false;
            if (statusCursor != null && statusCursor.moveToFirst()) {
                @SuppressLint("Range") int status = statusCursor.getInt(statusCursor.getColumnIndex("status"));
                isJoined = (status == 1);
                statusCursor.close();
            }

            updateJoinButton(holder.joinButton, isJoined);

            holder.joinButton.setOnClickListener(v -> {
                // 每次点击时都获取最新的用户状态
                Cursor updatedStatusCursor = databaseHelper.getUserEventStatus(userId, eventId);
                final boolean updatedIsJoined; // 定义为 final
                if (updatedStatusCursor != null && updatedStatusCursor.moveToFirst()) {
                    @SuppressLint("Range") int updatedStatus = updatedStatusCursor.getInt(updatedStatusCursor.getColumnIndex("status"));
                    updatedIsJoined = (updatedStatus == 1);
                    updatedStatusCursor.close();
                } else {
                    updatedIsJoined = false; // 确保有初始值
                }

                // 弹出确认对话框
                String dialogTitle = updatedIsJoined ? "Confirm Exit" : "Confirm Join";
                String dialogMessage = updatedIsJoined ? "Are you sure you want to exit this event?" : "Are you sure you want to join this event?";
                new AlertDialog.Builder(context)
                        .setTitle(dialogTitle)
                        .setMessage(dialogMessage)
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // 用户确认操作后，执行加入或退出逻辑
                            if (updatedIsJoined) {
                                // 当前已加入，点击退出
                                databaseHelper.insertOrUpdateUserEventStatus(eventId, userId, 0);
                                Toast.makeText(context, "You have exited the event.", Toast.LENGTH_SHORT).show();
                            } else {
                                // 当前未加入，点击加入
                                databaseHelper.insertOrUpdateUserEventStatus(eventId, userId, 1);
                                Toast.makeText(context, "You have joined the event.", Toast.LENGTH_SHORT).show();
                            }

                            // 刷新按钮状态
                            updateJoinButton(holder.joinButton, !updatedIsJoined);
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            // 用户取消操作后，什么都不做
                            dialog.dismiss();
                        })
                        .show();
            });



//            holder.joinButton.setOnClickListener(v -> {
//                // 每次点击时都获取最新的用户状态
//                Cursor updatedStatusCursor =databaseHelper.getUserEventStatus(userId, eventId);
//                boolean updatedIsJoined = false;
//                if (updatedStatusCursor != null && updatedStatusCursor.moveToFirst()) {
//                    @SuppressLint("Range") int updatedStatus = updatedStatusCursor.getInt(updatedStatusCursor.getColumnIndex("status"));
//                    updatedIsJoined = (updatedStatus == 1);
//                    updatedStatusCursor.close();
//                }
//
//                // 判断当前状态是加入还是退出
//                if (updatedIsJoined) {
//                    // 当前已加入，点击退出
//                    databaseHelper.insertOrUpdateUserEventStatus(eventId, userId, 0);
//                    Toast.makeText(context, "You have exited the event.", Toast.LENGTH_SHORT).show();
//                } else {
//                    // 当前未加入，点击加入
//                    databaseHelper.insertOrUpdateUserEventStatus(eventId, userId, 1);
//                    Toast.makeText(context, "You have joined the event.", Toast.LENGTH_SHORT).show();
//                }
//
//                // 刷新按钮状态
//                updateJoinButton(holder.joinButton, !updatedIsJoined);
//            });





        }
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    private void updateJoinButton(Button joinButton, boolean isJoined) {
        if (isJoined) {
            joinButton.setText("Exit");
            joinButton.setBackgroundColor(ContextCompat.getColor(context, R.color.gray));
        } else {
            joinButton.setText("Join");
            joinButton.setBackgroundColor(ContextCompat.getColor(context, R.color.red2));
        }

    }




    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView organizerAvatarImageView;
        TextView titleTextView, venueTextView, typeTextView, numberTextView, timeTextView, organizerNameTextView,descriptionTextView,posttimeTextView;
        Button joinButton;

        public PostViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.event_title);
            venueTextView = itemView.findViewById(R.id.event_venue);
            typeTextView = itemView.findViewById(R.id.event_type);
            numberTextView = itemView.findViewById(R.id.event_number);
            timeTextView = itemView.findViewById(R.id.event_time);
            posttimeTextView= itemView.findViewById(R.id.time_posted);

            organizerNameTextView = itemView.findViewById(R.id.organizer_name); // 发布者名字的 TextView
            organizerAvatarImageView = itemView.findViewById(R.id.profile_icon); // 发布者头像的 ImageView
            joinButton = itemView.findViewById(R.id.join_button);
//            descriptionTextView = itemView.findViewById(R.id.event_description);
        }
    }


    // 更新数据
    public void updateCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        notifyDataSetChanged();
    }

    // 搜索功能
    public void search(String query) {
        Cursor searchCursor = databaseHelper.searchEvents(query);
        updateCursor(searchCursor);
    }

    //分类识别帖子
    public void filterByCategory(String category) {
        // 查询数据库获取分类数据
        Cursor filteredCursor = databaseHelper.getEventsByCategory(category);
        if (filteredCursor != null) {
            cursor = filteredCursor;
            notifyDataSetChanged();
        }
    }

    public void resetFilter() {
        // 重置 Cursor 为原始数据
        cursor = databaseHelper.getAllEvents();
        notifyDataSetChanged();
    }



}
