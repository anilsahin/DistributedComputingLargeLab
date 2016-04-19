package distributed;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.jcraft.jsch.*;

import com.amazonaws.services.ec2.model.Instance;


public class ResourceManager implements Runnable {

	private HashMap<String,Integer> nodes=new HashMap<String, Integer>();/* Every node in the cluster should be here, the key will be the IP address, and the value 0-node idle, 1-node busy, -1-node down */
	private Boolean running;
	private String myIP;
	private static final int POLLING_FREQUENCY=1000;
	private List<String> jobQueue;
	private List<Instance> instances;
	private Boolean nodesInstantiated=false;
	private AWSOperations aws=AWSOperations.getInstance();
	private int jobQueueLimit = 32; // just random for now
	private String gridSchedulerIP;
	private String loggerIP;
	private static final int NODE_NR=5;
	public ResourceManager(String gridSchedulerIP,String loggerIP,String myIP)
	{
		instances=new ArrayList<Instance>();
		/* Instantiate 5 nodes here, get their IP address and put them in the nodes HashMap */
		for(int i=0;i<NODE_NR;i++)
		{
			Instance in=aws.createInstance("ami-9c11e6fc");
			instances.add(in);
			
		}
		this.gridSchedulerIP=gridSchedulerIP;
		this.loggerIP=loggerIP;
		this.myIP=myIP;
		running=true;
		jobQueue=new ArrayList<String>();
		startServer();
		connectToGridScheduler(gridSchedulerIP);
		Thread t=new Thread(this);
		t.start();
		
		
	}
	
	public String getGridSchedulerIP() {
		return gridSchedulerIP;
	}

