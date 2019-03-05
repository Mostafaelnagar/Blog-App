package blogapp.company.com.blogapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    Toolbar add_New_Post;
    private ImageView img_New_Post;
    private EditText edit_Post_Desc;
    private Button btn_New_Post;
    private ProgressBar new_Post_Progress;
    private Uri postImageURI = null;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String downloadImageUrl = "";
    private String current_User_Id = "";
    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        add_New_Post = findViewById(R.id.new_Post_Toolbar);
        setSupportActionBar(add_New_Post);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        img_New_Post = findViewById(R.id.new_Post_Image);
        edit_Post_Desc = findViewById(R.id.post_text_desc);
        btn_New_Post = findViewById(R.id.btn_Post);
        new_Post_Progress = findViewById(R.id.new_Post_Progress);
        //init firbase
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        //get user Id
        current_User_Id = firebaseAuth.getCurrentUser().getUid();
        img_New_Post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .setMinCropResultSize(512, 512)
                        .start(NewPostActivity.this);
            }
        });

        btn_New_Post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String desc = edit_Post_Desc.getText().toString();
                if (!TextUtils.isEmpty(desc) && postImageURI != null) {
                    new_Post_Progress.setVisibility(View.VISIBLE);
                    //create file called post images and store name of image(randomName.jpg) on it
                    final String randomName = UUID.randomUUID().toString();
                    final StorageReference image_Path = storageReference.child("post_images/original").child(randomName + ".jpg");
                    //store the image itself in the file
                    final UploadTask uploadTask = image_Path.putFile(postImageURI);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String message = e.toString();
                            Toast.makeText(NewPostActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            new_Post_Progress.setVisibility(View.INVISIBLE);
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }

                                    downloadImageUrl = image_Path.getDownloadUrl().toString();
                                    return image_Path.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {

                                        uploadNewPost(task, desc, randomName);

                                    } else {
                                        String errorMessage = task.getException().getMessage();
                                        Toast.makeText(NewPostActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                        new_Post_Progress.setVisibility(View.INVISIBLE);
                                    }

                                }
                            });
                        }
                    });
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageURI = result.getUri();
                img_New_Post.setImageURI(postImageURI); // set the image uri that getting from crop activity

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void uploadNewPost(Task<Uri> task, final String desc, String randomName) {
        if (task != null) {
            downloadImageUrl = task.getResult().toString();
        } else {
            downloadImageUrl = postImageURI.toString();
        }
        File actualImageFile = new File(postImageURI.getPath());
        try {
            compressedImageFile = new Compressor(NewPostActivity.this)
                    .setMaxHeight(720)
                    .setMaxWidth(720)
                    .setQuality(50)
                    .compressToBitmap(actualImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] thumbData = baos.toByteArray();
        final StorageReference thumbPath = storageReference.child("post_images/thumbs").child(randomName + ".jpg");
        final UploadTask uploadTask = thumbPath.putBytes(thumbData);
        Task<Uri> urlTask=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return thumbPath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    //create hashMap to store name and image of user
                    Map<String, Object> post_Map = new HashMap<>();
                    post_Map.put("image_url", downloadImageUrl);
                    post_Map.put("thumb_url", downloadUri.toString());
                    post_Map.put("desc", desc);
                    post_Map.put("user_id", current_User_Id);
                    post_Map.put("timestamp", FieldValue.serverTimestamp());
                    //create collection (users table ) and insert user id and Map
                    firebaseFirestore.collection("Posts").add(post_Map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(NewPostActivity.this, "Post Uploaded Successfully", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(NewPostActivity.this, MainActivity.class));
                                finish();
                            } else {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                            new_Post_Progress.setVisibility(View.GONE);
                        }
                    });
                } else {
                    // Handle failures
                    // ...
                }
            }
        });


    }
}
