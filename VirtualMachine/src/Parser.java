import java.io.*;
import javax.swing.*;

public class Parser {

	File vmFile;
	BufferedReader br;
	String currentCommand = "";
	int numberOfCommands = 0;
	int index = 0;

	public Parser(File vmFile) {
		this.vmFile = vmFile;
		countNumberOfCommands();
		initBufferedReader();
	}
	
	private void initBufferedReader() {
		try {
			br = new BufferedReader(new FileReader(vmFile));
			currentCommand = br.readLine( );
		}
		catch (IOException ex) {
			ex.printStackTrace( );
		}
	}
	
	private void countNumberOfCommands() {
		try {
			LineNumberReader lr = new LineNumberReader(new FileReader(vmFile));
			while(lr.readLine( ) != null) 
				numberOfCommands++;
			lr.close( );
		}
		catch (IOException ex) {
			ex.printStackTrace( );
		}
		//JOptionPane.showMessageDialog(null,"There are " + Integer.toString(numberOfCommands) + " commands");
	}
	
	public Boolean hasMoreCommands() {
		if (index < numberOfCommands)
			return true;
		else
			return false;
	}
	
	public void advance() {
		try {
			currentCommand = br.readLine( );
				//currentCommand.trim(); 
		}
		catch (IOException ex) {
			ex.printStackTrace( );
		}
		index++;				
	}
	
	public CommandType commandType() {
		if (currentCommand.startsWith("//"))
			return CommandType.C_IGNORE;
		else if (currentCommand.contains("add") || currentCommand.contains("sub") || currentCommand.contains("neg") ||
			currentCommand.contains("eq")  || currentCommand.contains("gt")  || currentCommand.contains("lt")  ||
			currentCommand.contains("and") || currentCommand.contains("or")  || currentCommand.contains("not"))
			return CommandType.C_ARITHMETIC;
		else if (currentCommand.contains("push") )
			return CommandType.C_PUSH;
		else if (currentCommand.contains("pop") )
			return CommandType.C_POP;
		else if (currentCommand.contains("label") )
			return CommandType.C_LABEL;
		else if (currentCommand.contains("if-goto") ) 
			return CommandType.C_IF;
		else if (currentCommand.contains("goto") )
			return CommandType.C_GOTO;
		else if (currentCommand.contains("function") )
			return CommandType.C_FUNCTION;
		else if (currentCommand.contains("return") )
			return CommandType.C_RETURN;
		else if (currentCommand.contains("call") )
			return CommandType.C_CALL;
		else
			return CommandType.C_IGNORE;
	}
	
	public String arg1() {
		if (currentCommand.contains("add") ) {
			return "add";
		} else if (currentCommand.contains("sub") ) {
			return "sub";
		} else if (currentCommand.contains("neg") ) {
			return "neg";
		} else if (currentCommand.contains("eq") ) {
			return "eq";
		} else if (currentCommand.contains("gt") ) {
			return "gt";
		} else if (currentCommand.contains("lt") ) {
			return "lt";
  		} else if (currentCommand.contains("and") ) {
			return "and";
		} else if (currentCommand.contains("or") ) {
			return "or";
		} else if (currentCommand.contains("not") ) {
			return "not";
		} else if (currentCommand.contains("push") ) {
			return currentCommand.split("\\s")[1];
		} else if (currentCommand.contains("pop") ) {
			return currentCommand.split("\\s")[1];
		} else if (currentCommand.contains("label") ) {
			return currentCommand.split("\\s")[1];
		} else if (currentCommand.contains("if-goto") ) {
			return currentCommand.split("\\s")[1];
		} else if (currentCommand.contains("goto") ) {
			return currentCommand.split("\\s")[1];
	 	} else if (currentCommand.contains("function") ) {
	 		return currentCommand.split("\\s")[1];
		} else if (currentCommand.contains("call") ) {
			return currentCommand.split("\\s")[1];
		} else 
			return "";
	}
	
	public int arg2() {
		String[ ] commandTokens = currentCommand.split("\\s");
		return Integer.parseInt(commandTokens[2]);
	}
}
