import java.io.*;
import java.util.HashMap;
import javax.swing.*;

//	 Conventions for reading grammar rules:
//
//  'xxx'	=	quoted boldface is used for tokens that appear verbatim ("terminals")
//	 xxx	=	language constructs  
//	 ()		=	parentheses are for grouping language constructs
//	 x|y	= 	indicates that either x or y can appear
//	 x?		= 	indicates that x ppears 0 or 1 times
//	 x*		=	indictes that x appears 0 or more times

public class CompilationEngine {
	
	public CompilationEngine(JackTokenizer tokenizer, VMWriter vmWriter, File vmFile) {
		this.tokenizer = tokenizer;
		this.vmWriter = vmWriter;
		this.vmFile = vmFile;	
		symbolTable = new SymbolTable();	
		asciiTable = new ASCIITable(); 
		CompileClass();	
	}

	private JackTokenizer tokenizer;	
	private	VMWriter vmWriter;
	private SymbolTable symbolTable;
	private ASCIITable asciiTable;
	private enum SubroutineType { CONSTRUCTOR, FUNCTION, METHOD};
	private File vmFile;
	private String className = "";
	private int ifCounter = -1;	
	private int whileCounter = -1;
	private Boolean looked_ahead_one_token = false;


	private void CompileClass() {
		//	'class'		className 	'{'		classVarDec*	subroutineDec* 	'}'
		
		advance_to_next_token();
		//  'className'	
		className = tokenizer.identifier();
		advance_to_next_token();
		// '{'

		while (tokenizer.hasMoreTokens() ) {
			tokenizer.advance();	
			if (isClassVariable()) {
				CompileClassVarDec();
				continue;		
			}
			if (isSubroutine()) {
				CompileSubroutine();
				continue;
			}
		}
		vmWriter.close();	
		// '}'	
	}
	
	private void CompileClassVarDec() {
		// 	('static' | 'field') 	type	varName		(','	varName)*	';'	
	
		// 'static' or 'field'
		Kind varKind;	
		String varType;	
		String varName;

		// 'static' or 'field'
		varKind = Kind.valueOf(tokenizer.keyWord().toString().toUpperCase());	
		advance_to_next_token();

		// type
		if (isIdentifier() ) {
			varType = tokenizer.identifier();				
		}
		else {
			varType = tokenizer.keyWord().toString(); 
		}
		advance_to_next_token();
		// varName	
		varName = tokenizer.identifier();
		// Add to SymbolTable
		symbolTable.Define(varName, varType, varKind);

		advance_to_next_token();
		// Add any additional variables to SymbolTable
		while (!isSemiColon() ) {
			if (isIdentifier() ) {
				// 'varName'
				varName = tokenizer.identifier();		
				symbolTable.Define(varName, varType, varKind);
			}
			advance_to_next_token();	
		}
	}

	private void CompileSubroutine() {
		// ('constructor' | 'function' | 'method')
		// ('void' | type)  subroutineName  '('  parameterList  ')'
		// subroutineBody	
		
		//  'constructs' | 'function' | 'method'
		symbolTable.startSubroutine();
		SubroutineType subroutineType = SubroutineType.valueOf(tokenizer.keyWord().toString().toUpperCase() );

		//  Reset if and while counter for each subroutine so
		//  if and while labels start again with index 0.
		ifCounter = -1;
		whileCounter = -1;
		
		switch (subroutineType) {
			case CONSTRUCTOR:
				compileConstructor();
				break;
			case METHOD:
				compileMethod();	
				break;
			case FUNCTION: 
				compileFunction();
				break;
			default:
				break;
		}
	}

	private void compileConstructor() {
		String returnType;	
		String constructorName;
		
		int nLocals = 0;	
		
		advance_to_next_token();
		//  'name of class'	
		advance_to_next_token();
		//  'subroutineName = ClassName.new'
		constructorName = className + "." + tokenizer.identifier();
	
		advance_to_next_token();
		//  '('
		advance_to_next_token();
		//  compile parameter list
		compileParameterList();
		//  ')'
		advance_to_next_token();
		//  '{'
		advance_to_next_token();	

		while (isSubroutineVar() ) {
			//  compile subroutine variables
			if (isSubroutineVar() ) {
				compileVarDec();
			}
			advance_to_next_token();
		}
		// Get the number of local subroutine variables
		nLocals = symbolTable.VarCount(Kind.VAR);	

		vmWriter.writeFunction(constructorName, nLocals);
		
		int nFields = symbolTable.VarCount(Kind.FIELD);
		vmWriter.writePush(Segment.CONST, nFields); 
		vmWriter.writeCall("Memory.alloc", 1); 
		vmWriter.writePop(Segment.POINTER, 0);
	
		//  compile statements	
		compileStatements();
		//  '}'
	}

