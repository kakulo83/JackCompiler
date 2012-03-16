import java.io.*;
import javax.swing.*;

public class JackCompiler {

	JackTokenizer tokenizer;
	CompilationEngine compilationEngine;
	VMWriter vmWriter;

	public JackCompiler() {}

	public void openFile() {
		JFileChooser jFileChooser = new JFileChooser("/Users/robertcarter/Desktop/Java/Elements of Computing Systems/Full Compiler/Compiler/resources");
		jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnVal = jFileChooser.showOpenDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File program = jFileChooser.getSelectedFile();
			compileProgram(program);		
		}
	}

	public void compileProgram(File program) {
		if (program.exists()) { 
			if (program.isDirectory() ) {
				// Create array of .jack files by using a filter
					FilenameFilter extensionFilter = new FilenameFilter( ) { 
					public boolean accept(File dir, String name) {
						return name.endsWith(".jack");
					}
				};
				File[ ] allJackFiles = program.listFiles(extensionFilter);
				// Loop through and translate all .jack files in directory			
				for (File jackFile : allJackFiles) {
					File vmFile = new File(jackFile.toString().split("\\.")[0].concat(".vm"));	
					compileFile(jackFile, vmFile);	
				}
			}			
			else {
				File vmFile = new File(program.toString().split("\\.")[0].concat(".vm"));	
				compileFile(program, vmFile);
			}
		}
	}

	private void compileFile(File jackFile, File vmFile) {
		try {
			tokenizer = new JackTokenizer(jackFile);
			vmWriter = new VMWriter(new PrintWriter(new FileWriter(vmFile)));	
			compilationEngine = new CompilationEngine(tokenizer, vmWriter, vmFile);	
		}
		catch (IOException exception) {
			System.err.println("Error creating output stream");
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		JackCompiler compiler = new JackCompiler();

		if (args.length == 0) {
			compiler.openFile();
		}
		else {
			File program = new File(args[0]);
			compiler.compileProgram(program);
		}
	}
}
