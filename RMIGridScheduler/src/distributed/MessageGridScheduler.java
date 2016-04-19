package distributed;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageGridScheduler extends Remote {

    void sendJob(String jobName) throws RemoteException;
    void connect(String resourceManagerIP) throws RemoteException;
    GridSchedulerStatus getStatus() throws RemoteException;
    int getGridSchedulerLoad() throws RemoteException;
}
