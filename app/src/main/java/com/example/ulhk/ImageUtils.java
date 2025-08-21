package com.example.ulhk;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;

public class ImageUtils {
    public static byte[] usersProfile(int id, Context context) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 使用 ContextCompat.getDrawable 以兼容更多版本
        Drawable drawable = ContextCompat.getDrawable(context, id);

        // 确保 drawable 是 BitmapDrawable
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            // 压缩为 PNG 格式
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        }

        return baos.toByteArray();
    }
}
