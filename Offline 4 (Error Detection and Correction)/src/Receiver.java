import java.util.Vector;

public class Receiver {
    String receivedData;
    String crcChecksum;
    String generatorPolynomial; // CRC generator polynomial
    String receivedDataWithoutChecksum; // Received data without CRC checksum
    Vector<String> deCodedData;
    int dataBytesInARow; // Number of data bytes
    int numberOfRows; // Number of rows
    String outputframe;

    public Receiver() {
        this.receivedData = "";
        this.crcChecksum = "";
        this.generatorPolynomial = "";
        this.receivedDataWithoutChecksum = "";
        this.deCodedData = new Vector<String>();
        this.outputframe = "";
    }

    public String getReceivedData() {
        return receivedData;
    }

    public void setReceivedData(String receivedData) {
        this.receivedData = receivedData;
    }

    public String getCrcChecksum() {
        return crcChecksum;
    }

    public void setCrcChecksum() {
        this.crcChecksum = Computations.computeCRC(this.receivedData, this.generatorPolynomial);// CRC checksum calculated from received frame
    }

    public String getGeneratorPolynomial() {
        return generatorPolynomial;
    }

    public void setGeneratorPolynomial(String generatorPolynomial) {
        this.generatorPolynomial = generatorPolynomial;
    }

    public String getReceivedDataWithoutChecksum() {
        return receivedDataWithoutChecksum;
    }

    public void setReceivedDataWithoutChecksum() {
        this.receivedDataWithoutChecksum = this.receivedData.substring(0, receivedData.length() - this.crcChecksum.length());
    }

    public int getDataBytesInARow() {
        return dataBytesInARow;
    }

    public void setDataBytesInARow(int dataBytesInARow) {
        this.dataBytesInARow = dataBytesInARow;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public void setNumberOfRows(int numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public void setDeCodedData(){
        int r = 1; // Number of parity bits (initialized for 2^r >= m + r)
        // Calculate the number of parity bits required
        while (Math.pow(2, r) < this.dataBytesInARow*8 + r + 1) {
            r++;
        }
        for(int i=0;i<this.numberOfRows; i++){
            this.deCodedData.add("");
        }
        int rowlength = this.dataBytesInARow*8 + r;
        int iterator = 0;
        for(int i=0;i<rowlength; i++){
            for(int j=0; j<this.numberOfRows; j++){
                Character temp = receivedDataWithoutChecksum.charAt(iterator++);
                this.deCodedData.set(j, deCodedData.get(j)+temp);
            }
        }
    }

    public Vector<String> getDeCodedData() {
        return deCodedData;
    }

    public void correctBlocks() {
        for(int i=0;i<this.numberOfRows;i++){
            this.deCodedData.set(i, Computations.correctError(deCodedData.get(i)));
        }
    }

    public void removeCheckBits(){
        for(int i=0; i<this.numberOfRows; i++){
            String temp = "";
            for(int j=0; j<this.deCodedData.get(i).length(); j++){
                if(((j+1)&(j))==0){
                    continue;
                }
                temp+=this.deCodedData.get(i).charAt(j);
            }
            this.deCodedData.set(i, temp);
        }
    }

    public void setOutputframe() {
        for(int i=0;i<this.numberOfRows;i++){
            for(int j=0; j<this.deCodedData.get(i).length(); j+=8){
                this.outputframe+=(char)Integer.parseInt(deCodedData.get(i).substring(j, j+8), 2);
            }
        }
    }

    public String getOutputframe() {
        return outputframe;
    }
}