	private void compileMethod() {
		String returnType;	
		String subroutineName;
		int nLocals = 0;	

		advance_to_next_token();
		if (isKeyWord() ) {
			//  'void'	
			returnType = "void";
		}
		else {
			//  'type'
			returnType = tokenizer.identifier();	
		}
		advance_to_next_token();

		//  'subroutineName = ClassName.subroutineName'
		subroutineName = className + "." + tokenizer.identifier();
	
		advance_to_next_token();
		//  '('
		advance_to_next_token();
		//  compile parameter list
		// 	First argument to a method must be a reference to object method operates on	
		// 	This is sort of a hack, but add an argument to the symbol table for the method
		// 	so the argument index are incremented correctly, otherwise a off by 1 error.
		symbolTable.Define("ObjectReference","null", Kind.ARG);
		compileParameterList();
		//  ')'
		advance_to_next_token();
		//  '{'
		advance_to_next_token();	

		while (isSubroutineVar() ) {
			//  compile subroutine variables
			if (isSubroutineVar() ) {
				compileVarDec();
			}
			advance_to_next_token();
		}

		nLocals = symbolTable.VarCount(Kind.VAR);	

		//  At this point it is known how many local variables are needed for the subroutine
		vmWriter.writeFunction(subroutineName, nLocals);
		//  Set the base of the this segment
		vmWriter.writePush(Segment.ARGUMENT, 0);
		vmWriter.writePop(Segment.POINTER, 0);
		//  compile statements	
		compileStatements();
		//  '}'
	}

	private void compileFunction() {
		String returnType;	
		String subroutineName;
		int nLocals = 0;	
		
		advance_to_next_token();
		if (isKeyWord() ) {
			//  'void'	
			returnType = "void";
		}
		else {
			//  'type'
			returnType = tokenizer.identifier();	
		}
		advance_to_next_token();

		//  'subroutineName = ClassName.subroutineName'
		subroutineName = className + "." + tokenizer.identifier();
	
		advance_to_next_token();
		//  '('
		advance_to_next_token();
		//  compile parameter list
		compileParameterList();
		//  ')'
		advance_to_next_token();
		//  '{'
		advance_to_next_token();	

		while (isSubroutineVar() ) {
			//  compile subroutine variables
			if (isSubroutineVar() ) {
				compileVarDec();
			}
			advance_to_next_token();
		}
		
		nLocals = symbolTable.VarCount(Kind.VAR);	
		//  At this point it is known how many local variables are needed for the subroutine
		vmWriter.writeFunction(subroutineName, nLocals);
		//  compile statements	
		compileStatements();
		//  '}'
	}

	private void compileParameterList() {
		// ((type varName) (',' type varName)*)?	
		String argType;
		String argName;		
		Kind argKind = Kind.ARG;

		if (isCloseParenthesis() ) {
			return;
		}	
		if (isKeyWord() ) {
			argType = tokenizer.keyWord().toString(); 
		}
		else {
			argType = tokenizer.identifier();
		}
		advance_to_next_token();
		argName = tokenizer.identifier();
		symbolTable.Define(argName, argType, argKind);
		advance_to_next_token();
		// potential ',' or ')'	
		//  Compile additional arguments
		while (!isCloseParenthesis() ) {
			if (isComma() ) {
				advance_to_next_token();
			}	
			if (isKeyWord() ) {
				argType = tokenizer.keyWord().toString(); 
				advance_to_next_token();
				argName = tokenizer.identifier();
				advance_to_next_token();
				symbolTable.Define(argName, argType, argKind);
			}
			else {
				argType = tokenizer.identifier();
				advance_to_next_token();
				argName = tokenizer.identifier();
				advance_to_next_token();
				symbolTable.Define(argName, argType, argKind);
			}
		}
	}
	
