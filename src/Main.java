public class Main {
    public static void main(String[] args) {

        MIPSSimulator sim = new MIPSSimulator();
        int exitCode = sim.mainLoop(args[1], args[0]);
        if(exitCode == 0){
            System.out.println("\n-- program is finished running --");
        }
        else{
            System.out.println("\n-- program is finished running (dropped off bottom) --");
        }
    }
}