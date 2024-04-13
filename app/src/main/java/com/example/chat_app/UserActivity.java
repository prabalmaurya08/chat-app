package com.example.chat_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import androidx.appcompat.app.AppCompatActivity;

import com.example.chat_app.Adapter.UserAdapter;
import com.example.chat_app.Listners.UserListner;
import com.example.chat_app.Modals.Users;
import com.example.chat_app.databinding.ActivityUserActivityBinding;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.PrefManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity  implements UserListner {
    private ActivityUserActivityBinding binding;
    PrefManager prefManager;
    Users  users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding=ActivityUserActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        prefManager=new PrefManager(getApplicationContext());
        binding.backArrow.setOnClickListener(v -> onBackPressed());
        getUser();
        setListner();

    }
    private void setListner(){
        binding.backArrow.setOnClickListener(v-> startActivity(new Intent(UserActivity.this, MainActivity.class)));
    }

    private void getUser(){
        loading(true);
        FirebaseFirestore db=FirebaseFirestore.getInstance();

        db.collection(Constants.KEY_COLLECTION_USERS).get().addOnCompleteListener(task ->{
            loading(false);
            String currentUserId=prefManager.getString(Constants.KEY_USER_ID);
            if(task.isSuccessful() &&task.getResult()!=null){
                List<Users> usersList=new ArrayList<>();
                for (QueryDocumentSnapshot documentSnapshot:task.getResult()) {
//                    if(currentUserId.equals(documentSnapshot.getId())){
//                        continue;
//                    }
                    Users users=new Users();
                    users.name=documentSnapshot.getString(Constants.KEY_NAME);
                    users.email=documentSnapshot.getString(Constants.KEY_EMAIL);
                    users.image=documentSnapshot.getString(Constants.KEY_IMAGE);
                    users.token=documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                    users.id=documentSnapshot.getId();





                    usersList.add(users);
                }
                if(!usersList.isEmpty()){
                    UserAdapter userAdapter=new UserAdapter(usersList,this);
                    binding.UserRecyclerView.setAdapter(userAdapter);
                    binding.UserRecyclerView.setVisibility(View.VISIBLE);
                }
                else{
                    ShowerrorMsg();
                }

            }
            else {
                ShowerrorMsg();
            }
        });
    }
    private void  ShowerrorMsg(){
        binding.addUserTv.setText(String.format("%s","No user Available"));
        binding.addUserTv.setVisibility(View.VISIBLE);
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.addUserPg.setVisibility(View.VISIBLE);

        }
        else {
            binding.addUserPg.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(Users users) {
        Intent i=new Intent(getApplicationContext(), ChatScreen.class);
       i.putExtra(Constants.KEY_USER,users);
       startActivity(i);
       finish();
    }
}