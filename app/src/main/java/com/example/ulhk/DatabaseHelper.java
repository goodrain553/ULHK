package com.example.ulhk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DBNAME = "ULHK.db";
    private static final int VERSION = 1;
    public static final String TABLE_NAME_1 = "ULHK_Users";
    public static final String TABLE_NAME_2 = "ULHK_Events";
    public static final String TABLE_NAME_3 = "ULHK_Users_Events";
    public static final String TABLE_NAME_4 = "ULHK_Friends";
    private Context context;
    //single
    private static DatabaseHelper instance;

    public DatabaseHelper(Context context) {

        super(context, DBNAME, null, VERSION);
        this.context = context;
    }

    //single
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable_1 = "CREATE TABLE " + TABLE_NAME_1 + " (" +
                "Users_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Users_name TEXT NOT NULL, " +
                "Users_key TEXT NOT NULL, " +
                "Users_Email TEXT NOT NULL, " +
                "Users_realName TEXT, " +
                "Users_signature TEXT,"+
                "Users_label INTEGER,"+
                "Users_profile BLOB, " + // 头像路径
                "Users_status INTEGER)";  // 在线状态

        String createTable_2 = "CREATE TABLE " + TABLE_NAME_2+ " (" +
                "Events_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Users_id INTEGER, "+
                "Events_title TEXT, " +
                "Events_venue TEXT, " +
                "Events_type TEXT, " +
                "Events_number INTEGER," +
                "Events_time DATETIME, " +
                "Events_description TEXT ," +
                "Events_posttime DATETIME ," +
                "FOREIGN KEY (Users_id) REFERENCES " + TABLE_NAME_1 + "(Users_id))";

        String createTable_3 = "CREATE TABLE " + TABLE_NAME_3 + " (" +
                "Users_Events_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Users_id INTEGER, " +
                "Events_id INTEGER, " +
                "status INTEGER, " +   // 选collected/joined
                "FOREIGN KEY (Users_id) REFERENCES " + TABLE_NAME_1 + "(Users_id), " +
                "FOREIGN KEY (Events_id) REFERENCES " + TABLE_NAME_2 + "(Events_id) ON DELETE CASCADE," +  // 外键约束，删除事件时自动删除相关记录
                "UNIQUE(Users_id, Events_id) " +  //
                ")";

        String createTable_4 = "CREATE TABLE " + TABLE_NAME_4 + " (" +
                "Friend_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "User_id INTEGER, " +
                "Friend_user_id INTEGER, " +
                "FOREIGN KEY (User_id) REFERENCES " + TABLE_NAME_1 + "(Users_id)ON DELETE CASCADE, " +
                "FOREIGN KEY (Friend_user_id) REFERENCES " + TABLE_NAME_1 + "(Users_id)ON DELETE CASCADE," +
                "UNIQUE(User_id, Friend_user_id) " +
                ")";

        db.execSQL(createTable_1);
        db.execSQL(createTable_2);
        db.execSQL(createTable_3);
        db.execSQL(createTable_4);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // 删除旧表
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_1);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_2);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_3);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_4);

            // 重新创建表
            onCreate(db);
        }
    }



    public void insertUsersData(String usersName, String usersKey, String usersEmail, String usersRealName, int usersLabel, int profileImageResId, String usersSignature,boolean usersStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            byte[] usersProfile = ImageUtils.usersProfile(profileImageResId, context);

            ContentValues values = new ContentValues();
            values.put("Users_name", usersName);
            values.put("Users_key", usersKey);
            values.put("Users_Email", usersEmail);
            values.put("Users_realName", usersRealName);
            values.put("Users_profile", usersProfile); //存储图片字节组
            values.put("Users_signature",usersSignature);
            values.put("Users_label",usersLabel);
            values.put("Users_status", usersStatus ? 1 : 0); // 在线状态，上线为1，不上线为0

            db.insert(TABLE_NAME_1, null, values);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }


    // 根据用户ID获取头像
    public byte[] getUserProfileById( int userId) {

        SQLiteDatabase db = this.getReadableDatabase();
        // 定义SQL查询语句
        String query = "SELECT Users_profile FROM " + TABLE_NAME_1 + " WHERE Users_id = ?";

        // 定义返回的头像数据
        byte[] userProfileData = null;

        // 执行查询
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            // 检查查询结果
            if (cursor != null && cursor.moveToFirst()) {
                // 获取BLOB字段（用户头像）
                userProfileData = cursor.getBlob(cursor.getColumnIndex("Users_profile"));
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // 返回头像数据，如果没有找到头像，则返回null
        return userProfileData;
    }






    //插入两个其他用户的发帖测试用例

    public void insertTestEvents() {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // 为用户2插入一个事件
            ContentValues values = new ContentValues();
            values.put("Users_id", 2);  // 用户ID为2
            values.put("Events_title", "Outdoor Hiking Trip");  // 事件标题
            values.put("Events_venue", "Mountain Park");  // 事件地点
            values.put("Events_type", "Outdoor");  // 事件类型
            values.put("Events_number", 15);  // 参加人数
            values.put("Events_time", "2024-12-01 09:00:00");  // 事件时间
            values.put("Events_description", "Join us for a refreshing hiking trip in Mountain Park!");  // 事件描述

            db.insert(TABLE_NAME_2, null, values);  // 插入事件数据

            // 为用户3插入另一个事件
            values.clear();  // 清空之前的值
            values.put("Users_id", 3);  // 用户ID为3
            values.put("Events_title", "Art Exhibition");  // 事件标题
            values.put("Events_venue", "City Gallery");  // 事件地点
            values.put("Events_type", "Art");  // 事件类型
            values.put("Events_number", 50);  // 参加人数
            values.put("Events_time", "2024-12-10 18:00:00");  // 事件时间
            values.put("Events_description", "Experience the latest artworks from local artists at City Gallery.");  // 事件描述

            db.insert(TABLE_NAME_2, null, values);  // 插入事件数据
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }


    public void insertTestUsers() {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // 插入第一个测试用户，id=1 为当前用户
            ContentValues values = new ContentValues();
            values.put("Users_id", 1);
            values.put("Users_name", "CurrentUser");
            values.put("Users_key", "key1");
            values.put("Users_Email", "currentuser@example.com");
            values.put("Users_realName", "Current User");
            values.put("Users_profile", ImageUtils.usersProfile(R.drawable.icon_user_default, context)); // 假设用默认图片
            values.put("Users_signature", "I am the current user.");
            values.put("Users_label", 1);  // 设置标签
            values.put("Users_status", 1);  // 当前用户在线
            db.insert(TABLE_NAME_1, null, values);

            // 插入第二个测试用户
            values.clear();
            values.put("Users_id", 2);
            values.put("Users_name", "TestUser2");
            values.put("Users_key", "key2");
            values.put("Users_Email", "testuser2@example.com");
            values.put("Users_realName", "Test User 2");
            values.put("Users_profile", ImageUtils.usersProfile(R.drawable.icon_user_default, context));
            values.put("Users_signature", "I am Test User 2.");
            values.put("Users_label", 2);
            values.put("Users_status", 0);  // 设置为离线
            db.insert(TABLE_NAME_1, null, values);

            // 插入第三个测试用户
            values.clear();
            values.put("Users_id", 3);
            values.put("Users_name", "TestUser3");
            values.put("Users_key", "key3");
            values.put("Users_Email", "testuser3@example.com");
            values.put("Users_realName", "Test User 3");
            values.put("Users_profile", ImageUtils.usersProfile(R.drawable.icon_user_default, context));
            values.put("Users_signature", "I am Test User 3.");
            values.put("Users_label", 3);
            values.put("Users_status", 1);
            db.insert(TABLE_NAME_1, null, values);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

//    public void clearTable() {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.execSQL("DELETE FROM " + TABLE_NAME_1);  // 删除 Users 表中的所有数据
//        db.execSQL("DELETE FROM " + TABLE_NAME_4);
//    }


    // 检查两位用户是否为好友
    public boolean areUsersFriends(int currentUserId, int friendUserId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_NAME_4 +
                " WHERE (User_id = ? AND Friend_user_id = ?) OR (User_id = ? AND Friend_user_id = ?)";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(currentUserId),
                String.valueOf(friendUserId),
                String.valueOf(friendUserId),
                String.valueOf(currentUserId)});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;  // 如果返回的计数大于0，表示已经是好友
    }


    public void addFriend(int userId, int friendUserId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("User_id", userId);
            values.put("Friend_user_id", friendUserId);

            db.insert(TABLE_NAME_4, null, values);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }


    public boolean editUser(int currentUserId,String name,String email,int label,String signature,byte[] usersProfile){
        SQLiteDatabase db =this.getWritableDatabase();
        try {

            // byte[] usersProfile = ImageUtils.usersProfile(profileImageResId, context);
            // 使用 SQL 语句更新数据
            String sql = " UPDATE " + TABLE_NAME_1 + " SET Users_name = ?, Users_Email = ?, Users_label = ?, Users_signature = ? , Users_profile = ? " +
                    "WHERE Users_id = ?";

            db.execSQL(sql, new Object[]{name, email, label, signature, usersProfile, currentUserId});
            return true; // 更新成功

        } catch (Exception e) {
            e.printStackTrace();
            return false; // 更新失败
        } finally {
            db.close(); // 确保数据库连接关闭
        }
    }


    public Cursor searchUsers(String input) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT Users_id, Users_name, Users_Email, Users_realName,Users_signature,Users_label,Users_status, Users_profile FROM " + TABLE_NAME_1 +
                " WHERE Users_Email LIKE ? OR Users_name LIKE ?";
        String likeInput = "%" + input + "%";
        return db.rawQuery(query, new String[]{likeInput, likeInput});
    }


    public Cursor getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME_1 +" WHERE Users_id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }



    public boolean removeFriend(int currentUserId, int friendUserId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // 使用合适的 SQL 语句删除好友关系
        String whereClause = "User_id = ? AND Friend_user_id = ?";
        String[] whereArgs = new String[] { String.valueOf(currentUserId), String.valueOf(friendUserId) };
        int rowsDeleted = db.delete(TABLE_NAME_4, whereClause, whereArgs);
        return rowsDeleted > 0;
    }



    public List<User> getFriendsByStatus(int currentUserId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<User> onlineFriends = new ArrayList<>();
        List<User> offlineFriends = new ArrayList<>();

        // 查询当前用户的所有好友
        String query = "SELECT u.Users_id, u.Users_name, u.Users_Email, u.Users_realName,u.Users_signature,u.Users_label,u.Users_status, u.Users_profile " +
                "FROM " + TABLE_NAME_4 + " f " +
                "JOIN " + TABLE_NAME_1 + " u ON (f.Friend_user_id = u.Users_id) " +
                "WHERE f.User_id = ?";  // 查询当前用户的所有好友

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(currentUserId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("Users_id"));
                String userName = cursor.getString(cursor.getColumnIndexOrThrow("Users_name"));
                String userEmail = cursor.getString(cursor.getColumnIndexOrThrow("Users_Email"));
                int userLabel = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow("Users_label")));
                String userSignature = cursor.getString(cursor.getColumnIndexOrThrow("Users_signature"));
                byte[] userProfile = cursor.getBlob(cursor.getColumnIndexOrThrow("Users_profile"));
                int userStatusInt = cursor.getInt(cursor.getColumnIndexOrThrow("Users_status"));
                boolean userStatus = (userStatusInt == 1);  // 如果值为1，则在线，否则离线

                User friend = new User(userId, userName, userEmail, userSignature,userProfile, userStatus==true, userLabel);
                // 根据在线状态将好友分组
                if (friend.getStatus()) {
                    onlineFriends.add(friend);
                } else {
                    offlineFriends.add(friend);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        // 返回一个包含在线和离线好友的列表
        List<User> allFriends = new ArrayList<>();
        allFriends.addAll(onlineFriends);
        allFriends.addAll(offlineFriends);
        return allFriends;
    }


    public void insertEvent(String eventsTitle, int userId,String eventsVenue, String eventsType, int eventsNumber, String eventsTime, String eventsDescription,String postTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("Events_title", eventsTitle);
            values.put("Users_id", userId);
            values.put("Events_venue", eventsVenue);
            values.put("Events_type", eventsType);
            values.put("Events_number", eventsNumber);
            values.put("Events_time", eventsTime);
            values.put("Events_description", eventsDescription);
            values.put("Events_posttime", postTime);

            db.insert(TABLE_NAME_2, null, values);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }



    public Cursor getAllEvents() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // 查询所有事件数据
            cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME_2, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cursor;
    }


    // 插入或更新用户的事件状态
    public void insertOrUpdateUserEventStatus(int eventId, int userId, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("Events_id", eventId);
            values.put("Users_id", userId);
            values.put("status", status);

            // 如果记录存在则更新，不存在则插入
            int rowsUpdated = db.update(TABLE_NAME_3, values, "Users_id = ? AND Events_id = ?",
                    new String[]{String.valueOf(userId), String.valueOf(eventId)});
            if (rowsUpdated == 0) {
                db.insert(TABLE_NAME_3, null, values);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    // 查询特定用户参与的事件状态
    public Cursor getUserEventStatus(int userId, int eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME_3 + " WHERE Users_id = ? AND Events_id = ?",
                    new String[]{String.valueOf(userId), String.valueOf(eventId)});
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cursor;
    }


    public Cursor getAllUserEventStatus() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME_3, null);
    }

    //搜索功能
    public Cursor searchEvents(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // 用 LIKE 进行模糊查询，搜索所有相关字段（Events_title、Events_venue、Events_type 等）
            String sql = "SELECT * FROM " + TABLE_NAME_2 + " WHERE " +
                    "Events_title LIKE ? OR " +
                    "Events_venue LIKE ? OR " +
                    "Events_type LIKE ? OR " +
                    "Events_description LIKE ?";

            // 为查询语句添加参数，参数为带有通配符 % 的搜索关键词
            cursor = db.rawQuery(sql, new String[]{
                    "%" + query + "%",
                    "%" + query + "%",
                    "%" + query + "%",
                    "%" + query + "%"
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cursor;
    }

    //查询用户加入的活动的函数
    public Cursor getUserJoinedEvents(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        // 查询 status 为 1 且与用户相关的事件
        String query = "SELECT * FROM " + TABLE_NAME_3 + " ues " +
                "JOIN " + TABLE_NAME_2 + " e ON ues.Events_id = e.Events_id " +
                "WHERE ues.Users_id = ? AND ues.status = 1";
        cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        return cursor;
    }

    //查询用户发布的所有活动111
    public Cursor getUserAllPosts(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        // 查询该用户发布的所有帖子，假设你要查询所有字段
        String query = "SELECT * FROM " + TABLE_NAME_2 + " WHERE Users_id = ?";

        // 执行查询，并返回一个 Cursor 对象，包含查询结果
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    //查询表1user_id
    public Cursor getUserInfoById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME_1 + " WHERE Users_id = ?";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    public Cursor getEventsByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME_2 + " WHERE Events_type = ?";
        String[] selectionArgs = new String[]{category};
        return db.rawQuery(sql, selectionArgs);
    }

    public Cursor getUserInfoByUserName(String userName){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME_1 + " WHERE Users_name = ?";
        return db.rawQuery(query, new String[]{String.valueOf(userName)});
    }


}
