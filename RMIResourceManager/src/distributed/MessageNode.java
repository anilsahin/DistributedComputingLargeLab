package distributed;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageNode extends Remote {
void AddJob(String name) throws RemoteException;
NodeStatus GetStatus() throws RemoteException;
}
