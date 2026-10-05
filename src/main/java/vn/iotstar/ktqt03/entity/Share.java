package vn.iotstar.ktqt03.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import java.time.LocalDate;

@Entity
@Table(name="Shares", schema="dbo")
public class Share {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ShareId")
    private Integer shareId;

    @Nationalized
    @Column(name="Emails", length=50)
    private String emails;

    @Column(name="SharedDate")
    private LocalDate sharedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="VideoId")
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="Username")
    private User user;

    public Share() {}

    public Integer getShareId() { return shareId; }
    public void setShareId(Integer shareId) { this.shareId = shareId; }

    public String getEmails() { return emails; }
    public void setEmails(String emails) { this.emails = emails; }

    public LocalDate getSharedDate() { return sharedDate; }
    public void setSharedDate(LocalDate sharedDate) { this.sharedDate = sharedDate; }

    public Video getVideo() { return video; }
    public void setVideo(Video video) { this.video = video; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
