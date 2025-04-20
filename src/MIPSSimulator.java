import java.util.Dictionary;
import java.util.Hashtable;

public class MIPSSimulator {
    //Program counter will be inherently divided by 4.
    //Commands that use PC like branches and jumps are already word addressed
    private int programCounter;
    //I think string is good for this since everything else is strings.
    // Include a read register function.
    private String[] registerArray = new String[32];
    //Arbitrary sizes. Could be dynamically resized, but this works for now.
    private String[] dataArray = new String[256];
    private Instruction[] textArray = new Instruction[256];
    //Codes in $v0 for syscalls
    private final int PRINTINTCODE = 1;
    private final int PRINTSTRINGCODE = 4;
    private final int READINTCODE = 5;
    private final int TERMINATECODE = 10;

    private boolean terminateSimulation = false;

    /*
    Main simulation loop.
    Run until end of program or syscall where registerArray[2] = 10
    Inputs: String paths for .data and .text file
    Output: 0 on safe return, -1 on unsafe return.
     */
    public int mainLoop(String dataFile, String textFile){
        while(textArray[programCounter] != null && !terminateSimulation){

        }
        return -1;
    }

    /*
    Simulate add instruction. rs + rt -> rd
     */
    private void add(String rs, String rt, String rd){
        int decRs = Integer.parseInt(rs, 16);
        int decRt = Integer.parseInt(rt, 16);
        int decRd = Integer.parseInt(rd, 16);
        registerArray[decRd] = String.format("%08x", decRs + decRt);
    }

    /*
    Simulate addiu instruction. rs + immediate -> rt
     */
    private void addiu(String rs, String rt, String immediate){
        int decRs = Integer.parseInt(rs, 16);
        int decRt = Integer.parseInt(rt, 16);
        int decImmediate = Integer.parseInt(immediate, 16);
        registerArray[decRt] = String.format("%08x", decRs + decImmediate);
    }

    /*
    Simulate and instruction. rs & rt -> rd
     */
    private void and(String rs, String rt, String rd){
        int decRs = Integer.parseInt(rs, 16);
        int decRt = Integer.parseInt(rt, 16);
        int decRd = Integer.parseInt(rd, 16);
        registerArray[decRd] = String.format("%08x", decRs & decRt);
    }

    /*
    Simulate andi instruction. rs & immediate -> rt
     */
    private void andi(String rs, String rt, String immediate){
        int decRs = Integer.parseInt(rs, 16);
        int decRt = Integer.parseInt(rt, 16);
        int decImmediate = Integer.parseInt(immediate, 16);
        registerArray[decRt] = String.format("%08x", decRs & decImmediate);
    }

    /*
    Simulate beq instruction. if rs == rt then PC + offset(immediate)
    Instruction already works using PC+4 and PC is incremented in main loop
     */
    private void beq(String rs, String rt, String offset){
        int decRs = Integer.parseInt(rs, 16);
        int decRt = Integer.parseInt(rt, 16);
        int decOffset = Integer.parseInt(offset, 16);
        //I think this will convert to negative. Check if highest bit is 1
        //If it is, undo two's complement and negate the integer.
        if((decOffset >> 15) == 1){
            decOffset = ~decOffset;
            decOffset += 1;
            decOffset = -decOffset;
        }
        if(registerArray[decRs].equals(registerArray[decRt])){
            programCounter += decOffset;
        }
    }

    /*
    Simulate bne instruction. If rs != rt then PC + offset(immediate)
     */
    private void bne(String rs, String rt, String offset){
        int decRs = Integer.parseInt(rs, 16);
        int decRt = Integer.parseInt(rt, 16);
        int decOffset = Integer.parseInt(offset, 16);
        //I think this will convert to negative. Check if highest bit is 1
        //If it is, undo two's complement and negate the integer.
        if((decOffset >> 15) == 1){
            decOffset = ~decOffset;
            decOffset += 1;
            decOffset = -decOffset;
        }
        if(!registerArray[decRs].equals(registerArray[decRt])){
            programCounter += decOffset;
        }
    }
}
