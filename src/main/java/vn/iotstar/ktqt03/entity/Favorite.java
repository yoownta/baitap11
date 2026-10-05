package vn.iotstar.ktqt03.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="Favorites", schema="dbo")
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="FavoriteId")
    private Integer favoriteId;

    @Column(name="LikedDate")
    private LocalDate likedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="VideoId")
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="Username")
    private User user;

    public Favorite() {}

    public Integer getFavoriteId() { return favoriteId; }
    public void setFavoriteId(Integer favoriteId) { this.favoriteId = favoriteId; }

    public LocalDate getLikedDate() { return likedDate; }
    public void setLikedDate(LocalDate likedDate) { this.likedDate = likedDate; }

    public Video getVideo() { return video; }
    public void setVideo(Video video) { this.video = video; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
