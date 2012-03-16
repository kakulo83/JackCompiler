public enum CommandType {
	
	C_ARITHMETIC {
		public String toString() {
			return "Arithmetic";
		}
	},
	
	C_PUSH {
		public String toString() {
			return "Push";
		}
	},
	
	C_POP {
		public String toString() {
			return "Pop";
		}
	},
	
	C_LABEL {
		public String toString() {
			return "Label";
		}
	},
	
	C_GOTO {
		public String toString() {
			return "Goto";
		}
	},
	
	C_IF {
		public String toString() {
			return "If";
		}
	},
	
	C_FUNCTION {
		public String toString() {
			return "Function";
		}
	},
	
	C_RETURN {
		public String toString() {
			return "Return";
		}
	},
	
	C_CALL {
		public String toString() {
			return "Call";
		}
	},

	C_IGNORE {
		public String toString() {
			return "Ignore";
		}
	},
}