	private void compileVarDec() {
		//	'var'	type	varName (','	varName)*)? ';'	
		
		// 'var'
		Kind varKind = Kind.VAR;	
		String varType;
		String varName;
		
		advance_to_next_token();	
		// 'type'	
		if (isKeyWord() ) {
			varType = tokenizer.keyWord().toString();	
		}
		else {
			varType = tokenizer.identifier();
		}	
		advance_to_next_token();
		// 'varName'
		varName = tokenizer.identifier();	
		//  Add variable to symbolTable	
		symbolTable.Define(varName, varType, varKind);

		//  Add any additional variables
		while (tokenizer.hasMoreTokens() ) {
			tokenizer.advance();	
			if (isSemiColon() ) {
				break;
			}	
			if (isIdentifier() ) {	
				varName = tokenizer.identifier();
				symbolTable.Define(varName, varType, varKind);	
			}
		}
		// ';'
	}
	
	private void compileStatements() {
		while (!isCloseCurlyBracket() ) {
			switch (tokenizer.keyWord()) {
				case LET:
					compileLet();
					break;
				case IF:
					compileIf();	
					break;
				case WHILE:
					compileWhile();
					break;
				case DO:
					compileDo();	
					break;
				case RETURN:
					compileReturn();
				default: break;
			}
			//  some compiation methods (if & term) have to peek ahead one token,
			//  looked_ahead_one_token keeps the token stream in sync with the reader
			if (!looked_ahead_one_token) {
				advance_to_next_token();
			}
			else {
				looked_ahead_one_token = false;
			}
		}
	}
	
	private void compileDo() {
		// 'do' subroutineCall ';'
		// 'do'
		Boolean isExternalSubroutine = false;	
		String subroutineCall="";	
		advance_to_next_token();
		// subroutineName or className or varName	
		String subroutineOwner = tokenizer.identifier();	
		advance_to_next_token();	
	
		if (isPeriod() ) {
			// '.'
			advance_to_next_token();
			// 'subroutineName'
			String subroutineName =  tokenizer.identifier(); 	
			
			advance_to_next_token();	
			// '('
			
			// Internal function call  
			if (subroutineOwner.equals(className)) {
				compileInternalFunctionCall(className, subroutineName);								
			}
			// External method cal	
			else if (!symbolTable.TypeOf(subroutineOwner).isEmpty() ) {
				compileExternalMethodCall(subroutineOwner, subroutineName); 
			}
			// External System Call
			else if (isSystemSubroutine(subroutineOwner) ) {
				compileSystemCall(subroutineOwner, subroutineName);
			}
			// External function call	
			else {
				compileExternalFunctionCall(subroutineOwner, subroutineName);
			}
		} 
		//  Internal method call  	
		else {
			// '('
			compileInternalMethodCall(className, subroutineOwner);	
		}
		vmWriter.writePop(Segment.TEMP, 0);
		// ')'
		advance_to_next_token();
		// ';'
	}
	
	private void compileLet() {
		// 'let' varName ('[' expression ']')? '=' expression ';'
		// 'let'
		advance_to_next_token();
		// 'varName'

		String varName = tokenizer.identifier();
		int nVarIndex = symbolTable.IndexOf(varName);	
		Kind varKind = symbolTable.KindOf(varName);	
		advance_to_next_token();

		if (isOpenBracket() ) {
			// '['
			advance_to_next_token();
			CompileExpression();
			vmWriter.writePush(symbolTable.SegmentOf(varName),symbolTable.IndexOf(varName) );
			vmWriter.WriteArithmetic(ArithmeticOperator.ADD);
			// ']'
			
			advance_to_next_token();
			// '='
			advance_to_next_token();
			CompileExpression();
			//  Push the value into temp 	
			vmWriter.writePop(Segment.TEMP, 0);
			// 	
			vmWriter.writePop(Segment.POINTER, 1);
			vmWriter.writePush(Segment.TEMP, 0);	
			vmWriter.writePop(Segment.THAT, 0);	
		}
		else {
			// '='
			advance_to_next_token();
			CompileExpression();
			Segment varSegment = symbolTable.SegmentOf(varName); 
			vmWriter.writePop(varSegment, nVarIndex);	
		}
		//  Ensure the correct closing token is next
		if (!isSemiColon() ) {
			advance_to_next_token();
		}
		// ';'
	}

