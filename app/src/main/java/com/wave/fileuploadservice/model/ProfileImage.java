package com.wave.fileuploadservice.model;

import java.io.Serializable;

public class ProfileImage implements Serializable
{
    String image_path;

    String is_profile_image;

    public ProfileImage(){}

    public ProfileImage(String image_path, String is_profile_image) {
        this.image_path = image_path;
        this.is_profile_image = is_profile_image;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public String getIs_profile_image() {
        return is_profile_image;
    }

    public void setIs_profile_image(String is_profile_image) {
        this.is_profile_image = is_profile_image;
    }
}
