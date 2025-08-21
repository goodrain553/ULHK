package com.example.ulhk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;
    private OnFriendClickListener onFriendClickListener;
    private int currentUserId;

    // 定义点击事件接口
    public interface OnFriendClickListener {
        void onFriendClick(int userId);
    }

    public FriendsAdapter(Context context, List<User> userList, OnFriendClickListener listener,int currentUserId) {
        this.context = context;
        this.userList = userList;
        this.onFriendClickListener = listener;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends, parent, false);
        return new UserViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        // 设置用户信息
        holder.userName.setText(user.getName());
        holder.userEmail.setText(user.getEmail());

        // 设置用户头像
        if (user.getProfile() != null && user.getProfile().length > 0) {
            Bitmap profileImageBitmap = BitmapFactory.decodeByteArray(user.getProfile(), 0, user.getProfile().length);
            if (profileImageBitmap != null) {
                Drawable profileImageDrawable = new BitmapDrawable(context.getResources(), profileImageBitmap);
                holder.userProfileImage.setImageDrawable(profileImageDrawable);
            } else {
                holder.userProfileImage.setImageResource(R.drawable.icon_user_default);
            }
        } else {
            holder.userProfileImage.setImageResource(R.drawable.icon_user_default);
        }

        // 设置用户签名
        holder.userSignature.setText(user.getSignature());
        if (user.getStatus()) {
            // Online: Set name color to black
            holder.userName.setTextColor(Color.BLACK);
        } else {
            // Offline: Set name color to gray
            holder.userName.setTextColor(Color.GRAY);
        }

        // 设置标签
        holder.userLabel.setText(getLabelText(user.getLabel()));
        holder.userLabel.setBackgroundResource(getLabelBackground(user.getLabel()));

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onFriendClickListener != null) {
                onFriendClickListener.onFriendClick(user.getId());
            }
        });

        // 设置删除按钮的点击事件
        holder.moreDel.setOnClickListener(v -> {
            showPopupMenu(v, currentUserId,user.getId(), position);  // 弹出 PopupMenu
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        ImageView userProfileImage, moreDel;
        TextView userName, userEmail, userSignature, userLabel;

        public UserViewHolder(View itemView) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.user_profile_image);
            userName = itemView.findViewById(R.id.user_name);
            userEmail = itemView.findViewById(R.id.user_email);
            userSignature = itemView.findViewById(R.id.user_signature);
            userLabel = itemView.findViewById(R.id.user_label);
            moreDel = itemView.findViewById(R.id.more_del);  // 删除按钮
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

    // 弹出 PopupMenu，显示删除选项
    private void showPopupMenu(View view, int currentUserId,int userId, int position) {
        PopupMenu popupMenu = new PopupMenu(context, view);  // 创建 PopupMenu
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_delete_friend, popupMenu.getMenu());  // 加载菜单项

        // 设置删除选项的点击事件
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_delete_friend) {
                deleteFriend(currentUserId,userId,position);  // 执行删除操作
                return true;
            }
            return false;
        });

        // 显示 PopupMenu
        popupMenu.show();
    }

    // 删除好友的操作
    private void deleteFriend(int currentUserId, int UserId,int position) {
        // 删除好友数据库操作
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        boolean success = databaseHelper.removeFriend(currentUserId, UserId);

        if (success) {
            // 删除成功后从列表中移除该好友
            userList.remove(position);
            // 刷新RecyclerView，确保更新UI
            notifyItemRemoved(position);

            Toast.makeText(context, "delete successful！！", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "wrong!!", Toast.LENGTH_SHORT).show();
        }
    }
}
