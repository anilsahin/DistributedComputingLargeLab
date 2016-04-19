package distributed;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MessageClusterImpl extends UnicastRemoteObject implements MessageCluster{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6618982323035634161L;
	ClusterStatus status;
	ResourceManager rm;

	protected MessageClusterImpl(ResourceManager instance) throws RemoteException {
		super();
		status=ClusterStatus.Idle;
		rm=instance;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void AddJob(String name) throws RemoteException {
		// TODO Auto-generated method stub
		rm.addjob(name);
	}

	@Override
	public ClusterStatus GetStatus() throws RemoteException {
		// TODO Auto-generated method stub
		return status;
	}

	@Override
	public int getLoad() throws RemoteException {
		// TODO Auto-generated method stub
		return rm.getLoad();
	}

}
