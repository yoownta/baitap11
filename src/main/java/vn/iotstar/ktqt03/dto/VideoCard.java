package vn.iotstar.ktqt03.dto;

public class VideoCard {
    private String videoId;
    private String title;
    private String poster;
    private String description;
    private String categoryName;
    private Integer categoryId;
    private int views;
    private boolean active;
    private long likeCount;
    private long shareCount;

    public VideoCard() {}

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public long getLikeCount() { return likeCount; }
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }

    public long getShareCount() { return shareCount; }
    public void setShareCount(long shareCount) { this.shareCount = shareCount; }

    public boolean isVideoFile() {
        return poster != null && poster.toLowerCase().endsWith(".mp4");
    }
}
