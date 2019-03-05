package blogapp.company.com.blogapp.Common;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import blogapp.company.com.blogapp.R;

public class PreviewImageActivity extends AppCompatActivity {
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image);
        imageView = findViewById(R.id.img_Preview);
        String img_url = getIntent().getStringExtra("img_url");
        Glide.with(getBaseContext()).load(img_url).into(imageView);
    }
}
