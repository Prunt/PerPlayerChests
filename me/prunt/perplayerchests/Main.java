package me.prunt.perplayerchests;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private Database db;

    boolean debug = false;

    Map<Block, HashMap<String, Inventory>> invs = new HashMap<>();
    Map<String, Set<Block>> emptyInvs = new HashMap<>();

    @Override
    public void onEnable() {
	// register events located in anouther class
	getServer().getPluginManager().registerEvents(new EventListener(this), this);

	saveDefaultConfig();

	// Initializes database
	db = new Database(this);

	// Checks which database is chosen
	if (getConfig().getString("database.type").equalsIgnoreCase("mysql")) {
	    // Creates MySQL connection
	    getDB().openMySQLConnection();
	} else {
	    // Creates SQLite connection
	    getDB().openSQLiteConnection();
	}

	// Checks connection
	if (getDB().getConnection() == null) {
	    return;
	}

	// Creates table if doesn't exist already
	try {
	    getDB().getStatement(
		    "CREATE TABLE IF NOT EXISTS " + getDB().getTableName() + " (player VARCHAR(20), block VARCHAR(50))")
		    .executeUpdate();
	} catch (SQLException e) {
	    e.printStackTrace();
	}

	// Load data from database
	loadAsync(new DBCallback() {
	    @Override
	    public void onQueryDone(ResultSet rs) {
		try {
		    if (debug)
			System.out.println("load");

		    while (rs.next()) {
			String pl = rs.getString("player");
			String bl = rs.getString("block");

			if (emptyInvs.containsKey(pl)) {
			    emptyInvs.get(pl).add(block(bl));
			} else {
			    Set<Block> set = new HashSet<>();
			    set.add(block(bl));
			    emptyInvs.put(pl, set);
			}
		    }
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    @Override
    public void onDisable() {
	for (Player p : getServer().getOnlinePlayers()) {
	    try {
		getDB().getConnection().setAutoCommit(false);
		if (debug)
		    System.out.println("shutdown");

		for (Entry<Block, HashMap<String, Inventory>> en : invs.entrySet()) {
		    Block b = en.getKey();

		    for (Entry<String, Inventory> en2 : en.getValue().entrySet()) {
			if (en2.getKey().equalsIgnoreCase(p.getName())) {
			    getDB().getStatement("INSERT INTO " + getDB().getTableName() + " (player, block) VALUES ('"
				    + p.getName() + "', '" + position(b) + "');").executeUpdate();
			}
		    }
		}

		getDB().getConnection().commit();
		getDB().getConnection().setAutoCommit(true);
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
	}
    }

    void savePlayer(Player p) {
	if (debug)
	    System.out.println("save");
	final Map<Block, HashMap<String, Inventory>> finvs = invs;

	// Start async processing
	Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
	    @Override
	    public void run() {
		try {
		    getDB().getConnection().setAutoCommit(false);

		    for (Entry<Block, HashMap<String, Inventory>> en : finvs.entrySet()) {
			Block b = en.getKey();

			for (Entry<String, Inventory> en2 : en.getValue().entrySet()) {
			    if (en2.getKey().equalsIgnoreCase(p.getName())) {
				getDB().getStatement("INSERT INTO " + getDB().getTableName()
					+ " (player, block) VALUES ('" + p.getName() + "', '" + position(b) + "');")
					.executeUpdate();
			    }
			}
		    }

		    getDB().getConnection().commit();
		    getDB().getConnection().setAutoCommit(true);
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    Database getDB() {
	return this.db;
    }

    private void loadAsync(DBCallback callback) {
	Plugin plugin = this;
	if (debug)
	    System.out.println("loadasync");

	// Start async processing
	Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
	    @Override
	    public void run() {
		try {
		    // Gets all blocks from database
		    ResultSet rs = getDB().getStatement("SELECT * FROM " + getDB().getTableName()).executeQuery();

		    // Back to sync processing
		    Bukkit.getScheduler().runTask(plugin, new Runnable() {
			@Override
			public void run() {
			    callback.onQueryDone(rs);
			}
		    });
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    private Block block(String s) {
	// Gets coordinates from given string
	String[] sl = s.split(";");
	String world = sl[0];
	int x = Integer.valueOf(sl[1]);
	int y = Integer.valueOf(sl[2]);
	int z = Integer.valueOf(sl[3]);

	// Returns block from given coordinates
	return getServer().getWorld(world).getBlockAt(x, y, z);
    }

    private String position(Block b) {
	return b.getWorld().getName() + ";" + b.getX() + ";" + b.getY() + ";" + b.getZ();
    }
}
