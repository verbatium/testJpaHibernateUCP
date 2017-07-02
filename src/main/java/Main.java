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

import javax.persistence.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    private static EntityManagerFactory entityManagerFactory;
    private static EntityManager entityManager;

    public static void main(String... args) throws IOException, LiquibaseException, SQLException {
        entityManagerFactory = Persistence.createEntityManagerFactory("MysqlPersistenceUnit");
        updateDB();
        addUser();
        findUSer();
        addUser();
        entityManagerFactory.close();
    }

    private static void findUSer() {
        entityManager = entityManagerFactory.createEntityManager();
        Cache cache = entityManagerFactory.getCache();
        boolean exists = cache.contains(User.class, "admin");
        if(exists){
            System.out.println("in cache");
        }
        else
        {
            System.out.println("not in cache");

        }

        entityManager.find(User.class,"admin");
        entityManager.close();
    }

    private static void addUser() {
        entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction userTransaction = entityManager.getTransaction();
        userTransaction.begin();
        User instance = new User();
        instance.setUserName("admin");
        instance.setPassword("password");
        entityManager.merge(instance);
        userTransaction.commit();
        entityManager.close();
    }


    private static void updateDB() throws LiquibaseException, SQLException {
        entityManager = entityManagerFactory.createEntityManager();

        Session session = (Session) entityManager.getDelegate();
        SessionFactoryImplementor sfi = (SessionFactoryImplementor) session.getSessionFactory();
        Connection connection = sfi.getJdbcServices()
                .getBootstrapJdbcConnectionAccess()
                .obtainConnection();

        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new liquibase.Liquibase("liquibase/migrate.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update(new Contexts(), new LabelExpression());
        entityManager.close();
    }
}
