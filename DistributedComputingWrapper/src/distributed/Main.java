package distributed;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.jcraft.jsch.*;


public class Main {

	static AWSOperations aws=AWSOperations.getInstance();
	
	public static String loggerImage="ami-944db9f4";
	public static String resourceManagerImage="ami-a77186c7";
	public static String gridSchedulerImage="ami-c37285a3";
	public static void executeSSHCommand(String command,String IP)
	{
		try{
			JSch jsch=new JSch();
			jsch.addIdentity("XXXXXXXXXXXXXXXXX");
			jsch.setConfig("StrictHostKeyChecking", "no");

			//enter your own EC2 instance IP here
			Session session=jsch.getSession("ec2-user", IP, 22);
			session.connect();

			//run stuff
			
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			((ChannelExec) channel).setErrStream(System.err);
			channel.connect();

			InputStream input = channel.getInputStream();
			StringBuilder sb=new StringBuilder();
			//start reading the input from the executed commands on the shell
			byte[] tmp = new byte[1024];
			while (true) {
				while (input.available() > 0) {
					int i = input.read(tmp, 0, 1024);
					if (i < 0) break;
					sb.append(new String(tmp, 0, i));
				}
				break;
				/*if (channel.isClosed()){
					System.out.println("exit-status: " + channel.getExitStatus());
					break;
				}
				Thread.sleep(1000);*/
			}

			channel.disconnect();
			session.disconnect();
			System.out.println(sb.toString());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			
		}
	}
	public static void submitJobToRM(String rmIP,String jobName)
	{
		Registry myRegistry;
		try {
			
			myRegistry = LocateRegistry.getRegistry(rmIP, 1099);
			
			// search for myMessage service
	        MessageCluster impl = (MessageCluster) myRegistry.lookup("ResourceManager");
	        impl.AddJob(jobName);        
	        
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		
		//create a logger instance
		
		Instance loggerIns=aws.createInstance(loggerImage);
		
		DescribeInstanceStatusRequest describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(loggerIns.getInstanceId());
		DescribeInstanceStatusResult describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
		List<InstanceStatus> state = describeInstanceResult.getInstanceStatuses();
		while (state.size() < 1) { 
		    // Do nothing, just wait, have thread sleep if needed
		    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
		    state = describeInstanceResult.getInstanceStatuses();
		}
		
		try {
			Thread.sleep(75000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//logger has been initialized
		Instance loggerInstance=aws.getInstance(loggerIns.getInstanceId());
		System.out.println("Logger initialized with public IP:"+loggerInstance.getPublicIpAddress());
		executeSSHCommand("java -jar Logger.jar "+loggerInstance.getPublicIpAddress()+" &", loggerInstance.getPublicIpAddress());
		
		Instance gridSchedulerIns=aws.createInstance(gridSchedulerImage);
		Instance gridSchedulerIns1=aws.createInstance(gridSchedulerImage);
		Instance gridSchedulerIns2=aws.createInstance(gridSchedulerImage);
		Instance gridSchedulerIns3=aws.createInstance(gridSchedulerImage);
		
		describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(gridSchedulerIns.getInstanceId());
		describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
		state = describeInstanceResult.getInstanceStatuses();
		while (state.size() < 1) { 
		    // Do nothing, just wait, have thread sleep if needed
		    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
		    state = describeInstanceResult.getInstanceStatuses();
		}
		describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(gridSchedulerIns1.getInstanceId());
		describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
		state = describeInstanceResult.getInstanceStatuses();
		while (state.size() < 1) { 
		    // Do nothing, just wait, have thread sleep if needed
		    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
		    state = describeInstanceResult.getInstanceStatuses();
		}
		describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(gridSchedulerIns2.getInstanceId());
		describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
		state = describeInstanceResult.getInstanceStatuses();
		while (state.size() < 1) { 
		    // Do nothing, just wait, have thread sleep if needed
		    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
		    state = describeInstanceResult.getInstanceStatuses();
		}
		try {
			Thread.sleep(75000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//grid scheduler has been initialized
		Instance gridSchedulerInstance=aws.getInstance(gridSchedulerIns.getInstanceId());
		Instance gridSchedulerInstance1=aws.getInstance(gridSchedulerIns1.getInstanceId());
		Instance gridSchedulerInstance2=aws.getInstance(gridSchedulerIns2.getInstanceId());
		System.out.println("GridScheduler initialized with public IP:"+gridSchedulerInstance.getPublicIpAddress());
		System.out.println("GridScheduler 1 initialized with public IP:"+gridSchedulerInstance1.getPublicIpAddress());
		System.out.println("GridScheduler 2 initialized with public IP:"+gridSchedulerInstance2.getPublicIpAddress());
		//executeSSHCommand("java -jar GridScheduler.jar "+gridSchedulerInstance.getPublicIpAddress()+" "+loggerInstance.getPublicIpAddress()+" &", gridSchedulerInstance.getPublicIpAddress());
		
		
		//first resource manager
		Instance resourceManagerIns=aws.createInstance(resourceManagerImage);
		Instance resourceManagerIns1=aws.createInstance(resourceManagerImage);
		Instance resourceManagerIns2=aws.createInstance(resourceManagerImage);
		Instance resourceManagerIns3=aws.createInstance(resourceManagerImage);
		Instance resourceManagerIns4=aws.createInstance(resourceManagerImage);
		Instance resourceManagerIns5=aws.createInstance(resourceManagerImage);
		Instance resourceManagerIns6=aws.createInstance(resourceManagerImage);
		Instance resourceManagerIns7=aws.createInstance(resourceManagerImage);
		Instance resourceManagerIns8=aws.createInstance(resourceManagerImage);
		Instance resourceManagerIns9=aws.createInstance(resourceManagerImage);
		Instance resourceManagerIns10=aws.createInstance(resourceManagerImage);
		Instance resourceManagerIns11=aws.createInstance(resourceManagerImage);
		Instance resourceManagerIns12=aws.createInstance(resourceManagerImage);
		Instance resourceManagerIns13=aws.createInstance(resourceManagerImage);
		Instance resourceManagerIns14=aws.createInstance(resourceManagerImage);
		

		describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(resourceManagerIns.getInstanceId());
		describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
		state = describeInstanceResult.getInstanceStatuses();
		while (state.size() < 1) { 
		    // Do nothing, just wait, have thread sleep if needed
		    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
		    state = describeInstanceResult.getInstanceStatuses();
		}
		//second rm
		
		describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(resourceManagerIns1.getInstanceId());
		describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
		state = describeInstanceResult.getInstanceStatuses();
		while (state.size() < 1) { 
		    // Do nothing, just wait, have thread sleep if needed
		    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
		    state = describeInstanceResult.getInstanceStatuses();
		}
		
		//third rm
		
		describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(resourceManagerIns2.getInstanceId());
		describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
		state = describeInstanceResult.getInstanceStatuses();
		while (state.size() < 1) { 
		    // Do nothing, just wait, have thread sleep if needed
		    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
		    state = describeInstanceResult.getInstanceStatuses();
		}
		
		//fourth rm
		
		describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(resourceManagerIns3.getInstanceId());
		describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
		state = describeInstanceResult.getInstanceStatuses();
		while (state.size() < 1) { 
		    // Do nothing, just wait, have thread sleep if needed
		    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
		    state = describeInstanceResult.getInstanceStatuses();
		}
		
		//fifth rm
				
				describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(resourceManagerIns4.getInstanceId());
				describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				state = describeInstanceResult.getInstanceStatuses();
				while (state.size() < 1) { 
				    // Do nothing, just wait, have thread sleep if needed
				    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				    state = describeInstanceResult.getInstanceStatuses();
				}
				//sixth rm
				
				describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(resourceManagerIns5.getInstanceId());
				describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				state = describeInstanceResult.getInstanceStatuses();
				while (state.size() < 1) { 
				    // Do nothing, just wait, have thread sleep if needed
				    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				    state = describeInstanceResult.getInstanceStatuses();
				}
				describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(resourceManagerIns6.getInstanceId());
				describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				state = describeInstanceResult.getInstanceStatuses();
				while (state.size() < 1) { 
				    // Do nothing, just wait, have thread sleep if needed
				    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				    state = describeInstanceResult.getInstanceStatuses();
				}
				describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(resourceManagerIns7.getInstanceId());
				describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				state = describeInstanceResult.getInstanceStatuses();
				while (state.size() < 1) { 
				    // Do nothing, just wait, have thread sleep if needed
				    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				    state = describeInstanceResult.getInstanceStatuses();
				}
				describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(resourceManagerIns8.getInstanceId());
				describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				state = describeInstanceResult.getInstanceStatuses();
				while (state.size() < 1) { 
				    // Do nothing, just wait, have thread sleep if needed
				    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				    state = describeInstanceResult.getInstanceStatuses();
				}
				describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(resourceManagerIns9.getInstanceId());
				describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				state = describeInstanceResult.getInstanceStatuses();
				while (state.size() < 1) { 
				    // Do nothing, just wait, have thread sleep if needed
				    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				    state = describeInstanceResult.getInstanceStatuses();
				}
				describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(resourceManagerIns10.getInstanceId());
				describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				state = describeInstanceResult.getInstanceStatuses();
				while (state.size() < 1) { 
				    // Do nothing, just wait, have thread sleep if needed
				    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				    state = describeInstanceResult.getInstanceStatuses();
				}
				describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(resourceManagerIns11.getInstanceId());
				describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				state = describeInstanceResult.getInstanceStatuses();
				while (state.size() < 1) { 
				    // Do nothing, just wait, have thread sleep if needed
				    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				    state = describeInstanceResult.getInstanceStatuses();
				}
				describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(resourceManagerIns12.getInstanceId());
				describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				state = describeInstanceResult.getInstanceStatuses();
				while (state.size() < 1) { 
				    // Do nothing, just wait, have thread sleep if needed
				    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				    state = describeInstanceResult.getInstanceStatuses();
				}
				describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(resourceManagerIns13.getInstanceId());
				describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				state = describeInstanceResult.getInstanceStatuses();
				while (state.size() < 1) { 
				    // Do nothing, just wait, have thread sleep if needed
				    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				    state = describeInstanceResult.getInstanceStatuses();
				}
				describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(resourceManagerIns14.getInstanceId());
				describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				state = describeInstanceResult.getInstanceStatuses();
				while (state.size() < 1) { 
				    // Do nothing, just wait, have thread sleep if needed
				    describeInstanceResult = AWSOperations.ec2.describeInstanceStatus(describeInstanceRequest);
				    state = describeInstanceResult.getInstanceStatuses();
				}
				
		
		try {
			Thread.sleep(75000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//resource manager has been initialized
		Instance resourceManagerInstance=aws.getInstance(resourceManagerIns.getInstanceId());
		Instance resourceManagerInstance1=aws.getInstance(resourceManagerIns1.getInstanceId());
		Instance resourceManagerInstance2=aws.getInstance(resourceManagerIns2.getInstanceId());
		Instance resourceManagerInstance3=aws.getInstance(resourceManagerIns3.getInstanceId());
		Instance resourceManagerInstance4=aws.getInstance(resourceManagerIns4.getInstanceId());
		Instance resourceManagerInstance5=aws.getInstance(resourceManagerIns5.getInstanceId());
		Instance resourceManagerInstance6=aws.getInstance(resourceManagerIns6.getInstanceId());
		Instance resourceManagerInstance7=aws.getInstance(resourceManagerIns7.getInstanceId());
		Instance resourceManagerInstance8=aws.getInstance(resourceManagerIns8.getInstanceId());
		Instance resourceManagerInstance9=aws.getInstance(resourceManagerIns9.getInstanceId());
		Instance resourceManagerInstance10=aws.getInstance(resourceManagerIns10.getInstanceId());
		Instance resourceManagerInstance11=aws.getInstance(resourceManagerIns11.getInstanceId());
		Instance resourceManagerInstance12=aws.getInstance(resourceManagerIns12.getInstanceId());
		Instance resourceManagerInstance13=aws.getInstance(resourceManagerIns13.getInstanceId());
		Instance resourceManagerInstance14=aws.getInstance(resourceManagerIns14.getInstanceId());
		
		System.out.println("Resource manager initialized with public IP:"+resourceManagerInstance.getPublicIpAddress());
		System.out.println("Resource manager 1 initialized with public IP:"+resourceManagerInstance1.getPublicIpAddress());
		System.out.println("Resource manager 2 initialized with public IP:"+resourceManagerInstance2.getPublicIpAddress());
		System.out.println("Resource manager 3 initialized with public IP:"+resourceManagerInstance3.getPublicIpAddress());
		System.out.println("Resource manager 4 initialized with public IP:"+resourceManagerInstance4.getPublicIpAddress());
		System.out.println("Resource manager 5 initialized with public IP:"+resourceManagerInstance5.getPublicIpAddress());
		System.out.println("Resource manager 6 initialized with public IP:"+resourceManagerInstance6.getPublicIpAddress());
		System.out.println("Resource manager 7 initialized with public IP:"+resourceManagerInstance7.getPublicIpAddress());
		System.out.println("Resource manager 8 initialized with public IP:"+resourceManagerInstance8.getPublicIpAddress());
		System.out.println("Resource manager 9 initialized with public IP:"+resourceManagerInstance9.getPublicIpAddress());
		System.out.println("Resource manager 10 initialized with public IP:"+resourceManagerInstance10.getPublicIpAddress());
		System.out.println("Resource manager 11 initialized with public IP:"+resourceManagerInstance11.getPublicIpAddress());
		System.out.println("Resource manager 12 initialized with public IP:"+resourceManagerInstance12.getPublicIpAddress());
		System.out.println("Resource manager 13 initialized with public IP:"+resourceManagerInstance13.getPublicIpAddress());
		System.out.println("Resource manager 14 initialized with public IP:"+resourceManagerInstance14.getPublicIpAddress());
		
		//executeSSHCommand("java -jar ResourceManager.jar "+resourceManagerInstance.getPublicIpAddress()+" "+gridSchedulerInstance.getPublicIpAddress()+" "+loggerInstance.getPublicIpAddress()+" &", resourceManagerInstance.getPublicIpAddress());
		
		
		Scanner s=new Scanner(System.in);
		int experimentnumber=s.nextInt();
		for(int i=0;i<100;i++)
			submitJobToRM(resourceManagerInstance.getPublicIpAddress(), "Experiment number:"+experimentnumber+"["+i+"]");
		
    }
	
	
}