	public void setGridSchedulerIP(String gridSchedulerIP) {
		this.gridSchedulerIP = gridSchedulerIP;
	}
	public NodeStatus getNodeStatus(String nodeIP)
	{
		Registry myRegistry;
		try {
			myRegistry = LocateRegistry.getRegistry(nodeIP, 1099);

	        // search for myMessage service
	        MessageNode impl = (MessageNode) myRegistry.lookup("Node");
	        return impl.GetStatus();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return NodeStatus.Down;
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return NodeStatus.Down;
		}
        
	}
	public void SendJobToNode(String nodeIP,String jobName)
	{
		Registry myRegistry;
		try {
			myRegistry = LocateRegistry.getRegistry(nodeIP, 1099);
			
			// search for myMessage service
	        MessageNode impl = (MessageNode) myRegistry.lookup("Node");
	        impl.AddJob(jobName);
	        Log("Resource manager sent job to node:"+nodeIP+",job name:"+jobName);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
	}
	public void sendJobToGridScheduler(String gridSchedulerIP,String jobName)
	{
		Registry myRegistry;
		try {
			myRegistry = LocateRegistry.getRegistry(gridSchedulerIP, 1099);

			// search for myMessage service
	        MessageGridScheduler impl = (MessageGridScheduler) myRegistry.lookup("GridScheduler");
	        impl.sendJob(jobName);
	        Log("Resource manager sent job to grid scheduler:"+gridSchedulerIP+",job name:"+jobName);
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
		System.out.println("Logging message:"+message);
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
	public String getFreeNode()
	{
		for(String key:nodes.keySet())
		{
			if(nodes.get(key)==0)
			{
				return key;
			}
		}
		return null;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(running)
		{
			if(nodesInstantiated!=true)
			{
				try {
					Thread.sleep(80000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for(int i=0;i<NODE_NR;i++){
				/*DescribeInstanceStatusRequest describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(instances.get(i).getInstanceId());
				DescribeInstanceStatusResult describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				List<InstanceStatus> state = describeInstanceResult.getInstanceStatuses();
				while (state.size() < 1) { 
				    // Do nothing, just wait, have thread sleep if needed
				    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				    state = describeInstanceResult.getInstanceStatuses();
				}*/
					
				Instance ins=aws.getInstance(instances.get(i).getInstanceId());
				nodes.put(ins.getPublicIpAddress(), 0);
				System.out.println("Node["+i+"]="+ins.getPublicIpAddress());
				}
				nodesInstantiated=true;
			}
			else
			{
			for(String key:nodes.keySet())
			{
				try{
				Registry myRegistry = LocateRegistry.getRegistry(key, 1099);
		        
		        // search for myMessage service
		        MessageNode impl = (MessageNode) myRegistry.lookup("Node");
		        if(impl.GetStatus()==NodeStatus.Idle)
		        	nodes.replace(key,0);
		        if(impl.GetStatus()==NodeStatus.Busy)
		        	nodes.replace(key,1);
				if(impl.GetStatus()==NodeStatus.Down)
					nodes.replace(key,-1);
				}
				catch(RemoteException rex)
				{
					nodes.replace(key,-1);
					rex.printStackTrace();
				} catch (NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					nodes.replace(key,-1);
				}
		        
			}
			if(jobQueue.size()>0)
			{
				String freeNodeIP=getFreeNode();
				if(freeNodeIP!=null)
				{
					SendJobToNode(freeNodeIP, jobQueue.get(0));
					jobQueue.remove(0);
				}
			}
			for(String key:nodes.keySet())
				System.out.println("Key:"+key+"Status:"+nodes.get(key));
			try {
				Thread.sleep(POLLING_FREQUENCY);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
		}
	}
	public void addjob(String jobName)
		{
		System.out.println("Queue size:"+jobQueue.size());
			Log("Resource manager received Job:"+jobName);
			/*String freeNodeIP=getFreeNode();
			if(freeNodeIP!=null){
			
			SendJobToNode(freeNodeIP, jobName);
			}
			else */
			if ( jobQueue.size() < jobQueueLimit )// then i guess in here it should check if job queue is full, then send it to a gridScheduler
			{
				
				jobQueue.add(jobName);
				Log("Resource manager queued job, name:"+jobName);
			}
			else{
				sendJobToGridScheduler(gridSchedulerIP, jobName);
				
			}
		}
	public int getLoad()
		{
			int freeSpots=0;
			for(String key:nodes.keySet())
			{
				if(nodes.get(key)==0)
					freeSpots++;
			}
			freeSpots+=jobQueueLimit-jobQueue.size();
			return freeSpots;
		}
	
	public void connectToGridScheduler(String gridSchedulerIP)
	{
		Registry myRegistry;
		try {
			
			myRegistry = LocateRegistry.getRegistry(gridSchedulerIP, 1099);
			
			
			// search for myMessage service
	        MessageGridScheduler impl = (MessageGridScheduler) myRegistry.lookup("GridScheduler");
	        
	        //Log("Resource Manager connected to Grid Scheduler:"+gridSchedulerIP);
	        impl.connect(myIP);
	      
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void startServer(){
	        try {
	            // create on port 1099
	            Registry registry = LocateRegistry.createRegistry(1099);

	            // create a new service named myMessage
	            registry.rebind("ResourceManager", new MessageClusterImpl(this));
	        
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        Log("Resource manager server started, RM IP:"+myIP);
	        System.out.println(System.getProperty("java.rmi.server.hostname").toString());
	        System.out.println("system is ready");
	    }
	public void executeSSHCommand(String command,HashMap<String,Integer> nodes)
	{
		
		for(String key:nodes.keySet()){
			System.out.println("Executing SSH Command on Node:"+key);
		try{
			JSch jsch=new JSch();
			jsch.addIdentity("XXXXXXXXX");
			JSch.setConfig("StrictHostKeyChecking", "no");

			//enter your own EC2 instance IP here
			Session session=jsch.getSession("ec2-user", key, 22);
			session.connect();

			//run stuff
			
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand("java -jar Node.jar "+key+" "+loggerIP);
			((ChannelExec) channel).setErrStream(System.err);
			channel.connect();

			/*InputStream input = channel.getInputStream();
			StringBuilder sb=new StringBuilder();
			//start reading the input from the executed commands on the shell
			byte[] tmp = new byte[1024];
			while (true) {
				while (input.available() > 0) {
					int i = input.read(tmp, 0, 1024);
					if (i < 0) break;
					sb.append(new String(tmp, 0, i));
				}
				if (channel.isClosed()){
					System.out.println("exit-status: " + channel.getExitStatus());
					break;
				}
				Thread.sleep(1000);
			}
*/
			channel.disconnect();
			session.disconnect();
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			
		}
		}
	}
	public static void main(String[] args) {
		System.setProperty("java.rmi.server.hostname", args[0]);
        @SuppressWarnings("unused")
		ResourceManager rm=new ResourceManager(args[1],args[2],args[0]);
    }
}
