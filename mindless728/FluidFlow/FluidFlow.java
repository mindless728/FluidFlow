package mindless728.FluidFlow;

import org.bukkit.Location;

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type; 
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * the main plugin that controls registration of fluid types
 *
 * @author mindless728
 */
public class FluidFlow extends JavaPlugin {
	/** the mapping from registered materials to their fluid */
	private HashMap<Material, Fluid> registeredM;

	/** the mapping from registered fluids to their material */
	private HashMap<Fluid, Material> registeredF;

	/** the mapping from fluid to its changed block object */
	private HashMap<Fluid, ChangedBlocks> changedBlocks;

	/** the block listener for this plugin */
	private FluidBlockListener blockListener;

	/** the object that changes the blocks from the async threads */
	private BlockChanger blockChanger;

	/** Default Constructor */
	public FluidFlow() {
		//basically allocate all of the objects needed
		registeredM = new HashMap<Material, Fluid>();
		registeredF = new HashMap<Fluid, Material>();
		changedBlocks = new HashMap<Fluid, ChangedBlocks>();
		blockListener = new FluidBlockListener(this);
		blockChanger = new BlockChanger(changedBlocks);

		//set the structure all fluids can see for changed blocks
		Fluid.setAllChanges(changedBlocks);
	}

	/**	called when the plugin is enabled */
	public void onEnable() {
		//schedule the block changer
		getServer().getScheduler().scheduleSyncRepeatingTask(this, blockChanger, 1, 1);

		//register the block based events
		getServer().getPluginManager().registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Low, this);
		getServer().getPluginManager().registerEvent(Type.BLOCK_PHYSICS, blockListener, Priority.Low, this);
		getServer().getPluginManager().registerEvent(Type.BLOCK_PLACE, blockListener, Priority.Low, this);
    	getServer().getPluginManager().registerEvent(Type.BLOCK_FROMTO, blockListener, Priority.Low, this);

		//tell the operator that the plugin has been enabled fully
		System.out.println(getDescription().getName()+" version "+getDescription().getVersion()+" enabled");
	}

	/** called when the plugin is disabled */
	public void onDisable() {
		ChangedBlocks temp = null;
		FluidBlock block = null;

		//stop all associated tasks with this plugin
		getServer().getScheduler().cancelTasks(this);

		//tell the operator that the plugin is shutting down
		System.out.println("Shutting down "+getDescription().getName()+", this may take a long time");

		//loop through the fluids
		for(Fluid f : changedBlocks.keySet()) {
			f.stop(); //stop the fluid thread
			temp = changedBlocks.get(f); //get the changed blocks from the fluid

			//tell the operator that the fluid needs to save the amount of blocks to the server still
			System.out.print("Stopping "+f.getMaterial()+": "+temp.size()+" fluid changes to save");

			//while the fluid has changed blocks, change them
			while(!temp.isEmpty()) {
				block = temp.remove();
				block.loc.getBlock().setType(block.newType);
			}
		}

		//clear the registration nodes
		registeredM.clear();
		registeredF.clear();

		//tell the operator that the plugin is shutdown
		System.out.println(getDescription().getName()+" version "+getDescription().getVersion()+" disabled");
	}

	/**
	 * attempts the register the fluid with this plugin, returns the changed block object to
	 * the fluid so they can use it
	 *
	 * @param fluid the fluid that is trying to register
	 * @param type the type that fluid is trying to register
	 *
	 * @return the changed block object if registration is succesful, null otherwise
	 */
	public ChangedBlocks registerFluid(Fluid fluid, Material type) {
		ChangedBlocks temp = new ChangedBlocks();

		//convert stationary fluid types to normal ones, i treat them the same
		if(type == Material.STATIONARY_WATER)
			type = Material.WATER;
		else if(type == Material.STATIONARY_LAVA)
			type = Material.LAVA;

		//if the fluid or type is already registered, return failure
		if(isFluidRegistered(fluid) || isMaterialRegistered(type) || temp == null)
			return null;

		//register the fluid and type
		registeredM.put(type, fluid);
		registeredF.put(fluid, type);

		//setup the changed blocks in the mapping
		changedBlocks.put(fluid, temp);

		//give the fluid the changed blocks object
		return temp;
	}

	/**
	 * unregisters the fluid, warning once a fluid is stopped in fashion, it cannot be restarted
	 *
	 * @param fluid the fluid to remove registration from
	 */
	public void deregisterFluid(Fluid fluid) {
		Material m = getMaterialFromFluid(fluid);
		//if the fluid isn't registered, return and do nothing
		if(m == null)
			return;

		//stop the fluid and remove all registration from it
		fluid.stop();
		registeredM.remove(m);
		registeredF.remove(fluid);
		changedBlocks.remove(fluid);
	}

	/**
	 * check to see if the fluid is registered
	 *
	 * @param fluid the fluid to check for
	 *
	 * @return whether or not the fluid is registered
	 */
	public boolean isFluidRegistered(Fluid fluid) {
		return registeredF.containsKey(fluid);
	}

	/**
	 * check to see if the material is registered
	 *
	 * @param type the material type to check for
	 *
	 * @return whether or not the material is registered
	 */
	public boolean isMaterialRegistered(Material type) {
		if(type == Material.STATIONARY_WATER)
			type = Material.WATER;
		else if(type == Material.STATIONARY_LAVA)
			type = Material.LAVA;
		return registeredM.containsKey(type);
	}

	/**
	 * gets the material type for a fluid
	 *
	 * @param fluid the fluid whos type you want
	 *
	 * @return the material type the fluid controls, null if it isn't registered
	 */
	public Material getMaterialFromFluid(Fluid fluid) {
		return registeredF.get(fluid);
	}

	/**
	 * gets the fluid that is associated with a material
	 *
	 * @param type the material type to check against
	 *
	 * @return the fluid that is registered with the known material, null if it isn't registered
	 */
	public Fluid getFluidFromMaterial(Material type) {
		if(type == Material.STATIONARY_WATER)
			type = Material.WATER;
		else if(type == Material.STATIONARY_LAVA)
			type = Material.LAVA;
		return registeredM.get(type);
	}
}
