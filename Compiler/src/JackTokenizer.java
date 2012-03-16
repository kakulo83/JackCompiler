import java.io.*;
import java.util.*;
import javax.swing.*;
//JOptionPane.showMessageDialog(null,currentToken);
public class JackTokenizer {

	File jackFile;
	BufferedReader br;
	static String[] KEYWORDS;
	static String[] SYMBOLS; 
	static {
		KEYWORDS = new String[] {"class","constructor","function","method","field","static","var","int","char","boolean","void","true","false","null","this","let","do","if","else","while","return"};
		SYMBOLS = new String[] {"{","}","(",")","[","]",".",",",";","+","-","*","/","&","|","<",">","=","~"};
	}
	String[] currentLine;
	String currentToken;
	int numberOfTokensInLine = 0;
	int tokenIndex = 0;
	int linesOfCode = 0;
	int linesIndex = 0;

	public JackTokenizer(File file) {
		jackFile = file;
		countLinesOfCode();
		initBufferedReader();
	}

	private void countLinesOfCode() {
		try {
			LineNumberReader lr = new LineNumberReader(new FileReader(jackFile));
			String newLine = "";
			while ((newLine = lr.readLine()) != null) {
				newLine = newLine.trim();
		
				if(!newLine.isEmpty() && !newLine.startsWith("//") && !newLine.startsWith("/*") && !newLine.startsWith("*")) {
					linesOfCode++;
				}
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}
		
	private void initBufferedReader() {
		try {
			br = new BufferedReader(new FileReader(jackFile));
			String newLine = br.readLine().trim();	
			
			// If the line read is a comment or is empty, keep going until a valid line of code is read
			while (newLine.startsWith("//") || newLine.startsWith("/*") || newLine.startsWith("*") || newLine.isEmpty()) {
				newLine = br.readLine().trim();
			}
			// At this point a valid line of code should be the current line
			newLine = sanitizeLine(newLine);
			List<String> temp = recursivelyGetTokens(newLine);
			currentLine = temp.toArray(new String[temp.size()]);		
			numberOfTokensInLine = currentLine.length;
			currentToken = currentLine[tokenIndex];
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}
		
	public Boolean hasMoreTokens() {
		if ((tokenIndex + 1 < numberOfTokensInLine))
			return true;
			
		if (linesIndex + 1 < linesOfCode)
			return true;
		else
			return false;
	}

	public void advance() {
		String newLine ="";
		try {
			if (tokenIndex + 1 < numberOfTokensInLine) {
				tokenIndex++;	
				currentToken = currentLine[tokenIndex];
			}
			else { 
				linesIndex++;	
				newLine = br.readLine().trim();

				while (newLine.startsWith("//") || newLine.startsWith("/*") || newLine.startsWith("*") || newLine.isEmpty()) {
					newLine = br.readLine().trim();
				}
				newLine = sanitizeLine(newLine);
				List<String> temp = recursivelyGetTokens(newLine);
				currentLine = temp.toArray(new String[temp.size()]);			
				numberOfTokensInLine = currentLine.length;
				tokenIndex = 0;
				currentToken = currentLine[tokenIndex];
			}
		}
		catch (IOException ex) { }	
	}

	public void tokensInLine() {
		JOptionPane.showMessageDialog(null,"Tokens in line: " + Arrays.deepToString(currentLine));
	}

	private List<String> recursivelyGetTokens(String lineOfCode) { 
		//  If the lineOfCode contains a String value contained within double quotes, save the String value and replace it 
		//  with a Dummy Token value of STRINGCONSTANT.  After the recursive tokenizing is done, replace the token with this
		//  Dummy value with the REAL value of the String token.
	
		List<String> inputTokens;
		
		List<String> terminalTokens;
		String stringConstantToken="";
		String dummyStringToken = "STRINGCONSTANT";
		Boolean haveString = false;
		
		if (lineOfCode.contains("\"")) {
			int doubleQuotesStart = lineOfCode.indexOf("\"");
			int doubleQuotesFinish = lineOfCode.lastIndexOf("\"") + 1;
			stringConstantToken = lineOfCode.substring(doubleQuotesStart, doubleQuotesFinish);
			lineOfCode = lineOfCode.replace(stringConstantToken, dummyStringToken);  
			haveString = true;
		}			
		inputTokens = new ArrayList<String>(Arrays.asList(lineOfCode.trim().split("\\s+")) );
		terminalTokens = new ArrayList<String>();	
	
		if (inputTokens.size() > 1) {
			Iterator iterator = inputTokens.iterator();
			while(iterator.hasNext()) {
				terminalTokens.addAll((List<String>)recursivelyGetTokens((String)iterator.next() ) );	
			}
			if (haveString) {
				int dummyIndex = terminalTokens.indexOf(dummyStringToken);
				terminalTokens.remove(dummyStringToken);
				terminalTokens.add(dummyIndex,stringConstantToken);
			}
		}			
		else {
			//  CompoundToken = (token1)(token of interest)(token2)  where token1 or token2 may or may not exist
			//  If token1 and token2 do not exist the KEYWORDS or SYMBOLS code block should catch the token of interest
			String compoundToken = (String)inputTokens.get(0);
			int compoundLength = compoundToken.length();

			if (Arrays.asList(KEYWORDS).contains(compoundToken)) {
				return inputTokens;
			}
			else if (Arrays.asList(SYMBOLS).contains(compoundToken)) {
				return inputTokens;
			}
			else if (isStringNumber(compoundToken)) {
				return inputTokens;
			}
			else if (compoundToken.contains(".")) {
				if (compoundToken.startsWith(".")) {
					String token1 = ".";
					String token2 = compoundToken.substring(1);
					inputTokens.add(token1);
					inputTokens.addAll(recursivelyGetTokens(token2));
				}
				else {
					int firstPeriodIndex = compoundToken.indexOf(".");
					String token1 = compoundToken.substring(0,firstPeriodIndex);
					String token2 = ".";
					String token3 = compoundToken.substring(firstPeriodIndex+1);
					inputTokens.addAll(recursivelyGetTokens(token1));
					inputTokens.add(token2);
					if (!token3.equals(""))
						inputTokens.addAll(recursivelyGetTokens(token3));
				}
				inputTokens.remove(0);
				return inputTokens;
			}
			else if (compoundToken.contains("(")) {

				if (compoundToken.startsWith("(")) {
					String token1 = "(";
					String token2 = compoundToken.substring(1);		
					inputTokens.add(token1);	
					inputTokens.addAll(recursivelyGetTokens(token2));		
				}
				else {
					int firstParenthesisIndex = compoundToken.indexOf("(");
					String token1 = compoundToken.substring(0,firstParenthesisIndex);
					String token2 = "(";
					String token3 = compoundToken.substring(firstParenthesisIndex+1);
					inputTokens.addAll(recursivelyGetTokens(token1));
					inputTokens.add(token2);
					if (!token3.equals(""))
						inputTokens.addAll(recursivelyGetTokens(token3));
				}
				inputTokens.remove(0);				
				return inputTokens;
			}
			else if (compoundToken.contains("[")) {
		
				if (compoundToken.startsWith("[")) {
					String token1 = "[";
					String token2 = compoundToken.substring(1);
					inputTokens.add(token1);
					inputTokens.addAll(recursivelyGetTokens(token2));
				}
				else {
					int firstParenthesisIndex = compoundToken.indexOf("[");
					String token1 = compoundToken.substring(0,firstParenthesisIndex);
					String token2 = "[";
					String token3 = compoundToken.substring(firstParenthesisIndex+1);
					inputTokens.addAll(recursivelyGetTokens(token1));
					inputTokens.add(token2);
					if (!token3.equals(""))
						inputTokens.addAll(recursivelyGetTokens(token3));
				}
				inputTokens.remove(0);
				return inputTokens;
			}
			else if (compoundToken.contains("]")) {

				if (compoundToken.startsWith("]")) {
					String token1 = "]";
					String token2 = compoundToken.substring(1);
					inputTokens.add(token1);
					inputTokens.addAll(recursivelyGetTokens(token2));
				}
				else {
					int firstParenthesisIndex = compoundToken.indexOf("]");
					String token1 = compoundToken.substring(0,firstParenthesisIndex);
					String token2 = "]";
					String token3 = compoundToken.substring(firstParenthesisIndex+1);
					inputTokens.addAll(recursivelyGetTokens(token1));
					inputTokens.add(token2);
					if (!token3.equals("")) 
						inputTokens.addAll(recursivelyGetTokens(token3));
				}
				inputTokens.remove(0);
				return inputTokens;
			}
			else if (compoundToken.contains(",") ) {
				int indexOfComma = compoundToken.indexOf(",");
				if (indexOfComma >= 1) {
					String token1 = compoundToken.split("[,]")[0];
					inputTokens.add(token1);
				}
				inputTokens.add(",");
				if (!compoundToken.endsWith(",")) {
					String token2 = compoundToken.split("[,]")[1];
					inputTokens.addAll(recursivelyGetTokens(token2));
				}
				inputTokens.remove(0);
				return inputTokens;
			}
			else if (compoundToken.contains(")") ) {
				
				if (compoundToken.startsWith(")")) {
					String token1 = ")";
					String token2 = compoundToken.substring(1);
					inputTokens.add(token1);
					inputTokens.addAll(recursivelyGetTokens(token2));
				}
				else {
					int firstParenthesisIndex = compoundToken.indexOf(")");
					String token1 = compoundToken.substring(0,firstParenthesisIndex);
					String token2 = ")";
					String token3 = compoundToken.substring(firstParenthesisIndex+1);
					inputTokens.addAll(recursivelyGetTokens(token1));
					inputTokens.add(token2);
					if (!token3.equals("")) 
						inputTokens.addAll(recursivelyGetTokens(token3));
				}
				inputTokens.remove(0);
				return inputTokens;
			}
			else if (compoundToken.contains("~")) {
				
				if (compoundToken.startsWith("~")) {
					String token1 = "~";
					String token2 = compoundToken.substring(1);
					inputTokens.add(token1);
					inputTokens.addAll(recursivelyGetTokens(token2));
				}
				else {
					int firstTildeIndex = compoundToken.indexOf("~");
					String token1 = compoundToken.substring(0,firstTildeIndex);
					String token2 = "~";
					String token3 = compoundToken.substring(firstTildeIndex+1);
					inputTokens.addAll(recursivelyGetTokens(token1));
					inputTokens.add(token2);
					if (!token3.equals("")) 
						inputTokens.addAll(recursivelyGetTokens(token3));										
				}
				inputTokens.remove(0);
				return inputTokens;
			}
			else if (compoundToken.contains("-")) {

				if (compoundToken.startsWith("-")) {
					String token1 = "-";
					String token2 = compoundToken.substring(1);
					inputTokens.add(token1);
					inputTokens.addAll(recursivelyGetTokens(token2));
				}	
				else {	
					int firstMinusIndex = compoundToken.indexOf("-");
					String token1 = compoundToken.substring(0,firstMinusIndex);
					String token2 = "-";
					String token3 = compoundToken.substring(firstMinusIndex+1);
					inputTokens.addAll(recursivelyGetTokens(token1));
					inputTokens.add(token2);
					if (!token3.equals(""))
						inputTokens.addAll(recursivelyGetTokens(token3));
				}
				inputTokens.remove(0);
				return inputTokens;
			}	
			else if (compoundToken.matches("\\p{Alnum}*")) {

				return inputTokens;
			}
			else {
				if (compoundToken.endsWith(";")) {
					inputTokens.add(compoundToken.substring(0,compoundToken.length()-1));
					inputTokens.add(";");
					inputTokens.remove(0);
				}
				return inputTokens;
			}
		}	
		return terminalTokens;	
	}

	public TokenType tokenType() {
		
		if (Arrays.asList(KEYWORDS).contains(currentToken)) {
			return TokenType.KEYWORD;
		}
		if (Arrays.asList(SYMBOLS).contains(currentToken)) {
			return TokenType.SYMBOL;
		}
		if (isStringNumber(currentToken)) {
			return TokenType.INT_CONST;
		}
		if (currentToken.matches("^\".+")) {
			return TokenType.STRING_CONST;
		}
		else //(currentToken.matches("\\p{Alnum}"))
			return TokenType.IDENTIFIER;
	}
	
	public KeyWord keyWord() {
		return KeyWord.valueOf(currentToken.toUpperCase());
	}
	
	public String symbol() {
		String symbolToken = "";
		if (currentToken.charAt(0) == '>') {
			symbolToken = "&gt;";	
		}
		else if (currentToken.charAt(0) == '<') {
			symbolToken = "&lt;";	
		}
		else if (currentToken.charAt(0) == '&') {
			symbolToken = "&amp;";
		}
		else
			symbolToken = Character.toString(currentToken.charAt(0));
		return symbolToken;
	}
	
	public String identifier() {
		return currentToken;
	}
	
	public int intVal() {
		return Integer.parseInt(currentToken);
	}

	public String stringVal() {
		currentToken = currentToken.replace("\"", "");
		return currentToken;
	}
	
	private String sanitizeLine(String newLine) {
		if (newLine.contains("//")) {
			int commentIndex = newLine.indexOf("//");
			newLine = newLine.substring(0,commentIndex);
		}
		return newLine;
	}

	private Boolean isStringNumber(String value) {
		try {
			if (Integer.parseInt(value) >= 0)
				return true;
			else 
				return false;
		}
		catch (NumberFormatException ex) {
			return false;
		}
	}
}
