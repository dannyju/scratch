package atp;

import java.io.File;
import java.security.Provider;
import java.security.Security;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;

//
// Simple ATP DB connection test program
// Complied against ojdbc8-19.3.0 but can be run with ojdbc8-12.2.0 as well
// java atp.DbUtil
//
public class DbUtil {

	// For ATP and ADW - use the TNS Alias name along with the TNS_ADMIN when using
	// DB_URL="jdbc:oracle:thin:@wallet_dbname?TNS_ADMIN=/Users/test/wallet_dbname";
	static String DB_URL = System.getenv("DB_URL");
	final static String DB_USER = System.getenv("DB_USER");
	final static String DB_PASSWORD = System.getenv("DB_PASSWORD");
	static boolean is1221Driver = false;
	static String walletDir;

	public static void main(String[] args) throws Exception {
		String driverVer = oracle.jdbc.OracleDriver.getDriverVersion();
		System.out.println("JDBC Driver Version: " + driverVer);

		// ojdbc8-12.2.0.1 can be down load at:
		// https://www.oracle.com/database/technologies/jdbc-ucp-122-downloads.html
		if ("12.2.0.1.0".equals(driverVer)) {
			is1221Driver = true;
			System.out.println("Using ojdbc8-12.2.1");
		}
		getAtpConnection();
	}

	public static void checkNull(String name, Object val) {
		if (val == null) {
			throw new RuntimeException(name + " cannot be NULL");
		}
	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	public static void setConnectionProperties(Properties info)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (is1221Driver) {
			// Connecting Using JDBC Thin Driver 12.2. Need set the Java properties and Java security providers
			// See https://docs.oracle.com/en/cloud/paas/atp-cloud/atpug/connect-jdbc-thin-wallet.html#GUID-32A48CAA-89AC-40A4-AFD1-BB962C562805

			// 1. explicitly register oracle.security.pki.OraclePKIProvider
			// to use Oracle Wallet
			System.out.println("Using OraclePKI ==> " + (Security.getProvider("OraclePKI") != null));
			Class clazz = Class.forName("oracle.security.pki.OraclePKIProvider");
			Security.addProvider((Provider) clazz.newInstance());
			// 2. ATP wallet related properties
			info.put("oracle.net.tns_admin", walletDir);
			info.put("oracle.net.ssl_server_dn_match", "true");
			info.put("oracle.net.ssl_version", "1.2");
			info.put("oracle.net.wallet_location", "(SOURCE=(METHOD=FILE)(METHOD_DATA=(DIRECTORY=" + walletDir + ")))");

			System.out.println("UCP connection properties " + info.toString());
			DB_URL = DB_URL.substring(0, DB_URL.indexOf("?TNS_ADMIN"));

		}
		info.put(OracleConnection.CONNECTION_PROPERTY_USER_NAME, DB_USER);
		info.put(OracleConnection.CONNECTION_PROPERTY_PASSWORD, DB_PASSWORD);
		info.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");

	}

	public static void getAtpConnection() throws Exception {
		checkNull("DB_URL", DB_URL);
		checkNull("DB_USER", DB_USER);
		checkNull("DB_PASSWORD", DB_PASSWORD);

		walletDir = DB_URL.substring(DB_URL.lastIndexOf("=") + 1);
		if (!new File(walletDir).exists()) {
			throw new RuntimeException("ATP Wallet dir " + walletDir + " does not existing");
		}

		Properties info = new Properties();

		setConnectionProperties(info);

		System.out.println(DB_URL);

		OracleDataSource ods = new OracleDataSource();
		ods.setURL(DB_URL);
		ods.setConnectionProperties(info);
		long t1 = System.currentTimeMillis();
		// With AutoCloseable, the connection is closed automatically.
		try (OracleConnection connection = (OracleConnection) ods.getConnection()) {
			long t2 = System.currentTimeMillis();
			System.out.println("Getting ATP Connection took " + (t2 - t1) / 1000 + "s");
			// Get the JDBC driver name and version
			DatabaseMetaData dbmd = connection.getMetaData();
			System.out.println("Driver Name: " + dbmd.getDriverName());
			System.out.println("Driver Version: " + dbmd.getDriverVersion());
			// Print some connection properties
			System.out.println("Default Row Prefetch Value is: " + connection.getDefaultRowPrefetch());
			System.out.println("Database Username is: " + connection.getUserName());
			System.out.println();
			// Perform a database operation
			printDbaUsers(connection);
		}
	}

	public static void printDbaUsers(Connection connection) throws SQLException {
		// Statement and ResultSet are AutoCloseable and closed automatically.
		try (Statement statement = connection.createStatement()) {
			// https://docs.oracle.com/cd/B19306_01/server.102/b14237/statviews_4174.htm#REFRN23302
			try (ResultSet resultSet = statement.executeQuery("select USERNAME, ACCOUNT_STATUS from DBA_USERS")) {
				System.out.println("USERNAME" + "  " + "ACCOUNT_STATUS");
				System.out.println("---------------------");
				while (resultSet.next())
					System.out.println(resultSet.getString(1) + " " + resultSet.getString(2) + " ");
			}
		}
	}
}
