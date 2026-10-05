package vn.iotstar.ktqt03.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import vn.iotstar.ktqt03.dao.CategoryDao;
import vn.iotstar.ktqt03.dao.StatisticDao;
import vn.iotstar.ktqt03.dao.VideoDao;
import vn.iotstar.ktqt03.dto.CategorySection;
import vn.iotstar.ktqt03.dto.Page;
import vn.iotstar.ktqt03.dto.VideoCard;
import vn.iotstar.ktqt03.entity.Category;
import vn.iotstar.ktqt03.entity.Video;
import vn.iotstar.ktqt03.util.Jpa;
import vn.iotstar.ktqt03.util.Validation;
import vn.iotstar.ktqt03.exception.NotFoundException;
import vn.iotstar.ktqt03.exception.ValidationException;

public class VideoService {
    private final VideoDao videoDao = new VideoDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final StatisticDao statisticDao = new StatisticDao();

    public Page<VideoCard> adminPage(int requestedPage) {
        return Jpa.read(em -> {
            int size = 6;
            long total = videoDao.countAll(em);
            int totalPages = (int) Math.max(1L, (total + size - 1) / size);
            int page = Math.max(1, Math.min(requestedPage, totalPages));
            int offset = (page - 1) * size;

            List<Video> videos = videoDao.findPage(em, offset, size);
            List<VideoCard> items = new ArrayList<>();
            for (Video v : videos) {
                items.add(mapToCard(v, 0, 0));
            }

            Page<VideoCard> p = new Page<>();
            p.setItems(items);
            p.setPage(page);
            p.setPageSize(size);
            p.setTotalPages(totalPages);
            p.setTotalItems(total);
            return p;
        });
    }

    public List<Category> listCategories() {
        return Jpa.read(em -> categoryDao.findAll(em));
    }

    public String create(VideoCard form) {
        Validation.video(form);
        return Jpa.tx(em -> {
            Category category = categoryDao.find(em, form.getCategoryId());
            if (category == null) throw new ValidationException("Category không tồn tại");

            Video video = new Video();
            video.setVideoId(videoDao.nextId(em));
            video.setTitle(form.getTitle());
            video.setPoster(form.getPoster());
            video.setViews(form.getViews());
            video.setDescription(form.getDescription());
            video.setActive(form.isActive());
            video.setCategory(category);

            videoDao.insert(em, video);
            return video.getVideoId();
        });
    }

    public void update(String id, VideoCard form) {
        Validation.id(id);
        Validation.video(form);
        Jpa.tx(em -> {
            Video video = videoDao.findDetail(em, id);
            if (video == null) throw new NotFoundException("Không tìm thấy video");

            Category category = categoryDao.find(em, form.getCategoryId());
            if (category == null) throw new ValidationException("Category không tồn tại");

            video.setTitle(form.getTitle());
            video.setPoster(form.getPoster());
            video.setViews(form.getViews());
            video.setDescription(form.getDescription());
            video.setActive(form.isActive());
            video.setCategory(category);
            return null;
        });
    }

    public void delete(String id) {
        Validation.id(id);
        Jpa.tx(em -> {
            Video video = videoDao.findDetail(em, id);
            if (video == null) throw new NotFoundException("Không tìm thấy video");
            videoDao.deleteRelations(em, id);
            videoDao.delete(em, video);
            return null;
        });
    }

    public VideoCard detail(String id) {
        Validation.id(id);
        return Jpa.read(em -> {
            Video video = videoDao.findDetail(em, id);
            if (video == null) return null;

            long likes = statisticDao.countLikes(em, id);
            long shares = statisticDao.countShares(em, id);
            return mapToCard(video, likes, shares);
        });
    }

    public List<CategorySection> homePages(Map<Integer, Integer> requestedPages) {
        return Jpa.read(em -> {
            List<Category> categories = categoryDao.findAll(em);
            List<CategorySection> sections = new ArrayList<>();
            for (Category c : categories) {
                int size = 3;
                long total = videoDao.countByCategory(em, c.getCategoryId());
                int totalPages = (int) Math.max(1L, (total + size - 1) / size);
                int page = requestedPages.getOrDefault(c.getCategoryId(), 1);
                page = Math.max(1, Math.min(page, totalPages));
                int offset = (page - 1) * size;

                List<Video> videos = videoDao.findByCategory(em, c.getCategoryId(), offset, size);
                List<VideoCard> items = new ArrayList<>();
                for (Video v : videos) {
                    long likes = statisticDao.countLikes(em, v.getVideoId());
                    long shares = statisticDao.countShares(em, v.getVideoId());
                    items.add(mapToCard(v, likes, shares));
                }

                Page<VideoCard> p = new Page<>();
                p.setItems(items);
                p.setPage(page);
                p.setPageSize(size);
                p.setTotalPages(totalPages);
                p.setTotalItems(total);

                CategorySection sec = new CategorySection();
                sec.setCategoryId(c.getCategoryId());
                sec.setCategoryName(c.getCategoryName());
                sec.setVideoPage(p);
                sections.add(sec);
            }

            return sections;
        });
    }

    private VideoCard mapToCard(Video v, long likeCount, long shareCount) {
        VideoCard card = new VideoCard();
        card.setVideoId(v.getVideoId());
        card.setTitle(v.getTitle());
        try { card.setPoster(Validation.poster(v.getPoster())); }
        catch (ValidationException ignored) { card.setPoster(Validation.DEFAULT_POSTER); }
        card.setDescription(v.getDescription());
        if (v.getCategory() != null) {
            card.setCategoryName(v.getCategory().getCategoryName());
            card.setCategoryId(v.getCategory().getCategoryId());
        } else {
            card.setCategoryName("Chưa phân loại");
        }
        card.setViews(v.getViews() == null ? 0 : v.getViews());
        card.setActive(Boolean.TRUE.equals(v.getActive()));
        card.setLikeCount(likeCount);
        card.setShareCount(shareCount);
        return card;
    }
}
