package blogapp.company.com.blogapp.Models;

import com.google.firebase.Timestamp;

import java.util.Date;

public class BlogPost extends BlogPostId{
    private String desc, image_url, thumb_url, user_id;
    public Date timestamp;

    public BlogPost(String desc, String image_url, String thumb_url, String user_id, Date timestamp) {
        this.desc = desc;
        this.image_url = image_url;
        this.thumb_url = thumb_url;
        this.user_id = user_id;
        this.timestamp = timestamp;
    }

    public BlogPost() {
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
