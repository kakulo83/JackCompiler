import java.util.*;

public class SymbolTable {

	public SymbolTable( ) {
		initializeSymbolTable( );
	}
	
	private Hashtable<String, String> hashTable;
	
	private void initializeSymbolTable( ) {
		hashTable = new Hashtable<String, String>( );
		
		hashTable.put("SP",     "0000000000000000");
		hashTable.put("LCL",    "0000000000000001");
		hashTable.put("ARG",    "0000000000000010");
		hashTable.put("THIS",   "0000000000000011");
		hashTable.put("THAT",   "0000000000000100");
		hashTable.put("R0",     "0000000000000000");
		hashTable.put("R1",     "0000000000000001");
		hashTable.put("R2",     "0000000000000010");
		hashTable.put("R3",     "0000000000000011");
		hashTable.put("R4",     "0000000000000100");
		hashTable.put("R5",     "0000000000000101");
		hashTable.put("R6",     "0000000000000110");
	    hashTable.put("R7",     "0000000000000111");
		hashTable.put("R8",     "0000000000001000");
		hashTable.put("R9",     "0000000000001001");
		hashTable.put("R10",    "0000000000001010");
		hashTable.put("R11",    "0000000000001011");
		hashTable.put("R12",    "0000000000001100");
		hashTable.put("R13",    "0000000000001101");
		hashTable.put("R14",    "0000000000001110");
		hashTable.put("R15",    "0000000000001111");
		hashTable.put("SCREEN", "0100000000000000");
		hashTable.put("KBD",    "0110000000000000");
	}
	
	public void addEntry(String symbol, int address) {
		String binaryAddress = convertToBinary(address);
		hashTable.put(symbol, binaryAddress);
		
		System.out.println("Adding symbol " + symbol + ", with address " + binaryAddress);
	}
	
	public Boolean contains(String symbol) {
		if (hashTable.containsKey(symbol) )
			return true;
		else
			return false;
	}
	
	public String GetAddress(String symbol) {
		
		// If the symbol is actually numerical 
		// convert it to binary and return the result
		
		// If the symbol is a symbol, retrieve it from
		// the hashtable and return that.  
		
		if (checkIfNumber(symbol) ) {
			int numericAddress = Integer.parseInt(symbol);
			return convertToBinary(numericAddress);
		}
		else
			return hashTable.get(symbol);
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
	
	private String convertToBinary(int address) {
		
		String binaryAddress = "";
		
		String v16 = "0"; String v15 = "0"; String v14 = "0"; 
		String v13 = "0"; String v12 = "0"; String v11 = "0"; 
		String v10 = "0"; String v9  = "0"; String v8  = "0"; 
		String v7  = "0"; String v6  = "0"; String v5  = "0"; 
		String v4  = "0"; String v3  = "0"; String v2  = "0"; String v1 = "0";
				
		if (16384 <= address) {
			address -= 16384;
			v15 = "1";
		}
		if (8192 <= address) {
			address -= 8192;
			v14 = "1";
		}
		if (4096 <= address) {
			address -= 4096;
			v13 = "1";
		}
		if (2048 <= address) {
			address -= 2048;
			v12 = "1";
		}
		if (1024 <= address) {
			address -= 1024;
			v11 = "1";
		}
		if (512 <= address) {
			address -= 512;
			v10 = "1";
		}
		if (256 <= address) {
			address -= 256;
			v9 = "1";
		}
		if (128 <= address) {
			address -= 128;
			v8 = "1";
		}
		if (64 <= address) {
			address -= 64;
			v7 = "1";
		}
		if (32 <= address) {
			address -= 32;
			v6 = "1";
		}
		if (16 <= address) {
			address -= 16;
			v5 = "1";
		}
		if (8 <= address) {
			address -= 8;
			v4 = "1";
		}
		if (4 <= address) {
			address -= 4;
			v3 = "1";
		}
		if (2 <= address) {
			address -= 2;
			v2 = "1";
		}
		if (1 == address) {
			address -= 1;
			v1 = "1";
		}
		
		binaryAddress = v16+v15+v14+v13+v12+v11+v10+v9+v8+v7+v6+v5+v4+v3+v2+v1;
		
		return binaryAddress;	
	}
}
