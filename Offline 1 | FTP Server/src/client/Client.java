package client;

import server.Client_Profile;
import util.NetworkUtil;

import java.io.IOException;
import java.util.Scanner;

public class Client {
    public static NetworkUtil networkUtil;

    public Client(String serverAddress, int serverPort) {
        try {
            Client.networkUtil = new NetworkUtil(serverAddress, serverPort);
            System.out.println("Connection Established");
            new ClientMenu(Client.networkUtil);
        } catch (Exception e) {
            System.out.println("Connection failed");
            System.out.println(e);
        }
    }

    public static void main(String args[]) {

        String serverAddress = "127.0.0.1"; // localhost ip is 127.0.0.1
        int serverPort = 6666;
        Client client = new Client(serverAddress, serverPort);
        Scanner scanner = new Scanner(System.in);

        while (true){

        }
    }
}


