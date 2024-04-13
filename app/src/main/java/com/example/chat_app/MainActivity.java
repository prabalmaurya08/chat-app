package com.example.chat_app;



import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.chat_app.Adapter.RecentConversation;
import com.example.chat_app.Listners.ConversationListner;
import com.example.chat_app.Modals.Users;
import com.example.chat_app.Register.Login;
import com.example.chat_app.databinding.ActivityMainBinding;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.PrefManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversationListner {
    private ActivityMainBinding binding;
    private PrefManager prefManager;
    private List<ChatMessage> conversationList;
    private RecentConversation conversationAdapter;
   private FirebaseFirestore firestore;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        prefManager=new PrefManager(getApplicationContext());
        init();
        loadDetails();
        getToken();
        setListner();
        listenConversation();





    }
    private void init(){
        conversationList=new ArrayList<>();
        conversationAdapter=new RecentConversation(conversationList,this);
        binding.recentItemRv.setAdapter(conversationAdapter);
        firestore=FirebaseFirestore.getInstance();
    }
    private final EventListener<QuerySnapshot> eventListener=(value, error) -> {
        if(error!=null){
            return;
        }
        if(value!=null){
            for(DocumentChange documentChange:value.getDocumentChanges()){
                if(documentChange.getType()==DocumentChange.Type.ADDED){
                    String senderID=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage=new ChatMessage();
                    chatMessage.senderId=senderID;
                    chatMessage.RecieverId=receiverId;
                    if(prefManager.getString(Constants.KEY_USER_ID).equals(senderID)){
                        chatMessage.conversationId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        chatMessage.conversationImage=documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversationName=documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                    }
                    else {
                        chatMessage.conversationName=documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversationImage=documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversationId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);

                    }
                    chatMessage.message=documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateobj=documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversationList.add(chatMessage);
                } else if (documentChange.getType()==DocumentChange.Type.ADDED) {
                    for (int i = 0; i < conversationList.size(); i++) {
                        String senderId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String recieverId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if(conversationList.get(i).senderId.equals(senderId) && conversationList.get(i).RecieverId.equals(recieverId)){
                            conversationList.get(i).message=documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversationList.get(i).dateobj=documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }

                }
            }
            Collections.sort(conversationList,(obj1,obj2) ->obj2.dateobj.compareTo(obj1.dateobj));
            conversationAdapter.notifyDataSetChanged();
            binding.recentItemRv.smoothScrollToPosition(0);
            binding.recentItemRv.setVisibility(View.VISIBLE);

        }
    };
    private void setListner(){
        binding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        binding.floatBtn.setOnClickListener(v->{
            startActivity(new Intent(MainActivity.this, UserActivity.class));
            finish();

        });
    }
    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::UpdateToken);
    }
    public void UpdateToken(String token){
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference documentReference=database.collection(Constants.KEY_COLLECTION_USERS).document(prefManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN,token).addOnSuccessListener(unused -> {

        }).addOnFailureListener(e->{

        });
    }
    private void signOut(){
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference documentReference=database.collection(Constants.KEY_COLLECTION_USERS).document(prefManager.getString(Constants.KEY_USER_ID));
        HashMap<String,Object> hm=new HashMap<>();
        hm.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(hm).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                prefManager.clear();
                Toast.makeText(MainActivity.this, "Signing out..........", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        }).addOnFailureListener(e->{
            Toast.makeText(this, "Failed to signOut", Toast.LENGTH_SHORT).show();
        });
    }
    //display user name
    private void loadDetails(){
        binding.displayName.setText(prefManager.getString(Constants.KEY_NAME));
        if(prefManager.getString(Constants.KEY_IMAGE)!=null){
            byte[] bytes= Base64.decode(prefManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
binding.profileImg.setImageBitmap(bitmap);
        }

    }
    private void listenConversation(){
        firestore.collection(Constants.KEY_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID,prefManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        firestore.collection(Constants.KEY_CONVERSATION)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,prefManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    @Override
    public void onUserCOnversationClicked(Users users) {
Intent intent=new Intent(MainActivity.this, ChatScreen.class);
intent.putExtra(Constants.KEY_USER,users);
startActivity(intent);
    }
}