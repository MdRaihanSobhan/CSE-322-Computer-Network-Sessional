import java.util.Vector;

public class Sender {
    Input input;
    int length;
    int numberOfRows;
    int dataBlock[][];
    String binaryString;
    String encodedDataString;
    Vector<String> encodedData;

    String serializedData;
    String crcChecksum; // CRC checksum
    String transmittedData;

    public Sender(Input input) {
        this.input = input;
        this.length = input.getDataString().length();
        this.numberOfRows = (int) Math.ceil((double) length / input.getDataBytesInARow());
        this.dataBlock = new int[numberOfRows][input.getDataBytesInARow()];
        this.binaryString= "";
        this.encodedDataString = "";
        this.encodedData = new Vector<String>();
        this.serializedData = "";
        this.crcChecksum = "";
        this.transmittedData = "";
    }

    public void CreateDataBlock(){
        int index = 0;
        for (int i = 0; i < numberOfRows; i++) {
            for (int j = 0; j < input.getDataBytesInARow(); j++) {
                this.dataBlock[i][j] = (int) input.getDataString().charAt(index);
                index++;
            }
        }
    }


    public void ConvertToBinaryASCII(){
        for (int i = 0; i < this.numberOfRows; i++) {
            for (int j = 0; j < input.getDataBytesInARow(); j++) {
                this.binaryString+=String.format("%8s", Integer.toBinaryString(this.dataBlock[i][j])).replace(' ', '0');
                System.out.print(String.format("%8s", Integer.toBinaryString(this.dataBlock[i][j])).replace(' ', '0'));
            }
            System.out.println();
        }
        System.out.println();
    }

    public void addCheckBits(){
        int m = this.input.getDataBytesInARow();
        for (int i = 0; i < this.numberOfRows; i++) {
            String encodedRow = Computations.logarithmicHammingEncode(this.binaryString.substring(i * m * 8, (i + 1) * m * 8));
            this.encodedDataString += encodedRow;
            this.encodedData.add(encodedRow);
            printEncodedData(encodedRow);
        }
        System.out.println();
    }

    public String serializeData(){
        for (int i = 0; i < this.encodedData.get(0).length(); i++) {
            for(int j=0; j<this.numberOfRows; j++){
                this.serializedData += this.encodedData.get(j).charAt(i);
            }
        }
        return this.serializedData;
    }



    public void printEncodedData(String encodedData) {
        for (int i = 0; i < encodedData.length(); i++) {
            char bit = encodedData.charAt(i);
            if (((i + 1) & (i)) == 0) { // Check if i+1 is a power of 2
                System.out.print("\u001B[32m" + bit + "\u001B[0m"); // Green color for check bits
            } else {
                System.out.print(bit);
            }
        }
        System.out.println(); // Print a newline after the encoded data
    }

    public void printTransmittedData(){
        // print the checksum in color cyan
        System.out.println(this.serializedData+ "\u001B[36m" + this.crcChecksum + "\u001B[0m");
    }
    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public void setNumberOfRows(int numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public int[][] getDataBlock() {
        return dataBlock;
    }

    public void setDataBlock(int[][] dataBlock) {
        this.dataBlock = dataBlock;
    }

    public String getBinaryString() {
        return binaryString;
    }

    public void setBinaryString(String binaryString) {
        this.binaryString = binaryString;
    }

    public String getEncodedDataString() {
        return encodedDataString;
    }

    public void setEncodedDataString(String encodedDataString) {
        this.encodedDataString = encodedDataString;
    }

    public Vector<String> getEncodedData() {
        return encodedData;
    }

    public void setEncodedData(Vector<String> encodedData) {
        this.encodedData = encodedData;
    }

    public String getSerializedData() {
        return serializedData;
    }

    public void setSerializedData(String serializedData) {
        this.serializedData = serializedData;
    }

    public String getCrcChecksum() {
        return crcChecksum;
    }

    public String getTransmittedData() {
        return transmittedData;
    }

    public void setCrcChecksum() {
        this.crcChecksum = Computations.computeCRC(this.serializedData, this.input.getGeneratorPolynomial());
    }

    public void setTransmittedData(){
        this.transmittedData = this.serializedData + this.crcChecksum;
    }

}
