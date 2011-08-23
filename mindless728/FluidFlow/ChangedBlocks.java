package mindless728.FluidFlow;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Keeps track of the list of changed blocks to keep order and a mapping from
 * the blocks to the new changed types for fast access
 *
 * @author mindless728
 */
public class ChangedBlocks {
	/** the order of the blocks to be changed */
	private LinkedList<FluidBlock> list;

	/** the mappping from the blocks to the new material type */
	private HashMap<FluidBlock, Material> map;

	/** default constructor */
	public ChangedBlocks() {
		list = new LinkedList<FluidBlock>();
		map = new HashMap<FluidBlock, Material>();
	}

	/**
	 * adds the block to the list or changes the mapping if it is already there
	 *
	 * @param fb the block to add/change
	 */
	public void add(FluidBlock fb) {
		if(fb == null)
			return;
		if(!contains(fb))
			list.add(fb);
		map.put(fb, fb.newType);
	}

	/**
	 * removes the first block in the list and the mapping
	 *
	 * @return the fist block in the list if there is one or null if there is no block
	 */
	public FluidBlock remove() {
		if(list.isEmpty())
			return null;

		FluidBlock ret = list.remove();
		map.remove(ret);
		return ret;
	}

	/**
	 * checks to see if the fluid block is already in the changed blocks
	 *
	 * @param fb the block to check for
 	 *
	 * @return whether or not the changed blocks contains the block
	 */
	public boolean contains(FluidBlock fb) {
		if(fb == null)
			return false;
		return map.containsKey(fb);
	}

	/**
	 * gets the mapped material from the mapping
	 *
	 * @param fb the block to get the new material from
	 *
	 * @return the mapped material if it exists or null if not
	 */
	public Material getType(FluidBlock fb) {
		if(fb == null)
			return null;
		return map.get(fb);
	}

	/**
	 * checks to see if the list is empty
	 *
	 * @return whether or not the list of changed blocks is empty
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * grabs the amount of changed blocks
	 *
	 * @return the amount of changed blocks
	 */
	public int size() {
		return list.size();
	}
}
