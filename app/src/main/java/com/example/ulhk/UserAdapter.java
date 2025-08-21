package com.example.ulhk;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;
    private OnAddFriendListener onAddFriendListener;
    private DatabaseHelper databaseHelper;
    private int currentUserId;

    // Interface for callback when adding a friend
    public interface OnAddFriendListener {
        void onAddFriend(int friendUserId, UserViewHolder holder);  // 添加 UserViewHolder 参数
    }


    public UserAdapter(Context context, List<User> userList, OnAddFriendListener listener,int currentUserId) {
        this.context = context;
        this.userList = userList;
        this.onAddFriendListener = listener;
        this.databaseHelper = new DatabaseHelper(context);
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        int friendUserId = user.getId();

        holder.userName.setText(user.getName());
        holder.userEmail.setText(user.getEmail());


        if (user.getProfile() != null && user.getProfile().length > 0) {
            // 将字节数组转换为 Bitmap
            Bitmap profileImageBitmap = BitmapFactory.decodeByteArray(user.getProfile(), 0, user.getProfile().length);

            if (profileImageBitmap != null) {
                // 将 Bitmap 转换为 Drawable
                Drawable profileImageDrawable = new BitmapDrawable(context.getResources(), profileImageBitmap);

                // 设置头像
                holder.userProfileImage.setImageDrawable(profileImageDrawable);
            } else {
                // 如果头像为空或无法转换为 Bitmap，则使用错误
                holder.userProfileImage.setImageResource(R.drawable.icon_user_default);
            }
        } else {
            // 如果没有设置头像（字节数组为空），则显示错误
            holder.userProfileImage.setImageResource(R.drawable.icon_user_default);
        }

        holder.userSignature.setText(user.getSignature());
        // 设置标签（假设 usersLabel 表示用户标签）
        holder.userLabel.setText(getLabelText(user.getLabel())); // 根据 label 设置文本
        holder.userLabel.setBackgroundResource(getLabelBackground(user.getLabel())); // 根据 label 设置背景

        // 检查是否已经是好友
        if (databaseHelper.areUsersFriends(currentUserId, friendUserId)) { // 假设当前用户 ID 是 1
            // 如果已经是好友
            holder.addFriendButton.setText("Already your friend");
            holder.addFriendButton.setBackgroundColor(context.getResources().getColor(R.color.gray));
            holder.addFriendButton.setTextColor(context.getResources().getColor(R.color.white));  // 设置文字颜色为白色
            holder.addFriendButton.setEnabled(false); // 禁用按钮
        } else {
            // 如果不是好友，设置为添加好友按钮
            holder.addFriendButton.setText("Add Friend");
            holder.addFriendButton.setBackgroundColor(context.getResources().getColor(R.color.red));
            holder.addFriendButton.setEnabled(true); // 启用按钮
        }

        holder.addFriendButton.setOnClickListener(v -> {
            if (onAddFriendListener != null) {
                // 触发回调，传递 user id 和 holder
                onAddFriendListener.onAddFriend(friendUserId, holder);
            }
        });
}


    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateUserList(List<User> newUserList) {
        userList.clear();
        userList.addAll(newUserList);
        notifyDataSetChanged();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        ImageView userProfileImage;
        TextView userName, userEmail, userSignature, userLabel;
        Button addFriendButton;

        public UserViewHolder(View itemView) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.user_profile_image);
            userName = itemView.findViewById(R.id.user_name);
            userEmail = itemView.findViewById(R.id.user_email);
            userSignature = itemView.findViewById(R.id.user_signature);
            userLabel = itemView.findViewById(R.id.user_label);
            addFriendButton = itemView.findViewById(R.id.add_friend_button);
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





