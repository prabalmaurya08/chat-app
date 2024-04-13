package com.example.chat_app.Adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app.ChatMessage;
import com.example.chat_app.databinding.ItemContainerMessageRecievedBinding;
import com.example.chat_app.databinding.ItemContainerMessageSentBinding;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Bitmap receiverProfileImg;
    private final List<ChatMessage> chatMessagesList;
    private final String SenderId;
    private final int VIEW_TYPE_SENDER=1;
    private final int VIEW_TYPE_RECEIVE=2;



    public ChatAdapter( List<ChatMessage> chatMessagesList,Bitmap receiverProfileImg, String senderId) {
        this.receiverProfileImg = receiverProfileImg;
        this.chatMessagesList = chatMessagesList;
       this.SenderId=senderId;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==VIEW_TYPE_SENDER){
            return new SentMessageViewHolder(ItemContainerMessageSentBinding.
                    inflate(LayoutInflater.from(parent.getContext()),
                            parent,false));

        }
        else {
            return new RecieverMessageViewHolder(ItemContainerMessageRecievedBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if(getItemViewType(position)==VIEW_TYPE_SENDER){
                ((SentMessageViewHolder)holder).setData(chatMessagesList.get(position));
            }
            else {
                ((RecieverMessageViewHolder)holder).setData(chatMessagesList.get(position),receiverProfileImg);
            }
    }

    @Override
    public int getItemCount() {
        return chatMessagesList.size();
    }
    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
    private final ItemContainerMessageSentBinding binding;
        public SentMessageViewHolder(ItemContainerMessageSentBinding itemContainerMessageSentBinding) {
            super(itemContainerMessageSentBinding.getRoot());
            binding=itemContainerMessageSentBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.textSendMsg.setText(chatMessage.message);
            binding.TimeDate.setText(chatMessage.datetime);
        }
    }
    @Override
    public int getItemViewType(int position) {
        if(chatMessagesList.get(position).senderId.equals(SenderId)){
            return VIEW_TYPE_SENDER;
        }
        else {
            return VIEW_TYPE_RECEIVE;
        }
    }
    static class RecieverMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerMessageRecievedBinding binding;

        public RecieverMessageViewHolder(ItemContainerMessageRecievedBinding itemContainerMessageRecievedBinding) {
            super(itemContainerMessageRecievedBinding.getRoot());
            binding=itemContainerMessageRecievedBinding;
        }
        public void setData(ChatMessage chatMessage,Bitmap receiverProfileImg){
            binding.recievedDateTime.setText(chatMessage.datetime);
            binding.recievedImg.setImageBitmap(receiverProfileImg);
            binding.textRecievedMsg.setText(chatMessage.message);

        }
    }
}
