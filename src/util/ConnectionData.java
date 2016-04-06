package util;

public class ConnectionData implements Cloneable {
    public static String SERVER_PARAM    = "host";
    public static String PORT_PARAM      = "port";
    public static String DATABASE_PARAM  = "database";
    public static String USER_PARAM      = "user";
    public static String PASSWORD_PARAM  = "password";
    public static String DBTYPE_PARAM    = "dbtype";

    private static String postgresqlUrlPrefix = "jdbc:postgresql://";
    private static String mssqlUrlPrefix = "jdbc:sqlserver://";
    private static String postgresPlusPrefix = "jdbc:edb://";
    private static String oraclePrefix = "jdbc:oracle:thin:@";
    private static String sybasePrefix = "jdbc:sybase:Tds:";
    private static String db2Prefix = "jdbc:db2://";
    private static String mariaDbPrefix = "jdbc:mariadb://";
    private static String mysqlPrefix = "jdbc:mysql://";
    
    private final String url, user, pass, db, server, port;
    private final DbType dbType;
    private Class<? extends XAConnectionUtil> xaConnectionUtil;

    /**
     * Use method {@link #url()} to instantiate this class.
     */
    private ConnectionData(String connectionUrl, Builder builder) {
        this.url = connectionUrl;
        this.user = builder.user;
        this.pass = builder.pass;
        this.db = builder.db;
        this.server = builder.server;
        this.port = builder.port;
        this.dbType = builder.dbType;
        this.xaConnectionUtil = builder.xaConnectionUtil;
    }

    public String url() {
        return url;
    }
    
    public String user() {
        return user;
    }
    
    public String pass() {
        return pass;
    }
    
    public String db() {
        return db;
    }
    
    public String server() {
        return server;
    }
    
    public String port() {
        return port;
    }
    
    public int portAsInt() {
        return Integer.parseInt(port);
    }

    public DbType dbType() {
        return dbType;
    }
    
    public Class<? extends XAConnectionUtil> xaConnectionUtil() {
        return xaConnectionUtil;
    }

    public String toString() {
        return String.format("jdbc url: '%s', connection props: %s:%s %s/%s", url, server, port, user, pass);
    }

    @Override
    public ConnectionData clone() throws CloneNotSupportedException {
        ConnectionData.Builder builder = new ConnectionData.Builder(this.url(), this.port())
            .db(this.db())
            .user(this.user())
            .pass(this.pass());

        switch(this.dbType()) {
            case DB2:
                builder.db2();
            default:
                throw new IllegalStateException("Not supported DB type for cloning");
        }
    }

    public static class Builder {
        private final String server, port;
        private String db = System.getProperty(ConnectionData.DATABASE_PARAM, "crashrec"); 
        private String user = System.getProperty(ConnectionData.USER_PARAM, "crashrec");
        private String pass = System.getProperty(ConnectionData.PASSWORD_PARAM, "crashrec");
        private DbType dbType;
        private Class<? extends XAConnectionUtil> xaConnectionUtil;

        public Builder() {
            this.server = System.getProperty(ConnectionData.SERVER_PARAM);
            this.port = System.getProperty(ConnectionData.PORT_PARAM);

            if(server == null || port == null) {
                throw new NullPointerException("host or port is not defined");
            }
        }

        /**
         * If system properties for server and port is not defined then default params are used.
         */
        public Builder(String defaultServer, String defaultPort) {
            this.server = System.getProperty(ConnectionData.SERVER_PARAM, defaultServer);
            this.port = System.getProperty(ConnectionData.PORT_PARAM, defaultPort);
        }
        
        public Builder user(String userName) {
            this.user = userName;
            return this;
        }
    
        public Builder pass(String password) {
            this.pass = password;
            return this;
        }
    
        public Builder db(String databaseName) {
            this.db = databaseName;
            return this;
        }
        
        public Builder type(String type) {
            this.dbType = DbType.valueOf(type.toUpperCase());
            return this;
        }
        
        public Builder dbType(DbType type) {
            this.dbType = type;
            return this;
        }

        public ConnectionData build() {
            switch(this.dbType) {
                case MSSQL:
                    return mssql();
                case POSTGRESQL:
                    return postgresql();
                case POSTGRESPLUS:
                    return postgresplus();
                case ORACLE:
                    return oracle();
                case SYBASE:
                    return sybase();
                case DB2:
                    return db2();
                case MYSQL:
                    return mysql();
                case MARIADB:
                    return mariadb();
                default:
                    throw new IllegalStateException("Unsupported db type connection data for " + this.dbType);
            }
        }

        private ConnectionData postgresql() {
            String connectionUrl = postgresqlUrlPrefix + server + ":" + port  + "/" + db;
            xaConnectionUtil = MssqlXAConnectionUtil.class;
            return new ConnectionData(connectionUrl, this);
        }

        private ConnectionData postgresplus() {
            String connectionUrl = postgresPlusPrefix + server + ":" + port  + "/" + db;
            xaConnectionUtil = PostgresPlusXAConnectionUtil.class;
            return new ConnectionData(connectionUrl, this);
        }

        private ConnectionData mssql() {
            String connectionUrl = mssqlUrlPrefix +  server + ":" + port
                    + ";databaseName=" + db + ";user=" + user + ";password=" + pass;
            xaConnectionUtil = MssqlXAConnectionUtil.class;
            return new ConnectionData(connectionUrl, this);
        }

        private ConnectionData oracle() {
            String connectionUrl = oraclePrefix +  server + ":" + port + ":" + db;
            xaConnectionUtil = OracleXAConnectionUtil.class;
            return new ConnectionData(connectionUrl, this);
        }
        
        private ConnectionData sybase() {
            String connectionUrl = sybasePrefix +  server + ":" + port + "/" + db;
            xaConnectionUtil = SybaseXAConnectionUtil.class;
            return new ConnectionData(connectionUrl, this);
        }
        
        private ConnectionData db2() {
            String connectionUrl = db2Prefix +  server + ":" + port + "/" + db;
            xaConnectionUtil = Db2XAConnectionUtil.class;
            return new ConnectionData(connectionUrl, this);
        }

        private ConnectionData mariadb() {
            String connectionUrl = mariaDbPrefix +  server + ":" + port + "/" + db;
            xaConnectionUtil = MariaDBXAConnectionUtil.class;
            return new ConnectionData(connectionUrl, this);
        }
        
        private ConnectionData mysql() {
            String connectionUrl = mysqlPrefix +  server + ":" + port + "/" + db;
            xaConnectionUtil = MySQLXAConnectionUtil.class;
            return new ConnectionData(connectionUrl, this);
        }
    }

}
