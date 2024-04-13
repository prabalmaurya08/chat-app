package com.example.chat_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat_app.Adapter.ChatAdapter;
import com.example.chat_app.Modals.Users;
import com.example.chat_app.databinding.ActivityChatScreenBinding;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.PrefManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChatScreen extends BaseActivity {
    private ActivityChatScreenBinding binding;
    private Users receiverUsers;

    private  String conversationId;
    private PrefManager prefManager;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessage;
    private FirebaseFirestore firestore;
    private Boolean isReceiverOnline=false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatScreenBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        setListner();
        LoadRecieverDeatils();
            init();
            listenMessage();

    }
    private void listenAvailabeForReceiver(){
        firestore.collection(Constants.KEY_COLLECTION_USERS).document(receiverUsers.id)
                .addSnapshotListener(ChatScreen.this,(value, error) -> {
                    if(error!=null){
                        return;
                    }
                    if(value!=null){
                        if(value.getLong(Constants.KEY_AVAILABLE)!=null){
                            int avalability= Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABLE).intValue());
                            isReceiverOnline=avalability==1;
                        }
                    }
                    if(isReceiverOnline){
                        binding.textOnline.setVisibility(View.VISIBLE);
                    }
                    else {
                        binding.textOnline.setVisibility(View.INVISIBLE);
                    }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabeForReceiver();
    }

    private void checkForConversation(){
        if(!chatMessage.isEmpty()){
            checkForconversationRemotely(prefManager.getString(Constants.KEY_USER_ID),receiverUsers.id);
        }
        checkForconversationRemotely(receiverUsers.id,prefManager.getString(Constants.KEY_USER_ID));
    }

    private final OnCompleteListener<QuerySnapshot> querySnapshotOnCompleteListener =task -> {
        if(task.isSuccessful() &&task.getResult()!=null && task.getResult().getDocuments().size()>0){
            DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
            conversationId=documentSnapshot.getId();
        }
    };
    private void checkForconversationRemotely(String senderId,String receiverId){
        firestore.collection(Constants.KEY_CONVERSATION).whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId).get().addOnCompleteListener(querySnapshotOnCompleteListener);
    }
    private void init(){
        prefManager=new PrefManager(getApplicationContext());
        chatMessage=new ArrayList<>();
        Bitmap receiverImageBitmap = null;
        if (receiverUsers != null && receiverUsers.image != null) {
            receiverImageBitmap = getBitmapFromEncodedString(receiverUsers.image);
            chatAdapter=new ChatAdapter(chatMessage,getBitmapFromEncodedString(receiverUsers.image),prefManager.getString(Constants.KEY_USER_ID));
            binding.chatRV.setAdapter(chatAdapter);
            firestore=FirebaseFirestore.getInstance();
        }





    }
    private void listenMessage(){
//        firestore.collection(Constants.KEY_COLLECTION).whereEqualTo(Constants.KEY_SENDER_ID,prefManager.getString(Constants.KEY_USER_ID))
//                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverUsers.id)
//                .addSnapshotListener(eventListener);
//        firestore.collection(Constants.KEY_COLLECTION).whereEqualTo(Constants.KEY_SENDER_ID,receiverUsers.id).
//                whereEqualTo(Constants.KEY_RECEIVER_ID,prefManager.getString(Constants.KEY_USER_ID)).addSnapshotListener(eventListener);

        if (firestore != null) {
            firestore=FirebaseFirestore.getInstance();
            firestore.collection(Constants.KEY_COLLECTION)
                    .whereEqualTo(Constants.KEY_SENDER_ID, prefManager.getString(Constants.KEY_USER_ID))
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUsers.id)
                    .addSnapshotListener(eventListener);

            firestore.collection(Constants.KEY_COLLECTION)
                    .whereEqualTo(Constants.KEY_SENDER_ID, receiverUsers.id)
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, prefManager.getString(Constants.KEY_USER_ID))
                    .addSnapshotListener(eventListener);
        }
    }
    private final EventListener<QuerySnapshot> eventListener=(value, error) -> {
        if(error!=null){
            return;
        }

        if(value!=null) {
            int count = chatMessage.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMsg = new ChatMessage();
                    chatMsg.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMsg.RecieverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMsg.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMsg.datetime = getReadAbleTimeStamp(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                     chatMsg.dateobj= documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessage.add(chatMsg);
                }
            }
            Collections.sort(chatMessage, (obj1, obj2) -> obj1.dateobj.compareTo(obj2.dateobj));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessage.size(), chatMessage.size());
                binding.chatRV.smoothScrollToPosition(chatMessage.size() - 1);
            }
            binding.chatRV.setVisibility(View.VISIBLE);

        }

            binding.csPg.setVisibility(View.GONE);
        if(conversationId==null){
            checkForConversation();
        }

    };
    private void addConversation(HashMap<String,Object> hashMap){
        firestore.collection(Constants.KEY_CONVERSATION).add(hashMap).addOnSuccessListener(documentReference -> {
            conversationId=documentReference.getId();
        });
    }
    private void updateConversation(String message)
    {
        DocumentReference documentReference=firestore.collection(Constants.KEY_CONVERSATION).document(conversationId);
        documentReference.update(Constants.KEY_LAST_MESSAGE,message,Constants.KEY_TIMESTAMP,new Date());
    }
    public String getReadAbleTimeStamp(Date date){
        return new SimpleDateFormat("MMMM dd,yyyy -hh :m a", Locale.getDefault()).format(date);
    }
    //        HashMap<String,Object> message=new HashMap<>();