	private void compileInternalMethodCall(String owner, String subroutine) {
		vmWriter.writePush(Segment.POINTER, 0);
		advance_to_next_token();
		int nArgs = CompileExpressionList(); 
		//  Add one for the first pushed argument of the reference of the object on which the method is supposed to operate	
		nArgs++;
		vmWriter.writeCall(owner + "." + subroutine, nArgs);
	}

	private void compileInternalFunctionCall(String owner, String subroutine) {
		advance_to_next_token();
		int nArgs = CompileExpressionList(); 
		vmWriter.writeCall(owner + "." + subroutine, nArgs);
	}

	private void compileExternalMethodCall(String owner, String subroutine) {
		Segment segment = symbolTable.SegmentOf(owner);	
		int index   = symbolTable.IndexOf(owner);
		vmWriter.writePush(segment, index);
		advance_to_next_token();
		int nArgs = CompileExpressionList(); 
		//  Add one for the first pushed argument of the reference of the object on which the method is supposed to operate	
		nArgs++;
		vmWriter.writeCall(symbolTable.TypeOf(owner) + "." + subroutine, nArgs);
	}

	private void compileExternalFunctionCall(String owner, String subroutine) {
		advance_to_next_token();
		int nArgs = CompileExpressionList(); 
		vmWriter.writeCall(owner + "." + subroutine, nArgs);
	}

	private void compileSystemCall(String owner, String subroutine) {
		advance_to_next_token();
		int nArgs = CompileExpressionList(); 
		vmWriter.writeCall(owner + "." + subroutine, nArgs);
	} 

	private void compileWhile() {
		// 'while' '(' expression ')' '{' statements '}'	
		whileCounter++;	
		int	whileDepth = whileCounter;
		// 'while'	
		vmWriter.WriteLabel("WHILE_EXP" + Integer.toString(whileDepth) );
		advance_to_next_token();
		// '('	
		advance_to_next_token();

		CompileExpression();
		
		// ')'
		vmWriter.WriteArithmetic(ArithmeticOperator.NOT);	
	
		advance_to_next_token();
		// '{'
		advance_to_next_token();
		vmWriter.WriteIf("WHILE_END" + Integer.toString(whileDepth) );
		compileStatements();		
		// '}'
		vmWriter.WriteGoto("WHILE_EXP" + Integer.toString(whileDepth) );	
		vmWriter.WriteLabel("WHILE_END" + Integer.toString(whileDepth) );	
	}	
	
	private void compileReturn() {
		// 'return' expression? ';'
		
		// 'return'
		advance_to_next_token();
		if (!isSemiColon() ) {
			CompileExpression();
			vmWriter.writeReturn();
		}
		else {
			vmWriter.writePush(Segment.CONST, 0);	
			vmWriter.writeReturn();	

		}
	}
	
	private void compileIf() {
		// 'if' '(' expression ')' '{' statements '}' 
		// ifDepth is used to avoid naming conflicts w/ possible nested ifs
		ifCounter++;
		int ifDepth = ifCounter;
		// 'if'
		advance_to_next_token();
		// '('
		advance_to_next_token();
		CompileExpression();
		// ')'
		vmWriter.WriteIf("IF_TRUE" + Integer.toString(ifDepth) ); 
		vmWriter.WriteGoto("IF_FALSE" + Integer.toString(ifDepth) ); 
		advance_to_next_token();	
		// '{'
		advance_to_next_token();
		vmWriter.WriteLabel("IF_TRUE" + Integer.toString(ifDepth) ); 
		compileStatements();	
		// '}'
	
		// CHECK FOR POSSIBLE TRAILING ELSE BLOCK
		advance_to_next_token();

		if (isElse() ) {
			vmWriter.WriteGoto("IF_END" + Integer.toString(ifDepth) );
			// 'else'	
			vmWriter.WriteLabel("IF_FALSE" + Integer.toString(ifDepth) );
			advance_to_next_token();
			// '{'
			advance_to_next_token();
			compileStatements();
			// '}'
			vmWriter.WriteLabel("IF_END" + Integer.toString(ifDepth) );
		}
		else {
			vmWriter.WriteLabel("IF_FALSE" + Integer.toString(ifDepth) );
			looked_ahead_one_token = true;
		}
	}
	
