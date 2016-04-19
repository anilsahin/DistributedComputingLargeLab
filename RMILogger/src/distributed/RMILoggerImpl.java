/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distributed;

import java.io.BufferedWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 *
 * @author anilsahin
 */
public class RMILoggerImpl extends UnicastRemoteObject implements RMILoggerInterface {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5134177547654931970L;

	@Override
    public void log(String message, String source) throws RemoteException{
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		try(FileWriter fw = new FileWriter("testlogger.txt", true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw))
			{
			    out.println("Source: " + source + " Message: " + message + " Date: " +  dateFormat.format(date));
			} catch (IOException e) {
			    //exception handling left as an exercise for the reader
				e.printStackTrace();
			}
        
	   /*
	   
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
            Logger.getLogger(RMILoggerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
        
    }
    
    protected RMILoggerImpl() throws RemoteException{
    
        super();
        
    
    }
    
    
}
