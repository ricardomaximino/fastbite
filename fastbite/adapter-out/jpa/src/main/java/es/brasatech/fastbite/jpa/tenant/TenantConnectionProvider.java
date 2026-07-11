package es.brasatech.fastbite.jpa.tenant;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class TenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    public TenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = getAnyConnection();
        System.out.println("TenantConnectionProvider.getConnection is called for tenant: " + tenantIdentifier);
        if (tenantIdentifier != null && !tenantIdentifier.trim().isEmpty() && !"default".equalsIgnoreCase(tenantIdentifier)) {
            // H2 Dialect uses SET SCHEMA tenant_tenantIdentifier
            if (tenantIdentifier.matches("^[a-zA-Z0-9_]+$")) {
                try (var statement = connection.createStatement()) {
                    String sql = "SET SCHEMA tenant_" + tenantIdentifier;
                    System.out.println("Executing: " + sql);
                    statement.execute(sql);
                }
            } else {
                throw new SQLException("Invalid tenant identifier: " + tenantIdentifier);
            }
        } else {
            // Default/Public schema
            try (var statement = connection.createStatement()) {
                System.out.println("Executing: SET SCHEMA PUBLIC");
                statement.execute("SET SCHEMA PUBLIC");
            }
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        releaseAnyConnection(connection);
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}