	private void CompileExpression() {
		// term (op term)*	
	
		Boolean startOfTerm = true;

		while (!isCloseParenthesis() && !isSemiColon() && !isCloseBracket() && !isComma() ) {
			// Expressions compiled using postfix notation:  Reverse Polish Notation
			if (isOperator() && !startOfTerm ) {	
				startOfTerm = false;
				ArithmeticOperator operator = getOperator();	
				advance_to_next_token();
				CompileTerm();
				vmWriter.WriteArithmetic(operator);
			}	
			else {
				CompileTerm();
				startOfTerm = false;
			}			
			if (!looked_ahead_one_token) {
				advance_to_next_token();
			}
			else {
				looked_ahead_one_token = false;
			}
		}
	}

	private void CompileTerm() {
		// integerConstant | stringConstant | keyWordConstant | varName |
		// varName '[' expression ']' | subroutineCall | '(' expression ')' |
		// unaryOp term
		// '( expression )'	
		if (isOpenParenthesis() ) {
			// '('
			advance_to_next_token();
			CompileExpression();
			// ')'
		}
		// 'unaryOp term'	
		else if (isUnaryOp() ) {
			// '-'  '~'	
			ArithmeticOperator unaryOp = getUnaryOperator(); 
			advance_to_next_token();
			// term	
			CompileTerm();	
			vmWriter.WriteArithmetic(unaryOp);
		}
		else if (isIntegerConstant() ) {
			int value = tokenizer.intVal();
			vmWriter.writePush(Segment.CONST, value); 
		}
		else if (isStringConstant() ) {
			String stringConst = tokenizer.stringVal();
			int stringSize = stringConst.length();	
			vmWriter.writePush(Segment.CONST, stringSize);	
			vmWriter.writeCall("String.new", 1);	

			for (int i = 0; i < stringSize; i++) {
				String character = Character.toString(stringConst.charAt(i) );	
				int asciiCode = (asciiTable.getDecimalCode(character) );
				vmWriter.writePush(Segment.CONST, asciiCode); 
				vmWriter.writeCall("String.appendChar", 2);
			}
		}
		else if (isKeyWord() ) {
			if (isTrueKeyWord() ) { 
				vmWriter.writePush(Segment.CONST, 0);
				vmWriter.WriteArithmetic(ArithmeticOperator.NOT);	
			}
			else if (isFalseKeyWord() ) {
				vmWriter.writePush(Segment.CONST, 0);
			}	
			else if (isNullKeyWord() ) {
				vmWriter.writePush(Segment.CONST, 0);	
			}	
			else {
				//  POINTER 0 = THIS
				vmWriter.writePush(Segment.POINTER, 0);	
			}
		}
		else {
			String varName = tokenizer.identifier();	
			advance_to_next_token();
			if (isPeriod() ) {
				// '.'	
				advance_to_next_token();
				// subroutineName	
				String subroutineOwner = varName;
				String subroutineName =  tokenizer.identifier(); 	
				advance_to_next_token();
				// '('

				// Internal function call 
				if (subroutineOwner.equals(className)) {
					compileInternalFunctionCall(className, subroutineName);								
				}
				// External method cal	
				else if (!symbolTable.TypeOf(subroutineOwner).isEmpty() ) {
					compileExternalMethodCall(subroutineOwner, subroutineName); 
				}
				// External System Call
				else if (isSystemSubroutine(subroutineOwner) ) {
					compileSystemCall(subroutineOwner, subroutineName);
				}
				// External function call
				else {
					compileExternalFunctionCall(subroutineOwner, subroutineName);
				}
			}
			else if (isOpenParenthesis() ) {
				// '('  	
				String subroutineOwner = varName;
				compileInternalMethodCall(className, subroutineOwner);	
			}
			else if  (isOpenBracket() ) {
				// '['  	
				advance_to_next_token();
	
				CompileExpression();
				vmWriter.writePush(symbolTable.SegmentOf(varName),symbolTable.IndexOf(varName) );
				vmWriter.WriteArithmetic(ArithmeticOperator.ADD);
				vmWriter.writePop(Segment.POINTER, 1);
				vmWriter.writePush(Segment.THAT, 0);
				// ']' 	
			}
			else {
				Kind varKind = symbolTable.KindOf(varName);
				int varIndex = symbolTable.IndexOf(varName);
				Segment varSegment = symbolTable.SegmentOf(varName); 
				vmWriter.writePush(varSegment, varIndex);	
				looked_ahead_one_token = true;	
			}
		}
	}

