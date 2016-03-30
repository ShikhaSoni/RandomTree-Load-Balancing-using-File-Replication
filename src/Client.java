/**
 * @author Shikha Soni
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

public class Client extends UnicastRemoteObject implements FileTransfer {

	private static final long serialVersionUID = 581985802852197629L;
	private ArrayList<String> servers;
	int port_num = 0;

	protected Client(int port_num) throws RemoteException {
		super();
		this.port_num = port_num;
		servers = new ArrayList<String>();
		Registry reg = LocateRegistry.createRegistry(port_num);
		reg.rebind("client", this);
	}

	public int getNum_server() {
		return servers.size();
	}

	public void add_server_IP(String IP) {
		servers.add(IP);
	}

	public FileTransfer remoteObject(int server_num) {
		FileTransfer remote = null;
		try {
			System.out.println("Server: "
					+ InetAddress.getByName(servers.get(server_num)));
			Registry reg = LocateRegistry.getRegistry(
					InetAddress.getByName(servers.get(server_num))
							.getHostAddress(), port_num);
			remote = (FileTransfer) reg.lookup("server");
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return remote;
	}

	@Override
	public boolean sendData(String filename, File fileobject)
			throws RemoteException {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					fileobject));
			PrintWriter writer = new PrintWriter(
					new File(filename + "Copy.txt"));
			String s = reader.readLine();
			while (s != null) {
				writer.println(s);
				s = reader.readLine();
			}
			reader.close();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public int hash_value(String filename, int level, int root_num) {
		int server_num = filename.hashCode() + level + root_num;
		return Math.abs(server_num % servers.size());
	}

	public void file_insertion(int num_files) {

		// transfer all the file in the system
		for (int file_num = 1; file_num <= num_files; file_num++) {
			// first get the hash for the file
			int level = 0;
			int root_server_num = hash_value("File_" + file_num + ".txt",
					level, 0);
			// make an insertion at this server after a look up from the list of
			// servers
			FileTransfer server = remoteObject(root_server_num);
			try {
				BufferedReader reader = new BufferedReader(new FileReader("File_" + file_num + ".txt"));
				String next = reader.readLine();
				server.sendString("File_" + file_num, servers.get(root_server_num));
				while(next != null){
					server.sendString("File_" + file_num , next);
					next = reader.readLine();
				}
				System.out.println("File_" + file_num + " inserted");
				reader.close();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void file_request(String filename) {
		// get remote object
		System.out.println("File request for: " + filename);
		Random random = new Random();
		int num = random.nextInt(4); //  int randomNum = rand.nextInt((max - min) + 1) + min;
		int server_num = hash_value(filename, 2, num);
		FileTransfer transfer = remoteObject(server_num);
		try {
			transfer.file_request(filename, server_num, 3, InetAddress.getLocalHost().getHostAddress());
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Read a text file for the IP address of the servers. Mention the name
		// of the server files as the second args
		// the first args is the port number used for communication
		String action = args[2];
		try {
			Client client = new Client(Integer.parseInt(args[0]));
			BufferedReader reader = new BufferedReader(new FileReader(args[1]));
			String next_IP = reader.readLine();
			System.out.println("My IP: " + InetAddress.getLocalHost().getHostName());
			while (next_IP != null) {
				//System.out.println(next_IP);
				client.add_server_IP(next_IP);
				next_IP = reader.readLine();
			}
			reader.close();

			// File Insertions
			if (action.equals("insert")) {
				System.out.println("File Insertion");
				client.file_insertion(Integer.parseInt(args[3]));
				System.out.println("File inserted succesfully!");
				//File Requests
				System.out.println("File request put in");
				client.file_request("File_1.txt");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see FileTransfer#file_request(java.lang.String, java.io.File, int, int)
	 */
	@Override
	public void file_request(String filename, int server_num, int level, String IP)
			throws RemoteException {
	}

	/* (non-Javadoc)
	 * @see FileTransfer#sendString(java.lang.String, java.lang.String)
	 */
	@Override
	public void sendString(String filename, String data) throws RemoteException {
		try {
			FileWriter fw = new FileWriter(filename + "Return.txt", true);
			fw.write(data);
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
