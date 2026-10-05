package vn.iotstar.ktqt03.dao;

import jakarta.persistence.EntityManager;
import vn.iotstar.ktqt03.entity.User;

public class UserDao {
    public User find(EntityManager em, String username) {
        return em.find(User.class, username);
    }

    public long existsEmail(EntityManager em, String email) {
        return em.createQuery("select count(u) from AppUser u where lower(u.email)=:email", Long.class)
                 .setParameter("email", email.toLowerCase())
                 .getSingleResult();
    }

    public void insert(EntityManager em, User user) {
        em.persist(user);
    }
}
