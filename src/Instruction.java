public class Instruction {
    //Used primarily to determine what variables to use in toString
    private final String RTYPE = "RFormat";
    private final String ITYPE = "IFormat";
    private final String JTYPE = "JFormat";
    private final String SYSCALL = "Syscall";

    private String instructionType;
    private String mnemonic;
    private String opcode;
    private String rs;
    private String rt;
    private String rd;
    private String shmt;
    private String funct;
    private String immediate;
    private String index;

    public Instruction(String hexString){
        disassemble(hexString);
    }

    public String toString(){
        String toPrint = "Error";
        switch(instructionType){
            case RTYPE:
                //mnemonic {opcode: XX, rs: XX, rt: XX, rd: XX, shmt: XX, funct: XX}
                toPrint = String.format("%s {opcode: %s, rs: %s, rt: %s, rd: %s, shmt: %s, funct: %s}",
                        mnemonic, opcode, rs, rt, rd, shmt, funct);
                break;
            case ITYPE:
                //mnemonic {opcode: XX, rs(base): XX, rt: XX, immediate(offset): XXXX}
                toPrint = String.format("%s {opcode: %s, rs(base): %s, rt: %s, immediate(offset): %s}",
                        mnemonic, opcode, rs, rt, immediate);
                break;
            case JTYPE:
                //mnemonic {opcode: XX, index: XXXXXXX}
                toPrint = String.format("%s {opcode: %s, index: %s}",
                        mnemonic, opcode, index);
                break;
            case SYSCALL:
                //mnemonic {opcode: XX, code: 000000, funct: XX}
                toPrint = String.format("%s {opcode: %s, code: 000000, funct: %s}",
                        mnemonic, opcode, funct);
                break;
        }
        return toPrint;
    }

    private void disassemble(String hexString){
        opcode = convertOpcode(hexString);
        instructionType = convertInstructionType(hexString);
        switch(instructionType){
            case RTYPE:
                rs = convertRs(hexString);
                rt = convertRt(hexString);
                rd = convertRd(hexString);
                shmt = convertShmt();
                funct = convertFunct(hexString);
                mnemonic = convertMnemonic();
                break;

            case ITYPE:
                mnemonic = convertMnemonic();
                rs = convertRs(hexString);
                rt = convertRt(hexString);
                immediate = convertImmediate(hexString);
                break;

            case JTYPE:
                mnemonic = convertMnemonic();
                index = convertIndex(hexString);
                break;

            case SYSCALL:
                funct = convertFunct(hexString);
                mnemonic = convertMnemonic();
                break;
        }
    }

    //Return the opcode associated with a given hex string.
    //Opcode is bits 1 to 6
    private String convertOpcode(String hexString){
        long decimalOpcode = Long.parseLong(hexString, 16) >> 26;
        return String.format("%02x", decimalOpcode);
    }

    //Use known opcode to return Instruction Type. Hex string used for syscall
    private String convertInstructionType(String hexString){
        String newInstructionType = "Error";
        //Syscall is always the same, but the opcode is the same as R format
        if(hexString.equals("0000000c")){
            newInstructionType = SYSCALL;
        }
        //000000 - R Format instructions
        else if(opcode.equals("00")){
            newInstructionType = RTYPE;
        }
        //Only one j type instruction, and opcode will be 02
        else if(opcode.equals("02")){
            newInstructionType = JTYPE;
        }
        else{
            newInstructionType = ITYPE;
        }
        return newInstructionType;
    }

    //Use known opcode, funct and instructionType to convert mnemonic
    private String convertMnemonic(){
        String newMnemonic = "Error";
        //R type and syscall instructions need to use funct
        if(instructionType.equals(RTYPE) || instructionType.equals(SYSCALL)){
            switch(funct){
                case "20": //add - 100000
                    newMnemonic = "add";
                    break;
                case "24": //and - 100100
                    newMnemonic = "and";
                    break;
                case "25": //or - 100101
                    newMnemonic = "or";
                    break;
                case "2a": //slt - 101010
                    newMnemonic = "slt";
                    break;
                case "22"://sub - 100010
                    newMnemonic = "sub";
                    break;
                case "0c": //syscall - 001100
                    newMnemonic = "syscall";
                    break;
            }
        }
        //everything else uses opcode
        else{
            switch(opcode){
                case "09": //addiu - 001001
                    newMnemonic = "addiu";
                    break;
                case "0c": //andi - 001100
                    newMnemonic = "andi";
                    break;
                case "04": //beq - 000100
                    newMnemonic = "beq";
                    break;
                case "05": //bne - 000101
                    newMnemonic = "bne";
                    break;
                case "02": //j - 000010
                    newMnemonic = "j";
                    break;
                case "0f": //lui - 001111
                    newMnemonic = "lui";
                    break;
                case "23": //lw - 100011
                    newMnemonic = "lw";
                    break;
                case "0d": //ori - 001101
                    newMnemonic = "ori";
                    break;
                case "2b": //sw - 101011
                    newMnemonic = "sw";
                    break;
            }
        }
        return newMnemonic;
    }

    //Use bits 7-11 to get rs
    private String convertRs(String hexString){
        long decimalRs = Long.parseLong(hexString, 16) >> 21;
        //Need to keep last 5 bits
        decimalRs = decimalRs & 31;
        return String.format("%02x", decimalRs);
    }

    //Use bits 12-16 to get rt
    private String convertRt(String hexString){
        long decimalRt = Long.parseLong(hexString, 16) >> 16;
        //Keep last 5 bits
        decimalRt = decimalRt & 31;
        return String.format("%02x", decimalRt);
    }

    //Use bits 17-21 to get rd
    private String convertRd(String hexString){
        long decimalRd = Long.parseLong(hexString, 16) >> 11;
        decimalRd = decimalRd & 31;
        return String.format("%02x", decimalRd);
    }

    //I know shmt can be something, but it's always 00000 in our usage.
    private String convertShmt(){
        return "00";
    }

    //Bits 27-32 are used to find Funct.
    private String convertFunct(String hexString){
        //Need last six bits, so & 111111
        long decimalFunct = Long.parseLong(hexString, 16) & 63;
        return String.format("%02x", decimalFunct);
    }

    //bits 17-32 are used to determine the immediate
    private String convertImmediate(String hexString){
        return hexString.substring(4);
    }

    //index from j is bits 7-32
    private String convertIndex(String hexString){
        long decimalIndex = Long.parseLong(hexString, 16) & 67108863;
        return String.format("%07x", decimalIndex);
    }
}
