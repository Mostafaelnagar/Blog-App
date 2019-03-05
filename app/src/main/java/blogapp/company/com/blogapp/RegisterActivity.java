package blogapp.company.com.blogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText edit_Email, edit_Password, edit_Confirm_Password;
    private Button btn_register, bn_Have_Account;
    private ProgressBar reg_Progress;
    private FirebaseAuth mAuht;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //init views
        edit_Email = (EditText) findViewById(R.id.reg_Email);
        edit_Password = (EditText) findViewById(R.id.reg_Password);
        edit_Confirm_Password = (EditText) findViewById(R.id.reg_Confirm_Password);
        btn_register = (Button) findViewById(R.id.reg_Btn);
        bn_Have_Account = (Button) findViewById(R.id.login_Btn);
        reg_Progress = (ProgressBar) findViewById(R.id.reg_Progress);
        //init firebase
        mAuht = FirebaseAuth.getInstance();
        //reg btn action
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String reg_Email = edit_Email.getText().toString();
                String reg_Password = edit_Password.getText().toString();
                String reg_Confirm_Password = edit_Confirm_Password.getText().toString();
                if (!TextUtils.isEmpty(reg_Email) && !TextUtils.isEmpty(reg_Password) && !TextUtils.isEmpty(reg_Confirm_Password)) {
                    if (reg_Password.equals(reg_Confirm_Password)) {
                        reg_Progress.setVisibility(View.VISIBLE);
                        mAuht.createUserWithEmailAndPassword(reg_Email, reg_Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(RegisterActivity.this, SetupActivity.class));
                                    finish();
                                } else {
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Password doesn't match", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        //have an account button action
        bn_Have_Account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = mAuht.getCurrentUser();
        if (firebaseUser != null) {
            sendToMain();
        }
    }

    private void sendToMain() {
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }
}
