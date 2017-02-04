package com.mnouswen.server;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

/**
 * Created on 2/1/2017.
 */
public class PersistenceManager {
    public static PersistenceManager INSTANCE = new PersistenceManager();
    private EntityManagerFactory emFactory;
    private PersistenceManager() {
        // "jpa-example" was the value of the name attribute of the
        // persistence-unit element.
        emFactory = Persistence.createEntityManagerFactory("wildfly-example");
    }
    public EntityManager getEntityManager() {
        EntityManager em =  emFactory.createEntityManager();
        em.setFlushMode(FlushModeType.COMMIT);
        return em;
    }
    public void close() {
        emFactory.close();
    }
}
