package com.example.chat_app.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app.Listners.UserListner;
import com.example.chat_app.Modals.Users;
import com.example.chat_app.databinding.UseritemBinding;


import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserviewHolder> {
    private final List<Users> usersList;
    private final UserListner userListner;

    public UserAdapter(List<Users> usersList, UserListner userListner) {
        this.usersList = usersList;
        this.userListner = userListner;
    }



    @NonNull
    @Override
    public UserviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UseritemBinding binding=UseritemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);



        return new UserviewHolder(binding);


    }

    @Override
    public void onBindViewHolder(@NonNull UserviewHolder holder, int position) {
    holder.setUserData(usersList.get(position));
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }


    class UserviewHolder extends RecyclerView.ViewHolder{
        UseritemBinding binding;
        UserviewHolder( UseritemBinding useritemBinding){
            super(useritemBinding.getRoot());
            binding=useritemBinding;
        }
        void setUserData(Users users){
            binding.upperTv.setText(users.name);
            binding.lowerTv.setText(users.email);
            binding.userPic.setImageBitmap(getUserImage(users.image));
            binding.getRoot().setOnClickListener(v-> userListner.onUserClicked(users));
        }
    }
    private Bitmap getUserImage(String encodedImg){
        byte[] bytes= Base64.decode(encodedImg,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
