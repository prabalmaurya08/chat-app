package com.example.chat_app.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app.ChatMessage;
import com.example.chat_app.Listners.ConversationListner;
import com.example.chat_app.Modals.Users;
import com.example.chat_app.databinding.UseritemRecentChatBinding;

import java.util.List;

public class RecentConversation extends RecyclerView.Adapter<RecentConversation.ConversationViewHolder> {
    private List<ChatMessage> chatMessageList;
    private final ConversationListner conversationListner;

    public RecentConversation(List<ChatMessage> chatMessageList,ConversationListner conversationListner) {
        this.chatMessageList = chatMessageList;
        this.conversationListner=conversationListner;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ConversationViewHolder(UseritemRecentChatBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.setData(chatMessageList.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        UseritemRecentChatBinding binding;
        ConversationViewHolder(UseritemRecentChatBinding useritemRecentChatBinding){
            super(useritemRecentChatBinding.getRoot());
            binding=useritemRecentChatBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.recentPic.setImageBitmap(getConversationImg(chatMessage.conversationImage));
            binding.username.setText(chatMessage.conversationName);
            binding.recentmsg.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v->{
                Users users=new Users();
                users.id= chatMessage.conversationId;
                users.name= chatMessage.conversationName;
                users.image= chatMessage.conversationImage;
                conversationListner.onUserCOnversationClicked(users);
            });

        }

    }
    private Bitmap getConversationImg(String encoded){




        byte[] bytes= Base64.decode(encoded,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
