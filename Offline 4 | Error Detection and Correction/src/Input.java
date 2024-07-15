public class Input {
    String dataString;
    int dataBytesInARow; // Number of data bytes
    double probability; // Probability of bit error
    String generatorPolynomial; // Generator polynomial
    Character paddingChar;

    public Input(String dataString, int dataBytesInARow, double probability, String generatorPolynomial) {
        this.dataString = dataString;
        this.dataBytesInARow = dataBytesInARow;
        this.probability = probability;
        this.generatorPolynomial = generatorPolynomial;
        this.paddingChar = '~';
    }
    // If the size of the data string is not a multiple of m, append the padding character (~) to the data string accordingly. Print the updated data string.
    public void addPadding(){
        int padding = dataBytesInARow - (dataString.length() % dataBytesInARow);
        if (padding != dataBytesInARow) {
            for (int i = 0; i < padding; i++) {
                dataString += paddingChar;
            }
        }
    }

    public void setDataString(String dataString) {
        this.dataString = dataString;
    }

    public void setDataBytesInARow(int dataBytesInARow) {
        this.dataBytesInARow = dataBytesInARow;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public void setGeneratorPolynomial(String generatorPolynomial) {
        this.generatorPolynomial = generatorPolynomial;
    }

    public String getDataString() {
        return dataString;
    }

    public int getDataBytesInARow() {
        return dataBytesInARow;
    }

    public double getProbability() {
        return probability;
    }

    public String getGeneratorPolynomial() {
        return generatorPolynomial;
    }
}
