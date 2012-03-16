import java.io.*;
import javax.swing.*;

public class CodeWriter {

	private File asmFile;
	private BufferedWriter bw;
	String nl = " \n";
	private String fileName = "";
	private String functionName = "";
	private int label_ID_Number = 0;
	private enum Segments { ARGUMENT, LOCAL, STATIC, CONSTANT, THIS, THAT, POINTER, TEMP };
	private enum Arithmetic { ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT };	
	private enum StackOperation { PUSH, POP };
		
	public CodeWriter(File outputFile) {
		this.asmFile = outputFile;
		initBufferedWriter();
		initializeBootstrap();
	}
	
	private void initBufferedWriter() {
		try {
			bw = new BufferedWriter(new FileWriter(asmFile));
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void initializeBootstrap() {
		String assembly = "@256" + nl;
 		assembly += "D=A" + nl;
		assembly += "@SP" + nl;
		assembly += "M=D" + nl;	
		// init Return
		assembly += "@return-Sys.init" + nl;		
		assembly += "D=A" + nl;
		assembly += "@SP" + nl;
		assembly += "A=M" + nl;
		assembly += "M=D" + nl;
		assembly += "@SP" + nl;
		assembly += "M=M+1" + nl;
		// init LCL 
		assembly += "@LCL" + nl;
		assembly += "D=0" + nl;
		assembly += "@SP" + nl;
		assembly += "A=M" + nl;
		assembly += "M=D" + nl;
		assembly += "@SP" + nl;
		assembly += "M=M+1" + nl;
		// init ARG 
		assembly += "@ARG" + nl;
		assembly += "D=0" + nl;
		assembly += "@SP" + nl;
		assembly += "A=M" + nl;
		assembly += "M=D" + nl;
		assembly += "@SP" + nl;
		assembly += "M=M+1" + nl;
		// init THIS 
		assembly += "@THIS" + nl;
		assembly += "D=0" + nl;
		assembly += "@SP" + nl;
		assembly += "A=M" + nl;
		assembly += "M=D" + nl;
		assembly += "@SP" + nl;
		assembly += "M=M+1" + nl;
		// init THAT 
		assembly += "@THAT" + nl;
		assembly += "D=0" + nl;
		assembly += "@SP" + nl;
		assembly += "A=M" + nl;
		assembly += "M=D" + nl;
		assembly += "@SP" + nl;
		assembly += "M=M+1" + nl;
		
		// Jump to System.init
		assembly += "@Sys.init" + nl;
		assembly += "0;JMP" + nl;
		writeAssemblyCommands(assembly);
	}
	
	public void setFileName(String fileName) {
		int periodIndex = fileName.lastIndexOf(".");
		this.fileName = fileName.substring(0,periodIndex);
	}
	
	public void writeArithmetic(String command) {
		
		//  @VALUE  places the address of VALUE into the A register
		//  M refers to the value of the memory block whose address is currently in A register
		//  SP is a pointer to the address the top most available stack memory space
		//  D register contains only data
		
		//  label_ID_Number is used to differentiate the jump labels.  Since all lables within a function contain the function's name, an
		//  additional marker (label_ID_Number) is used to make each one unique.  Savy?
		
		label_ID_Number++;	 
		String assembly = "";
		String incrementSP = "@SP" + nl + "M=M+1" + nl;
		String decrementSP = "@SP" + nl + "M=M-1" + nl;
		String getArg1 = decrementSP + "A=M" + nl;
		String getArg1andArg2 = decrementSP + "A=M" + nl + "D=M" + nl + decrementSP + "A=M" + nl;				
		Arithmetic arithmeticCommand = Arithmetic.valueOf(command.toUpperCase() );
				
		switch (arithmeticCommand) {
			case ADD:   
				assembly += getArg1andArg2;
				assembly += "D=D+M" + nl;
				assembly += "@SP" + nl;
				assembly += "A=M" + nl;	
				assembly += "M=D" + nl;
				assembly += incrementSP;
				break;
			case SUB:   
				assembly += getArg1andArg2;
				assembly += "D=M-D" + nl;
				assembly += "@SP" + nl;
				assembly += "A=M" + nl;
				assembly += "M=D" + nl;
				assembly += incrementSP;
				break;
			case NEG:
				assembly += getArg1;
				assembly += "D=-M" + nl;
				assembly += "M=D" + nl;
				assembly += incrementSP;
			 	break;
			case EQ:
				assembly += getArg1andArg2;
				assembly += "D=D-M" + nl;
				assembly += "@EQUALS" + Integer.toString(label_ID_Number) + nl;
			    assembly += "D;JEQ" + nl;
				assembly += "@SP" + nl;
				assembly += "A=M" + nl;
				assembly += "M=0" + nl;
				assembly += incrementSP;
				assembly += "@END" + Integer.toString(label_ID_Number) + nl;
				assembly += "0;JMP" + nl;
				assembly += "(EQUALS" + Integer.toString(label_ID_Number) + ")" + nl;
				assembly += "@SP" + nl;
				assembly += "A=M" + nl;
				assembly += "M=-1" + nl;
				assembly += incrementSP;
				assembly += "(END" + Integer.toString(label_ID_Number) + ")" + nl;
				break;
			case GT:
				assembly += getArg1andArg2;
				assembly += "D=M-D" + nl;	// D = x - y
				assembly += "@GREATER" + Integer.toString(label_ID_Number) + nl;
				assembly += "D;JGT" + nl;	// Jump if x is greater than y
				assembly += "@SP" + nl;	// If x is NOT greater than y
				assembly += "A=M" + nl;
				assembly += "M=0" + nl;
				assembly += "@SP" + nl;
				assembly += "M=M+1" + nl;
				assembly += "@END" + Integer.toString(label_ID_Number) + nl;
				assembly += "0;JMP" + nl;
				assembly += "(GREATER" + Integer.toString(label_ID_Number) + ")" + nl;
				assembly += "@SP" + nl;	// If x is greater than y
				assembly += "A=M" + nl;
				assembly += "M=-1" + nl;
				assembly += "@SP" + nl;
				assembly += "M=M+1" + nl;
				assembly += "(END" + Integer.toString(label_ID_Number) + ")" + nl;
				break;
			case LT:
				assembly += getArg1andArg2;
				assembly += "D=D-M" + nl;	// D = y - x
				assembly += "@GREATER" + Integer.toString(label_ID_Number) + nl;
				assembly += "D;JGT" + nl;	// Jump if x is less than y
				assembly += "@SP" + nl;	// If x is NOT less than y
				assembly += "A=M" + nl;
				assembly += "M=0" + nl; // FALSE
				assembly += "@SP" + nl;
				assembly += "M=M+1" + nl;
				assembly += "@END" + Integer.toString(label_ID_Number) +nl;
				assembly += "0;JMP" + nl;
				assembly += "(GREATER" + Integer.toString(label_ID_Number) + ")" + nl;
				assembly += "@SP" + nl;	// If x is less than y
				assembly += "A=M" + nl;
				assembly += "M=-1" + nl; // TRUE
				assembly += "@SP" + nl;
				assembly += "M=M+1" + nl;
				assembly += "(END" + Integer.toString(label_ID_Number) + ")" + nl;
				break;
			case AND:
				assembly += getArg1andArg2;
				assembly += "D=D&M" + nl;  // D = x & y
				assembly += "@SP" + nl;
				assembly += "A=M" + nl;
				assembly += "M=D" + nl;
				assembly += "@SP" + nl;
				assembly += "M=M+1" + nl;
				break;
			case OR:
				assembly += getArg1andArg2;
				assembly += "D=D|M" + nl;
				assembly += "@SP" + nl;
				assembly += "A=M" + nl;
				assembly += "M=D" + nl;
				assembly += "@SP" + nl;
				assembly += "M=M+1" + nl;		
				break;
			case NOT: 
				assembly += "@SP" + nl;
				assembly += "M=M-1" + nl;
				assembly += "A=M" + nl;
				assembly += "M=!M" + nl;
				assembly += "@SP" + nl;
				assembly += "M=M+1" + nl;
				break;
			default: 
				break;
		}
		writeAssemblyCommands(assembly);
	}
	
	public void writePushPop(CommandType command, String segment, int index) {

		String assembly = "";
		StackOperation stackCommand = StackOperation.valueOf(command.toString().toUpperCase() );
		Segments stackSegment = Segments.valueOf(segment.toUpperCase());
		
		if (stackCommand == StackOperation.PUSH) {
	
			String pushToStack = "D=M" + nl + "@SP" + nl + "A=M" + nl + "M=D" + nl + "@SP" + nl + "M=M+1" + nl;
									
			switch (stackSegment) {
				case ARGUMENT:
				  	assembly += "@ARG" + nl;
					assembly += "A=M" + nl;
					for (int i=0; i<index; i++) { assembly += "A=A+1" + nl; }
					assembly += pushToStack;
					break;
				case LOCAL:
					assembly += "@LCL" + nl;
					assembly += "A=M" + nl;
					for (int i=0; i<index; i++) { assembly += "A=A+1" + nl; }
					assembly += pushToStack;
					break;
				case STATIC:  
					assembly += "@" + this.fileName + "." + Integer.toString(index) + nl;
					assembly += pushToStack;					
					break;
				case CONSTANT:
				    assembly += "@" + Integer.toString(index) + nl;
					assembly += "D=A" + nl;
					assembly += "@SP" + nl;
					assembly += "A=M" + nl;
					assembly += "M=D" + nl;
					assembly += "@SP" + nl;
					assembly += "M=M+1" + nl;
					break;
				case THIS:   
					assembly += "@THIS" + nl;
					assembly += "A=M" + nl;					
					for (int i=0; i<index; i++) { assembly += "A=A+1" + nl; }					
					assembly += pushToStack;
					break;								
				case THAT:   
					assembly += "@THAT" + nl;
					assembly += "A=M" + nl;
					for (int i=0; i<index; i++) { assembly += "A=A+1" + nl; }					
					assembly += pushToStack;
					break;
				case POINTER:   
					assembly += "@3" + nl;					
					for (int i=0; i<index; i++) { assembly += "A=A+1" + nl; }
					assembly += pushToStack;
					break;
				case TEMP:   
					assembly += "@5" + nl;
					for (int i=0; i<index; i++) { assembly += "A=A+1" + nl; }
					assembly += pushToStack;
					break;
				default: break;
			}
		}
		else {
			
			String popFromStack = "@SP" + nl + "M=M-1" + nl + "A=M" + nl + "D=M" + nl; 
			
			switch (stackSegment) {
				case ARGUMENT: 
				 	assembly += popFromStack;
					assembly += "@ARG" + nl;
					assembly += "A=M" + nl;
					for (int i=0; i<index; i++) { assembly += "A=A+1" + nl; }
					assembly += "M=D" + nl;
					break;
				case LOCAL:
					assembly += popFromStack;
					assembly += "@LCL" + nl;
					assembly += "A=M" + nl;
					for (int i=0; i<index; i++) { assembly += "A=A+1" + nl; }
					assembly += "M=D" + nl;
					break;
				case STATIC:  
					assembly += popFromStack;
					assembly += "@" + this.fileName + "." + Integer.toString(index) + nl;
					assembly += "M=D" + nl;
					break;
				case THIS:   
					assembly += popFromStack;
					assembly += "@THIS" + nl;
					assembly += "A=M" + nl;
					for (int i=0; i<index; i++) { assembly += "A=A+1" + nl; }					
					assembly += "M=D" + nl;
					break;								
				case THAT:   
					assembly += popFromStack;
					assembly += "@THAT" + nl;
					assembly += "A=M" + nl;				
					for (int i=0; i<index; i++) { assembly += "A=A+1" + nl; }					
					assembly += "M=D" + nl;
					break;
				case POINTER:   
					assembly += popFromStack;
					assembly += "@3" + nl;
					for (int i=0; i<index; i++) { assembly += "A=A+1" + nl; }
					assembly += "M=D" + nl;
					break;
				case TEMP:   
					assembly += popFromStack;
					assembly += "@5" + nl;
					for (int i=0; i<index; i++) { assembly += "A=A+1" + nl; }
					assembly += "M=D" + nl;
					break;
				default: break;
			}
		}
		writeAssemblyCommands(assembly);
	}

	public void writeLabel(String label) {
		String assembly = "(" + this.functionName + "$" + label + ")" + nl;
		writeAssemblyCommands(assembly);
	}

	public void writeGoto(String label) {
		String assembly = "@" + this.functionName + "$" + label + nl;
		assembly += "0;JMP" + nl;
		writeAssemblyCommands(assembly);
	}

	public void writeIf(String label) {
		String assembly = "@SP" + nl;
		assembly += "M=M-1" + nl;
		assembly += "A=M" + nl;
		assembly += "D=M" + nl;
		assembly += "@" + this.functionName + "$" + label + nl;
		assembly += "D;JNE" + nl;
		writeAssemblyCommands(assembly);
	}

	public void writeFunction(String functionName, int numLocals) {
		this.functionName = functionName;
		String asmCommands = "(" + functionName + ")" + nl;
		for (int i=0; i<numLocals; i++) {
			asmCommands += "@0" + nl;
			asmCommands += "D=A" + nl;
			asmCommands += "@SP" + nl;
			asmCommands += "A=M" + nl;
			asmCommands += "M=D" + nl;
			asmCommands += "@SP" + nl;
			asmCommands += "M=M+1" + nl;
		}
		writeAssemblyCommands(asmCommands);
	} 
	
	public void writeCall(String calledFunction, int numArgs) {
		label_ID_Number++;
		
		String asmCommands = "";
		// Push return-address
		asmCommands += "@return-" + calledFunction + Integer.toString(label_ID_Number) + nl;
		asmCommands += "D=A" + nl;
		asmCommands += "@SP" + nl;
		asmCommands += "A=M" + nl;
		asmCommands += "M=D" + nl;
		asmCommands += "@SP" + nl;
		asmCommands += "M=M+1" + nl;
		// Push LCL
		asmCommands += "@LCL" + nl;		
		asmCommands += "D=M" + nl;
		asmCommands += "@SP" + nl;
		asmCommands += "A=M" + nl;
		asmCommands += "M=D" + nl;
		asmCommands += "@SP" + nl;
		asmCommands += "M=M+1" + nl;
		// Push ARG
		asmCommands += "@ARG" + nl;
		asmCommands += "D=M" + nl;
		asmCommands += "@SP" + nl;
		asmCommands += "A=M" + nl;
		asmCommands += "M=D" + nl;
		asmCommands += "@SP" + nl;
		asmCommands += "M=M+1" + nl;
		// Push THIS
		asmCommands += "@THIS" + nl;
		asmCommands += "D=M" + nl;
		asmCommands += "@SP" + nl;
		asmCommands += "A=M" + nl;
		asmCommands += "M=D" + nl;
		asmCommands += "@SP" + nl;
		asmCommands += "M=M+1" +  nl;
		// Push THAT
		asmCommands += "@THAT" + nl;
		asmCommands += "D=M" + nl;
		asmCommands += "@SP" + nl;
		asmCommands += "A=M" + nl;
		asmCommands += "M=D" + nl;
		asmCommands += "@SP" + nl;
		asmCommands += "M=M+1" +  nl;
		// ARG = SP-n-5
		asmCommands += "@SP" + nl; 
		asmCommands += "D=M" + nl;
		asmCommands += "D=D-1" + nl;
		asmCommands += "D=D-1" + nl;
		asmCommands += "D=D-1" + nl;
		asmCommands += "D=D-1" + nl;
		asmCommands += "D=D-1" + nl;
		for (int i=0; i<numArgs; i++)
			asmCommands += "D=D-1" + nl;	
		asmCommands += "@ARG" + nl;	
		asmCommands += "M=D" + nl;
		// LCL = SP
		asmCommands += "@SP" + nl;
		asmCommands += "D=M" + nl;
		asmCommands += "@LCL" + nl;
		asmCommands += "M=D" + nl;
		// goto f
		asmCommands += "@" + calledFunction + nl;
		asmCommands += "0;JMP" + nl;
		// return-address														
		asmCommands += "(return-" + calledFunction + Integer.toString(label_ID_Number) + ")" + nl;
		writeAssemblyCommands(asmCommands);					
	}

	public void writeReturn() {
		
		// Note:   M stands for Memory[A], where A is the address contained in the A register
		
		String asmCommands = "";
		// FRAME = LCL  
		asmCommands += "@LCL" + nl;		// Register A contains the address for RAM[1]
		asmCommands += "D=M" + nl;		// Register D contains the address for RAM[1]
		asmCommands += "@FRAME" + nl;
		asmCommands += "M=D" + nl;		// Temporary variable FRAME contains the address for RAM[1]
		// RET = *(FRAME-5)	    
		asmCommands += "@FRAME" + nl;
		asmCommands += "D=M" + nl;
		asmCommands += "D=D-1" + nl;
		asmCommands += "D=D-1" + nl;
		asmCommands += "D=D-1" + nl;
		asmCommands += "D=D-1" + nl;
		asmCommands += "D=D-1" + nl;
		asmCommands += "A=D" + nl;
		asmCommands += "D=M" + nl;
		asmCommands += "@RET" + nl;
		asmCommands += "M=D" + nl;	 	//  Assume Correct
		// *ARG = pop( )
		asmCommands += "@SP" + nl;
		asmCommands += "A=M-1" + nl;
		asmCommands += "D=M" + nl;
		asmCommands += "@ARG" + nl;
		asmCommands += "A=M" + nl;
		asmCommands += "M=D" + nl;		//	Assume Correct 
		// SP = ARG + 1
		asmCommands += "@ARG" + nl;
		asmCommands += "D=M" + nl;
		asmCommands += "D=D+1" + nl;
		asmCommands += "@SP" + nl;
		asmCommands += "M=D" + nl;		//	Assume Correct
		// THAT = *(FRAME-1)			
		asmCommands += "@FRAME" + nl;
		asmCommands += "D=M-1" + nl;
		asmCommands += "A=D" + nl;
		asmCommands += "D=M" + nl;
		asmCommands += "@THAT" + nl;
		asmCommands += "M=D" + nl;
		// THIS = *(FRAME-2)
		asmCommands += "@FRAME" + nl;
		asmCommands += "D=M-1" + nl;
		asmCommands += "D=D-1" + nl;
		asmCommands += "A=D" + nl;
		asmCommands += "D=M" + nl;
		asmCommands += "@THIS" + nl;
		asmCommands += "M=D" + nl;
		// ARG  = *(FRAME-3)
		asmCommands += "@FRAME" + nl;
		asmCommands += "D=M-1" + nl;
		asmCommands += "D=D-1" + nl;
		asmCommands += "D=D-1" + nl;
		asmCommands += "A=D" + nl;
		asmCommands += "D=M" + nl;
		asmCommands += "@ARG" + nl;
		asmCommands += "M=D" + nl;
		// LCL  = *(FRAME-4)
		asmCommands += "@FRAME" + nl;
		asmCommands += "D=M-1" + nl;
		asmCommands += "D=D-1" + nl;
		asmCommands += "D=D-1" + nl;
		asmCommands += "D=D-1" + nl;
		asmCommands += "A=D" + nl;
		asmCommands += "D=M" + nl;
		asmCommands += "@LCL" + nl;
		asmCommands += "M=D" + nl;
		// goto RET
		asmCommands += "@RET" + nl;
		asmCommands += "A=M" + nl;
		asmCommands += "0;JMP" + nl;
		
		writeAssemblyCommands(asmCommands);
	}
	
	private void writeAssemblyCommands (String assembly) {
		try {
			bw.write(assembly);
		}
		catch (IOException ex) {
			ex.printStackTrace( );
		}
	}
	
	public void close() {
		try {
			bw.close( );
		}
		catch (IOException ex) {
			ex.printStackTrace( );
		}
	}
}
