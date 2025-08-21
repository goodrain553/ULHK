package com.example.ulhk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.ulhk.DatabaseHelper;

import java.util.Objects;


public class loginActivity extends AppCompatActivity {

    ImageButton loginButton;
    EditText user_name;
    EditText user_password;
    DatabaseHelper dh;
    ImageButton GoRegisterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        dh = DatabaseHelper.getInstance(this);

        user_name = (EditText)findViewById(R.id.login_user_name);
        user_password = (EditText)findViewById(R.id.login_password);

        loginButton = (ImageButton) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(view -> {

            String S_username = user_name.getText().toString();
            String S_pswd = user_password.getText().toString();

            Cursor userObj = dh.getUserInfoByUserName(S_username);
            if(userObj.moveToFirst()) {
                String pswd = userObj.getString(userObj.getColumnIndexOrThrow("Users_key"));
                boolean loginCheck = (Objects.equals(pswd, S_pswd));

                if (loginCheck) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("username", S_username);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    //pop-up reminder.
                    Toast.makeText(loginActivity.this, R.string.wrong_password_or_user_name, Toast.LENGTH_LONG).show();
                }
            }
            else{
                Toast.makeText(loginActivity.this, R.string.wrong_username, Toast.LENGTH_LONG).show();

            }

        });


        GoRegisterButton = (ImageButton) findViewById(R.id.login_signupButton);
        GoRegisterButton.setOnClickListener(view -> {

            Intent intent = new Intent(getApplicationContext(),RegisterActivity.class);
            startActivity(intent);

        });
    }


}