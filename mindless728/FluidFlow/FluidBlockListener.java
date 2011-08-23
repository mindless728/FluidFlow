package mindless728.FluidFlow;

import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.Material;

import java.util.LinkedList;

/**
 * catches block based events
 *
 * @author mindless728
 */
public class FluidBlockListener extends BlockListener {
	/** the plugin that is associated with this event listener */
	private FluidFlow plugin;

	/** the block faces for adjacent blocks */
	private BlockFace[] adjacentBlocks = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

	/**
	 * Constructor taking the FluidFlow plugin as a parameter
	 *
	 * @param p the fluid flow plugin
	 */
	public FluidBlockListener(FluidFlow p) {
		plugin = p;
	}

	/**
	 * adds a flow to the correct fluid (if one exists)
	 *
	 * @param fb the fluid flow to add
	 */
	private void addFluidFlow(FluidBlock fb) {
		Fluid fluid = plugin.getFluidFromMaterial(fb.loc.getBlock().getType());
		if(fluid == null)
			return;
		fluid.addFlow(fb);
	}

	/**
	 * checks each of the adjacent blocks to the one broken and adds flows
	 * to the corresponding fluid
	 *
	 * @param event the event that happened
	 */
	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		FluidBlock fb = new FluidBlock(event.getBlock().getLocation());
		LinkedList<FluidBlock> adjFb = fb.getBlockFaces(adjacentBlocks);

		for(FluidBlock f : adjFb) {
			addFluidFlow(f);
		}
	}

	/**
	 * is called when a block is affected by gravity and tests to see if it is a type
	 * that is registered, if it is cancel the event
	 */
	@Override
	public void onBlockPhysics(BlockPhysicsEvent event) {
		Material type = event.getBlock().getType();

		//change the stationary types to normal types, i consider them the same
		if(type == Material.STATIONARY_WATER)
			type = Material.WATER;
		else if(type == Material.STATIONARY_LAVA)
			type = Material.LAVA;

		if(plugin.isMaterialRegistered(type))
			event.setCancelled(true);
	}

	/**
	 * is called when a block is placed and tests to see if it is a type that
	 * is registered with the plugin, if so it adds the block to the flow list
	 * of the fluid that has registered that material
	 *
	 * @param event the event that happened
	 */
	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		FluidBlock fb = new FluidBlock(event.getBlock().getLocation());
		LinkedList<FluidBlock> adjFb = fb.getBlockFaces(adjacentBlocks);

		addFluidFlow(fb);
		for(FluidBlock f : adjFb) {
			addFluidFlow(f);
		}
	}

	/**
	 * is called when a normal minecraft fluid tries to flow, check to see if the
	 * type is registered with the fluid flow plugin, if so cancel it
	 *
	 * @param event the event that happened
	 */
	@Override
	public void onBlockFromTo(BlockFromToEvent event) {
		Material type = event.getBlock().getType();

		//change the stationary types to normal types, i consider them the same
		if(type == Material.STATIONARY_WATER)
			type = Material.WATER;
		else if(type == Material.STATIONARY_LAVA)
			type = Material.LAVA;

		if(plugin.isMaterialRegistered(type))
			event.setCancelled(true);
	}
}
