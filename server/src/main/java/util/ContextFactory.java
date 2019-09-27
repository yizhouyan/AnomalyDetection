package util;

import conf.ModelStorageConfig;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by yizhouyan on 9/26/19.
 * This class contains logic for connecting to the database.
 */

public class ContextFactory {
    /**
     * Create a database context that reflects a connection to a database.
     * @param username - The username to connect to the database.
     * @param password - The password to connect to the database.
     * @param jdbcUrl - The JDBC URL of the database.
     * @param dbType - The database type.
     * @return The database context.
     * @throws SQLException - Thrown if there are problems connecting to the database.
     * @throws IllegalArgumentException - Thrown if the dbType is unsupported.
     */
    public static DSLContext create(
            String username,
            String password,
            String jdbcUrl,
            ModelStorageConfig.DatabaseType dbType
    ) throws SQLException, IllegalArgumentException {
        Connection conn = DriverManager.getConnection(jdbcUrl, username, password);

        switch (dbType) {
            case SQLITE: return DSL.using(conn, SQLDialect.SQLITE);
        }

        throw new IllegalArgumentException(
                "Cannot connect to DatabaseType: " + dbType);
    }
}
