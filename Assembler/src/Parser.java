//
//  Parser.java
//  
//
//  Created by Robert Carter on 5/7/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

import java.io.*;

public class Parser {

	public Parser(File asmFile) {
		
		this.asmFile = asmFile;
		
		try {
			bf = new BufferedReader(new FileReader(asmFile));
			currentCommand = bf.readLine( );
			cleanUpCommands( );
		}
		catch (IOException ex) {
			ex.printStackTrace( );
		}
		
		try {
			LineNumberReader lr = new LineNumberReader(new FileReader(asmFile));
			while(lr.readLine( ) != null) 
				numberOfCommands++;
			lr.close( );
		}
		catch (IOException ex) {
			ex.printStackTrace( );
		}
			
	}
	
	File asmFile;
	BufferedReader bf;
	String currentCommand = "";
	int numberOfCommands = 0;
	int currentCommandIndex = 0;
	
	public Boolean hasMoreCommands( ) {
		if (currentCommandIndex < numberOfCommands)
			return true;
		else
			return false;
	}
	
	public void advance( ) {
		currentCommandIndex++;
		
		try {
			currentCommand = bf.readLine( );
			cleanUpCommands( );
		}
		catch (IOException ex) {
			ex.printStackTrace( );
		}
	}
	
	public CommandType commandType( ) {
		
		if (currentCommand == null) 
			return CommandType.IGNORE;
		else if (currentCommand.length( ) == 0)
			return CommandType.IGNORE;
		else if (currentCommand.charAt(0) == '/')
			return CommandType.IGNORE;
		else if (currentCommand.contains("@") )
			return CommandType.A_COMMAND;
		else if (currentCommand.contains("=") || currentCommand.contains(";") )
			return CommandType.C_COMMAND;
		else
			return CommandType.L_COMMAND;
	}
	
	public String symbol( ) {
		int indexOfAT = currentCommand.indexOf("@");
		String addressMnemonic = currentCommand.substring(indexOfAT+1);
		return addressMnemonic;
	}
	
	public String dest( ) {
		if (currentCommand.contains(";") )
			return "null";
		int mnemonicEndIndex = currentCommand.indexOf("=");
		String destMnemonic = currentCommand.substring(0,mnemonicEndIndex);
		return destMnemonic;
	}
	
	public String comp( ) {
		
		// Jump command, mnemonic is before ';' 
		if (currentCommand.contains(";") ) {
			int mnemonicEndIndex = currentCommand.indexOf(";");
			String compMnemonic = currentCommand.substring(0,mnemonicEndIndex);
			return compMnemonic;
		}
		// Computation command, mnemonic is after '='
		else {
			int mnemonicStartIndex = currentCommand.indexOf("=");
			String compMnemonic = currentCommand.substring(mnemonicStartIndex+1);
			return compMnemonic;
		}
		
	}
	
	public String jump( ) {
		
		if (currentCommand.contains(";") ) {
			int mnemonicStartIndex = currentCommand.indexOf(";");
			String jumpMnemonic = currentCommand.substring(mnemonicStartIndex+1);
			return jumpMnemonic;
		}
		else 
			return "null";
	}
	
	private void cleanUpCommands( ) {
		if (currentCommand == null || currentCommand.length( ) == 0)
			return;
		currentCommand = currentCommand.trim( );
		
		if (currentCommand.contains("//") ) {
			int commentIndex = currentCommand.indexOf("//");
			currentCommand = currentCommand.substring(0,commentIndex);
			currentCommand = currentCommand.trim( );
		}
	}
	
}
