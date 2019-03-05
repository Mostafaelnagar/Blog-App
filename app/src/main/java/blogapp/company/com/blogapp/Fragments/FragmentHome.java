package blogapp.company.com.blogapp.Fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import blogapp.company.com.blogapp.Adapters.BlogRecyeclerAdapter;
import blogapp.company.com.blogapp.Models.BlogPost;
import blogapp.company.com.blogapp.Models.Users;
import blogapp.company.com.blogapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHome extends Fragment {

    private RecyclerView blog_List_View;
    private List<BlogPost> blog_List;
    private FirebaseFirestore firebaseFirestore;
    private BlogRecyeclerAdapter blogRecyeclerAdapter;
    private DocumentSnapshot lastVisable;
    private boolean isFirstPageFirstLoad = true;
    private FirebaseAuth firebaseAuth;
    private List<Users> usersList;

    public FragmentHome() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_home, container, false);
        usersList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        blog_List_View = view.findViewById(R.id.blog_List_View);
        blog_List = new ArrayList<>();
        firebaseFirestore = FirebaseFirestore.getInstance();
        blog_List_View.setLayoutManager(new LinearLayoutManager(container.getContext()));
        blog_List_View.setHasFixedSize(true);
        blogRecyeclerAdapter = new BlogRecyeclerAdapter(blog_List,usersList);
        blog_List_View.setAdapter(blogRecyeclerAdapter);
        if (firebaseAuth.getCurrentUser() != null) {
            blog_List_View.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if (reachedBottom) {
                        String desc = lastVisable.getString("desc");
                        Toast.makeText(container.getContext(), "reached " + desc, Toast.LENGTH_SHORT).show();
                        loadMorePosts();
                    }
                }
            });
            //retrieve data by date
            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);
            //retrieve data using SnapshotListener
            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        if (isFirstPageFirstLoad) {
                            lastVisable = queryDocumentSnapshots.getDocuments()
                                    .get(queryDocumentSnapshots.size() - 1);
                            blog_List.clear();
                            usersList.clear();
                        }
                        //for loop to loop on the data
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String blog_Id = doc.getDocument().getId();
                                final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blog_Id);
                                String user_Id = doc.getDocument().getString("user_id");
                                firebaseFirestore.collection("Users").document(user_Id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().exists()) {
                                                Users users = task.getResult().toObject(Users.class);

                                                if (isFirstPageFirstLoad) {
                                                    usersList.add(users);
                                                    blog_List.add(blogPost);
                                                } else {
                                                    usersList.add(0, users);
                                                    blog_List.add(0, blogPost);// put the new post at the top of list
                                                }
                                                blogRecyeclerAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    }
                                });


                            }
                        }
                        isFirstPageFirstLoad = false;
                    }
                }
            });
        }
        return view;
    }

    public void loadMorePosts() {

        Query firstQuery = firebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING).startAfter(lastVisable).limit(3);

        firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    lastVisable = queryDocumentSnapshots.getDocuments()
                            .get(queryDocumentSnapshots.size() - 1);
                    //for loop to loop on the data
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String blog_Id = doc.getDocument().getId();
                            final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blog_Id);
                            String user_Id = doc.getDocument().getString("user_id");
                            firebaseFirestore.collection("Users").document(user_Id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().exists()) {
                                            Users users = task.getResult().toObject(Users.class);
                                                usersList.add(users);
                                                blog_List.add(blogPost);
                                            blogRecyeclerAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

}
