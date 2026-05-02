package za.ac.tut.databaseConnection;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionPoolManager {

    private static final int DEFAULT_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 30;
    private static final long IDLE_TIMEOUT_MS = 300000;
    private static final long VALIDATION_INTERVAL_MS = 60000;

    private static volatile ConnectionPoolManager instance;
    private final BlockingQueue<PooledConnection> available;
    private final CopyOnWriteArrayList<PooledConnection> inUse;
    private final AtomicInteger totalCreated = new AtomicInteger(0);
    private final int poolSize;
    private volatile boolean shutdown;
    private Thread validationThread;

    private ConnectionPoolManager(int poolSize) {
        this.poolSize = poolSize;
        this.available = new LinkedBlockingQueue<>(MAX_POOL_SIZE);
        this.inUse = new CopyOnWriteArrayList<>();
        this.shutdown = false;
        startValidationThread();
    }

    public static synchronized ConnectionPoolManager getInstance() {
        if (instance == null) {
            int size = parsePoolSize();
            instance = new ConnectionPoolManager(size);
            System.out.println("Tickify: Connection pool initialized with size " + size);
        }
        return instance;
    }

    public static synchronized void shutdownPool() {
        if (instance != null) {
            instance.shutdown = true;
            if (instance.validationThread != null) {
                instance.validationThread.interrupt();
            }
            instance.drainAndClose();
            System.out.println("Tickify: Connection pool shut down gracefully.");
        }
    }

    public Connection getConnection() throws SQLException {
        if (shutdown) {
            throw new SQLException("Connection pool has been shut down.");
        }

        PooledConnection pooled = available.poll();
        if (pooled != null) {
            if (isConnectionValid(pooled.delegate)) {
                inUse.add(pooled);
                return pooled;
            }
            closeQuietly(pooled.delegate);
        }

        if (totalCreated.get() < MAX_POOL_SIZE) {
            totalCreated.incrementAndGet();
            Connection raw = DatabaseConnection.createNewConnection();
            PooledConnection wrapped = new PooledConnection(raw);
            inUse.add(wrapped);
            return wrapped;
        }

        try {
            pooled = available.poll(30, TimeUnit.SECONDS);
            if (pooled != null) {
                if (isConnectionValid(pooled.delegate)) {
                    inUse.add(pooled);
                    return pooled;
                }
                closeQuietly(pooled.delegate);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for a connection.", e);
        }

        Connection raw = DatabaseConnection.createNewConnection();
        PooledConnection wrapped = new PooledConnection(raw);
        inUse.add(wrapped);
        return wrapped;
    }

    private boolean isConnectionValid(Connection conn) {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    private void returnToPool(PooledConnection pooled) {
        inUse.remove(pooled);
        if (shutdown || !isConnectionValid(pooled.delegate)) {
            closeQuietly(pooled.delegate);
            return;
        }
        try {
            if (pooled.delegate.getAutoCommit() == false) {
                pooled.delegate.rollback();
            }
        } catch (SQLException ignored) {
        }
        available.offer(pooled);
    }

    private void startValidationThread() {
        validationThread = new Thread(() -> {
            while (!shutdown) {
                try {
                    Thread.sleep(VALIDATION_INTERVAL_MS);
                } catch (InterruptedException e) {
                    break;
                }
                validateIdleConnections();
            }
        }, "Tickify-Pool-Validator");
        validationThread.setDaemon(true);
        validationThread.start();
    }

    private void validateIdleConnections() {
        long now = System.currentTimeMillis();
        int excess = totalCreated.get() - poolSize;
        for (int i = 0; i < excess; i++) {
            PooledConnection pooled = available.poll();
            if (pooled == null) {
                break;
            }
            if (System.currentTimeMillis() - pooled.lastReturned > IDLE_TIMEOUT_MS) {
                closeQuietly(pooled.delegate);
                totalCreated.decrementAndGet();
            } else if (!isConnectionValid(pooled.delegate)) {
                closeQuietly(pooled.delegate);
                totalCreated.decrementAndGet();
            } else {
                available.offer(pooled);
            }
        }
    }

    private void drainAndClose() {
        PooledConnection pooled;
        while ((pooled = available.poll()) != null) {
            closeQuietly(pooled.delegate);
        }
        for (PooledConnection pc : inUse) {
            closeQuietly(pc.delegate);
        }
        inUse.clear();
    }

    private static void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    private static int parsePoolSize() {
        String env = System.getenv("TICKIFY_DB_POOL_SIZE");
        if (env != null && !env.trim().isEmpty()) {
            try {
                int parsed = Integer.parseInt(env.trim());
                return Math.max(1, Math.min(parsed, MAX_POOL_SIZE));
            } catch (NumberFormatException ignored) {
            }
        }
        String prop = System.getProperty("tickify.db.poolSize");
        if (prop != null && !prop.trim().isEmpty()) {
            try {
                int parsed = Integer.parseInt(prop.trim());
                return Math.max(1, Math.min(parsed, MAX_POOL_SIZE));
            } catch (NumberFormatException ignored) {
            }
        }
        return DEFAULT_POOL_SIZE;
    }

    private class PooledConnection implements Connection {
        private final Connection delegate;
        private volatile long lastReturned;

        PooledConnection(Connection delegate) {
            this.delegate = delegate;
            this.lastReturned = System.currentTimeMillis();
        }

        private void markReturned() {
            this.lastReturned = System.currentTimeMillis();
        }

        @Override
        public void close() throws SQLException {
            markReturned();
            returnToPool(this);
        }

        public void reallyClose() throws SQLException {
            closeQuietly(delegate);
        }

        @Override
        public Statement createStatement() throws SQLException { return delegate.createStatement(); }
        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException { return delegate.prepareStatement(sql); }
        @Override
        public CallableStatement prepareCall(String sql) throws SQLException { return delegate.prepareCall(sql); }
        @Override
        public String nativeSQL(String sql) throws SQLException { return delegate.nativeSQL(sql); }
        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException { delegate.setAutoCommit(autoCommit); }
        @Override
        public boolean getAutoCommit() throws SQLException { return delegate.getAutoCommit(); }
        @Override
        public void commit() throws SQLException { delegate.commit(); }
        @Override
        public void rollback() throws SQLException { delegate.rollback(); }
        @Override
        public boolean isClosed() throws SQLException { return delegate.isClosed(); }
        @Override
        public DatabaseMetaData getMetaData() throws SQLException { return delegate.getMetaData(); }
        @Override
        public void setReadOnly(boolean readOnly) throws SQLException { delegate.setReadOnly(readOnly); }
        @Override
        public boolean isReadOnly() throws SQLException { return delegate.isReadOnly(); }
        @Override
        public void setCatalog(String catalog) throws SQLException { delegate.setCatalog(catalog); }
        @Override
        public String getCatalog() throws SQLException { return delegate.getCatalog(); }
        @Override
        public void setTransactionIsolation(int level) throws SQLException { delegate.setTransactionIsolation(level); }
        @Override
        public int getTransactionIsolation() throws SQLException { return delegate.getTransactionIsolation(); }
        @Override
        public SQLWarning getWarnings() throws SQLException { return delegate.getWarnings(); }
        @Override
        public void clearWarnings() throws SQLException { delegate.clearWarnings(); }
        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException { return delegate.createStatement(resultSetType, resultSetConcurrency); }
        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException { return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency); }
        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException { return delegate.prepareCall(sql, resultSetType, resultSetConcurrency); }
        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException { return delegate.getTypeMap(); }
        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException { delegate.setTypeMap(map); }
        @Override
        public void setHoldability(int holdability) throws SQLException { delegate.setHoldability(holdability); }
        @Override
        public int getHoldability() throws SQLException { return delegate.getHoldability(); }
        @Override
        public Savepoint setSavepoint() throws SQLException { return delegate.setSavepoint(); }
        @Override
        public Savepoint setSavepoint(String name) throws SQLException { return delegate.setSavepoint(name); }
        @Override
        public void rollback(Savepoint savepoint) throws SQLException { delegate.rollback(savepoint); }
        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException { delegate.releaseSavepoint(savepoint); }
        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability); }
        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability); }
        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability); }
        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException { return delegate.prepareStatement(sql, autoGeneratedKeys); }
        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException { return delegate.prepareStatement(sql, columnIndexes); }
        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException { return delegate.prepareStatement(sql, columnNames); }
        @Override
        public Clob createClob() throws SQLException { return delegate.createClob(); }
        @Override
        public Blob createBlob() throws SQLException { return delegate.createBlob(); }
        @Override
        public NClob createNClob() throws SQLException { return delegate.createNClob(); }
        @Override
        public SQLXML createSQLXML() throws SQLException { return delegate.createSQLXML(); }
        @Override
        public boolean isValid(int timeout) throws SQLException { return delegate.isValid(timeout); }
        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException { delegate.setClientInfo(name, value); }
        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException { delegate.setClientInfo(properties); }
        @Override
        public String getClientInfo(String name) throws SQLException { return delegate.getClientInfo(name); }
        @Override
        public Properties getClientInfo() throws SQLException { return delegate.getClientInfo(); }
        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException { return delegate.createArrayOf(typeName, elements); }
        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException { return delegate.createStruct(typeName, attributes); }
        @Override
        public void setSchema(String schema) throws SQLException { delegate.setSchema(schema); }
        @Override
        public String getSchema() throws SQLException { return delegate.getSchema(); }
        @Override
        public void abort(Executor executor) throws SQLException { delegate.abort(executor); }
        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException { delegate.setNetworkTimeout(executor, milliseconds); }
        @Override
        public int getNetworkTimeout() throws SQLException { return delegate.getNetworkTimeout(); }
        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException { return delegate.unwrap(iface); }
        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException { return delegate.isWrapperFor(iface); }
    }
}
