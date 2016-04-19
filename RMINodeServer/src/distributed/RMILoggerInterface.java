/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distributed;

import java.rmi.Remote;
import java.rmi.RemoteException;
/**
 *
 * @author anilsahin
 */
public interface RMILoggerInterface extends Remote{
    
    public void log(String message, String source) throws RemoteException;
    
}
