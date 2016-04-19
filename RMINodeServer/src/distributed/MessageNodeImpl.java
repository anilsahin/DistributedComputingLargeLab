package distributed;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MessageNodeImpl extends UnicastRemoteObject implements MessageNode {
/**
	 * 
	 */
	private static final long serialVersionUID = 3917610263966400169L;

	NodeStatus status;
	Node instance;
	protected MessageNodeImpl(Node instance) throws RemoteException {
		super();
		this.instance=instance;
		status=NodeStatus.Idle;
		// TODO Auto-generated constructor stub
	}

	public void AddJob(String name) throws RemoteException {
		// TODO Auto-generated method stub
		//instance.Log("Job started on Node, job name:"+name);
		Thread t=new Thread(new Runnable(){
		
			public void run() {
				try {
					Thread.sleep(20000);
					status=NodeStatus.Idle;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		);
		
		if(status!=NodeStatus.Busy)
		{
			t.start();
			status=NodeStatus.Busy;
		}
		
	}

	public NodeStatus GetStatus() throws RemoteException {
		// TODO Auto-generated method stub
		return status;
	}

}
