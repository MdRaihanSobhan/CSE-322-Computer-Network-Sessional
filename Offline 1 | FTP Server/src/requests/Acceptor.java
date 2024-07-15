package requests;

public class Acceptor {
    private String acceptor_id;
    private String uploaded_directory;

    public Acceptor(String acceptor_id, String uploaded_directory) {
        this.acceptor_id = acceptor_id;
        this.uploaded_directory = uploaded_directory;
    }

    public String getAcceptor_id() {
        return acceptor_id;
    }

    public void setAcceptor_id(String acceptor_id) {
        this.acceptor_id = acceptor_id;
    }

    public String getUploaded_directory() {
        return uploaded_directory;
    }

    public void setUploaded_directory(String uploaded_directory) {
        this.uploaded_directory = uploaded_directory;
    }
}
