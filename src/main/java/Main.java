import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.log4j.Logger;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.*;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    static Logger LOG = Logger.getLogger(Main.class);
    private static EntityManagerFactory entityManagerFactory;

    public static void main(String... args) throws Exception {
        Log.setLog(new Slf4jLog());
        entityManagerFactory = Persistence.createEntityManagerFactory("MysqlPersistenceUnit");
        try {
            updateDB();
            addUser();
            findUser();
            addUser();
            startServer();
        } finally {
            entityManagerFactory.close();
        }
    }

    private static void startServer() throws Exception {
        Server server = new Server(8080);
        server.setDumpAfterStart(false);
        server.setDumpBeforeStop(false);
        // Setup JMX
        MBeanContainer mbContainer = new MBeanContainer(
                ManagementFactory.getPlatformMBeanServer());
        server.addBean(mbContainer);

        // Export the loggers as MBeans.
        server.addBean(Log.getLog());

        server.start();
        //server.dumpStdErr();
        server.join();
    }

    private static void findUser() {
        EntityManager entityManager;
        entityManager = entityManagerFactory.createEntityManager();
        try {
            Cache cache = entityManagerFactory.getCache();
            boolean exists = cache.contains(User.class, "admin");
            if (exists) {
                LOG.info("in cache");
            } else {
                LOG.info("not in cache");

            }
            User admin = entityManager.find(User.class, "admin");
            LOG.info("found: " + admin);
        } finally {
            entityManager.close();
        }
    }

    private static void addUser() {
        EntityManager entityManager;
        entityManager = entityManagerFactory.createEntityManager();
        try {
            EntityTransaction userTransaction = entityManager.getTransaction();
            userTransaction.begin();
            User instance = new User();
            instance.setUserName("admin");
            String hashed = BCrypt.hashpw("password", BCrypt.gensalt(12));
            instance.setPassword(hashed);
            entityManager.merge(instance);
            userTransaction.commit();
        } finally {
            entityManager.close();
        }
    }


    private static void updateDB() throws LiquibaseException, SQLException {
        EntityManager entityManager;
        entityManager = entityManagerFactory.createEntityManager();
        try {
            Session session = (Session) entityManager.getDelegate();
            SessionFactoryImplementor sfi = (SessionFactoryImplementor) session.getSessionFactory();
            Connection connection = sfi.getJdbcServices()
                    .getBootstrapJdbcConnectionAccess()
                    .obtainConnection();

            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new liquibase.Liquibase("liquibase/migrate.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());
        } finally {
            entityManager.close();
        }
    }
}
