package org.cherry.model;

import java.time.LocalDate;
import java.util.Date;

public class Schedule {
    private String id;
    private String photoId;
    private Date date;

    public Schedule(String id, String photoId, Date date) {
        this.id = id;
        this.photoId = photoId;
        this.date = date;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
