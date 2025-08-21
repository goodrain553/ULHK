package com.example.ulhk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.ulhk.DatabaseHelper;


public class RegisterActivity extends AppCompatActivity {

    EditText userNameET,passWordET,eMailET,realNameET;
    ImageButton registerSignUpButton;
    DatabaseHelper dh ;
    ImageButton registerGetBackButton;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        dh = DatabaseHelper.getInstance(this);

        registerGetBackButton = (ImageButton) findViewById(R.id.RegisterGetBackButton);
        userNameET = (EditText) findViewById(R.id.regUserNameEditText);
        passWordET = (EditText) findViewById(R.id.regPassWordEditText);
        eMailET = (EditText) findViewById(R.id.regEMainEditText);
        realNameET = (EditText) findViewById(R.id.regRealNameEditText);

        registerSignUpButton = (ImageButton) findViewById(R.id.regSignUpButton);

        registerSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = userNameET.getText().toString();
                String password = passWordET.getText().toString();
                String eMail = eMailET.getText().toString();
                String reName = realNameET.getText().toString();
                String signature = "this man is too lazy, nothing left here.";
                //int profile = 0;
                int userLabel = -1;
                dh.insertUsersData(username,password,eMail,reName,userLabel,R.drawable.icon_user_default,signature,false);
                Toast.makeText(RegisterActivity.this,"sign up successfully",Toast.LENGTH_LONG);
                finish();
            }
        });

        registerGetBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}