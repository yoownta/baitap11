package vn.iotstar.ktqt03.dto;

import java.util.Map;

public class CategorySection {
    private Integer categoryId;
    private String categoryName;
    private Page<VideoCard> videoPage;
    private Map<Integer, String> pageUrls;
    private String firstUrl;
    private String previousUrl;
    private String nextUrl;
    private String lastUrl;

    public CategorySection() {}

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Page<VideoCard> getVideoPage() { return videoPage; }
    public void setVideoPage(Page<VideoCard> videoPage) { this.videoPage = videoPage; }

    public Map<Integer, String> getPageUrls() { return pageUrls; }
    public void setPageUrls(Map<Integer, String> pageUrls) { this.pageUrls = pageUrls; }

    public String getFirstUrl() { return firstUrl; }
    public void setFirstUrl(String firstUrl) { this.firstUrl = firstUrl; }

    public String getPreviousUrl() { return previousUrl; }
    public void setPreviousUrl(String previousUrl) { this.previousUrl = previousUrl; }

    public String getNextUrl() { return nextUrl; }
    public void setNextUrl(String nextUrl) { this.nextUrl = nextUrl; }

    public String getLastUrl() { return lastUrl; }
    public void setLastUrl(String lastUrl) { this.lastUrl = lastUrl; }
}
