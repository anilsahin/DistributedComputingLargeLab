package distributed;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import java.util.List;

import org.apache.commons.codec.binary.Base64;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

import com.amazonaws.auth.BasicAWSCredentials;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;

import com.amazonaws.services.ec2.model.DescribeInstancesResult;

import com.amazonaws.services.ec2.model.Instance;

import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;


/**
 * Welcome to your new AWS Java SDK based project!
 *
 * This class is meant as a starting point for your console-based application that
 * makes one or more calls to the AWS services supported by the Java SDK, such as EC2,
 * SimpleDB, and S3.
 *
 * In order to use the services in this sample, you need:
 *
 *  - A valid Amazon Web Services account. You can register for AWS at:
 *       https://aws-portal.amazon.com/gp/aws/developer/registration/index.html
 *
 *  - Your account's Access Key ID and Secret Access Key:
 *       http://aws.amazon.com/security-credentials
 *
 *  - A subscription to Amazon EC2. You can sign up for EC2 at:
 *       http://aws.amazon.com/ec2/
 *
 *  - A subscription to Amazon SimpleDB. You can sign up for Simple DB at:
 *       http://aws.amazon.com/simpledb/
 *
 *  - A subscription to Amazon S3. You can sign up for S3 at:
 *       http://aws.amazon.com/s3/
 */
public class AWSOperations {

    /*
     * Before running the code:
     *      Fill in your AWS access credentials in the provided credentials
     *      file template, and be sure to move the file to the default location
     *      (C:\\Users\\Nikola\\.aws\\credentials) where the sample code will load the
     *      credentials from.
     *      https://console.aws.amazon.com/iam/home?#security_credential
     *
     * WARNING:
     *      To avoid accidental leakage of your credentials, DO NOT keep
     *      the credentials file in your source directory.
     */

    static AmazonEC2      ec2;
    static AmazonS3       s3;
    static AmazonSimpleDB sdb;
    private static AWSOperations singleton;
    private AWSOperations()
    {
    	init();
    }
    public static AWSOperations getInstance()
    {
    	if(singleton==null)
    		singleton=new AWSOperations();
    	return singleton;
    }
    /**
     * The only information needed to create a client are security credentials
     * consisting of the AWS Access Key ID and Secret Access Key. All other
     * configuration, such as the service endpoints, are performed
     * automatically. Client parameters, such as proxies, can be specified in an
     * optional ClientConfiguration object when constructing a client.
     *
     * @see com.amazonaws.auth.BasicAWSCredentials
     * @see com.amazonaws.auth.PropertiesCredentials
     * @see com.amazonaws.ClientConfiguration
     */
    private static void init(){

        /*
         * The ProfileCredentialsProvider will return your [nikola]
         * credential profile by reading from the credentials file located at
         * (C:\\Users\\Nikola\\.aws\\credentials).
         */
        BasicAWSCredentials credentials = null;
        try {
            //credentials = new ProfileCredentialsProvider("nikola").getCredentials();
//            credentials = new ProfileCredentialsProvider("default").getCredentials();
            //credentials = new DefaultAWSCredentialsProviderChain();
            credentials = new BasicAWSCredentials("XXXXXXXXXXXXXXXXXXXX ", "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (C:\\Users\\Nikola\\.aws\\credentials), and is in valid format.",
                    e);
        }
        ec2 = new AmazonEC2Client(credentials);
        s3  = new AmazonS3Client(credentials);
        sdb = new AmazonSimpleDBClient(credentials);
//        ec2.setEndpoint("ec2.us-west-2.amazonaws.com");
        ec2.setEndpoint("ec2.us-west-2.amazonaws.com");
        

    }
    public Instance createInstance(String imageName)
    {
        
    	try
        {
   		
    		
         RunInstancesRequest runInstancesRequest= new RunInstancesRequest();
         runInstancesRequest.withImageId(imageName)
         .withInstanceType("t2.micro")
         .withMinCount(1)
         .withMaxCount(1)
         .withKeyName("devenv-key")
         .withDisableApiTermination(false)
         .withSecurityGroups("distributed-grid");
         RunInstancesResult runInstancesResult=ec2.runInstances(runInstancesRequest);
         return runInstancesResult.getReservation().getInstances().get(0);
       
        }
        catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
            return null;
    }
    }
    
    public void terminateInstance(String instanceId)
    {
    	try {
    	    // Terminate instances.
    		List<String> instanceIds=new ArrayList<String>();
    		instanceIds.add(instanceId);
    		System.out.println("Terminating instances: " + instanceIds);
    	    TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest(instanceIds);
    	    ec2.terminateInstances(terminateRequest);
    	} catch (AmazonServiceException e) {
    	    // Write out any exceptions that may have occurred.
    	    System.out.println("Error terminating instances");
    	    System.out.println("Caught Exception: " + e.getMessage());
    	    System.out.println("Reponse Status Code: " + e.getStatusCode());
    	    System.out.println("Error Code: " + e.getErrorCode());
    	    System.out.println("Request ID: " + e.getRequestId());
    	}
    }
    public List<Instance> getAllInstances()
    {
    	DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        List<Instance> instances = new ArrayList<Instance>();

        for (Reservation reservation : reservations) {
            instances.addAll(reservation.getInstances());
        }
        return instances;
        
    }
    public Instance getInstance(String instanceId)
    {
    	DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        List<Instance> instances = new ArrayList<Instance>();

        for (Reservation reservation : reservations) {
            instances.addAll(reservation.getInstances());
        }
        for(Instance i:instances)
        {
        	if(i.getInstanceId().equals(instanceId))
        		return i;
        }
        return null;
    }
    
    
    
  
}