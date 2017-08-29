package me.prunt.perplayerchests;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {
    private Main main;

    private HashMap<Player, Block> openedInvs = new HashMap<>();

    EventListener(Main main) {
	this.main = main;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent e) {
	Player p = e.getPlayer();

	if (p.isOp()) {
	    return;
	}

	if (main.debug)
	    System.out.print(p.getName() + " PIE");

	Block b = e.getClickedBlock();

	// TODO shift-click crashib

	// if the right-clicked block is inventory holder
	if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getState() instanceof Chest) {
	    // cancel the event
	    e.setCancelled(true);

	    // if it's empty
	    if (main.emptyInvs.containsKey(p.getName())) {
		if (main.debug)
		    System.out.println("empty");

		// if the clicked block is in the list
		if (main.emptyInvs.get(p.getName()).contains(b)) {
		    p.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "Missioonid > " + ChatColor.RED
			    + "Vanades kirstudes uuesti sobrada ei saa!");

		    return;
		}
	    }

	    if (main.debug)
		System.out.print("RCB IH");

	    // get the inventoryholder's inventory
	    Inventory invh = ((InventoryHolder) e.getClickedBlock().getState()).getInventory();
	    // creates a new inventory that is opened for the player
	    Inventory inv = main.getServer().createInventory(p, invh.getSize());

	    // copy items from one inventory to another - just in case
	    for (int i = 0; i < invh.getSize(); i++) {
		ItemStack is = invh.getItem(i);
		inv.setItem(i, is);
	    }

	    if (main.debug)
		System.out.print(main.invs.toString());

	    // if an inventory with same contents is already listed
	    if (main.invs.containsKey(b)) {
		if (main.debug)
		    System.out.print("+CON");

		HashMap<String, Inventory> list = main.invs.get(b);

		if (main.debug)
		    System.out.print(list.toString());

		// if the chest has already been opened by that player
		if (list.containsKey(p.getName())) {
		    if (main.debug)
			System.out.print("+OPEN");

		    // will open the old inventory
		    inv = list.get(p.getName());
		} else {
		    if (main.debug)
			System.out.print("-OPEN");

		    // populate the invs list
		    list.put(p.getName(), inv);
		    main.invs.put(b, list);

		    if (main.debug)
			System.out.print(main.invs.toString());
		}
	    } else {
		if (main.debug)
		    System.out.print("-CON");

		HashMap<String, Inventory> list = new HashMap<>();

		// populate the invs list
		list.put(p.getName(), inv);

		if (main.debug)
		    System.out.print(list.toString());

		main.invs.put(b, list);

		if (main.debug)
		    System.out.print(main.invs.toString());
	    }

	    p.openInventory(inv);
	    openedInvs.put(p, b);
	} else if (!(e.getClickedBlock().getState() instanceof Furnace)) {
	    return;
	}
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
	if (main.debug)
	    System.out.print(e.getPlayer() + " ICE");

	// if it's the personal inventory
	if (openedInvs.containsKey(e.getPlayer())) {
	    if (main.debug)
		System.out.print("PERS");

	    // gets old list from invs list
	    HashMap<String, Inventory> list = main.invs.get(openedInvs.get(e.getPlayer()));

	    if (main.debug)
		System.out.print(list.toString());

	    // updates the inventory
	    list.put(e.getPlayer().getName(), e.getInventory());
	    main.invs.put(openedInvs.get(e.getPlayer()), list);

	    if (main.debug)
		System.out.print(main.invs.toString());

	    // removes from list since inventory is not opened anymore
	    openedInvs.remove(e.getPlayer());
	}
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
	main.savePlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent e) {
	main.savePlayer(e.getPlayer());
    }
}
