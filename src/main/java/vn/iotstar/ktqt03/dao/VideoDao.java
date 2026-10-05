package vn.iotstar.ktqt03.dao;

import jakarta.persistence.EntityManager;
import java.util.List;
import vn.iotstar.ktqt03.entity.Video;

public class VideoDao {
    public long countAll(EntityManager em) {
        return em.createQuery("select count(v) from Video v", Long.class).getSingleResult();
    }

    public List<Video> findPage(EntityManager em, int offset, int size) {
        return em.createQuery("select v from Video v left join fetch v.category order by v.title, v.videoId", Video.class)
                 .setFirstResult(offset)
                 .setMaxResults(size)
                 .getResultList();
    }

    public long countByCategory(EntityManager em, Integer categoryId) {
        return em.createQuery("select count(v) from Video v where v.category.categoryId=:cid", Long.class)
                 .setParameter("cid", categoryId)
                 .getSingleResult();
    }

    public List<Video> findByCategory(EntityManager em, Integer categoryId, int offset, int size) {
        return em.createQuery("select v from Video v where v.category.categoryId=:cid order by v.title, v.videoId", Video.class)
                 .setParameter("cid", categoryId)
                 .setFirstResult(offset)
                 .setMaxResults(size)
                 .getResultList();
    }

    public Video findDetail(EntityManager em, String videoId) {
        List<Video> list = em.createQuery("select v from Video v left join fetch v.category where v.videoId=:vid", Video.class)
                             .setParameter("vid", videoId)
                             .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public String nextId(EntityManager em) {
        Number value = (Number) em.createNativeQuery("SELECT NEXT VALUE FOR dbo.VideoIdSeq").getSingleResult();
        return Long.toString(value.longValue());
    }

    public void insert(EntityManager em, Video video) {
        em.persist(video);
    }

    public void deleteRelations(EntityManager em, String videoId) {
        em.createQuery("delete from Favorite f where f.video.videoId=:id")
          .setParameter("id", videoId).executeUpdate();
        em.createQuery("delete from Share s where s.video.videoId=:id")
          .setParameter("id", videoId).executeUpdate();
    }

    public void delete(EntityManager em, Video video) {
        em.remove(video);
    }
}
