import java.io.InputStream;
import java.util.List;

import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.*;
import com.jcraft.jsch.Session;


public class Main {

	static AWSOperations aws=AWSOperations.getInstance();
	
	public static String loggerImage="ami-944db9f4";
	public static String resourceManagerImage="ami-ba4db9da";
	public static String gridSchedulerImage="ami-bf48bcdf";
	public static void executeSSHCommand(String command,String IP)
	{
		try{
			JSch jsch=new JSch();
			jsch.addIdentity("xxxxxxxxxxxxxxxxxxxxxxx");
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
		describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(gridSchedulerIns.getInstanceId());
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
		System.out.println("GridScheduler initialized with public IP:"+gridSchedulerInstance.getPublicIpAddress());
		executeSSHCommand("java -jar GridScheduler.jar "+gridSchedulerInstance.getPublicIpAddress()+" "+loggerInstance.getPublicIpAddress()+" &", gridSchedulerInstance.getPublicIpAddress());
		
		Instance resourceManagerIns=aws.createInstance(resourceManagerImage);
		describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(resourceManagerIns.getInstanceId());
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
		System.out.println("Resource manager initialized with public IP:"+resourceManagerInstance.getPublicIpAddress());
		executeSSHCommand("java -jar ResourceManager.jar "+gridSchedulerInstance.getPublicIpAddress()+" "+loggerInstance.getPublicIpAddress()+" "+resourceManagerInstance.getPublicIpAddress()+" &", resourceManagerInstance.getPublicIpAddress());
		
		
    }
	
	
}
