package vn.iotstar.ktqt03.util;

import jakarta.persistence.*;
import java.util.Map;
import java.util.function.Function;

public final class Jpa {
    private static EntityManagerFactory emf;
    private Jpa() {}
    public static String requiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank())
            throw new IllegalStateException("Thiếu biến môi trường " + key);
        return value;
    }
    public static void init() {
        emf = Persistence.createEntityManagerFactory("ktqt03PU", Map.of(
            "jakarta.persistence.jdbc.url", requiredEnv("KTQT_DB_URL"),
            "jakarta.persistence.jdbc.user", requiredEnv("KTQT_DB_USER"),
            "jakarta.persistence.jdbc.password", requiredEnv("KTQT_DB_PASSWORD")
        ));
    }
    public static <T> T read(Function<EntityManager,T> work) {
        EntityManager em = emf.createEntityManager();
        try { return work.apply(em); }
        finally { em.close(); }
    }
    public static <T> T tx(Function<EntityManager,T> work) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = work.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally { em.close(); }
    }
    public static void close() {
        if (emf != null && emf.isOpen()) emf.close();
    }
}
