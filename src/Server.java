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
import java.util.HashMap;

public class Server extends UnicastRemoteObject implements FileTransfer {
	private static final long serialVersionUID = 162589627968815972L;
	private int port_num = 0;
	private HashMap<String, Integer> file_counts;
	private ArrayList<String> servers;

	/**
	 * 
	 * @throws RemoteException
	 */
	protected Server(int port_num) throws RemoteException {
		super();
		file_counts = new HashMap<String, Integer>();
		servers = new ArrayList<String>();
		Registry reg = LocateRegistry.createRegistry(port_num);
		reg.rebind("server", this);
	}

	/**
	 * 
	 * @param filename
	 */
	public void file_request(String filename, int server_num, int level, String remoteIP) {
		// check if the file exists, increase the count, if the count reaches 5,
		// pass it on to the children.
		// if the file doesn't exist, add it in the map
		System.out.println("Request for File: " + filename);
		int count = 1;
		if (file_counts.containsKey(filename)) {
			//transfer the file 
			FileTransfer object = get_remote_object(remoteIP);
			remoteSend(filename, object);
			
			//copy the files if very popular
			count = file_counts.get(filename) + 1;
			if (count >= 5) {
				// replicate to the children
				// children can be calculated using formula.
				// Child 1: server_num * 2
				// Child 2: (server_num * 2) + 1
				
				// Child 1:
				int child_1 = server_num * 2;
				String child_server_1 = servers.get(hash_value(filename,
						level - 1, child_1));
				FileTransfer filetransfer_1 = get_remote_object(child_server_1);
				childCopy(filename, filetransfer_1);

				// Child 2:
				int child_2 = (server_num * 2) + 1;
				String child_server_2 = servers.get(hash_value(filename,
						level - 1, child_2));
				filetransfer_1 = get_remote_object(child_server_2);
				childCopy(filename, filetransfer_1);

			}
		} else {
			// if I dont have it, calculate the parent
			int parent = (int) Math.ceil(server_num / 2);
			int parent_server_num = hash_value(filename, level, parent);
			try {
			System.out.println("I " + InetAddress.getLocalHost().getHostName() + " do not have the file, requesting parent: ");
			FileTransfer trans = get_remote_object(servers.get(hash_value(
					filename, level - 1, parent)));
			
				trans.file_request(filename, parent_server_num, level - 1, remoteIP);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		file_counts.put(filename, count);
	}

	public void childCopy(String filename, FileTransfer object) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filename));
			String next = reader.readLine();
			while (next != null) {
				object.sendString(filename, next);
				next = reader.readLine();
			}
			reader.close();
			System.out.println(filename + " inserted");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void remoteSend(String filename, FileTransfer object) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filename));
			String next = reader.readLine();
			while (next != null) {
				object.sendString(filename, next);
				next = reader.readLine();
			}
			reader.close();
			System.out.println(filename + " inserted");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	/**
	 * This method can accept files from the client
	 */
	public boolean sendData(String filename, File fileobject)
			throws RemoteException {
		try {
			System.out.println("Received file: " + filename);
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
			file_add(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * public boolean file_request(String filename, int server_num, int level)
	 * throws RemoteException { //if I dont have it, calculate the parent int
	 * parent = (int) Math.ceil(server_num / 2); file_req(filename, server_num,
	 * level - 1); return false; }
	 */
	public void file_add(String filename) {
		file_counts.put(filename, 1);
	}

	/**
	 * 
	 * @param filename
	 * @param level
	 * @param root_num
	 * @return
	 */
	public int hash_value(String filename, int level, int root_num) {
		int server_num = filename.hashCode() + level + root_num;
		return Math.abs(server_num % servers.size());
	}

	/**
	 * 
	 * @param IP
	 */
	public void add_server_IP(String IP) {
		servers.add(IP);
	}

	/**
	 * 
	 * @param IP
	 * @return
	 */

	public FileTransfer get_remote_object(String IP) {
		Registry reg;
		FileTransfer remote = null;
		try {
			System.out.println("Server: "
					+ InetAddress.getByName(IP).toString());
			reg = LocateRegistry.getRegistry(InetAddress.getByName(IP)
					.getHostAddress(), port_num);
			remote = (FileTransfer) reg.lookup("server");
		} catch (RemoteException | UnknownHostException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		return remote;
	}

	/**
	 * @param args
	 * @throws RemoteException
	 */
	public static void main(String[] args) throws RemoteException {
		// Display your IP address for the client to know.
		Server server = new Server(Integer.parseInt(args[0]));
		try {
			System.out.println("My IP address: "
					+ InetAddress.getLocalHost().getHostName().toString());
			BufferedReader reader = new BufferedReader(new FileReader(args[1]));
			String next_IP = reader.readLine();
			while (next_IP != null
					&& !next_IP.equals(InetAddress.getLocalHost().getHostName()
							.toString())) {
				server.add_server_IP(next_IP);
				// System.out.println(next_IP + " Server add");
				next_IP = reader.readLine();
			}
			reader.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Server start");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see FileTransfer#sendString(java.lang.String, java.lang.String)
	 */
	@Override
	public void sendString(String filename, String data) {
		// System.out.println("__________________________________Called");
		try {
			FileWriter fw = new FileWriter(filename + "Copy.txt", true);
			fw.write(data);
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