//        message.put(Constants.KEY_SENDER_ID,prefManager.getString(Constants.KEY_USER_ID));
//        message.put(Constants.KEY_RECEIVER_ID,prefManager.getString(Constants.KEY_RECEIVER_ID));
//        message.put(Constants.KEY_TIMESTAMP,new Date());
//        message.put(Constants.KEY_MESSAGE,binding.csTypeMsg.getText().toString());
//        firestore.collection(Constants.KEY_COLLECTION).add(message);
//        binding.csTypeMsg.setText(null);
    private void sendMessage(){



            FirebaseFirestore db=FirebaseFirestore.getInstance();
            HashMap<String,Object> message=new HashMap<>();
            message.put(Constants.KEY_SENDER_ID,prefManager.getString(Constants.KEY_USER_ID));

                message.put(Constants.KEY_RECEIVER_ID,receiverUsers.id);


            message.put(Constants.KEY_TIMESTAMP,new Date());
            message.put(Constants.KEY_MESSAGE,binding.csTypeMsg.getText().toString());

            // Add the message to the Firestore collection
            db.collection(Constants.KEY_COLLECTION).add(message);
            if(conversationId!=null){
                updateConversation(binding.csTypeMsg.getText().toString());
            }
            else {
                HashMap<String,Object> hashMap=new HashMap<>();
                hashMap.put(Constants.KEY_SENDER_ID,prefManager.getString(Constants.KEY_USER_ID));
                hashMap.put(Constants.KEY_SENDER_NAME,prefManager.getString(Constants.KEY_NAME));
                hashMap.put(Constants.KEY_SENDER_IMAGE,prefManager.getString(Constants.KEY_IMAGE));
                hashMap.put(Constants.KEY_RECEIVER_ID,receiverUsers.id);
                hashMap.put(Constants.KEY_RECEIVER_IMAGE,receiverUsers.image);
                hashMap.put(Constants.KEY_RECEIVER_NAME,receiverUsers.name);
                hashMap.put(Constants.KEY_LAST_MESSAGE,binding.csTypeMsg.getText().toString());
                hashMap.put(Constants.KEY_TIMESTAMP,new Date());
                addConversation(hashMap);
            }


            // Clear the message input field
            binding.csTypeMsg.setText(null);
    }
    private Bitmap getBitmapFromEncodedString(String encodedImg){
        byte[] bytes= Base64.decode(encodedImg,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
    private void LoadRecieverDeatils(){
        receiverUsers=(Users) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.csSetName.setText(receiverUsers.name);
        Serializable extra = getIntent().getSerializableExtra(Constants.KEY_USER);
        if (extra instanceof Users) {
            receiverUsers = (Users) extra;
            binding.csSetName.setText(receiverUsers.name);
        } else {
            // Handle the case where the extra is not of type Users
            // You can log an error or show a message to the user

        }

    }
    private void setListner(){

       binding.chatBack.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               startActivity(new Intent(ChatScreen.this, UserActivity.class));
           }
       });
        binding.frameSend.setOnClickListener(v-> sendMessage());
    }
}