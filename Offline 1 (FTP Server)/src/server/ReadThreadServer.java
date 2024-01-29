package server;

import requests.Acceptor;
import requests.Message;
import util.NetworkUtil;

import java.io.*;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReadThreadServer implements Runnable{

    private String client_id;
    private long bytes_so_far = 0;

    public long getBytes_so_far() {
        return bytes_so_far;
    }

    public void setBytes_so_far(long bytes_so_far) {
        this.bytes_so_far = bytes_so_far;
    }

    private Thread thread;

    long f_length;

    public void setF_length(long f_length) {
        this.f_length = f_length;
    }

    public long getF_length() {
        return f_length;
    }

    String file_directory = null;

    private boolean online;
    private NetworkUtil networkUtil;
    public HashMap<String, Client_Profile> clientMap;

    private SocketAddress Client_IP;




    public String getFileName(){
        try {
            return  (String) this.networkUtil.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public long getFile_size(){
        try {
            return  (long) this.networkUtil.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
    public String getFile_directory(String privacy, String f_name){
        return "FTPServer/" + client_id + "/" + privacy + "/" + f_name;
    }

    public void setOutputStreams(File file){
        try {
            this.networkUtil.setFos(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.networkUtil.setBos(new BufferedOutputStream(this.networkUtil.getFos()));
    }

    public void flush_and_close_streams(){
        try {
            this.networkUtil.getBos().flush();
            this.networkUtil.getBos().close();
            this.networkUtil.getFos().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String read_file_chunk_by_chunk(  long no_of_chunks_received, long total_no_of_chunks){
        String client_response=" ";
        long tempCount=0;
        Boolean exit_operation= false;
        try {
            while (no_of_chunks_received != total_no_of_chunks) {
                Object o = this.networkUtil.read();
                if( o.getClass().equals(client_response.getClass() ) ){
                    client_response = (String) o;
                    exit_operation = true;
                    break;
                }
                byte[] byteArray = (byte[]) o;
                int received_chunk_size = 0;
                received_chunk_size = byteArray.length;
                tempCount += received_chunk_size;
                this.networkUtil.getBos().write(byteArray,0, received_chunk_size);

                this.networkUtil.write(tempCount + " out of " + this.getF_length() + " bytes received");
                this.networkUtil.write("ACK");
                no_of_chunks_received += 1;
            }
            flush_and_close_streams();

            if(exit_operation == false){
                client_response = (String) this.networkUtil.read();
            }
        }catch (Exception e){
            System.out.println(e);
        }
        this.setBytes_so_far(tempCount);
        return client_response;

    }

    public void checkReceiveCompletion(String response ){
        if(response.equalsIgnoreCase("DONE")){
            ServerMethods.releaseBuffer(this.getBytes_so_far());
            if(this.getBytes_so_far() == this.getF_length()){
                try {
                    this.networkUtil.write("FINISH");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else{
                System.out.println( file_directory + ": upload incomplete, so the file is being deleted " );
                try {
                    this.networkUtil.write("FAILURE");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                File deleting_file = new File(file_directory);
                boolean success = deleting_file.delete();
                if(success == true){
                    System.out.println( "For the file: "+file_directory + " ,  deletion successful");
                }
                else{
                    System.out.println("For the file: "+ file_directory + " deletetion failed");
                }
            }
        }
        else if(response.equalsIgnoreCase("TIMEOUT")){
            File to_delete = new File(file_directory);
            boolean success = to_delete.delete();
            if(success == true){
                System.out.println( "For the file: "+ file_directory + ",  deleting because of timeout");
            }
            else{
                System.out.println("For the file: "+ file_directory + " deletetion failed");
            }
        }
    }
    public String receive_from_client (String privacy){
        try {
            // get file name from client
            String f_name = getFileName();
            // get file size from client
            this.setF_length(getFile_size());

            System.out.println("Client with id " + client_id + " has requested to upload a " + privacy + " file ' " + f_name + " ' of length : " + this.getF_length() + " bytes");
            // check buffer availability
            boolean available = ServerMethods.is_Buffer_Available(this.getF_length());
            this.networkUtil.write(available);
            if(!available){
                System.out.println("Buffer size exceeded, denying the client");
                return null;
            }
            // send chunk size , randomly chosen between max and min chunk size
            int chunk_size = ServerMethods.getRandomChunkSize();
            this.networkUtil.write(chunk_size);
            file_directory = getFile_directory(privacy,f_name);

            int fileID = ServerMethods.addFile(file_directory) ;

            this.networkUtil.write(fileID);

            File file = new File(file_directory);

            setOutputStreams(file);


            long total_no_of_chunks=0;
            if(this.getF_length()%chunk_size==0){
                total_no_of_chunks= this.getF_length()/chunk_size;
            }
            else{
                total_no_of_chunks = this.getF_length()/chunk_size+1;
            }
            long no_of_chunks_received = 0;
            this.setBytes_so_far(0);
            String client_response = read_file_chunk_by_chunk(no_of_chunks_received,  total_no_of_chunks);
            System.out.println(client_response);

            checkReceiveCompletion(client_response);

            file_directory = null;
            return f_name;

        }catch(Exception e){
            System.out.println(e);
            return null;
        }
    }

    public void send_file_to_client(File file){
        try{
            // send file size
            long length = file.length();
            this.networkUtil.write(length);
            // get chunk size from server
            int chunk_size = ServerMethods.getMaxChunkSize();
            InputStream is = new FileInputStream(file); //create an inputstream from the file
            byte[] buf = new byte[chunk_size]; //create buffer
            int len = 0;
            int count = 0;
            while ((len = is.read(buf)) != -1) {
                //os.write(buf, 0, len); //write buffer
                // copy buf to len array
                if(len == chunk_size){
                    this.networkUtil.write(buf);
                }
                else{
                    byte[] buf2 = new byte[len];
                    System.arraycopy(buf, 0, buf2, 0, len);
                    this.networkUtil.write(buf2);
                }
                this.networkUtil.getOos().reset();
                count += 1;
            }
            this.networkUtil.write("COMPLETED");
            is.close();
        }
        catch(Exception e){
            System.out.println(e);
        }

    }

    public void showFiles(String type){
        List<String> results = new ArrayList<String>();
        results.add(type + " :");

        File[] files = new File("FTPServer/" + client_id + "/" + type + "/" ).listFiles();

        for (File file : files) {
            if (file.isFile()) {
                int fileid = ServerMethods.get_file_id("FTPServer/" + client_id + "/" + type + "/" + file.getName());
                results.add("File Id - "+ fileid + " : " + file.getName());
            }
        }
        try {
            this.networkUtil.write(results); // write id 1
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        results.clear();
    }

    public void showDenyMessage(){
        System.out.println("No file found in the given directory with this file ID");
        try {
            this.networkUtil.write("There is no file found with this file ID in your folders");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Boolean doesClientExists(String id_of_client ){
        String[] clientList = ServerMethods.get_client_list();
        for( int i = 0 ; i < clientList.length ; i++ ){
            if( clientList[i].equalsIgnoreCase(id_of_client) ){
                return true;
            }
        }
        return false;
    }
    public ReadThreadServer(HashMap<String, Client_Profile> map, NetworkUtil networkUtil) {
        this.clientMap = map;
        this.networkUtil = networkUtil;
        this.thread = new Thread(this);
        this.Client_IP = networkUtil.getSocket().getRemoteSocketAddress();
        this.online= true;
        thread.start();
    }
    @Override
    public void run() {
        try {
            while (this.online){
                this.client_id = (String) this.networkUtil.read();
                Client_Profile clientProfile = new Client_Profile();
                clientProfile.setNetworkUtil(this.networkUtil);
                clientProfile.setOnline(true);
                boolean logged_in = ServerMethods.login(client_id, clientProfile);
                this.networkUtil.write(logged_in);

                if(logged_in==false){
                    continue;
                }
                while (true){
                    String chosen = (String) this.networkUtil.read();

                    if( chosen.equalsIgnoreCase("0") ){
                        this.networkUtil.getOis().close();
                        this.networkUtil.getOos().close();
                        this.networkUtil.getSocket().close();
                        ServerMethods.logout(client_id);
                        System.out.println("Client with id: "+ client_id + " has logged out");
                        this.online = false;
                        break;
                    }
                    else if( chosen.equalsIgnoreCase("1") ){
                        System.out.println("Client with id: "+ client_id + " has requested for client list");
                        ArrayList<String> active_users = ServerMethods.get_Online_Clients();
                        this.networkUtil.write(active_users);
                        String[] users = ServerMethods.get_All_Clients();
                        this.networkUtil.write(users);
                    }
                    else if(chosen.equalsIgnoreCase("2")){
                        showFiles("public");
                        showFiles("private");
                    }
                    else if(chosen.equalsIgnoreCase("3")){
                        showFiles("public");
                        showFiles("private");

                        int fileid;
                        try{
                            fileid = (int) this.networkUtil.read();
                        }
                        catch (Exception e){
                            continue;
                        }
                        System.out.println("User has entered file ID: "+ fileid);
                        String directoryOfFile = ServerMethods.get_directory_of_file(fileid);
                        System.out.println("Client with id: "+ client_id + " wants to download a file, its full path: " + directoryOfFile);
                        if( directoryOfFile == null ){
                            showDenyMessage();
                            continue;
                        }
                        String publicPath ="FTPServer/" + client_id + "/public/";
                        String privatePath ="FTPServer/" + client_id + "/private/";
                        if(directoryOfFile.startsWith(publicPath)){
                            this.networkUtil.write("available");
                            File file = new File(directoryOfFile);
                            this.networkUtil.write(file.getName());
                            send_file_to_client(file);
                        }
                        else if(directoryOfFile.startsWith(privatePath)){
                            this.networkUtil.write("available");
                            File file = new File(directoryOfFile);
                            this.networkUtil.write(file.getName());
                            send_file_to_client(file);
                        }
                        else{
                            showDenyMessage();
                        }
                    }

                    else if (chosen.equalsIgnoreCase("4")){
                        String id_of_client = (String) this.networkUtil.read();
                        Boolean registered = doesClientExists(id_of_client);
                        if(registered==false){
                            System.out.println("Client with id: "+ id_of_client + " doesnt exist");
                            this.networkUtil.write((Integer)0);
                        }
                        else {
                            System.out.println("Client with id: "+ client_id + " requested to see the files of " + "another client with id: "+ id_of_client);
                            List<String> fileList = new ArrayList<String>();
                            String publicPath= "FTPServer/" + id_of_client + "/public/";
                            File[] files = new File(publicPath).listFiles();
                            for (File file : files) {
                                if (file.isFile()) {
                                    fileList.add(file.getName());
                                }
                            }
                            this.networkUtil.write(fileList);

                            String option_given = (String) this.networkUtil.read();
                            if( option_given.equalsIgnoreCase("1") ){
                                String filename = (String) this.networkUtil.read();
                                String wantedFilePath = "FTPServer/" + id_of_client + "/public/" + filename;
                                File file = new File(wantedFilePath);
                                if(!file.exists()){
                                    System.out.println("No file found whose name is " + filename);
                                    this.networkUtil.write("No such file");
                                    continue;
                                }
                                this.networkUtil.write("available");
                                send_file_to_client(file);
                            }
                            else if( option_given.equalsIgnoreCase("2") ){
                                // do nothing
                            }
                        }

                    }
                    else if(chosen.equalsIgnoreCase("5")){
                        System.out.println("Client with id: "+ client_id + " wants to request for a file.");
                        String request_Message = (String) this.networkUtil.read();
                        ServerMethods.createMessage(client_id, request_Message);
                    }

                    else if( chosen.equalsIgnoreCase("6") ){
                        String privacy = (String) this.networkUtil.read(); // public or private
                        if( privacy.equalsIgnoreCase("1") ){
                            privacy = "public";
                        }
                        else if( privacy.equalsIgnoreCase("2") ){
                            privacy = "private";
                        }

                        String filename = receive_from_client(privacy);
                    }

                    else if(chosen.equalsIgnoreCase("7")){
                        ArrayList<Message> req = Server.messageList;
                        ArrayList<String> other_req = new ArrayList<String>();
                        ArrayList<String> own_req = new ArrayList<String>();
                        for(int i = 0 ; i < req.size() ; i++ ){
                            if(req.get(i).getSender().equalsIgnoreCase(client_id)){
                                ArrayList<Acceptor> acceptors = req.get(i).getUploaded_file_list();
                                if(acceptors.size() == 0){
                                    continue;
                                }
                                for( int j = 0 ; j < acceptors.size() ; j++ ){
                                    own_req.add(req.get(i).getMessage_id() + " : " + acceptors.get(j).getAcceptor_id() + " uploaded : " + acceptors.get(j).getUploaded_directory());
                                }
                                ServerMethods.deleteMessage(req.get(i).getMessage_id());
                            }
                            else{
                                other_req.add(req.get(i).getMessage_id() + " : " + req.get(i).getSender() + " requested for a file : " + req.get(i).getMessage());
                            }
                        }
                        this.networkUtil.write(other_req);
                        this.networkUtil.write(own_req);
                    }

                    else if (chosen.equalsIgnoreCase("8")) {
                        String privacy = "public";
                        int req_id = (int) this.networkUtil.read();
                        boolean msgIdAvailable = ServerMethods.is_msg_id_available(req_id);
                        this.networkUtil.write(msgIdAvailable);
                        if( msgIdAvailable == false ){
                            System.out.println("No message with this id");
                            continue;
                        }
                        else {
                            String filename = receive_from_client(privacy);
                            String path = "FTPServer/" + client_id + "/public/" + filename;
                            ServerMethods.add_requested_file(req_id, client_id, path);
                        }
                    }

                    else {
                        System.out.println("Invalid option chosen");
                    }
                }
            }


        } catch (Exception e){
            System.out.println(e);
        }

    }
}
