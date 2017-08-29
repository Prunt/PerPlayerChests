package me.prunt.perplayerchests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {
    // Main class
    private Main plugin;

    // Database-related variables
    private String host, database, username, password, table;
    private int port;
    private Connection connection;

    Database(Main plugin) {
	this.plugin = plugin;

	// Gets database variables from config
	host = this.plugin.getConfig().getString("database.host");
	database = this.plugin.getConfig().getString("database.database");
	username = this.plugin.getConfig().getString("database.username");
	password = this.plugin.getConfig().getString("database.password");
	table = this.plugin.getConfig().getString("database.table");
	port = this.plugin.getConfig().getInt("database.port");
    }

    /**
     * Closes current connection
     */
    String getTableName() {
	return table;
    }

    /**
     * Returns current connection
     */
    Connection getConnection() {
	return connection;
    }

    /**
     * Opens MySQL connection
     */
    void openMySQLConnection() {
	try {
	    if (connection != null && !connection.isClosed()) {
		return;
	    }

	    synchronized (this) {
		if (connection != null && !connection.isClosed()) {
		    return;
		}
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection(
			"jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username,
			this.password);
	    }
	} catch (SQLException e) {
	    plugin.getLogger().severe("Could not connect to database. Check config. Disabling plugin.");
	    plugin.getServer().getPluginManager().disablePlugin(plugin);
	} catch (ClassNotFoundException e) {
	    plugin.getLogger().severe("com.mysql.jdbc.Driver is not installed. Disabling plugin.");
	    plugin.getServer().getPluginManager().disablePlugin(plugin);
	}
    }

    /**
     * Opens SQLite connection
     */
    void openSQLiteConnection() {
	try {
	    if (connection != null && !connection.isClosed()) {
		return;
	    }

	    synchronized (this) {
		if (connection != null && !connection.isClosed()) {
		    return;
		}
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager
			.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getPath() + "/" + this.database + ".db");
	    }
	} catch (SQLException e) {
	    plugin.getLogger().severe("Could not connect to database. Disabling plugin.");
	    plugin.getServer().getPluginManager().disablePlugin(plugin);
	} catch (ClassNotFoundException e) {
	    plugin.getLogger().severe("org.sqlite.JDBC is not installed. Disabling plugin.");
	    plugin.getServer().getPluginManager().disablePlugin(plugin);
	}
    }

    /**
     * Closes current connection
     */
    void closeConnection() {
	try {
	    connection.close();
	} catch (SQLException e) {
	    plugin.getLogger().severe("Could not close connection.");
	}
    }

    /**
     * Returns PreparedStatement
     */
    PreparedStatement getStatement(String sql) {
	try {
	    return connection.prepareStatement(sql);
	} catch (SQLException e) {
	    plugin.getLogger().severe("Could not prepare SQL statement:");
	    e.printStackTrace();

	    return null;
	}
    }

}
