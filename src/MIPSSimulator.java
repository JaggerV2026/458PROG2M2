import java.io.File;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class MIPSSimulator {
    //Program counter will be inherently divided by 4.
    //Commands that use PC like branches and jumps are already word addressed
    private int programCounter;
    private final int INITIALSP = 2147479548;
    private final int INITIALDATA = 268500992;
    //I think string is good for this since everything else is strings.
    // Include a read register function.
    private String[] registerArray = new String[32];
    private String[] memoryArray = new String[256];
    //Arbitrary sizes. Could be dynamically resized, but this works for now.
    private String[] dataArray = new String[256];
    private Instruction[] textArray = new Instruction[256];
    //Codes in $v0 for syscalls
    private final int PRINTINTCODE = 1;
    private final int PRINTSTRINGCODE = 4;
    private final int READINTCODE = 5;
    private final int TERMINATECODE = 10;

    private boolean terminateSimulation = false;

    public MIPSSimulator() {
        //$zero value
        registerArray[0] = "00000000";
        //$sp value
        registerArray[29] = "7fffeffc";
    }

    /*
    Main simulation loop.
    Run until end of program or syscall where registerArray[2] = 10
    Inputs: String paths for .data and .text file
    Output: 0 on safe return, -1 on unsafe return.
     */
    public int mainLoop(String dataFile, String textFile) {
       //Read files
        try{
            //Read .data
            File dataInput = new File(dataFile);
            Scanner dataReader = new Scanner(dataInput);
            //Want to end at a line of 00000000, but should be included in case it
            //contains the null termination
            boolean endOfData = false;
            int dataArrayIndex = 0;
            while(dataReader.hasNextLine() & !endOfData){
                String dataLine = dataReader.nextLine();
                endOfData = dataLine.equals("00000000");
                //Each element should contain 2 hex characters
                /*
                for(int i = 0; i < 4; i++){
                    dataArray[dataArrayIndex] = dataLine.substring(i * 2, (i * 2) + 2);
                    ++dataArrayIndex;
                }
                 */
                //Reversing data for the array
                for(int i = 3; i >= 0; i--){
                    dataArray[dataArrayIndex] = dataLine.substring(i * 2, (i * 2) + 2);
                    ++dataArrayIndex;
                }
            }
            dataReader.close();

            //Read .text
            File textInput = new File(textFile);
            Scanner textReader = new Scanner(textInput);
            int textArrayIndex = 0;
            while(textReader.hasNextLine()){
                textArray[textArrayIndex] = new Instruction(textReader.nextLine());
                ++textArrayIndex;
            }
            textReader.close();

        }
        catch(FileNotFoundException e){
            System.out.println("Data file not found");
        }

        //Maybe include a check that prohibits $0 from being changed
        while (textArray[programCounter] != null && !terminateSimulation) {
            Instruction currentInstruction = textArray[programCounter];
            ++programCounter;
            switch(currentInstruction.getMnemonic()){
                case "add":
                    add(currentInstruction.getRs(),
                            currentInstruction.getRt(),
                            currentInstruction.getRd());
                    break;
                case "addiu":
                    addiu(currentInstruction.getRs(),
                            currentInstruction.getRt(),
                            currentInstruction.getImmediate());
                    break;
                case "and":
                    and(currentInstruction.getRs(),
                            currentInstruction.getRt(),
                            currentInstruction.getRd());
                    break;
                case "andi":
                    andi(currentInstruction.getRs(),
                            currentInstruction.getRt(),
                            currentInstruction.getImmediate());
                    break;
                case "beq":
                    beq(currentInstruction.getRs(),
                            currentInstruction.getRt(),
                            currentInstruction.getImmediate());
                    break;
                case "bne":
                    bne(currentInstruction.getRs(),
                            currentInstruction.getRt(),
                            currentInstruction.getImmediate());
                    break;
                case "j":
                    j(currentInstruction.getIndex());
                    break;
                case "lui":
                    lui(currentInstruction.getRt(),
                            currentInstruction.getImmediate());
                    break;
                case "lw":
                    lw(currentInstruction.getRs(),
                            currentInstruction.getRt(),
                            currentInstruction.getImmediate());
                    break;
                case "or":
                    or(currentInstruction.getRs(),
                            currentInstruction.getRt(),
                            currentInstruction.getRd());
                    break;
                case "ori":
                    ori(currentInstruction.getRs(),
                            currentInstruction.getRt(),
                            currentInstruction.getImmediate());
                    break;
                case "slt":
                    slt(currentInstruction.getRs(),
                            currentInstruction.getRt(),
                            currentInstruction.getRd());
                    break;
                case "sub":
                    sub(currentInstruction.getRs(),
                            currentInstruction.getRt(),
                            currentInstruction.getRd());
                    break;
                case "sw":
                    sw(currentInstruction.getRs(),
                            currentInstruction.getRt(),
                            currentInstruction.getImmediate());
                    break;
                case "syscall":
                    syscall();
                    break;
                default:
                    System.out.println("Unknown instruction");
                    break;
            }
        }
        //Used to indicate safe vs unsafe termination
        if(terminateSimulation){
            return 0;
        }
        else{
            return -1;
        }
    }

    /*
    Simulate add instruction. rs + rt -> rd
     */
    private void add(String rs, String rt, String rd) {
        int decRs = Integer.parseInt(rs, 16);
        int decRt = Integer.parseInt(rt, 16);
        int decRd = Integer.parseInt(rd, 16);
        int leftArg = Integer.parseInt(registerArray[decRs], 16);
        int rightArg = Integer.parseInt(registerArray[decRt], 16);
        registerArray[decRd] = String.format("%08x", leftArg + rightArg);
    }

    /*
    Simulate addiu instruction. rs + immediate -> rt
     */
    private void addiu(String rs, String rt, String immediate) {
        int decRs = Integer.parseInt(rs, 16);
        int decRt = Integer.parseInt(rt, 16);
        int decImmediate = Integer.parseInt(immediate, 16);
        int leftArg = Integer.parseInt(registerArray[decRs], 16);
        registerArray[decRt] = String.format("%08x", leftArg + decImmediate);
    }

    /*
    Simulate and instruction. rs & rt -> rd
     */
    private void and(String rs, String rt, String rd) {
        int decRs = Integer.parseInt(rs, 16);
        int decRt = Integer.parseInt(rt, 16);
        int decRd = Integer.parseInt(rd, 16);
        int leftArg = Integer.parseInt(registerArray[decRs], 16);
        int rightArg = Integer.parseInt(registerArray[decRt], 16);
        registerArray[decRd] = String.format("%08x", leftArg & rightArg);
    }

    /*
    Simulate andi instruction. rs & immediate -> rt
     */
    private void andi(String rs, String rt, String immediate) {
        int decRs = Integer.parseInt(rs, 16);
        int decRt = Integer.parseInt(rt, 16);
        int decImmediate = Integer.parseInt(immediate, 16);
        int leftArg = Integer.parseInt(registerArray[decRs], 16);
        registerArray[decRt] = String.format("%08x", leftArg & decImmediate);
    }

    /*
    Simulate beq instruction. if rs == rt then PC + offset(immediate)
    Instruction already works using PC+4 and PC is incremented in main loop
     */
    private void beq(String rs, String rt, String offset) {
        int decRs = Integer.parseInt(rs, 16);
        int decRt = Integer.parseInt(rt, 16);
        int decOffset = Integer.parseInt(offset, 16);
        //I think this will convert to negative. Check if highest bit is 1
        //If it is, undo two's complement and negate the integer.
        if ((decOffset >> 15) == 1) {
            decOffset = ~decOffset;
            decOffset += 1;
            decOffset = -decOffset;
        }
        if (registerArray[decRs].equals(registerArray[decRt])) {
            programCounter += decOffset;
        }
    }

    /*
    Simulate bne instruction. If rs != rt then PC + offset(immediate)
     */
    private void bne(String rs, String rt, String offset) {
        int decRs = Integer.parseInt(rs, 16);
        int decRt = Integer.parseInt(rt, 16);
        int decOffset = Integer.parseInt(offset, 16);
        //I think this will convert to negative. Check if highest bit is 1
        //If it is, undo two's complement and negate the integer.
        if ((decOffset >> 15) == 1) {
            decOffset = ~decOffset;
            decOffset += 1;
            decOffset = -decOffset;
        }
        if (!registerArray[decRs].equals(registerArray[decRt])) {
            programCounter += decOffset;
        }
    }

    /*
    Simulate j instruction. PC = ?
    4194304 = 0x00400000
     */
    private void j(String index) {
        int decIndex = Integer.parseInt(index, 16);
        //Sign extend index by 2 bits
        //Find program counter with first value being 0x00400000
        //Get 4 highest bits by bitshifting right 28, then left 28
        //Bitwise or of sign extended index and program counter
        //Convert back to initial value of zero by subtracting 0x00400000
        decIndex = decIndex << 2;
        int truePC = ((programCounter + 4194304) >> 28) << 28;
        programCounter = ((decIndex | truePC) - 4194304) >> 2;
        //programCounter = ((decIndex) | (((programCounter + 4194304) >> 28) << 28)) - 419304;
    }

    /*
    Simulate lui instruction. immediate | (16 bits of zero) -> rt
     Basically  "instruction" + "0000" -> rt
     */
    private void lui(String rt, String immediate) {
        int decRt = Integer.parseInt(rt, 16);
        registerArray[decRt] = immediate + "0000";
    }

    /*
    Simulate lw instruction. memory[base(rs) + offset(immediate)] -> rt
     */
    private void lw(String base, String rt, String offset) {
        int decBase = Integer.parseInt(base, 16);
        int decRt = Integer.parseInt(rt, 16);
        int decOffset = Integer.parseInt(offset, 16);
        int regBase = Integer.parseInt(registerArray[decBase], 16);
        //Memory array is only 256 elements, but most references are based off
        //of $sp which starts at INITIALSP. Need to find difference
        registerArray[decRt] = memoryArray[INITIALSP - (regBase + decOffset)];
    }

    /*
    Simulate or instruction. rs | rt -> rd
     */
    private void or(String rs, String rt, String rd) {
        int decRs = Integer.parseInt(rs, 16);
        int decRt = Integer.parseInt(rt, 16);
        int decRd = Integer.parseInt(rd, 16);
        int leftArg = Integer.parseInt(registerArray[decRs], 16);
        int rightArg = Integer.parseInt(registerArray[decRt], 16);
        registerArray[decRd] = String.format("%08x", leftArg | rightArg);
    }

    /*
    Simulate ori instruction. rs | immediate -> rt
     */
    private void ori(String rs, String rt, String immediate) {
        int decRs = Integer.parseInt(rs, 16);
        int decRt = Integer.parseInt(rt, 16);
        int decImmediate = Integer.parseInt(immediate, 16);
        int leftArg = Integer.parseInt(registerArray[decRs], 16);
        registerArray[decRt] = String.format("%08x", leftArg | decImmediate);
    }

    /*
    Simulate slt instruction. (rs < rt) -> rd. True = 1, False = 0.
     */
    private void slt(String rs, String rt, String rd) {
        int decRs = Integer.parseInt(rs, 16);
        int decRt = Integer.parseInt(rt, 16);
        int decRd = Integer.parseInt(rd, 16);
        int leftArg = Integer.parseInt(registerArray[decRs], 16);
        int rightArg = Integer.parseInt(registerArray[decRt], 16);
        if (leftArg < rightArg) {
            registerArray[decRd] = String.format("%08x", 1);
        } else {
            registerArray[decRd] = String.format("%08x", 0);
        }
    }

    /*
    Simulate sub instruction. rs - rt -> rd
    */
    private void sub(String rs, String rt, String rd) {
        int decRs = Integer.parseInt(rs, 16);
        int decRt = Integer.parseInt(rt, 16);
        int decRd = Integer.parseInt(rd, 16);
        int leftArg = Integer.parseInt(registerArray[decRs], 16);
        int rightArg = Integer.parseInt(registerArray[decRt], 16);
        registerArray[decRd] = String.format("%08x", leftArg - rightArg);
    }

    /*
    Simulate sw instruction. rt -> memory[base(rs) + offset(immediate)]
    */
    private void sw(String base, String rt, String offset) {
        int decBase = Integer.parseInt(base, 16);
        int decRt = Integer.parseInt(rt, 16);
        int decOffset = Integer.parseInt(offset, 16);
        int regBase = Integer.parseInt(registerArray[decBase], 16);
        //Memory array is only 256 elements, but most references are based off
        //of $sp which starts at INITIALSP. Need to find difference
        memoryArray[INITIALSP - (regBase + decOffset)] = registerArray[decRt];
    }

    /*
    Simulate syscall instruction
        $v0 = 1: print integer in $a0
        $v0 = 4: print null terminated string in $a0
        $v0 = 5: read integer and save to $v0
        $v0 = 10: Exit program
    $v0 is register 2
    $a0 is register 4
     */
    private void syscall(){
        int v0 = Integer.parseInt(registerArray[2], 16);
        switch(v0){
            case PRINTINTCODE:
                int a0 = Integer.parseInt(registerArray[4], 16);
                System.out.println(a0);
                break;
            case PRINTSTRINGCODE:
                //Might need to divide this by 4. Not sure yet.
                //First read gives initial index of 1, but array starts at 0.
                //I'm assuming this is just how this works.
                int dataIndex = Integer.parseInt(registerArray[4],16) - INITIALDATA;
                int dataRead = Integer.parseInt(dataArray[dataIndex], 16);
                while(dataRead != 0){
                    char ch = (char)dataRead;
                   System.out.print(ch);
                   ++dataIndex;
                   /* Original idea
                   if(dataIndex % 4 == 0){
                       dataIndex += 7;
                   }
                   else{
                       --dataIndex;
                   }
                   */
                   dataRead = Integer.parseInt(dataArray[dataIndex], 16);
                }
                break;
            case READINTCODE:
                Scanner scan = new Scanner(System.in);
                int readInt = scan.nextInt();
                registerArray[2] = String.format("%08x", readInt);
                scan.close();
                break;
            case TERMINATECODE:
                terminateSimulation = true;
                break;
        }
    }
}

