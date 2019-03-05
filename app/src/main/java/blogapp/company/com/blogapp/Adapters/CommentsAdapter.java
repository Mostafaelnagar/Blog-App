package blogapp.company.com.blogapp.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import blogapp.company.com.blogapp.Models.BlogPost;
import blogapp.company.com.blogapp.Models.Comments;
import blogapp.company.com.blogapp.Models.Users_Comments;
import blogapp.company.com.blogapp.R;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.viewHolder> {
    List<Comments> comment_List;
    List<Users_Comments> users_commentsList;
    public Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public CommentsAdapter(List<Comments> comment_List, List<Users_Comments> users_commentsList) {
        this.comment_List = comment_List;
        this.users_commentsList = users_commentsList;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment_list_item, viewGroup, false);

        context = viewGroup.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new CommentsAdapter.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final viewHolder viewHolder, int position) {
        viewHolder.setIsRecyclable(false);
        String comment = comment_List.get(position).getMessage();
        viewHolder.setComment_message(comment);
        //user Data

        String name = users_commentsList.get(position).getName();
        String image = users_commentsList.get(position).getImage();
        viewHolder.getUserImage(image);
        viewHolder.getUserName(name);
        viewHolder.comment_Container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });


    }

    @Override
    public int getItemCount() {
        if (comment_List != null) {
            return comment_List.size();
        } else {
            return 0;
        }

    }

    public class viewHolder extends RecyclerView.ViewHolder {
        private TextView comment_message, comment_User_Name;
        private View mView;
        private ImageView comment_User_Image;
        private ConstraintLayout comment_Container;

        public viewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            comment_Container = itemView.findViewById(R.id.comment_Container);
        }

        public void setComment_message(String message) {

            comment_message = mView.findViewById(R.id.comment_message);
            comment_message.setText(message);

        }

        public void getUserName(String username) {
            comment_User_Name = mView.findViewById(R.id.comment_username);
            comment_User_Name.setText(username);
        }

        public void getUserImage(String downloadUserImage) {
            comment_User_Image = mView.findViewById(R.id.comment_image);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).setDefaultRequestOptions(requestOptions).load(downloadUserImage).into(comment_User_Image);
        }
    }
}
