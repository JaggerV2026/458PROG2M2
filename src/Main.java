public class Main {
    public static void main(String[] args) {

        MIPSSimulator sim = new MIPSSimulator();
        String dataFilePath = "C:\\Users\\jagge\\COMPSCI458Java\\458PROG2M2\\TestFiles\\EvenOrOdd\\EvenOrOdd.data";
        String textFilePath = "C:\\Users\\jagge\\COMPSCI458Java\\458PROG2M2\\TestFiles\\EvenOrOdd\\EvenOrOdd.text";
        sim.mainLoop(dataFilePath, textFilePath);
    }
}