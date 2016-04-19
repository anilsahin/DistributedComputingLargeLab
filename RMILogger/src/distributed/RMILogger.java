/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distributed;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anilsahin
 */
public class RMILogger extends UnicastRemoteObject implements RMILoggerInterface {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5134177547654931970L;

	@Override
    public void log(String message, String source) throws RemoteException{
    
        
	   DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	   Date date = new Date();
	   
		File file = new File("testlogger.txt");
        
        try {
            
        	file.createNewFile();
            
            try ( 
                
            	FileWriter logger = new FileWriter(file, true)) {
                
                logger.write("Source: " + source + " Message: " + message + " Date: " +  dateFormat.format(date));
                logger.flush();
                logger.close();
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(RMILogger.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    protected RMILogger() throws RemoteException{
    
        super();
        
    
    }
    
    
}
