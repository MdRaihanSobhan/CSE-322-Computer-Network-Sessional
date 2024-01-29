package server;

import requests.Message;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class ServerMethods extends Server {



    public static long getMaxBufferSize() {
        return MAX_BUFFER_SIZE;
    }

    public static int getMaxChunkSize() {
        return MAX_CHUNK_SIZE;
    }

    public static int getMinChunkSize() {
        return MIN_CHUNK_SIZE;
    }

    public static long getCurBufferSize() {
        return CUR_BUFFER_SIZE;
    }


    public static boolean is_Buffer_Available(long fileLength){
        if( CUR_BUFFER_SIZE + fileLength > MAX_BUFFER_SIZE ) return false;
        CUR_BUFFER_SIZE += fileLength;
        System.out.println("Currently used buffer : " + CUR_BUFFER_SIZE + " bytes");
        return true;
    }

    public static void releaseBuffer(long fileLength){
        CUR_BUFFER_SIZE -= fileLength;
        System.out.println("Currently used buffer : " + CUR_BUFFER_SIZE + " bytes");
    }


    public static int getRandomChunkSize() {
        Random random = new Random();
        int chunkSize = random.nextInt(MAX_CHUNK_SIZE - MIN_CHUNK_SIZE + 1) + MIN_CHUNK_SIZE;
        return chunkSize;
    }

    public static int addFile(String directory){
        file_id++;
        fileMap.put(file_id, directory);
        return file_id;
    }
    public static Integer get_file_id(String filepath){
        for (Map.Entry<Integer, String> entry : fileMap.entrySet()) {
            if (entry.getValue().equals(filepath)) {
                return entry.getKey();
            }
        }
        return 0;
    }
    
    public static String[] get_client_list(){
        File rootDirectory = new File("FTPServer");
        return rootDirectory.list();
    }

    public static String get_directory_of_file(int file_id){
        return fileMap.get(file_id);
    }


    public static ArrayList<String> get_Online_Clients(){
        return new ArrayList<String>(clientMap.keySet());
    }

    public static String[] get_All_Clients(){
        File directoryPath = new File("FTPServer");
        return directoryPath.list();
    }

    public static boolean login(String client_id, Client_Profile clientProfile){
        if(Server.clientMap.containsKey(client_id) == true){
            System.out.println(client_id + " is already logged in, denied by the Server");
            return false;
        }
        Server.add(client_id, clientProfile);
        System.out.println(client_id + " has logged in with IP Address : " + clientProfile.getNetworkUtil().getSocket().getRemoteSocketAddress());
        String path = "FTPServer/" + client_id + "/";
        File directory = new File(path);

        if (!directory.exists()) {
            directory.mkdir();
            File public_directory = new File( path + "public/");
            File private_directory = new File( path + "private/");
            public_directory.mkdir();
            private_directory.mkdir();
            System.out.println("Folder created with the name " + client_id + " in File Server");
        }
        return true;
    }


    public static void createMessage(String client_id, String description){
        messageList.add(new Message(messageId, client_id, description));
        messageId = messageId+1;
    }
    public static void add_requested_file(int msg_id , String granter, String filepath){
        for( int i = 0 ; i < messageList.size() ; i++ ){
            if(messageList.get(i).getMessage_id() == msg_id){
                messageList.get(i).grantRequest(granter, filepath);
                break;
            }
        }
    }

    public static boolean is_msg_id_available(int msg_id){
        for( int i = 0 ; i < messageList.size() ; i++ ){
            if(messageList.get(i).getMessage_id() == msg_id){
                return true;
            }
        }
        return false;
    }

    public static void deleteMessage(int msg_id){
        for( int i = 0 ; i < messageList.size() ; i++ ){
            if(messageList.get(i).getMessage_id() == msg_id){
                messageList.remove(i);
                break;
            }
        }
    }
    public static void logout(String clientId){
        Server.clientMap.remove(clientId);
    }

}
