/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distributed;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;


import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;





/**
 *
 * @author anilsahin
 */
public class Logger {
    
    
    InetAddress ip;
    
    public Logger(){
    
        try{
            
           ip=InetAddress.getLocalHost();
        
        }catch (UnknownHostException e) {
        
            e.printStackTrace();
        }
    }
    
    public String getNodeIP()
    {
        return ip.toString();
    }
    
    private void startServer(){
        try {
            // create on port 1099
            Registry registry = LocateRegistry.createRegistry(1099);
             
            // create a new service named myMessage
            registry.rebind("Logger", new RMILoggerImpl());
        
        } catch (Exception e) {
            e.printStackTrace();
        }     
        System.out.println("system is ready for logging");
    }
    
    
    
    
    public static void main(String[] args) throws IOException {
        
        Logger l = new Logger();
        System.setProperty("java.rmi.server.hostname", args[0]);
        l.startServer();
        
        
        
        //System.out.println("maraba");
    }
    
}
