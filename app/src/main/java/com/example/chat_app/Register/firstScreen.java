package com.example.chat_app.Register;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chat_app.MainActivity;
import com.example.chat_app.R;
import com.example.chat_app.databinding.ActivityFirstScreenBinding;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.PrefManager;

public class firstScreen extends AppCompatActivity {
    private  ActivityFirstScreenBinding firstScreenBinding;
    private PrefManager prefManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firstScreenBinding= ActivityFirstScreenBinding.inflate(getLayoutInflater());
        setContentView(firstScreenBinding.getRoot());
        setListners();
        prefManager=new PrefManager(getApplicationContext());
        if(prefManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent i=new Intent(getApplicationContext(), MainActivity.class);

            startActivity(i);
            finish();
        }
    }
        private void setListners(){
            firstScreenBinding.loginBtn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
            });
            firstScreenBinding.signUpBtn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), SignUp.class));
                    finish();
                }
            });
        }

}