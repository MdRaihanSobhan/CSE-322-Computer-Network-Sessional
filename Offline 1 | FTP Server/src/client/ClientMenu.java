package client;

import util.NetworkUtil;

import java.awt.*;
import java.io.*;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.Integer.parseInt;

public class ClientMenu {
    private NetworkUtil networkUtil;
    private String clientName;
    private boolean isLoggedIn;

    public ClientMenu(NetworkUtil networkUtil) {
        this.networkUtil = networkUtil;
        while (true){
            boolean bul = login();
            if(bul==true){
                break;
            }
        }
        while (true){
            Scanner scanner = new Scanner(System.in);
            System.out.println("0. Log out \t \t");
            System.out.print("1. Lookup Clients \t \t");
            System.out.print("2. See your files \t \t");
            System.out.print("3. Download your files \t \t");
            System.out.println("4. Lookup other clients' files");
            System.out.print("5. Request a file \t \t");
            System.out.print("6. Upload a file \t \t");
            System.out.print("7. View Requests \t \t \t");
            System.out.println("8. Grant Requests");

            String chosen = scanner.nextLine();
            try {
                this.networkUtil.write(chosen);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if( chosen.equalsIgnoreCase("0") ){
                log_out();
            }
            else if(chosen.equalsIgnoreCase("1")){
                lookUp();
            }
            else if(chosen.equalsIgnoreCase("2")){
                showFiles();
            }
            else if(chosen.equalsIgnoreCase("3")){
                downloadFiles();
            }
            else if(chosen.equalsIgnoreCase("4")){
                searchOtherCliens();
            }
            else if(chosen.equalsIgnoreCase("5")){
                sendRequest();
            }
            else if( chosen.equalsIgnoreCase("6") ){
                upload();
            }
            else if (chosen.equalsIgnoreCase("7")) {
                viewRequests();
            }
            else if (chosen.equalsIgnoreCase("8")) {
                grantRequests();
            } else {
                System.out.println("Invalid option chosen");
            }
        }
    }

    public boolean login(){
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.println("Enter your client id : ");
            String id = scanner.nextLine();
            try {
                this.networkUtil.write(id);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            boolean login = false;
            try {
                login = (boolean) this.networkUtil.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            if(login == true){
                System.out.println("Successful login");
                return true;
            }
            System.out.println("Client is already logged in");
            return false;
        }
    }
    public void lookUp(){
        ArrayList<String> online_clients;
        String[] all_clients;
        try {
            online_clients = (ArrayList<String>) this.networkUtil.read();

            all_clients = (String[]) this.networkUtil.read();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        for(int i=0; i<all_clients.length; i++) {
            String client_id = all_clients[i];

            if(online_clients.contains(client_id)){
                client_id += ": online";
            }
            else{
                client_id += ": offline";
            }
            System.out.println(client_id);

        }
    }

    public void seeFiles(){
        try {
            ArrayList<String> filelist =  (ArrayList<String>) this.networkUtil.read(); // read id 1
            for(int j = 0; j < filelist.size() ; j++){
                System.out.println(filelist.get(j));
            }
        }catch (IOException ie){
            System.out.println(ie);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void showFiles(){
        System.out.println("Here are your files");
        seeFiles(); // public
        seeFiles(); // private
    }
    public void downloadFiles(){
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("Here are your files");
            seeFiles(); // public
            seeFiles(); // private

            System.out.println("Write the id of the file you want to download from the above list : ");
            int fileid = Integer.parseInt( scanner.nextLine());
            try{
                this.networkUtil.write(fileid);
            }
            catch (Exception e){
                this.networkUtil.write("failed");
                return;
            }
            String file_name;
            String exists = (String) this.networkUtil.read();
            if(exists.equalsIgnoreCase("available")){
                file_name = (String) this.networkUtil.read();
                download_file_from_server(file_name);
            }
            else{
                System.out.println("File doesn't exist with the id : " + fileid );
            }

        }catch (IOException ie){
            System.out.println(ie);
        }catch (ClassNotFoundException cnfe){
            System.out.println(cnfe);
        }

    }
    public void log_out(){
        try {
            this.networkUtil.getOis().close();
            this.networkUtil.getOos().close();
            this.networkUtil.getSocket().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Logged out");
        System.exit(0);
    }

    public void upload(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Which kind of file do you want to upload?");
        System.out.println("1.  Public");
        System.out.println("2.  Private");
        String file_type = scanner.nextLine();
        try {
            this.networkUtil.write(file_type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        send_file();
    }

    public File openDialogBox(){
        System.out.println("Choose a file to upload to the server, from the dialogue box opened now");
        Frame frame = new Frame();
        FileDialog fileToOpen = new FileDialog(frame, "Select File to Open", FileDialog.LOAD);
        fileToOpen.setVisible(true);
        System.out.println(fileToOpen.getDirectory() + fileToOpen.getFile() + " chosen. ");
        return new File(fileToOpen.getDirectory() + fileToOpen.getFile() );
    }

    public void setFileInfo(File file){
        System.out.println("Rename your file? Give the filename : ");
        Scanner scanner = new Scanner(System.in);
        String filename = scanner.nextLine();
        long length = file.length();
        try {
            this.networkUtil.write(filename);
            this.networkUtil.write(length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // send file size
    }

    public boolean isAvailable(){
        try {
            return  (boolean) this.networkUtil.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public int get_chunkSize_from_Server(){
        int chunkSize = 0;
        try {
            chunkSize = (int) this.networkUtil.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Chunk size provided from Server : " + chunkSize);
        return chunkSize;
    }

    public int get_fileId_from_Server(){
        try {
            return (int) this.networkUtil.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void write_file_Chunk_by_Chunk(int chunkSize) {
        try {
            byte[] buffer = new byte[chunkSize];
            int tempChnk = 0;
            // this inputstream.read returns how many bytes it has read from the file it opened,
            // if doesn't return,it means file read completed, assignment operator returns -1
            while ((tempChnk = this.networkUtil.getIs().read(buffer)) != -1) {
                if(tempChnk == chunkSize){
                    this.networkUtil.write(buffer);
                }
                else{
                    byte[] buf2 = new byte[tempChnk];
                    System.arraycopy(buffer, 0, buf2, 0, tempChnk);
                    this.networkUtil.write(buf2);
                }
                this.networkUtil.getOos().reset();
                this.networkUtil.getSocket().setSoTimeout(30000);
                try {
                    String ack = (String) this.networkUtil.read();
                    System.out.println(ack);
                    Thread.sleep(1000);
                    String has_ack = (String) this.networkUtil.read();
                    if(has_ack.equalsIgnoreCase("ACK")){
                        System.out.println("Send next chunk");
                    }
                }catch (SocketTimeoutException se){
                    System.out.println("Timeout, terminating");
                    break;
                }
            }
            this.networkUtil.write("DONE");
            this.networkUtil.getIs().close();
        } catch (Exception e){
            System.out.println(e);
        }

    }


    public void send_file(){
        try{
            File opened = openDialogBox();   // client choose which file to upload
            setFileInfo(opened);             //client sends filename and file length to server

            // Server checks if buffer size overflows or not if this file is uploaded and returns it to client
            if(isAvailable() == false){
                System.out.println("Buffer limit has been exceeded the maximum size, operation denied by the server!");
                return;
            }
            // client receives a random chunk size and file id from the server
            int randomChunkSize = get_chunkSize_from_Server();
            int fileID = get_fileId_from_Server();

            this.networkUtil.setIs(new FileInputStream(opened));

            //If the client does not receive any acknowledgement within 30 seconds,
            // it sends a timeout message to the server and terminates the transmission.
            this.networkUtil.getSocket().setSoTimeout(30000);
            write_file_Chunk_by_Chunk(randomChunkSize);

            String confirmed = (String) this.networkUtil.read();
            if(confirmed.equalsIgnoreCase("FINISH")){
                System.out.println("File transferred successfully with fileID " + fileID);
            }
            else if(confirmed.equalsIgnoreCase("FAILURE")){
                System.out.println("File transfer with filID "+ fileID+ " not successful");
            }

        } catch(SocketTimeoutException se){
            System.out.println(se);
            try{
                this.networkUtil.write("TIMEOUT");
            }
            catch (Exception e){
                System.out.println(e);
            }

        }
        catch(Exception e){
            System.out.println(e);
        }

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
    public void download_file_from_server(String filename){
        try {
            String path = "Downloads/";
            File directory = new File(path);

            if (!directory.exists()) {
                directory.mkdir();
            }
            // get file size
            long filesize = (long) this.networkUtil.read();
            System.out.println("Downloading " + filename + " of size " + filesize);
            File file=new File("Downloads/" + filename);


            setOutputStreams(file);
            String acknowledge = "";
            int bytesread = 0;
            int total = 0;

            while (true) {
                Object o = this.networkUtil.read();
                if( o.getClass().equals(acknowledge.getClass() ) ){
                    acknowledge = (String) o;
                    break;
                }
                byte[] con = (byte[]) o;
                bytesread = con.length;
                total += bytesread;
                this.networkUtil.getBos().write(con, 0, bytesread);
            }
            flush_and_close_streams();

            System.out.println(acknowledge);
            if(acknowledge.equalsIgnoreCase("COMPLETED")){
                System.out.println(" File download completed");
            }

        }catch(Exception e){
            System.out.println(e);
        }
    }

    public void showFilesOfClient(String Client_id){
        try {
            Object o = this.networkUtil.read();
            Integer is_available = 0;
            if( o.getClass().equals( is_available.getClass() ) ){
                is_available = (Integer) o;
                if( is_available == 0 ){
                    System.out.println("There is no client having id "+ Client_id );
                    return;
                }
            }
            List<String> fileList = (List<String>) o;
            System.out.println(Client_id + " - public files : ");
            for( int i = 0 ; i < fileList.size() ; i++ ){
                System.out.println(fileList.get(i));
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public void searchOtherCliens(){
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("Enter the client ID :");
            String client_id = scanner.nextLine();
            this.networkUtil.write(client_id);

            showFilesOfClient(client_id);

            System.out.println("Wanna send a request to download a file from the above List ?");
            System.out.println("1. Yaa, obviously \t \t 2. No, just wanted to see the list ");
            String optionGiven = scanner.nextLine();
            this.networkUtil.write(optionGiven);
            if(optionGiven.equalsIgnoreCase("1")){
                System.out.println("Enter the filename you want to download : ");
                String name_of_file = scanner.nextLine();
                this.networkUtil.write(name_of_file);
                String available = (String) this.networkUtil.read();
                if(available.equalsIgnoreCase("available")){
                    download_file_from_server(name_of_file);
                }
                else{
                    System.out.println("There is no file with name : " + name_of_file );
                }

            }
        }catch (Exception e){
            System.out.println(e);
        }

    }

    public void sendRequest(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the file request, consisting of a short file description and a request id,\n" +
                "This request will be broadcast to all the connected clients.");
        String request_message = scanner.nextLine();
        try {
            this.networkUtil.write(request_message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void viewRequests(){
        System.out.println("Unread Messages : ");
        ArrayList<String> messages = null;
        try {
            messages = (ArrayList<String>) this.networkUtil.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        for(String msg : messages){
            System.out.println("Message ID : " + msg);
        }
        ArrayList<String> uploaded_files = null;
        try {
            uploaded_files = (ArrayList<String>) this.networkUtil.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Here is all the uploaded files by otehr clients, seeing your messages : ");
        for(String ups : uploaded_files){
            System.out.println("Request ID : " + ups);
        }
    }

    public void grantRequests(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter message id ");
        int message_id = parseInt(scanner.nextLine());
        try {
            this.networkUtil.write(message_id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        boolean is_available = false;
        try {
            is_available = (boolean) this.networkUtil.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if( is_available == false ){
            System.out.println("No message id exists with id "+ message_id);
            return;
        }
        send_file();
    }
}
