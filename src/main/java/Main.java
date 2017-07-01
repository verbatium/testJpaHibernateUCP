import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String... args) throws IOException, LiquibaseException, SQLException {
        updateDB();
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("MysqlPersistenceUnit");
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction userTransaction = em.getTransaction();
        userTransaction.begin();
        User instance = new User();
        instance.setUserName("valerik");
        instance.setPassword("password");
        em.merge(instance);
        userTransaction.commit();
        em.close();
        entityManagerFactory.close();
    }

    public static void updateDB() throws LiquibaseException, SQLException {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("MysqlPersistenceUnit");
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        Session session = (Session) entityManager.getDelegate();
        SessionFactoryImplementor sfi = (SessionFactoryImplementor) session.getSessionFactory();
        Connection connection = sfi.getJdbcServices()
                .getBootstrapJdbcConnectionAccess()
                .obtainConnection();

        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new liquibase.Liquibase("liquibase/migrate.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update(new Contexts(), new LabelExpression());
    }
}
