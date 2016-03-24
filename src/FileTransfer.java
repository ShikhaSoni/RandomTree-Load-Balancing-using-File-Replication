/**
 * @author Shikha Soni
 */
import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileTransfer extends Remote {
	public boolean sendData(String filename, File fileObject)
			throws RemoteException;

	public void  file_request(String filename, int server_num, int level, String remoteIP)
			throws RemoteException;
	
	public void sendString(String filename, String data) throws RemoteException;

}
