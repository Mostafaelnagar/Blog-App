package blogapp.company.com.blogapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class SetupActivity extends AppCompatActivity {
    private CircleImageView circleImageView;
    private Uri mainImageURI = null;
    private EditText edit_Setup_Name;
    private Button setup_Btn;
    private ProgressBar setup_Progress;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth; // for getting userID and store image and data for him
    private FirebaseFirestore firebaseFirestore;
    private String downloadImageUrl = "";
    private String user_Id = "";
    private boolean isChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.setup_Toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Setup");
        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user_Id = firebaseAuth.getCurrentUser().getUid();//get id for current user
        circleImageView = (CircleImageView) findViewById(R.id.setup_Image);
        edit_Setup_Name = (EditText) findViewById(R.id.setup_Name);
        setup_Btn = (Button) findViewById(R.id.setup_btn);
        setup_Progress = (ProgressBar) findViewById(R.id.setup_Progress);
        setup_Btn.setEnabled(false);

        //get the user data from users collection
        firebaseFirestore.collection("Users").document(user_Id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    //check if the user has data on this collection or not
                    if (task.getResult().exists()) {
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        mainImageURI = Uri.parse(image);
                        edit_Setup_Name.setText(name);
                        //set Default image when true image is loading
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.placeholder(R.drawable.profile_placeholder);
                        requestOptions.circleCrop();
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(requestOptions).load(image).into(circleImageView);
                    }
                    setup_Progress.setVisibility(View.GONE);
                    setup_Btn.setEnabled(true);
                }
            }
        });

        setup_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String name = edit_Setup_Name.getText().toString();
                if (!TextUtils.isEmpty(name) && mainImageURI != null) {
                    setup_Progress.setVisibility(View.VISIBLE);
                    if (isChange) {


                        //create file called profile images and store name of image(user_id.jpg) on it
                        final StorageReference image_Path = storageReference.child("profile_images").child(user_Id + ".jpg");
                        //store the image itself in the file
                        final UploadTask uploadTask = image_Path.putFile(mainImageURI);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                String message = e.toString();
                                Toast.makeText(SetupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                setup_Progress.setVisibility(View.INVISIBLE);
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

                                            uploadUserData(task, name);

                                        } else {
                                            String errorMessage = task.getException().getMessage();
                                            Toast.makeText(SetupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                            setup_Progress.setVisibility(View.INVISIBLE);
                                        }

                                    }
                                });
                            }
                        });
                    } else {
                        uploadUserData(null, name);
                    }
                }
            }
        });
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
// start picker to get image for cropping and then use the image in cropping activity
                        bringImagePicker();
                    }
                } else {
                    bringImagePicker();

                }
            }
        });

    }

    private void uploadUserData(Task<Uri> task, String name) {
        if (task != null) {
            downloadImageUrl = task.getResult().toString();
        } else {
            downloadImageUrl = mainImageURI.toString();
        }
        //create hashMap to store name and image of user
        Map<String, String> user_Map = new HashMap<>();
        user_Map.put("name", name);
        user_Map.put("image", downloadImageUrl);
        //create collection (users table ) and insert user id and Map
        firebaseFirestore.collection("Users").document(user_Id).set(user_Map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SetupActivity.this, "Successfully Uploaded", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SetupActivity.this, MainActivity.class));
                    finish();
                } else {
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, errorMessage, Toast.LENGTH_LONG).show();

                }
                setup_Progress.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void bringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)

                .start(SetupActivity.this);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageURI = result.getUri();
                circleImageView.setImageURI(mainImageURI); // set the image uri that getting from crop activity
                isChange = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
