import java.io.*;
import javax.swing.*;

public class VMtranslator {

	Parser parser;
	CodeWriter codeWriter;
	File assemblyFile;

	public VMtranslator() {} 
	
	public void openFile() {
		JFileChooser jFileChooser = new JFileChooser("/");
		jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnVal = jFileChooser.showOpenDialog(null);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File program = jFileChooser.getSelectedFile();
			parseProgram(program);		
		}
	}

	public void parseProgram(File program) {
		
		if (program.exists()) { 
				
			if (program.isDirectory() ) {
				assemblyFile = new File(program.toString().concat("/" + program.getName() + ".asm"));
				codeWriter = new CodeWriter(assemblyFile);
				// Create array of .vm files by using a filter
					FilenameFilter extensionFilter = new FilenameFilter( ) { 
					public boolean accept(File dir, String name) {
						return name.endsWith(".vm");
					}
				};
				File[ ] allvmFiles = program.listFiles(extensionFilter);
				// Loop through and translate all .vm files in directory			
				for (File vmFile : allvmFiles) {
					codeWriter.setFileName(vmFile.getName());
					parseFile(vmFile);
				}
			}			
			else {
				assemblyFile = new File(program.toString().substring(0,program.toString().length()-3).concat(".asm"));
				codeWriter = new CodeWriter(assemblyFile);
				codeWriter.setFileName(program.getName());
				parseFile(program);
			}
			codeWriter.close();		
		} 
		else {
			//JOptionPane.showMessageDialog(null,"Program does not exist, please select a real program");
			//this.openFile();
		}
	}
	
	private void parseFile(File vmFile) {
		parser = new Parser(vmFile);
		
		while(parser.hasMoreCommands()) {
			writeCommand(parser.commandType());
			//JOptionPane.showMessageDialog(null,parser.commandType().toString());
			parser.advance();
		}
	}
		
	private void writeCommand(CommandType command) {
		switch (command) {
			case C_ARITHMETIC:
				codeWriter.writeArithmetic(parser.arg1() );
				break;
			case C_PUSH:
				codeWriter.writePushPop(command, parser.arg1(), parser.arg2() );
				break;
			case C_POP:
				codeWriter.writePushPop(command, parser.arg1(), parser.arg2() );
				break;
			case C_LABEL:
				codeWriter.writeLabel(parser.arg1() );
				break;
			case C_GOTO:
				codeWriter.writeGoto(parser.arg1() );
				break;
			case C_IF:
				codeWriter.writeIf(parser.arg1() );
				break;
			case C_FUNCTION:
				codeWriter.writeFunction(parser.arg1(), parser.arg2() );
				break;
			case C_RETURN:
				codeWriter.writeReturn();
				break;
			case C_CALL:
				codeWriter.writeCall(parser.arg1(), parser.arg2() );
				break;
			default:
				return;
		}
	}	
			
	public static void main(String[] args) {
		VMtranslator vm = new VMtranslator();
		if (args.length == 0) {
			vm.openFile();
		}
		else {
			File program = new File(args[0]);
			vm.parseProgram(program);
		}
	}
	
}

