import com.server.ServerImpl;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {

    private static final int REGISTRY_PORT_NUMBER = 1099;

    public static void main(String[] args) throws RemoteException {
        // Starting RMI registry at localhost
        Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT_NUMBER);

        // Creating and binding the remote object
        ServerImpl server = new ServerImpl();
        registry.rebind("Update", server);
    }
}
