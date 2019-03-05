package blogapp.company.com.blogapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import blogapp.company.com.blogapp.Adapters.CommentsAdapter;
import blogapp.company.com.blogapp.Models.BlogPost;
import blogapp.company.com.blogapp.Models.Comments;
import blogapp.company.com.blogapp.Models.Users_Comments;

public class CommentActivity extends AppCompatActivity {
    Toolbar comment_toolbar;
    private EditText edit_Comment;
    private ImageView comment_Btn;
    private String blog_Post_Id;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String current_user_Id;
    private RecyclerView comment_List;
    private List<Comments> listItemsComments;
    private CommentsAdapter commentsAdapter;
    private List<Users_Comments> users_commentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        comment_toolbar = findViewById(R.id.comment_toolbar);
        setSupportActionBar(comment_toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        //init views
        edit_Comment = findViewById(R.id.comment_field);
        comment_Btn = findViewById(R.id.comment_post_btn);
        blog_Post_Id = getIntent().getStringExtra("blog_Post_Id");
        comment_List = findViewById(R.id.comment_list);
        //recyclerView settings
        listItemsComments = new ArrayList<>();
        users_commentsList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(listItemsComments,users_commentsList);
        comment_List.setHasFixedSize(true);
        comment_List.setLayoutManager(new LinearLayoutManager(this));
        comment_List.setAdapter(commentsAdapter);

        //getCurrent user id
        current_user_Id = firebaseAuth.getCurrentUser().getUid();

        //get All comment in recyclerView
        firebaseFirestore.collection("Posts/" + blog_Post_Id + "/Comments")
                .addSnapshotListener(CommentActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (!queryDocumentSnapshots.isEmpty()) {

                            //for loop to loop on the data
                            for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    final Comments comments = doc.getDocument().toObject(Comments.class);
                                    String user_Id = doc.getDocument().getString("user_id");
                                    firebaseFirestore.collection("Users").document(user_Id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                if (task.getResult().exists()) {
                                                    Users_Comments users_comments = task.getResult().toObject(Users_Comments.class);
                                                    users_commentsList.add(users_comments);

                                                    listItemsComments.add(comments);
                                                    commentsAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                    });

                                }
                            }
                        }
                    }
                });

        comment_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = edit_Comment.getText().toString();
                if (!comment.isEmpty()) {
                    //create sub collection for comments
                    Map<String, Object> comment_map = new HashMap<>();
                    comment_map.put("message", comment);
                    comment_map.put("user_id", current_user_Id);
                    comment_map.put("timestamp", FieldValue.serverTimestamp());
                    firebaseFirestore.collection("Posts/" + blog_Post_Id + "/Comments").add(comment_map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                edit_Comment.setText("Your Comment here ...");
                                Toast.makeText(CommentActivity.this, "Comment Added Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CommentActivity.this, "Failed to add comment,try again" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });
    }
}
