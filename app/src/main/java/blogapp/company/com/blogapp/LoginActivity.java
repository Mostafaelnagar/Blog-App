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

public class LoginActivity extends AppCompatActivity {
    private EditText login_Email_Text, login_Password_Text;
    private Button login_Btn, login_Reg_Btn;
    private FirebaseAuth mAuth;
    private ProgressBar login_Progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //init views
        login_Email_Text = (EditText) findViewById(R.id.reg_Email);
        login_Password_Text = (EditText) findViewById(R.id.reg_Password);
        login_Btn = (Button) findViewById(R.id.login_Btn);
        login_Reg_Btn = (Button) findViewById(R.id.login_Reg_Btn);
        login_Progress = (ProgressBar) findViewById(R.id.login_Progress);
        //init firebase
        mAuth = FirebaseAuth.getInstance();

        //login Button action
        login_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String login_Email = login_Email_Text.getText().toString();
                String login_Password = login_Password_Text.getText().toString();
                if (!TextUtils.isEmpty(login_Email) && !TextUtils.isEmpty(login_Password)) {
                    login_Progress.setVisibility(View.VISIBLE);
                    //is used for login with email and password
                    mAuth.signInWithEmailAndPassword(login_Email, login_Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                sendToMain();
                            } else {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                            login_Progress.setVisibility(View.INVISIBLE);

                        }
                    });
                }
            }
        });
        //register button action
        login_Reg_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        //if user already Login send him to MainActivity
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            sendToMain();
        }
    }

    private void sendToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
