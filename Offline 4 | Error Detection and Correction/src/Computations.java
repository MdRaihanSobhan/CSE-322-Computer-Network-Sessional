public class Computations {
    public static String computeCRC(String inputDataStream, String generatorPolynomial) {
        int maxDegreeOfGeneratorPolynomial = generatorPolynomial.length() - 1;
        int inputDataStreamLength = inputDataStream.length();
        char[] inputBits = inputDataStream.toCharArray();
        char[] generatorPoly = generatorPolynomial.toCharArray();

        // Multiply the input bitstream by 2^(maxDegreeOfGeneratorPolynomial - 1)
        char[] trailingZeroes = new char[maxDegreeOfGeneratorPolynomial];
        for (int i = 0; i < maxDegreeOfGeneratorPolynomial; i++) {
            trailingZeroes[i] = '0';
        }

        // Concatenate input bitstream with trailing zeros
        char[] newDataStream = new char[inputDataStreamLength + maxDegreeOfGeneratorPolynomial];
        System.arraycopy(inputBits, 0, newDataStream, 0, inputDataStreamLength);
        System.arraycopy(trailingZeroes, 0, newDataStream, inputDataStreamLength, maxDegreeOfGeneratorPolynomial);

        // Perform CRC division (modulo-2 division)
        for (int i = 0; i < inputDataStreamLength; i++) {
            if (newDataStream[i] == '1') {
                for (int j = 0; j <= maxDegreeOfGeneratorPolynomial; j++) {
                    newDataStream[i + j] = (newDataStream[i + j] == generatorPoly[j]) ? '0' : '1'; // XOR operation for modulo-2 division
                }
            }
        }
//        System.out.println("New Data Stream : "+ newDataStream);

        // Extract the CRC checksum (remainder) as the last maxDegreeOfGeneratorPolynomial bits1
        String checksum_CRC = new String(newDataStream, inputDataStreamLength, maxDegreeOfGeneratorPolynomial);

        return checksum_CRC;
    }


    public static String logarithmicHammingEncode(String data) {
        int m = data.length();
        int r = 1; // Number of parity bits (initialized for 2^r >= m + r)
        // Calculate the minimum number of parity bits required
        while (Math.pow(2, r) < m + r + 1) {
            r = r + 1;
        }


        int n = m + r; // Total number of bits in the encoded data
        char[] encoded = new char[n];  // Create an array to hold the encoded bits (including parity bits)

        int dataIndex = 0;
        int parityIndex = 0;
        // Initialize the encoded array with the original data and placeholders for parity bits
        for (int i = 0; i < n; i++) {
            if (i + 1 == Math.pow(2, parityIndex)) {
                encoded[i] = '0'; // Initialize parity bit positions with '0' , even parity
                parityIndex++;
            } else {
                encoded[i] = data.charAt(dataIndex);
                dataIndex++;
            }
        }

        // Calculate the parity bits
        for (int i = 0; i < r; i++) {
            int index_of_parity = (int) Math.pow(2, i) - 1;
            int parityBit = 0;

            for (int j = index_of_parity; j < n; j++) {
                if (((j + 1) & (1 << i)) != 0) {
                    parityBit ^= (encoded[j] - '0'); // XOR operation for parity calculation
                }
            }

            encoded[index_of_parity] = (char) ('0' + parityBit); // Update the parity bit
        }

        return new String(encoded);
    }



    // Correct at most 1 bit error in a Hamming encoded binary string
    public static String correctError(String data) {
        int numberOfParityBits = 0;
        int numberOfDataBits = 0;
        int DataLengthByBits = data.length();
        int position_of_error_bit = 0;

        // Calculate the number of parity and data bits
        while (numberOfDataBits + numberOfParityBits + 1 <= DataLengthByBits) {
            numberOfParityBits++;
            numberOfDataBits += numberOfParityBits; // data bits increase by the number of parity bits each time
        }

        int[] bits = new int[DataLengthByBits];
        for (int i = 0; i < DataLengthByBits; i++) {
            bits[i] = Character.getNumericValue(data.charAt(i));
        }

        // Check parity bits
        for (int i = 0; i < numberOfParityBits; i++) {
            int parityBitIndex = (int) Math.pow(2, i) - 1;
            int parity = 0;
            for (int j = parityBitIndex; j < DataLengthByBits; j += 2 * (parityBitIndex + 1)) {
                for (int k = j; k < j + parityBitIndex + 1 && k < DataLengthByBits; k++) {
                    parity ^= bits[k];
                }
            }
            if (parity != 0) {
                position_of_error_bit += parityBitIndex + 1; // Add the position of the error
            }
        }

        if (position_of_error_bit > 0) {
            // Correct the error bit
            if(position_of_error_bit<= DataLengthByBits) {
                bits[position_of_error_bit - 1] ^= 1;
            }
        }

        // Convert the corrected bits back to a string
        StringBuilder dataAfterCorrection = new StringBuilder();
        for (int bit : bits) {
            dataAfterCorrection.append(bit);
        }

        return dataAfterCorrection.toString();
    }

}
