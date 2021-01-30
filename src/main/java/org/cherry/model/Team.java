package org.cherry.model;

public class Team  implements Comparable<Team>{
    private String id;
    private String name;
    private String rating;
    private String photoId;

    public Team(String id, String name, String rating, String photoId) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.photoId = photoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public Integer getIntRating(){
        return Integer.parseInt(rating);
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int compareTo(Team team) {
        Integer rating = Integer.parseInt(getRating());
        return rating.compareTo(Integer.parseInt(getRating()));
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }
}
