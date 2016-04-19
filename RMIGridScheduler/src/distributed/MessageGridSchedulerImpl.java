package distributed;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MessageGridSchedulerImpl extends UnicastRemoteObject implements MessageGridScheduler
{

	GridSchedulerStatus status;
	GridScheduler gsinstance;
	/**
	 * 
	 */
	private static final long serialVersionUID = 7652730440968687865L;


	protected MessageGridSchedulerImpl(GridScheduler instance) throws RemoteException {
		super();
		status=GridSchedulerStatus.Idle;
		gsinstance=instance;
		// TODO Auto-generated constructor stub
	}


	public void sendJob(String jobName) throws RemoteException {
		// TODO Auto-generated method stub
		gsinstance.offloadJob(jobName);
		
	}

	
	public void connect(String resourceManagerIP) throws RemoteException {
		// TODO Auto-generated method stub
		gsinstance.connectResourceManager(resourceManagerIP);
	}

	
	public GridSchedulerStatus getStatus() throws RemoteException {
		// TODO Auto-generated method stub
		return status;
	}


	public int getGridSchedulerLoad() throws RemoteException {
		// TODO Auto-generated method stub
		return gsinstance.getGridSchedulerLoad();
	}
	

}