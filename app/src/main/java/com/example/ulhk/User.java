package com.example.ulhk;

public class User {
    private int id;
    private String name;
    private String email;
    private String signature;  // 用户签名
    private byte[] profile;    // 头像路径（存储为字节数组）
    private boolean status;    // 用户在线状态，true为在线，false为离线
    private int label;         // 用户标签，可以用来表示其他特征

    // 构造函数调整为与成员变量顺序一致
    public User(int id, String name, String email, String signature, byte[] profile, boolean status, int label) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.signature = signature;
        this.profile = profile;
        this.status = status;
        this.label = label;
    }

    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getSignature() {
        return signature;
    }

    public byte[] getProfile() {
        return profile;
    }

    public boolean getStatus() {
        return status;
    }

    public int getLabel() {
        return label;
    }

    // 可选的 Setter 方法，如果需要的话
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void setProfile(byte[] profile) {
        this.profile = profile;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setLabel(int label) {
        this.label = label;
    }
}
