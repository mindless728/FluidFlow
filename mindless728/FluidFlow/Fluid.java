package mindless728.FluidFlow;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * the base fluid that is inherited by other plugin devs to create fluids
 *
 * @author mindless728
 */
public abstract class Fluid extends JavaPlugin implements Runnable {
	/** the queued up flows */
	private LinkedList<FluidBlock> flows;

	/** this fluids changed blocks */
	private ChangedBlocks changedBlocks;

	/** all of the changed blocks */
	private static HashMap<Fluid, ChangedBlocks> allChanges;

	/** the FluidFlow plugin, needed for fluid registration */
	private FluidFlow plugin;

	/** tells whether the fluid is running or not */
	private boolean running;

	/** tells whether the fluid is stopped or not */
	private boolean stopped;

	/** the sleep time to wait if there are no flows, in nano-seconds */
	private int sleepTime = 100000;

	/**
	 * lets all fluids know about each other's changes, for synchronizing between
	 * fluids to let each other know about cahnged blocks
	 *
	 * @param ac the mapping from fluids to changed blocks
	 */
	protected static void setAllChanges(HashMap<Fluid, ChangedBlocks> ac) {
		allChanges = ac;
	}

	/** default constructor */
	public Fluid() {
		//creates the list object to hold the flows in order
		flows = new LinkedList<FluidBlock>();

		running = false;
		stopped = false;
	}

	/** called when the fluid is enabled */
	public void onEnable() {
		//get the fluid flow plugin
		plugin = (FluidFlow)getServer().getPluginManager().getPlugin("FluidFlow");

		//try to register the fluid with its material type
		if((changedBlocks = plugin.registerFluid(this, getMaterial())) == null) {
			//if it could not be registered, disable the plugin and return
			System.out.println("**** ERROR! "+getDescription().getName()+": could not register fluid, check Material."+getMaterial()+" and check for a conflict ****");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		//start the fluid's asynchronous thread
		start();

		//tell the operator that the plugin is enabled
		System.out.println(getDescription().getName()+" version "+getDescription().getVersion()+" enabled");
	}

	/** called when the fluid is disabled */
	public void onDisable() {
		//tell the operator that the plugin is disabled
		System.out.println(getDescription().getName()+" version "+getDescription().getVersion()+" disabled");
	}

	/**
	 * adds a flow to the flows list
	 *
	 * @param flow the flow to add
	 */
	public void addFlow(FluidBlock flow) {
		flowChange(flow);
	}

	/**
	 * removes the first flow in the list
	 *
	 * @return the flow if there is one, null otherwise
	 */
	public FluidBlock getFlow() {
		return flowChange(null);
	}

	/**
	 * controls the adding/removal of flows in the flows list
	 *
	 * @param flow the flow to add, leave null if removing
	 *
	 * @return the flow if removing a flow and one exists, null otherwise
	 */
	public synchronized FluidBlock flowChange(FluidBlock flow) {
		FluidBlock ret = null;
		//check to see if you are adding or removing
		if(flow != null) {
			//adding
			flows.add(flow);
			ret = null;
		} else if(flows.size() > 0) {
			//removing
			ret = flows.remove();
		}
		return ret;
	}

	/** the separate thread that runs the fluid */
	public void run() {
		FluidBlock temp;

		//set the state to running and not stopped
		running = true;
		stopped = false;

		//loop while the fluid is running
		while(running) {
			//get the next flow
			temp = getFlow();

			//test to see if there wasn't a flow
			if(temp == null) {
				//wait a small amount of time
				sleep(sleepTime);
				continue;
			}

			//pass the flow to the actual flow method
			flow(temp);
		}

		//if you get here, the fluid is stopped
		stopped = true;
	}

	/**
	 * sets the fluid block to a new type
	 *
	 * @param fb the fluid block to change
	 * @param type the new type you want it changed to
	 */
	public void setType(FluidBlock fb, Material type) {
		//set newType in the fluid block
		fb.newType = type;

		//lock the changed blocks
		synchronized(changedBlocks) {
			//add this changed block to the changed block list
			changedBlocks.add(fb);
		}
	}

	/**
	 * gets the material type of a fluid block
	 *
	 * @param fb the fluid block to get the type from
	 *
	 * @return the type the fluid block points to
	 */
	public Material getType(FluidBlock fb) {
		Material ret = null;
		ChangedBlocks temp = null;

		//loop through all of the materials to get a type, first one
		//that returns a type wins
		for(Fluid f : allChanges.keySet()) {
			temp = allChanges.get(f);
			synchronized(temp) {
				ret = temp.getType(fb);
			}
			if(ret != null)
				break;
		}

		//if there was no type from the changed blocks, grab the server type
		if(ret == null)
			ret = fb.loc.getBlock().getType();

		//return the type
		return ret;
	}

	/**
	 * sleeps the fluid for an amount of time in nano-seconds
	 * CAUTION: do not use in main server thread EVER!
	 *
	 * @param time the amount of time in nano-seconds to sleep for
	 */
	private void sleep(int time) {
		try{Thread.sleep(0,time);}catch(Exception e){}
	}

	/** starts the asynchronous thread the fluid uses to run */
	private void start() {
		(new Thread(this)).start();
	}

	/** stops the asynchronous thread the fluid uses to run */
	public void stop() {
		//set running to false to stop the fluid
		running = false;

		//wait for it to stop
		while(!stopped) sleep(sleepTime);
	}

	/**
	 * abstract method to get the material used by the fluid
	 *
	 * @return the material type the fluid uses, DO NOT RETURN NULL
	 */
	public abstract Material getMaterial();

	/**
	 * abstract method that the fluid uses to flow a single block
	 *
	 * @param block the fluid block that is trying to flow
	 */
	public abstract void flow(FluidBlock block);
}
