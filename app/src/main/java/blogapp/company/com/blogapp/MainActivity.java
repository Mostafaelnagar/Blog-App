package blogapp.company.com.blogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import blogapp.company.com.blogapp.Fragments.AccountFragment;
import blogapp.company.com.blogapp.Fragments.FragmentHome;
import blogapp.company.com.blogapp.Fragments.NotificationFragment;

public class MainActivity extends AppCompatActivity {
    private Toolbar main_Toolbar;
    private FirebaseAuth mAuth;
    private FloatingActionButton btn_Add_Post;
    private FirebaseFirestore firebaseFirestore;
    private String current_User_Id = "";
    private BottomNavigationView mainBottomNav;
    private FragmentHome fragmentHome;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main_Toolbar = (Toolbar) findViewById(R.id.main_Toolbar);
        setSupportActionBar(main_Toolbar);
        getSupportActionBar().setTitle("Blog App");
        //init firebase
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        if (mAuth.getCurrentUser() != null) {
            btn_Add_Post = (FloatingActionButton) findViewById(R.id.add_post_btn);
            mainBottomNav = findViewById(R.id.mainBottomNav);

            //Fragments
            fragmentHome = new FragmentHome();
            notificationFragment = new NotificationFragment();
            accountFragment = new AccountFragment();
            initializeFragment();
            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_Container);
                    switch (menuItem.getItemId()) {
                        case R.id.bottom_action_home:
                            replaceFragment(fragmentHome,currentFragment);
                            return true;
                        case R.id.bottom_action_notification:
                            replaceFragment(notificationFragment,currentFragment);
                            return true;
                        case R.id.bottom_action_account:
                            replaceFragment(accountFragment,currentFragment);
                            return true;
                        default:
                            return false;
                    }
                }
            });

            btn_Add_Post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, NewPostActivity.class));
                }
            });

        }

    }

    private void initializeFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_Container, fragmentHome);
        transaction.add(R.id.main_Container, notificationFragment);
        transaction.add(R.id.main_Container, accountFragment);
        transaction.hide(notificationFragment);
        transaction.hide(accountFragment);
        transaction.commit();
    }

    private void replaceFragment(Fragment fragment, Fragment currentFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (fragment == fragmentHome) {
            transaction.hide(notificationFragment);
            transaction.hide(accountFragment);
        } if (fragment==notificationFragment){
            transaction.hide(fragmentHome);
            transaction.hide(accountFragment);
        } if (fragment==accountFragment){
            transaction.hide(notificationFragment);
            transaction.hide(fragmentHome);
        }
        transaction.show(fragment);
        transaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //check if the user loged in or not
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            sendToLoging();
        } else {
            //this for check if the user complete his data or not
            //if not complete the data redirect him to setupActivity
            current_User_Id = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(current_User_Id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            startActivity(new Intent(MainActivity.this, SetupActivity.class));
                            finish();
                        }
                    } else {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "error" + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_Logout:
                logOut();
                return true;
            case R.id.action_Setting:
                startActivity(new Intent(this, SetupActivity.class));
                finish();
                return true;
            default:
                return false;
        }
    }

    private void logOut() {
        mAuth.signOut();
        sendToLoging();
    }

    private void sendToLoging() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }


}
