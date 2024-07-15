package requests;

import java.util.ArrayList;

public class Message {
    private int message_id;
    private String sender;
    private String message;
    private ArrayList<Acceptor> uploaded_file_list;


    public Message(int message_id, String sender, String message) {
        this.message_id = message_id;
        this.sender = sender;
        this.message = message;
        this.uploaded_file_list = new ArrayList<Acceptor>();
    }

    public void grantRequest(String acceptor_id, String up_directory){
        Acceptor p = new Acceptor(acceptor_id, up_directory);
        uploaded_file_list.add(p);
    }

    public int getMessage_id() {
        return message_id;
    }

    public void setMessage_id(int message_id) {
        this.message_id = message_id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<Acceptor> getUploaded_file_list() {
        return uploaded_file_list;
    }

    public void setUploaded_file_list(ArrayList<Acceptor> uploaded_file_list) {
        this.uploaded_file_list = uploaded_file_list;
    }
}