	private int CompileExpressionList() {
		int nArgs = 0;
		while (!isCloseParenthesis() ) {
			CompileExpression();
			nArgs++;	
			if (isComma() ) {
				advance_to_next_token();
			}
		}
		return nArgs;
	}

	// 	Helper methods and other stuff to make the code more readable 
	private void advance_to_next_token() {
		if (tokenizer.hasMoreTokens() ) {
			tokenizer.advance();
		}
	}

	private Boolean isSystemSubroutine(String subroutineOwner) {
		if (subroutineOwner.equals("Math") || subroutineOwner.equals("Array")
			|| subroutineOwner.equals("Output") || subroutineOwner.equals("Screen")
			|| subroutineOwner.equals("Keyboard") || subroutineOwner.equals("Memory")
			|| subroutineOwner.equals("Sys") )
	   	{
			return true;
		}
		else { 
			return false;
		}
	}

	private Boolean isClassVariable() {
		if (tokenizer.tokenType().equals(TokenType.KEYWORD) ) { 
			if (tokenizer.keyWord().equals(KeyWord.STATIC) || tokenizer.keyWord().equals(KeyWord.FIELD)) {	
				return true;
			}
			return false;
		}
		else {
			return false;
		}
	}	

	private Boolean isSubroutine() {
		if (tokenizer.tokenType().equals(TokenType.KEYWORD) ) { 
			if (tokenizer.keyWord().equals(KeyWord.CONSTRUCTOR) || tokenizer.keyWord().equals(KeyWord.FUNCTION) || tokenizer.keyWord().equals(KeyWord.METHOD) ) {
				return true;
			}
			return false;
		}
		else {
			return false;
		}
	}

	private Boolean isSubroutineVar() {
		if (tokenizer.tokenType().equals(TokenType.KEYWORD) ) { 
			if (tokenizer.keyWord().equals(KeyWord.VAR) ) {
				return true;
			}
			return false;
		}
		else {
			return false;
		}
	}

	private Boolean isKeyWord() {
		if (tokenizer.tokenType().equals(TokenType.KEYWORD) ) {
			return true;	
		}
		else {
			return false;
		}
	}
		
