package distributed;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageCluster extends Remote {
void AddJob(String name) throws RemoteException;
ClusterStatus GetStatus() throws RemoteException;
int getLoad() throws RemoteException;
}
