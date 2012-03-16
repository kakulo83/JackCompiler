import java.io.*;

public class VMWriter {

	public VMWriter(PrintWriter output) {
		this.output = output;	
	}

	private PrintWriter output;	

	public void writePush(Segment segment, int Index) {
		output.println("push " + segment.toString() + " " + Index);		
	}

	public void writePop(Segment segment, int Index) {
		output.println("pop " + segment.toString() + " "+ Index);	
	}

	public void WriteArithmetic(ArithmeticOperator operator) {
		switch (operator) {
			case ADD:
				output.println("add");		
				break;
			case SUB:
				output.println("sub");	
				break;
			case NEG: 
				output.println("neg");	
				break;
			case EQ:
				output.println("eq");	
				break;
			case GT:
				output.println("gt");
				break;
			case LT:
				output.println("lt");
				break;
			case AND:
				output.println("and");
				break;
			case OR:
				output.println("or");
				break;
			case NOT:
				output.println("not");
				break;
			case MULT:
				output.println("call Math.multiply 2");	
				break;
			case DIVIDE:
				output.println("call Math.divide 2");	
				break;
			default:
				break;
		}
	}

	public void WriteLabel(String label) {
		output.println("label " + label);	
	}

	public void WriteGoto(String label) {
		output.println("goto " + label);	
	}

	public void WriteIf(String label) {
		output.println("if-goto " + label);	
	}

	public void writeCall(String name, int nArgs) {
		output.println("call " + name + " " + nArgs);	
	}

	public void writeFunction(String name, int nLocals) {
		output.println("function " + name + " " + nLocals);	
	}

	public void writeReturn() {
		output.println("return");	
	}

	public void close() {
		output.flush();
		output.close();	
	}	
}
