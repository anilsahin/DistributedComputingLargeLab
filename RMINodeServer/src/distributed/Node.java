package distributed;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Node {

	String myIP;
	//String loggerIP;
	public Node(String myIP){
		this.myIP=myIP;
		//this.loggerIP=loggerIP;
		startServer();
	}
	
	public String getNodeIP()
	{
		return myIP;
	}
	/*public void Log(String message)
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
	}*/
	private void startServer(){
        try {
            // create on port 1099
            Registry registry = LocateRegistry.createRegistry(1099);
            
            // create a new service named myMessage
            registry.rebind("Node", new MessageNodeImpl(this));
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Log("Node server started");
        System.out.println("IP:"+myIP);
        System.out.println(System.getProperty("java.rmi.server.hostname").toString());
        System.out.println("system is ready");
    }
	
	public static void main(String[] args) {
		System.setProperty("java.rmi.server.hostname", args[0]);
        Node n=new Node(args[0]);
    }
}
