package blogapp.company.com.blogapp.Models;

public class Users_Comments {
    private String image,name;

    public Users_Comments() {
    }

    public Users_Comments(String image, String name) {
        this.image = image;
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
