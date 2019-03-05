package blogapp.company.com.blogapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import blogapp.company.com.blogapp.CommentActivity;
import blogapp.company.com.blogapp.Common.PreviewImageActivity;
import blogapp.company.com.blogapp.Models.BlogPost;
import blogapp.company.com.blogapp.Models.Users;
import blogapp.company.com.blogapp.R;

public class BlogRecyeclerAdapter extends RecyclerView.Adapter<BlogRecyeclerAdapter.viewHolder> {
    List<BlogPost> blog_List;
    List<Users> usersList;
    public Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public BlogRecyeclerAdapter(List<BlogPost> blog_List, List<Users> usersList) {
        this.blog_List = blog_List;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.blog_list_item, viewGroup, false);
        context = viewGroup.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final viewHolder viewHolder, final int position) {
        viewHolder.setIsRecyclable(false);
        final String blog_Post_Id = blog_List.get(position).BlogPostId;
        final String current_User_Id = firebaseAuth.getCurrentUser().getUid();
        String textDesc = blog_List.get(position).getDesc();
        viewHolder.getDesc(textDesc);
        String blog_Image_Url = blog_List.get(position).getImage_url();
        String blog_Thumb_Url = blog_List.get(position).getThumb_url();
        viewHolder.getPostImage(blog_Image_Url, blog_Thumb_Url);
        //user Data

        String name = usersList.get(position).getName();
        String image = usersList.get(position).getImage();
        viewHolder.getUserName(name);
        viewHolder.getUserImage(image);


        long miliseconds = blog_List.get(position).timestamp.getTime();
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(miliseconds)).toString();
        viewHolder.getPlogDate(dateString);
        //get likes count
        firebaseFirestore.collection("Posts/" + blog_Post_Id + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    int count = queryDocumentSnapshots.size();
                    viewHolder.getLikesCount(count);
                } else {
                    viewHolder.getLikesCount(0);
                }
            }
        });
        //get likes
        firebaseFirestore.collection("Posts/" + blog_Post_Id + "/Likes").document(current_User_Id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    viewHolder.blog_Like_Btn.setImageResource(R.mipmap.action_like_accent);
                } else {
                    viewHolder.blog_Like_Btn.setImageResource(R.mipmap.action_like_gray);

                }
            }
        });
        //Likes actions
        viewHolder.blog_Like_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create a sub collection called Likes inside Posts collection
                //there are two ways to write it
                // firebaseFirestore.collection("Posts").document(blog_Post_Id).collection("Likes") first way
                // firebaseFirestore.collection("Posts/"+blog_Post_Id+"/Likes") second way
                firebaseFirestore.collection("Posts/" + blog_Post_Id + "/Likes").document(current_User_Id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        //check if the user already like this post or not
                        if (!task.getResult().exists()) {
                            //if not exist add new like
                            Map<String, Object> likes_Map = new HashMap<>();
                            likes_Map.put("timestamp", FieldValue.serverTimestamp());
                            firebaseFirestore.collection("Posts/" + blog_Post_Id + "/Likes").document(current_User_Id).set(likes_Map);
                        } else {
                            //if exist delete it

                            firebaseFirestore.collection("Posts/" + blog_Post_Id + "/Likes").document(current_User_Id).delete();

                        }
                    }
                });

            }
        });
        //get Comments count
        firebaseFirestore.collection("Posts/" + blog_Post_Id + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    int count = queryDocumentSnapshots.size();
                    viewHolder.getCommentsCount(count);
                } else {
                    viewHolder.getCommentsCount(0);
                }
            }
        });
        //comment intent
        viewHolder.blog_Comment_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent commentIntent = new Intent(context, CommentActivity.class);
                commentIntent.putExtra("blog_Post_Id", blog_Post_Id);
                context.startActivity(commentIntent);
            }
        });

//        //delete post
//        String blog_user_Id = blog_List.get(position).getUser_id();
//        if (current_User_Id.equals(blog_user_Id)) {
//            //show the delete button
//        }
//        //delete button action
//        viewHolder.textCommentCount.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                firebaseFirestore.collection("Posts").document(blog_Post_Id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        blog_List.remove(position);
//                        usersList.remove(position);
//                    }
//                });
//            }
//        });

        //preview Blog image post

        viewHolder.blog_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(context, ""+blog_List.get(position).getImage_url(), Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(context, PreviewImageActivity.class);
                intent1.putExtra("img_url", blog_List.get(position).getImage_url());
                context.startActivity(intent1);

            }
        });
    }

    @Override
    public int getItemCount() {
        return blog_List.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        View view;
        private TextView textDesc;
        private TextView textBlogDate;
        private TextView textUser_Name;
        private TextView textLikesCount;
        private TextView textCommentCount;
        private ImageView blog_Image;
        private ImageView blog_User_Image;
        private ImageView blog_Like_Btn;
        private ImageView blog_Comment_Btn;
        //private LinearLayout blog_Comment_Layout;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            blog_Like_Btn = itemView.findViewById(R.id.blog_Like_Btn);
            blog_Comment_Btn = itemView.findViewById(R.id.blog_Comment_Icon);
            textCommentCount = itemView.findViewById(R.id.blog_Comment_Num);
        }

        public void getDesc(String text) {
            textDesc = view.findViewById(R.id.blog_Desc);
            textDesc.setText(text);
        }

        public void getPostImage(String downloadUri, String downloadThumbUri) {
            blog_Image = view.findViewById(R.id.blog_Image);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);
            Glide.with(context).setDefaultRequestOptions(requestOptions).load(downloadUri)
                    .thumbnail(Glide.with(context).load(downloadThumbUri)).into(blog_Image);
        }

        public void getPlogDate(String plogDate) {
            textBlogDate = view.findViewById(R.id.blog_Date);
            textBlogDate.setText(plogDate);
        }

        public void getUserName(String username) {
            textUser_Name = view.findViewById(R.id.blog_User_Name);
            textUser_Name.setText(username);
        }

        public void getUserImage(String downloadUserImage) {
            blog_User_Image = view.findViewById(R.id.blog_user_Image);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).setDefaultRequestOptions(requestOptions).load(downloadUserImage).into(blog_User_Image);
        }

        public void getLikesCount(int count) {
            textLikesCount = view.findViewById(R.id.blog_Liked_Num);
            textLikesCount.setText(count + " Likes");
        }
        public void getCommentsCount(int count) {
            textCommentCount = view.findViewById(R.id.blog_Comment_Num);
            textCommentCount.setText(count + " comment");
        }

    }
}
