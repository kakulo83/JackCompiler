import java.util.*;
import javax.swing.*;

public class SymbolTable {
	
	public SymbolTable() {
		// Creates a new empty symbol table	
		class_symbols = new HashMap<String, SymbolProperties>();
		subroutine_symbols = new HashMap<String, SymbolProperties>();
	}

	private HashMap<String, SymbolProperties> class_symbols;
	private HashMap<String, SymbolProperties> subroutine_symbols;
	private short argIndex = 0; 
	private short varIndex = 0; 
	private short fieldIndex = 0;
	private short staticIndex = 0;

	public void startSubroutine() {
		// Starts a new subroutine scope (i.e., resets the subroutine's symbol table)	
		subroutine_symbols.clear();
		argIndex = 0;	
		varIndex = 0;
	}

	public void Define(String name, String type, Kind kind) {
		// Defines a new identifier of a given name, type, and kind and assigns it a 
		// running index.  STATIC and FIELD identifiers have a class scope, while ARG
		// and VAR identifiers have a subroutine scope.	
		switch (kind) {
			case STATIC:
				class_symbols.put(name, new SymbolProperties(type, kind, staticIndex) );
				staticIndex++;	
				break;
			case FIELD:
				class_symbols.put(name, new SymbolProperties(type, kind, fieldIndex) );
				fieldIndex++;	
				break;
			case ARG: 
				subroutine_symbols.put(name, new SymbolProperties(type, kind, argIndex) ); 	
				argIndex++;
				break;
			case VAR:
				subroutine_symbols.put(name, new SymbolProperties(type, kind, varIndex) );
				varIndex++;
				break;
			default:
				break;
		}
	}

	public int VarCount(Kind kind) {
		// Returns the number of variables of the given kind already defined in the
		// current scope.
		int count = 0;
		switch (kind) {
			case STATIC:
				count = staticIndex;
				break;
			case FIELD:
				count = fieldIndex;	
				break;
			case ARG: 
				count = argIndex;	
				break;
			case VAR:
				count = varIndex;	
				break;
			default:
				break;
		}
		return count;
	}

	public Kind KindOf(String name) {
		// Returns the kind of the named identifier in the current scope.  If the 
		// identifier is unknown in the current scope, returns NONE.
		// Kinds:  static, field, arg, var, none	
		SymbolProperties symbolProperties;

		if (class_symbols.containsKey(name)) {
			symbolProperties = (SymbolProperties) class_symbols.get(name);
			return symbolProperties.getKind();
		}
		else if (subroutine_symbols.containsKey(name)) {
			symbolProperties = (SymbolProperties) subroutine_symbols.get(name);
			return symbolProperties.getKind();
		} 
		else {
			return Kind.NONE;
		}
	}

	public Segment SegmentOf(String name) {
		// Returns the memory segment the virtual machine will use
		// to push/pop the variable.
		// variables defined within methods/functions map to the LOCAL Segment
		// static variables map to the STATIC segment.
		Segment varSegment = Segment.LOCAL;

		switch (this.KindOf(name)) {
			case STATIC:
				varSegment = Segment.STATIC;
				break;
			case FIELD:
				varSegment = Segment.THIS;	
				break;
			case ARG: 
				varSegment = Segment.ARGUMENT;
				break;
			case VAR:
				varSegment = Segment.LOCAL;
				break;
			default:
				break;
		}
		return varSegment;
	}

	public String TypeOf(String name) {
		// Returns the type of the named identifier in the current scope.
		// Types:  

		SymbolProperties symbolProperties;

		if (class_symbols.containsKey(name)) {
			 symbolProperties = (SymbolProperties) class_symbols.get(name);
			 return symbolProperties.getType();
		}
		else if (subroutine_symbols.containsKey(name)) {
			 symbolProperties = (SymbolProperties) subroutine_symbols.get(name);
			 return symbolProperties.getType();
		}
		else {
			return "";
		}	
	}

	public int IndexOf(String name) {
		// Returns the index assigned to the named identifier.

		// If the variable has already been defined, return its index
		SymbolProperties symbolProperties;
		
		if (class_symbols.containsKey(name)) {
			 symbolProperties = (SymbolProperties) class_symbols.get(name);
			 return symbolProperties.getIndex();
		}
		else if (subroutine_symbols.containsKey(name)) {
			 symbolProperties = (SymbolProperties) subroutine_symbols.get(name);
			 return symbolProperties.getIndex();
		}
		else {
			return -1;
		}		
	}

	// Convenience class for packaging up data
	class SymbolProperties {
		SymbolProperties(String type, Kind kind, int index) {
			this.type = type;
			this.kind = kind;
			this.index = index;
		}	
	
		private String type;
		private Kind kind;
		private int index;

		public String getType() {
			return type;
		}

		public Kind getKind() {
			return kind;
		}

		public int getIndex() {
		   return index;
		}	   
	}
}
