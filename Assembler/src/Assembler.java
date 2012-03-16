import java.io.*;
import javax.swing.*;
import java.util.Hashtable;
import javax.swing.filechooser.*;

public class Assembler {

	public Assembler( ) {
		frame = new JFrame( );
		frame.setVisible(false);
		openAssemblyFile( );
		pass1( );
		pass2( );
	}
	
	JFrame frame;
	File asmFile;
	File hackFile;
	Code code;
	SymbolTable symbolTable;
		
	private void initializeObjects( ) {
		code = new Code( );
		symbolTable = new SymbolTable( );
	}
	
	private void openAssemblyFile( ) {
		JFileChooser fc = new JFileChooser( );
		fc.showOpenDialog(frame);
		asmFile = fc.getSelectedFile( );
		
		if (asmFile != null) 
			initializeObjects( );
		else
			System.exit(0);
	}
	
	private void pass1( ) {
		Parser parser1 = new Parser(asmFile);
		CommandType commandType;
		int ROM_address = 0;
		
		while (parser1.hasMoreCommands( ) ) {
			commandType = parser1.commandType( );
					
			if (commandType == CommandType.L_COMMAND) {
				// Get label string without start/end parenthesis
				String labelP = parser1.symbol( );
				int endParenthesisIndex = labelP.indexOf(")");
				String label = labelP.substring(1,endParenthesisIndex);
				symbolTable.addEntry(label, ROM_address);
											   				
			}
			parser1.advance( );
			
			commandType = parser1.commandType( );
			if ( (commandType == CommandType.A_COMMAND)||(commandType == CommandType.C_COMMAND) )
				ROM_address++;
			
		} // End while 
		
	}
	  
	private void pass2( ) {
		Parser parser2 = new Parser(asmFile);
		CommandType commandType;
		int RAM_address = 16;
		String machineCode = "";
		 
		while (parser2.hasMoreCommands( ) ) {
			commandType = parser2.commandType( );
						
			if (commandType == CommandType.A_COMMAND) {
				if (symbolTable.contains(parser2.symbol() ) ) {
					machineCode += symbolTable.GetAddress(parser2.symbol() );
				}
				else {
					// If the symbol is numerical
					if (checkIfNumber(parser2.symbol( ) ) )
						machineCode += symbolTable.GetAddress(parser2.symbol( ) );
					else {
						symbolTable.addEntry(parser2.symbol(),RAM_address);
						machineCode += symbolTable.GetAddress(parser2.symbol() );
						RAM_address++;
					}
				}
			}
			if (commandType == CommandType.C_COMMAND) {
				machineCode += code.comp(parser2.comp( ) );
				machineCode += code.dest(parser2.dest( ) );
				machineCode += code.jump(parser2.jump( ) );
			}
		    
			parser2.advance( );
			// ADD CONDITION FOR ADDING NEW LINE CHARACTER
			Boolean addnewLineCharacter = true;
			if (parser2.commandType( ) == CommandType.IGNORE)
				addnewLineCharacter = false;
			if (parser2.commandType( ) == CommandType.L_COMMAND)
				addnewLineCharacter = false;
			
			if (addnewLineCharacter) 
				machineCode += "\n";
				
		} // End while
		
		writeToHackFile(machineCode);
	}
	
	private void writeToHackFile(String machineCode) {
		//  Change to allow user determine save location
		hackFile = new File("/");
		
		try {
			hackFile.createNewFile( );
		}
		catch (IOException ex) {
			ex.printStackTrace( );
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(hackFile) );
			writer.write(machineCode);
			writer.close( );
		}
		catch (IOException ex) {
			ex.printStackTrace( );
		}
		finally {
			System.exit(0);
		}
	}
	
	private Boolean checkIfNumber(String in) {
		try {
			Integer.parseInt(in);
		}
		catch (NumberFormatException ex) {
			return false;
		}
		return true;
	}
	
	public static void main(String[ ] args) {
		Assembler assembler = new Assembler( );
	}
	
}
