import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
public class Main {
	public static int []memory;
	public static int [] regs;
	public static int PC;
	public final static int R0 = 0;
	public static HashMap<String, Integer> opcodes;
	public static int CLOCK_CYCLE ;
	public static int total_number_of_instructions;
public static void main(String[] args) throws IOException {
	// Load and Store Format. 
	init_Opcodes();
	memory = new int[2048];
	CLOCK_CYCLE = 0;
	regs = new int [33];
	BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\OWNER\\eclipse-workspace\\CA_Project\\src\\Assembly.txt"));
	String current ;
	ArrayList<String> temp = new ArrayList<String>();
	while ((current=bufferedReader.readLine())!=null) {
		temp.add(current);
	}
	total_number_of_instructions = temp.size();
	parse_instructions(temp);
	run();
	System.out.println(PC);
	bufferedReader.close();
}
public static int return_type(int x) {
	if (x==0||x==1||x==9||x==8)
		return 0; // R-type
	if (x==7)
		return 1; //J-type
	
	return 2; //I-Type 
}
public static void parse_instructions(ArrayList<String> tempArray) {
	int i= 0;
	for (String a : tempArray) {
		a = a.replace("("," ");
		if (i>=1024) {
			System.err.println("There is no more space for instructions you should\n"
					+ "optimize your code the maximum number of instructions allowed\n"
					+ "is 1024");
		}
		int instruction_address = 0;
		// in case there are no labels in the line .
		if (!a.contains(":")) {
			String [] instruction = a.split(" ");
			int index = opcodes.getOrDefault(instruction[0].toLowerCase(), -1);
			if (index ==-1) {
				System.err.println("Unrecodnized instrution !!!");
				return;
			}
			int opcode = index;
			int type = return_type(opcode);
			 instruction_address= opcode<<28;
			if (type==1) {
				try {
					int jump_address = Integer.parseInt(instruction[1]);
					instruction_address+=jump_address;
				}catch (Exception e) {
					System.err.println("You should pass an immediate value");
					return;
				}
			}else {
				int destReg;
				try {
					destReg =  Integer.parseInt(instruction[1].replaceAll("[$Rr() ]", ""));
				}catch (Exception e) {
					System.err.println("This Register "+instruction[1]+" doesn't exist");
					return;
				}
				destReg<<=23;
				instruction_address+= (destReg);
				int srcReg1;
				try {
					srcReg1 =  Integer.parseInt(instruction[2].replaceAll("[$Rr() ]", ""));
				}catch (Exception e) {
					System.err.println("This Register "+instruction[2]+" doesn't exist");
					return;
				}
				srcReg1<<=18;
				instruction_address+= (srcReg1);
				if (type == 0) {
				if (opcode==9 || opcode==8) {
					int shamt ;
					try {
						shamt = Integer.parseInt(instruction[3].replaceAll("[$Rr() ]", ""));
						instruction_address+=shamt;
					}catch (Exception e) {
						System.err.println("This Register "+instruction[3]+" doesn't exist");
						return;
					}
					}else {
						int srcReg2;
						try {
							srcReg2 = Integer.parseInt(instruction[3].replaceAll("[$Rr() ]", ""));
						}catch (Exception e) {
							System.err.println("This Register "+instruction[3]+" doesn't exist");
							return;
						}
						srcReg2<<=13;
						instruction_address+= (srcReg2);
					}
			}else {
				try {
				int imm = Integer.parseInt(instruction[3].replaceAll("[$Rr() ]", ""));
				instruction_address+=imm;
				}catch (Exception e) {
					System.err.println("You should pass an immediate value");
					return;
				}
			}
		}
		}
		System.out.println(Integer.toBinaryString(instruction_address));
		memory[i] = instruction_address;
		i++;
	}
}
public static void init_Opcodes() {
	opcodes = new HashMap<String, Integer>();
	opcodes.put("add",0);
	opcodes.put("sub",1);
	opcodes.put("muli",2);
	opcodes.put("addi",3);
	opcodes.put("bne",4);
	opcodes.put("andi",5);
	opcodes.put("ori",6);
	opcodes.put("j",7);
	opcodes.put("sll",8);
	opcodes.put("srl",9);
	opcodes.put("lw",10);
	opcodes.put("sw",11);
}

public static void run() {
	fetch(0);
}
private static void fetch(int PC_tmp) {
	int tmp_pc = PC;
	if (tmp_pc>= total_number_of_instructions)
		return;
	decode(memory[PC_tmp]);
}
private static  void decode(int inst) {
	int tmp_pc = PC;
	PC+=1;
	int opcode,rd,rs,rt,address,shamt,imm;
	opcode = inst&0b11110000000000000000000000000000;
	opcode>>=28;
	opcode += (opcode<0)?16:0;
	rd = inst&0b00001111100000000000000000000000;
	rd>>=23;
	rs = inst&0b00000000011111000000000000000000;
	rs>>=18;
	rt = inst&0b00000000000000111110000000000000;
	rt>>=13;
	shamt = inst&0b1111111111111;
	address = inst&0b1111111111111111111111111111;
	imm = inst&0b111111111111111111;
	execute(opcode, rd, rs, rt, imm, address,shamt);
}
private static  void execute(int opcode,int rd,int rs,int rt,int imm,int address,int shamt) {
	int result;
	int tmp_pc = PC;
	switch(opcode) {
	case(0):result = regs[rs] + regs[rt];writeback(rd,result);fetch(tmp_pc);break;
	case(1):result = regs[rs] - regs[rt];writeback(rd,result);fetch(tmp_pc);break;
	case(2):result = regs[rs] * imm;writeback(rd,result);fetch(tmp_pc);break;
	case(3):result = regs[rs] + imm;writeback(rd,result);fetch(tmp_pc);break;
	case(4):result = (regs[rd]-regs[rs]!=0)?(PC+=imm):(tmp_pc);fetch(result);break;
	case(5):result = regs[rs] & imm;writeback(rd,result);fetch(tmp_pc);break;
	case(6):result = regs[rs] | imm;writeback(rd,result);fetch(tmp_pc);break;
	case(7):PC = address;fetch(PC);break;
	case(8):result = regs[rs] << shamt;writeback(rd,result);fetch(tmp_pc);break;
	case(9):result = regs[rs] >> shamt;writeback(rd,result);fetch(tmp_pc);break;
	case(10):result = regs[rs] + imm;memory(rd,address,true);fetch(tmp_pc);break;
	case(11):memory(rd,regs[rs] + imm,false);fetch(tmp_pc);break;
	}
}
private static  void memory(int rd,int address,boolean load) {
	int tmp_pc = PC;
	if(load) {
		writeback(rd, memory[address]); //rd is not the same for both we have to change it in regs[rd]
	}else {
		memory[rd] = regs[rd];
		fetch(tmp_pc);
	}
}
private static  void writeback(int reg,int result) {
	int tmp_pc = PC;
	regs[reg] = result;
}
}