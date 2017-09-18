import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MysqlTest {
	
	public static void main(String[] args) throws Exception {

        String hostname = "10.10.22.92";
        String port = "3306";
        String database = "employees";
        String user = "root";
        String password = "123456";

        String baseUrl = "jdbc:mysql:fabric://" + hostname + ":" + Integer.valueOf(port) + "/";

        // Load the driver if running under Java 5
        if (!com.mysql.jdbc.Util.isJdbc4()) {
            Class.forName("com.mysql.fabric.jdbc.FabricMySQLDriver");
        }

        // 1. Create database and table for our demo
        Connection rawConnection = DriverManager.getConnection(baseUrl + "mysql?fabricServerGroup=sevgroup1", user, password);
        Statement statement = rawConnection.createStatement();
        statement.executeUpdate("create database if not exists employees");
        statement.close();
        rawConnection.close();

        // We should connect to the global group to run DDL statements, they will be replicated to the server groups for all shards.

        // The 1-st way is to set it's name explicitly via the "fabricServerGroup" connection property
        rawConnection = DriverManager.getConnection(baseUrl + database + "?fabricServerGroup=sevgroup1", user, password);
        statement = rawConnection.createStatement();
        statement.executeUpdate("create database if not exists employees");
        statement.close();
        rawConnection.close();

        // The 2-nd way is to get implicitly connected to global group when the shard key isn't provided, ie. set "fabricShardTable" connection property but 
        // don't set "fabricShardKey"
        rawConnection = DriverManager.getConnection(baseUrl + "employees?fabricShardTable=employees.employees", user, password);
        // At this point, we have a connection to the global group for  the `employees.employees' shard mapping.
        statement = rawConnection.createStatement();
        statement.executeUpdate("drop table if exists employees");
        statement.executeUpdate("create table employees (emp_no int not null, first_name varchar(50), last_name varchar(50), primary key (emp_no))");

	}
}
