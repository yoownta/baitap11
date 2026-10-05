package vn.iotstar.ktqt03.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name="Videos", schema="dbo")
public class Video {
    @Id
    @Nationalized
    @Column(name="VideoId", length=50)
    private String videoId;

    @Nationalized
    @Column(name="Title", length=200)
    private String title;

    @Nationalized
    @Column(name="Poster", length=50)
    private String poster;

    @Column(name="Views")
    private Integer views;

    @Nationalized
    @Column(name="Description", length=500)
    private String description;

    @Column(name="Active")
    private Boolean active;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CategoryId")
    private Category category;

    public Video() {}

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public Integer getViews() { return views; }
    public void setViews(Integer views) { this.views = views; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}
