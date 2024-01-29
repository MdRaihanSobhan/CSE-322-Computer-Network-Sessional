import java.util.Scanner;


public class Main {
    public static void printErrorDetectionResult(boolean errorDetected){
        System.out.print("result of CRC checksum matching: ");
        if(errorDetected){
            System.out.println("error detected");
        }
        else{
            System.out.println("no error detected");
        }
    }
    public static void printDecodedData(Receiver receiver){
        for(int i=0;i<receiver.getNumberOfRows();i++){
            System.out.println(receiver.getDeCodedData().get(i));
        }
        System.out.println();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String dataString;
        System.out.print("enter data string: ");
        dataString = scanner.nextLine();
        System.out.print("enter number of data bytes in a row <m>: ");
        int m = scanner.nextInt();
        System.out.print("enter probability <p>: ");
        double p = scanner.nextDouble();
        System.out.print("enter generator polynomial <g(x)>: ");
        String g = scanner.next();
        System.out.println();

        Input input = new Input(dataString, m, p, g);

        // Step 1: If the size of the data string is not a multiple of m,
        // append the padding character (~) to the data string accordingly.
        // Print the updated data string.
        input.addPadding();
        System.out.println("data string after padding: " + input.getDataString());
        System.out.println();

        // Step 2: Create the data block, which contains the 8-bit binary representation of the ascii codes of the characters of the data string.
        // Each row contains ascii codes of m characters. The first row shows the first m characters,
        // the second row shows the next m characters, and so on. Print the data block. Note that,
        // there will be l/m rows in the data block, where l is the length of the padded data string.
        Sender sender = new Sender(input);
        sender.CreateDataBlock();
        System.out.println("data block <ascii code of m characters per row>: ");
        sender.ConvertToBinaryASCII();

        // Step 3: Add check bits to correct at most one-bit error in each row of the data block.
        // Print the updated data block.Note that, the check bits must be shown in a different color (green)
        System.out.println("data block after adding check bits: ");
        sender.addCheckBits();

        // Step 4: Serialize the data block in a column-major fashion. Print the serialized data.
        String serialized= sender.serializeData();
        System.out.println("data bits after column-wise serialization: \n" + serialized);
        System.out.println();

        // Step 5:  Compute the CRC checksum of the above bit stream using the generator polynomial
        // Append the checksum to the bit stream. This is the frame to be transmitted.
        // Print the frame. Note that, the appended checksum must be shown in a different color (cyan).
        sender.setCrcChecksum();
        System.out.println("data bits after appending CRC checksum (sent frame>: ");
        sender.setTransmittedData();
        sender.printTransmittedData();
        System.out.println();

        // Step 6: Simulate the physical transmission by toggling each bit of the stream with a probability of p.
        // Print the received frame. Note that, the erroneous bits must be shown in a different color (red).
        Receiver receiver = new Receiver();
        Transmission transmission = new Transmission(input, sender, receiver);
        transmission.transmitData();
        System.out.println("received frame: ");
        transmission.printReceivedData();

        // Step 7: Verify the correctness of the received frame using the generator polynomial
        // Print the result of the error detection procedure.
        boolean errorDetected = transmission.detectError();
        printErrorDetectionResult(errorDetected);
        System.out.println();

        // Step 8: Remove the CRC checksum bits from the data stream and de-serialize it into the data-block in a column-major fashion.
        // Print the data block. Note that, the erroneous bits must be shown in a different color (red).
        System.out.println("data block after removing CRC checksum bits: ");
        receiver.setReceivedDataWithoutChecksum();
        receiver.setDeCodedData();
        transmission.printDataBlockAfterRemovingCRC();

        // Step 9: Correct the error in each row of the data block according to the method of book
        // Observe that, if there is more than one error in a row,
        // this error correction mechanism will fail to correct the error.
        // Print the data block after correcting the errors and removing the check bits.
        receiver.correctBlocks();
        // print the corrected data blocks
//        System.out.println("data block after error correction: ");
//        printDecodedData(receiver);
        receiver.removeCheckBits();
        System.out.println("data block after removing check bits: ");
        printDecodedData(receiver);

        // Step 10:  From the bits of the data block, compute the ascii codes of the characters.
        // Print the data string.
        System.out.print("output frame: ");
        receiver.setOutputframe();
        System.out.println(receiver.getOutputframe());

    }
}