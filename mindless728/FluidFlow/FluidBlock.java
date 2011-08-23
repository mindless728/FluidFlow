package mindless728.FluidFlow;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.LinkedList;

/**
 * holds the data needed when a fluid is talking about a block
 *
 * @author mindless728
 */
public class FluidBlock {
	/** the location of the block */
	public Location loc;

	/** the extra data the block holds */
	public BlockData data;

	/** the new material type when changing the block */
	public Material newType;

	/** the hash code of the block, calculated when the block is created */
	private Integer hash;

	/**
	 * Constructor taking the location of the block
	 *
	 * @param l the location of teh block
	 */
	public FluidBlock(Location l) {
		this(l, null);
	}

	/**
	 * Constructor taking the location and the met-data for the block
	 *
	 * @param l the location of the block
	 * @param bd the meta-data for the block
	 */
	public FluidBlock(Location l, BlockData bd) {
		loc = l;
		data = bd;

		//calculate the hash code
		hash = new Integer((loc.getBlockY() & 0xFF) | ((loc.getBlockX() & 0xFFF) << 8) | ((loc.getBlockZ() & 0xFFF) << 20));;
	}

	/**
	 * gets the adjacent blocks based on an array of block faces to use
	 *
	 * @param faces the block faces to use for getting the adjacent blocks
	 *
	 * @return the list of fluid blocks that are adjacent to the block
	 */
	public LinkedList<FluidBlock> getBlockFaces(BlockFace[] faces) {
		LinkedList<FluidBlock> ret = new LinkedList<FluidBlock>();
		Block temp = null;

		//loop through the block faces
		for(BlockFace bf : faces) {
			//get the block described by the face
			try {
				temp = loc.getBlock().getRelative(bf);
			} catch (NullPointerException npe) {}

			//add the fluidblock by location
			if(temp != null)
				ret.add(new FluidBlock(temp.getLocation()));
		}
		return ret;
	}

	/**
	 * gets the hashCode for the block
	 *
	 * @return the hashCode of the block, 0 if location is null
	 */
	public int hashCode() {
		if(loc == null)
			return 0;
		return hash.intValue();
	}

	/**
	 * test to see if another object is equal to this one
	 *
	 * @param o the object to test against
	 *
	 * @return true if and only if the fluid blocks describe the same location, false for everything else
	 */
	public boolean equals(Object o) {
		//if the block being tested against isn't a FluidBlock,
		//then they are not equal
		if(!(o instanceof FluidBlock))
			return false;

		//if either the object to test against is null or the location is,
		//then they are not equal
		FluidBlock fb = (FluidBlock)o;
		if(fb == null || fb.loc == null)
			return false;
		
		//equal when the locations describe the same point
		return loc.getWorld() == fb.loc.getWorld() &&
			   loc.getBlockX() == fb.loc.getBlockX() &&
			   loc.getBlockY() == fb.loc.getBlockY() &&
			   loc.getBlockZ() == fb.loc.getBlockZ();
	}
}
