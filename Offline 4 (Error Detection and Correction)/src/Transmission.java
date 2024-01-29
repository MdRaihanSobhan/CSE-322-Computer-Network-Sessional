public class Transmission {
    Input input;
    Sender sender;
    Receiver receiver;
    public Transmission(Input input, Sender sender, Receiver receiver) {
        this.input = input;
        this.sender = sender;
        this.receiver = receiver;
        this.receiver.setGeneratorPolynomial(input.getGeneratorPolynomial());
        this.receiver.setDataBytesInARow(input.getDataBytesInARow());
        this.receiver.setNumberOfRows(sender.getNumberOfRows());
    }

    public void transmitData(){
        String receivedData="";
        for (int i = 0; i < sender.getTransmittedData().length(); i++) {

            if (Math.random() < this.input.getProbability()) {
                receivedData += (sender.getTransmittedData().charAt(i) == '0') ? '1' : '0';
            } else {
                receivedData += sender.getTransmittedData().charAt(i);
            }
        }
        receiver.setReceivedData(receivedData);
    }

    public void printReceivedData(){
        // print the received frame in red color
        for (int i = 0; i < this.receiver.getReceivedData().length(); i++) {
            if (this.receiver.getReceivedData().charAt(i) == this.sender.getTransmittedData().charAt(i)) {
                System.out.print(this.receiver.getReceivedData().charAt(i));
            } else {
                System.out.print("\u001B[31m" + this.receiver.getReceivedData().charAt(i) + "\u001B[0m");
            }
        }
        System.out.println("\n");
    }

    public boolean detectError(){
        receiver.setCrcChecksum();
        String receivedCRCChecksum = receiver.getCrcChecksum();

        if (receivedCRCChecksum.contains("1")) {
            return true;
        } else {
            return false;
        }
    }

    public void printDataBlockAfterRemovingCRC(){
        for(int i=0;i<this.sender.getNumberOfRows();i++){
            for(int j=0; j<this.receiver.getDeCodedData().get(i).length(); j++){
                if (this.receiver.getDeCodedData().get(i).charAt(j) == this.sender.getEncodedData().get(i).charAt(j)) {
                    System.out.print(this.receiver.getDeCodedData().get(i).charAt(j));
                } else {
                    System.out.print("\u001B[31m" + this.receiver.getDeCodedData().get(i).charAt(j) + "\u001B[0m");
                }
            }
            System.out.println();
        }
        System.out.println();
    }




}
