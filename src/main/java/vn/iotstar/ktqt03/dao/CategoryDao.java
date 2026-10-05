package vn.iotstar.ktqt03.dao;

import jakarta.persistence.EntityManager;
import java.util.List;
import vn.iotstar.ktqt03.entity.Category;

public class CategoryDao {
    public List<Category> findAll(EntityManager em) {
        return em.createQuery("select c from Category c order by c.categoryId", Category.class).getResultList();
    }

    public Category find(EntityManager em, Integer id) {
        return em.find(Category.class, id);
    }
}
