package vn.iotstar.ktqt03.dao;

import jakarta.persistence.EntityManager;

public class StatisticDao {
    public long countLikes(EntityManager em, String videoId) {
        return em.createQuery("select count(f) from Favorite f where f.video.videoId=:id", Long.class)
                 .setParameter("id", videoId).getSingleResult();
    }

    public long countShares(EntityManager em, String videoId) {
        return em.createQuery("select count(s) from Share s where s.video.videoId=:id", Long.class)
                 .setParameter("id", videoId).getSingleResult();
    }
}
