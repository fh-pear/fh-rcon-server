
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.io.IOException;
import javax.naming.ConfigurationException;

public final class Config {
    //constants

    public static final String DEFAULT_DBPORT = "3306";
    public static final int DEFAULT_RCONPORT = 28960;
    public static final int DEFAULT_LISTENPORT = 21300;

    // database variables
    /**
     * database hostname
     */
    private static String dbHost;
    /**
     * database user name
     */
    private static String dbUser;
    /**
     * database password
     */
    private static String dbPassword;
    /**
     * database port, defaults to 3306
     */
    private static String dbPort;
    /**
     * database name to access
     */
    private static String database;

    // gameserver variables
    /**
     * rcon password for the gameserver
     */
    private static String rconPassword;
    /**
     * server port used by the gameserver. Default 28960
     */
    private static int serverPort;
    /**
     * hostname of the gameserver
     */
    private static String serverHost;
    /**
     * port number that this application will listen on
     */
    private static int listenPort;

    public static void init(String fileName) throws NumberFormatException, IOException, ConfigurationException {
        Properties props = new Properties();
        InputStream is = new FileInputStream(fileName);

        props.load(is);

        dbHost = props.getProperty("db_host");
        dbUser = props.getProperty("db_user");
        dbPassword = props.getProperty("db_password");
        database = props.getProperty("database");
        dbPort = props.getProperty("db_port");

        rconPassword = props.getProperty("rcon_password");
        serverPort = Integer.parseInt(props.getProperty("server_port"));
        serverHost = props.getProperty("server_host");
        listenPort = Integer.parseInt(props.getProperty("listen_port"));

        checkValues();
    }

    // some settings can be blank, and we can assign defaults
    // others we absolutely need (ex serverHost, dbHost)
    public static void checkValues() throws ConfigurationException {
        /* REQUIRED settings for database */
        String message = " configuration property is required. Shutting down program...";
        if (dbHost == null || dbHost.equals("")) {
            throw new ConfigurationException("db_host" + message);
        }
        if (dbUser == null || dbUser.equals("")) {
            throw new ConfigurationException("db_user" + message);
        }
        if (dbPassword == null || dbPassword.equals("")) {
            throw new ConfigurationException("db_password" + message);
        }
        if (database == null || database.equals("")) {
            throw new ConfigurationException("database" + message);
        }

        /* Optional settings for database. If not initialized, do it here */
        if (dbPort == null || dbPort.equals("")) {
            dbPort = DEFAULT_DBPORT;
        }

        /* REQUIRED settings for the gameserver */
        if (rconPassword == null || rconPassword.equals("")) {
            throw new ConfigurationException("rcon_password" + message);
        }
        if (serverHost == null || serverHost.equals("")) {
            throw new ConfigurationException("server_host" + message);
        }

        /* Optional settings for the gameserver. */
        if (serverPort == 0) {
            serverPort = DEFAULT_RCONPORT;
        }
        if (listenPort == 0) {
            listenPort = DEFAULT_LISTENPORT;
        }
    }

    //jdbc:mysql://<dbHost>:<dbPort>/<database>
    public static String getDatabaseUrl() {
        String str = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + database;

        return str;
    }

    public static String getRconPassword() {
        return rconPassword;
    }

    public static String getServerHost() {
        return serverHost;
    }

    public static int getServerPort() {
        return serverPort;
    }

    public static int getListenPort() {
        return listenPort;
    }

    public static String getDatabaseUser() {
        return dbUser;
    }

    public static String getDatabasePassword() {
        return dbPassword;
    }
}
