package com.example.chat_app.Register;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.lights.Light;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chat_app.MainActivity;
import com.example.chat_app.R;
import com.example.chat_app.databinding.ActivityLoginBinding;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.PrefManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.regex.Pattern;

public class Login extends AppCompatActivity {
private ActivityLoginBinding binding;
private PrefManager prefManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        prefManager=new PrefManager(getApplicationContext());
        if(prefManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent i=new Intent(getApplicationContext(),MainActivity.class);

            startActivity(i);
            finish();
        }
        setListner();

    }
    private void setListner(){
        binding.loginBtn3.setOnClickListener(v -> {
            if(isValid()){
                login();
            }
        });
        binding.signUpBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignUp.class));
                finish();
            }
        });
    }
    private void login(){
        loading(true);
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS).whereEqualTo(Constants.KEY_EMAIL,binding.loginEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASS,binding.loginPassword.getText().toString())
                .get().addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult()!=null && !task.getResult().getDocuments().isEmpty()){
                        DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                        prefManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        prefManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                        prefManager.putString(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME));
                        prefManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent i=new Intent(getApplicationContext(), MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();

                    }
                    else{
                        loading(false);


                        Toast.makeText(Login.this, "Failed", Toast.LENGTH_SHORT).show();
                        binding.signUpBtn3.setVisibility(View.VISIBLE);
                    }
                });

    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.loginBtn3.setVisibility(View.INVISIBLE);
            binding.loginProgressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.loginProgressBar.setVisibility(View.INVISIBLE);
            binding.signUpBtn3.setVisibility(View.VISIBLE);
        }
    }

    private boolean isValid(){
        if(!Patterns.EMAIL_ADDRESS.matcher(binding.loginEmail.getText().toString().trim()).matches()){
            Toast.makeText(this, "Enter Valid Email Address", Toast.LENGTH_SHORT).show();
            return false;
        } else if (binding.loginPassword.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            return false;

        }
        else{
            return true;
        }
    }
}