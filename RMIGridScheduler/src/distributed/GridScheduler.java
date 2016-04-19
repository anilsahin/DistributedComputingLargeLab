package distributed;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GridScheduler implements Runnable{

	private Boolean running;
	
	private static final int POLLING_FREQUENCY=1000;
	private String myIP;
	private String loggerIP;
	/* just represented as hashMap for now to have the variable here. TODO: Find suitable representation for cluster/resourceManagers */
	/* From Class Resource Manager as reference:
	 * Every node in the cluster should be here, the key will be the IP address, and the value 0-node idle, 1-node busy, -1-node down */
	
	private HashMap<String,Integer> clusters;
	// For now cluster refers to resource manager and vice versa
	
	
	
	// representation of other gridSchedulerNodes for now
	// similar as clusters, status this time is taken from GridSchedulerStatus
	private HashMap<String,Integer> gridSchedulerNodes; 
	
	private List<String> jobQueue;
	
	public GridScheduler(String myIP,String loggerIP){
		
		running=true;
		this.myIP=myIP;
		this.loggerIP=loggerIP;
		jobQueue=new ArrayList<String>();
		clusters=new HashMap<String, Integer>();
		gridSchedulerNodes=new HashMap<String, Integer>();
		Thread t=new Thread(this);
		t.start();
		
	}
	
	
	public ClusterStatus getClusterStatus(String clusterIP)
	{
		Registry myRegistry;
		try {
			myRegistry = LocateRegistry.getRegistry(clusterIP, 1099);

	        // search for myMessage service
			MessageCluster impl = (MessageCluster) myRegistry.lookup("ResourceManager"); // could also be cluster
	        return impl.GetStatus();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ClusterStatus.Down;
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ClusterStatus.Down;
		}
        
	}
	
	public int getClusterLoad(String clusterIP)
	{
		Registry myRegistry;
		try {
			myRegistry = LocateRegistry.getRegistry(clusterIP, 1099);

	        // search for myMessage service
			MessageCluster impl = (MessageCluster) myRegistry.lookup("ResourceManager"); // could also be cluster
	        return impl.getLoad();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	
	public int getGridSchedulerLoad()
	{
		int totalFreeSlots=0;
		for(String key:clusters.keySet())
		{
			totalFreeSlots+=getClusterLoad(key);
		}
		return totalFreeSlots;
	}
	
	public int getGridSchedulerLoad(String gsIP)
	{
		Registry myRegistry;
		try {
			myRegistry = LocateRegistry.getRegistry(gsIP, 1099);
			
			// search for myMessage service
			MessageGridScheduler impl = (MessageGridScheduler) myRegistry.lookup("GridScheduler"); // could also be cluster
	        return impl.getGridSchedulerLoad();
	        } catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	// again instead of sendToNode it should be send to ResourceManager
	public void SendJobToCluster(String clusterIP,String jobName)
	{
		Registry myRegistry;
		try {
			myRegistry = LocateRegistry.getRegistry(clusterIP, 1099);
			
			// search for myMessage service
			MessageCluster impl = (MessageCluster) myRegistry.lookup("ResourceManager"); // could also be cluster
	        impl.AddJob(jobName);
	        Log("Grid Scheduler sent job to cluster:"+clusterIP+",job name:"+jobName);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
	}
			
	public void offloadJob(String jobName)
	{
		int maxFreeSlots=0;
		String maxFreeSlotsClusterIP=null;
		for(String key:clusters.keySet())
		{
			int currFreeSlots=clusters.get(key);
			if(currFreeSlots>maxFreeSlots)
			{
				maxFreeSlots=currFreeSlots;
				maxFreeSlotsClusterIP=key;
			}
		}
		if(maxFreeSlots>0)
		{
			SendJobToCluster(maxFreeSlotsClusterIP, jobName);
		}
		else
		{
			//there is not a cluster with free slots, let's find a grid scheduler with free slots
			int maxFreeSlotsGS=0;
			String maxFreeSlotsGsIP=null;
			for(String key:gridSchedulerNodes.keySet())
			{
				int currFreeSlots=gridSchedulerNodes.get(key);
				if(currFreeSlots>maxFreeSlotsGS)
				{
					maxFreeSlotsGS=currFreeSlots;
					maxFreeSlotsGsIP=key;
				}
			}
			if(maxFreeSlotsGS>0)
			{
				sendJobToOtherGridScheduler(maxFreeSlotsGsIP, jobName);
			}
			else
				jobQueue.add(jobName);
			
		}
	}
	
	public void sendJobToOtherGridScheduler(String GSIP,String jobName)
	{
		Registry myRegistry;
		try {
			myRegistry = LocateRegistry.getRegistry(GSIP, 1099);
			
			// search for myMessage service
			MessageGridScheduler impl = (MessageGridScheduler) myRegistry.lookup("GridScheduler"); // could also be cluster
	        impl.sendJob(jobName);
	        Log("Grid Scheduler sent job to other grid scheduler:"+GSIP+",job name:"+jobName);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void Log(String message)
	{
		Registry myRegistry;
		try {
			myRegistry = LocateRegistry.getRegistry(loggerIP, 1099);

			// search for myMessage service
	        RMILoggerInterface impl = (RMILoggerInterface) myRegistry.lookup("Logger");
	        impl.log(message,myIP);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
		// TODO Auto-generated method stub
		while(running)
		{
			for(String key:clusters.keySet())
			{
				clusters.replace(key,getClusterLoad(key));		        
			}
			for(String key:gridSchedulerNodes.keySet())
			{	
				gridSchedulerNodes.replace(key,getGridSchedulerLoad(key));
			}
			if(jobQueue.size()>0)
			{
				offloadJob(jobQueue.get(0));
				jobQueue.remove(0);
			}
			for(String key:clusters.keySet())
			{
				System.out.println("Cluster:"+key+", Load:"+clusters.get(key));		        
			}
			for(String key:gridSchedulerNodes.keySet())
			{	
				System.out.println("Grid Scheduler:"+key+", Load:"+gridSchedulerNodes.get(key));
			}
			try {
				Thread.sleep(POLLING_FREQUENCY);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void connectResourceManager(String rmIP)
	{
		clusters.put(rmIP, 0);
		Log("Resource manager with IP:"+rmIP+",connected to Grid Scheduler");
	}
	public void addGridScheduler(String gsIP)
	{
		gridSchedulerNodes.put(gsIP, 0);
		Log("Grid Scheduler with IP:"+gsIP+",connected to Grid Scheduler");
	}
	private void startServer(){
        try {
            // create on port 1099
            Registry registry = LocateRegistry.createRegistry(1099);

            // create a new service named GridScheduler
            registry.rebind("GridScheduler", new MessageGridSchedulerImpl(this));
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log("Grid Scheduler server started");
        System.out.println(System.getProperty("java.rmi.server.hostname").toString());
        System.out.println("system is ready");
    }
	public static void main(String[] args) {
		System.setProperty("java.rmi.server.hostname", args[0]);
        GridScheduler gs=new GridScheduler(args[0],args[1]);
        if(args.length>2)
        {
        	for(int i=2;i<args.length;i++)
        		gs.addGridScheduler(args[i]);
        }
        gs.startServer();
    }
}


