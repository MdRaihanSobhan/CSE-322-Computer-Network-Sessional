package server;

import requests.Message;
import util.NetworkUtil;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    private ServerSocket serverSocket;
    protected static HashMap<String, Client_Profile> clientMap;
    protected static HashMap<Integer, String> fileMap;


    protected static long MAX_BUFFER_SIZE = 100000 * 1024 ;
    protected static int MIN_CHUNK_SIZE = 1024;
    protected static int MAX_CHUNK_SIZE = 8192;
    protected static volatile long CUR_BUFFER_SIZE = 0;
    protected static int file_id = 0;
    protected static ArrayList<Message> messageList = new ArrayList<Message>();

    protected static int messageId=0;

    public Server() {
        String path = "FTPServer/";
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdir();
        }

        clientMap = new HashMap<String, Client_Profile>();
        fileMap = new HashMap<Integer, String>();
        try {
            serverSocket = new ServerSocket(6666);
            while (true) {
                System.out.println("Waiting for connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection established");
                serve(clientSocket);
            }
        } catch (Exception e) {
            System.out.println("server.Server starts:" + e);
        }
    }

    protected static void add(String clientId, Client_Profile clientProfile) {
        clientMap.put(clientId,clientProfile);
    }

    public void serve(Socket clientSocket) throws IOException, ClassNotFoundException {
        NetworkUtil networkUtil = new NetworkUtil(clientSocket);
        new ReadThreadServer(clientMap, networkUtil);
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();
    }
}
