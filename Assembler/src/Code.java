import java.io.*;

public class Code {

	public Code( ) {
		
	}
	
	public String dest(String mnemonic) {
		String destBits = "";
		
	    if(mnemonic.equals("null")) 
			destBits = "000";
	    else if (mnemonic.equals("M")) 
			destBits = "001";
		else if (mnemonic.equals("D")) 
			destBits = "010";
		else if (mnemonic.equals("MD")) 
			destBits = "011";
		else if (mnemonic.equals("A")) 
			destBits = "100";
		else if (mnemonic.equals("AM")) 
			destBits = "101";
		else if (mnemonic.equals("AD")) 
			destBits = "110";
		else // (mnemonic.equals("AMD")) 
			destBits = "111";
		return destBits;		
	}
	
	public String comp(String mnemonic) {
		String compBits = "111";	// Computation instructions prefixed with 3x 1's
		
		if(mnemonic.equals("0")) 
			compBits += "0101010";
		else if (mnemonic.equals("1")) 
			compBits += "0111111";
		else if (mnemonic.equals("-1")) 
			compBits += "0111010";
		else if (mnemonic.equals("D")) 
			compBits += "0001100";
		else if (mnemonic.equals("A")) 
			compBits += "0110000";
		else if (mnemonic.equals("!D")) 
			compBits += "0001101";
		else if (mnemonic.equals("!A")) 
			compBits += "0110001";
		else if (mnemonic.equals("-D")) 
			compBits += "0001111";
		else if (mnemonic.equals("-A")) 
			compBits += "0110011";
		else if (mnemonic.equals("D+1")) 
			compBits += "0011111";
		else if (mnemonic.equals("A+1"))
			compBits += "0110111";
		else if (mnemonic.equals("D-1"))
			compBits += "0001110";
		else if (mnemonic.equals("A-1"))
			compBits += "0110010";
		else if (mnemonic.equals("D+A"))
			compBits += "0000010";
		else if (mnemonic.equals("D-A"))
			compBits += "0010011";
		else if (mnemonic.equals("A-D"))
			compBits += "0000111";
		else if (mnemonic.equals("D&A"))
			compBits += "0000000";
		else if (mnemonic.equals("D|A"))
			compBits += "0010101";
		else if (mnemonic.equals("M"))
			compBits += "1110000";
		else if (mnemonic.equals("!M"))
			compBits += "1110001";
		else if (mnemonic.equals("-M"))
			compBits += "1110011";
		else if (mnemonic.equals("M+1"))
			compBits += "1110111";
		else if (mnemonic.equals("M-1"))
			compBits += "1110010";
		else if (mnemonic.equals("D+M"))
			compBits += "1000010";
		else if (mnemonic.equals("D-M"))
			compBits += "1010011";
		else if (mnemonic.equals("M-D"))
			compBits += "1000111";
		else if (mnemonic.equals("D&M"))
			compBits += "1000000";
		else
			compBits += "1010101";
		return compBits;		
	}
	
	public String jump(String mnemonic) {
		
		String jumpBits = "";
		
		if (mnemonic.equals("null"))
			jumpBits = "000";
		else if (mnemonic.equals("JGT"))
			jumpBits = "001";
		else if (mnemonic.equals("JEQ"))
			jumpBits = "010";
		else if (mnemonic.equals("JGE"))
			jumpBits = "011";
		else if (mnemonic.equals("JLT"))
			jumpBits = "100";
		else if (mnemonic.equals("JNE"))
			jumpBits = "101";
		else if (mnemonic.equals("JLE"))
			jumpBits = "110";
		else 
			jumpBits = "111";
		return jumpBits;		
	}
}
