package com.example.chat_app.Register;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.chat_app.R;
import com.example.chat_app.databinding.ActivitySignUpBinding;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.PrefManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

public class SignUp extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private  String encodedImg;
    PrefManager prefManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        binding=ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        prefManager=new PrefManager(getApplicationContext());
        setListener();
    }

    private void setListener(){
        binding.loginBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });

        binding.signUpBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  addDataToFirebase();
                if(isValidDetails()){
                    signUp();
                }

            }
        });
        binding.frameImg.setOnClickListener(v->{
            Intent i=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImg.launch(i);
        });

    }
    private void signUp(){
        loading(true);
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        HashMap<String , Object> user=new HashMap<>();
        user.put(Constants.KEY_NAME,binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL,binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASS,binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE,encodedImg);
        db.collection(Constants.KEY_COLLECTION_USERS).add(user).addOnSuccessListener(documentReference -> {
            loading(false);
            prefManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
            prefManager.putString(Constants.KEY_IMAGE,encodedImg);
            prefManager.putString(Constants.KEY_NAME,binding.inputName.getText().toString());
            prefManager.putString(Constants.KEY_USER_ID,documentReference.getId());
            Intent intent=new Intent(getApplicationContext(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e ->{
            loading(false);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        });
    }
    private String  encodedImg(Bitmap bitmap){
        int previewWidth=150;
        int previewHeight=bitmap.getHeight()*previewWidth /bitmap.getWidth();
        Bitmap previewBitmap=Bitmap.createScaledBitmap(bitmap,previewHeight,previewWidth,false);
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes=byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);

    }
    private final ActivityResultLauncher<Intent> pickImg=registerForActivityResult(new
            ActivityResultContracts.StartActivityForResult(),result ->{
                if(result.getData()!=null){
                    Uri imageUri=result.getData().getData();
                    try {
                        InputStream inputStream=getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                        binding.addImg.setImageBitmap(bitmap);
                        binding.textAddImg.setVisibility(View.GONE);
                        encodedImg=encodedImg(bitmap);
                    }
                    catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
            }
    );
//    private  void addDataToFirebase(){
//        FirebaseFirestore db=FirebaseFirestore.getInstance();
//        HashMap<String ,Object> hm=new HashMap<>();
//        hm.put("Prabal","Maurya");
//        hm.put("Abcd","abcd");
//        db.collection("users").add(hm).addOnSuccessListener(documentReference -> {
//            Toast.makeText(this, "Successfull", Toast.LENGTH_SHORT).show();
//        });
//    }
    boolean isValidDetails(){


         if(binding.inputName.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show();
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }

    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.signUpBtn2.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.signUpBtn2.setVisibility(View.VISIBLE);
        }
    }
}