	private Boolean isSymbol() {
		if (tokenizer.tokenType().equals(TokenType.SYMBOL) ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private Boolean isIdentifier() {
		if (tokenizer.tokenType().equals(TokenType.IDENTIFIER) ) {
			return true;
		}
		else {
			return false;
		}
	}

	private Boolean isTokenA(String arg) {
		if (tokenizer.tokenType().equals(TokenType.SYMBOL) && tokenizer.symbol().equals(arg)) {
			return true;
		}
		else {
			return false;
		}
	}

	//  Most of these can be replaced with isTokenA(String arg) but seem less readable to me 
	private Boolean isSemiColon() {
		if (isSymbol() && tokenizer.symbol().equals(";") ) {
			return true;
		}
		else {
			return false;
		}
	}

	private Boolean isOperator() {
		if (isSymbol() && tokenizer.symbol().equals("+") || tokenizer.symbol().equals("-") || 
						  tokenizer.symbol().equals("*") || tokenizer.symbol().equals("/") ||
						  tokenizer.symbol().equals("&amp;") || tokenizer.symbol().equals("|") || 
						  tokenizer.symbol().equals("&lt;") || tokenizer.symbol().equals("&gt;") || 
						  tokenizer.symbol().equals("=")
		   ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private ArithmeticOperator getOperator() {
		if (tokenizer.symbol().equals("+") ) {
			return ArithmeticOperator.ADD;	
		}
		else if (tokenizer.symbol().equals("-") ) {
			return ArithmeticOperator.SUB;
		}
		else if (tokenizer.symbol().equals("=") ) {
			return ArithmeticOperator.EQ;	
		}
		else if (tokenizer.symbol().equals("&gt;") ) {
			return ArithmeticOperator.GT;
		}
		else if (tokenizer.symbol().equals("&lt;") ) {
			return ArithmeticOperator.LT; 
		} 
		else if (tokenizer.symbol().equals("&amp;") ) {
			return ArithmeticOperator.AND;
		}
		else if (tokenizer.symbol().equals("|") ) {
			return ArithmeticOperator.OR;	
		}
		else if (tokenizer.symbol().equals("*") ) {
			return ArithmeticOperator.MULT;
		}
		else {
			return ArithmeticOperator.DIVIDE;
		}
	}	

	private ArithmeticOperator getUnaryOperator() {
		if (tokenizer.symbol().equals("-") ) {
			return ArithmeticOperator.NEG;
		}
		else {
			return ArithmeticOperator.NOT;
		}
	}

	private Boolean isUnaryOp() {
		if (isSymbol() && (tokenizer.symbol().equals("~") || tokenizer.symbol().equals("-") ) ) {
			return true;
		}
		else {
			return false;
		}
	}

	private Boolean isTrueKeyWord() {
		if (isKeyWord() && tokenizer.keyWord().equals(KeyWord.TRUE)) {
			return true;
		}
		else {
			return false;
		}
	}

	private Boolean isFalseKeyWord() { 
		if (isKeyWord() && tokenizer.keyWord().equals(KeyWord.FALSE)) {
			return true; 
		}
		else {
			return false;
		}
	}

	private Boolean isNullKeyWord() {
		if (isKeyWord() && tokenizer.keyWord().equals(KeyWord.NULL)) {
			return true;
		}
		else {
			return false;
		}
	}

	private Boolean isElse() {
		if (isKeyWord() && tokenizer.keyWord().equals(KeyWord.ELSE)) {
			return true;
		}
		else {
			return false;
		}
	}

	private Boolean isComma() {
		if (isSymbol() && tokenizer.symbol().equals(",") ) {
			return true;
		}
		else {
			return false;
		}
	}

	private Boolean isEqualSign() {
		if (isSymbol() && tokenizer.symbol().equals("=") ) {
			return true;
		}
		else {
			return false;
		}
	}

	private Boolean isOpenParenthesis() {
		if (isSymbol() && tokenizer.symbol().equals("(") ) {
			return true;
		}
		else {
			return false;
		}
	}

	private Boolean isCloseParenthesis() {
		if (isSymbol() && tokenizer.symbol().equals(")") ) {
			return true;
		}
		else {
			return false;
		}
	}

	private Boolean isOpenBracket() {
		if (isSymbol() && tokenizer.symbol().equals("[") ) {
			return true;
		}
		else {
			return false;
		}
	}

	private Boolean isCloseBracket() {
		if (isSymbol() && tokenizer.symbol().equals("]") ) {
			return true;
		}
		else {
			return false;
		}
	}

	private Boolean isPeriod() {
		if (isSymbol() && tokenizer.symbol().equals(".") ) {
			return true;
		}
		else {
			return false;
		}
	}

	private Boolean isCloseCurlyBracket() {
		if (isSymbol() && tokenizer.symbol().equals("}") ) {
			return true;
		}
		else {
			return false;
		}
	}

	private Boolean isIntegerConstant() {
		if (tokenizer.tokenType().equals(TokenType.INT_CONST) ) {
			return true;
		}
		else {
			return false;
		}
	}

	private Boolean isStringConstant() {
		if (tokenizer.tokenType().equals(TokenType.STRING_CONST) ) {
			return true;
		}
		else {
			return false;
		}
	}

	private void print_token() {                                                                                                                                                                        
		if (tokenizer.tokenType().equals(TokenType.SYMBOL) ) {                                                                                                                                          
			JOptionPane.showMessageDialog(null, tokenizer.symbol() );                                                                                                                                   
		}                                                                                                                                                                                               
		if (tokenizer.tokenType().equals(TokenType.KEYWORD) ) {                                                                                                                                         
			JOptionPane.showMessageDialog(null, tokenizer.keyWord().toString() );                                                                                                                       
		}                                                                                                                                                                                               
		if (tokenizer.tokenType().equals(TokenType.IDENTIFIER) ) {                                                                                                                                      
			JOptionPane.showMessageDialog(null, tokenizer.identifier() );                                                                                                                               
		}                                                                                                                                                                                               
		if (tokenizer.tokenType().equals(TokenType.INT_CONST) ) {                                                                                                                                       
			JOptionPane.showMessageDialog(null, tokenizer.intVal() );                                                                                                                                   
		}                                                                                                                                                                                               
	}          
}
