/* Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. */
package oracle.ucp.hibernate.sample;

import oracle.ucp.UniversalConnectionPoolException;
import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.admin.UniversalConnectionPoolManagerImpl;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

public class UCPConnectionProvider
        extends DriverManagerConnectionProviderImpl {
    private static final Logger logger =
            Logger.getLogger(UCPConnectionProvider.class.getCanonicalName());
    private static final String URL = "hibernate.ucp.url";
    private static final String USER = "hibernate.ucp.user";
    private static final String PASSWORD = "hibernate.ucp.password";
    private static final String CONN_FACTORY = "hibernate.ucp.connectionFactoryClassName";
    private static final String POOL_NAME = "hibernate.ucp.connectionPoolName";
    private static final String MAX_POOL_SIZE = "hibernate.ucp.maxPoolSize";
    private static final String MIN_POOL_SIZE = "hibernate.ucp.minPoolSize";
    private static final String INITIAL_POOL_SIZE = "hibernate.ucp.initialPoolSize";
    private static final String FAN_ENABLED = "hibernate.ucp.fastConnectionFailoverEnabled";
    private static final String ONS_CONFIG = "hibernate.ucp.onsConfiguration";
    private static final String CONN_VALIDATE = "hibernate.ucp.validateConnectionOnBorrow";
    private PoolDataSource pds;

    public UCPConnectionProvider() {
        super();
        try {
            pds = PoolDataSourceFactory.getPoolDataSource();
            logger.finest("PoolDataSource initialized: " + pds);
        } catch (Exception exc) {
            logger.warning(getStackTraceString(exc));
        }
    }

    public void configure(Map props) throws HibernateException {
        if (pds == null)
            throw new HibernateException("PoolDataSource was not initialized.");
        if (props == null)
            throw new HibernateException("Null configuration properties passed in.");
        try {
            logger.finest("Passed in properties: " + props);
            String tempval = (String) props.get(CONN_FACTORY);
            if (tempval != null)
                pds.setConnectionFactoryClassName(tempval);
            tempval = (String) props.get(URL);
            if (tempval != null)
                pds.setURL(tempval);
            tempval = (String) props.get(USER);
            if (tempval != null)
                pds.setUser(tempval);
            tempval = (String) props.get(PASSWORD);
            if (tempval != null)
                pds.setPassword(tempval);
            tempval = (String) props.get(POOL_NAME);
            if (tempval != null)
                pds.setConnectionPoolName(tempval);
            tempval = (String) props.get(MAX_POOL_SIZE);
            if (tempval != null)
                pds.setMaxPoolSize(Integer.parseInt(tempval));
            tempval = (String) props.get(MIN_POOL_SIZE);
            if (tempval != null)
                pds.setMinPoolSize(Integer.parseInt(tempval));
            tempval = (String) props.get(INITIAL_POOL_SIZE);
            if (tempval != null)
                pds.setInitialPoolSize(Integer.parseInt(tempval));
            tempval = (String) props.get(FAN_ENABLED);
            if (tempval != null)
                pds.setFastConnectionFailoverEnabled(Boolean.parseBoolean(tempval));
            tempval = (String) props.get(ONS_CONFIG);
            if (tempval != null)
                pds.setONSConfiguration(tempval);
            tempval = (String) props.get(CONN_VALIDATE);
            if (tempval != null)
                pds.setValidateConnectionOnBorrow(Boolean.parseBoolean(tempval));
        } catch (SQLException sqlexc) {
            logger.warning(getStackTraceString(sqlexc));
        }
    }

    public Connection getConnection() throws SQLException {
        final Connection conn = pds.getConnection();
        logger.finest("Got connection " + conn + " from " + pds +
                ", number of available connections = " +
                pds.getAvailableConnectionsCount() +
                ", borrowed connections = " +
                pds.getBorrowedConnectionsCount());
        return conn;
    }

    public void closeConnection(Connection conn) throws SQLException {
        conn.close();
        logger.finest("Closed connection " + conn + " from " + pds +
                ", number of available connections = " +
                pds.getAvailableConnectionsCount() +
                ", borrowed connections = " +
                pds.getBorrowedConnectionsCount());
    }

    public void close() {
        try {
            final UniversalConnectionPoolManager mgr =
                    UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();
            mgr.destroyConnectionPool(pds.getConnectionPoolName());
            logger.finest("Closed PoolDataSource " + pds);
        } catch (UniversalConnectionPoolException exc) {
            logger.warning(getStackTraceString(exc));
        }
    }

    public boolean supportsAggressiveRelease() {
        return true;
    }

    public boolean isUnwrappableAs(Class cls) {
        return false;
    }

    public <T> T unwrap(Class<T> cls) {
        return null;
    }

    private String getStackTraceString(Throwable exc) {
        final Writer stackTraceWriter = new StringWriter(1024);
        final PrintWriter pw = new PrintWriter(stackTraceWriter);
        exc.printStackTrace(pw);
        return stackTraceWriter.toString();
    }

    @Override
    public void stop() {
        //close();
    }
}