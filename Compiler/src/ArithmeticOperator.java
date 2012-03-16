public enum ArithmeticOperator {
	ADD {
		public String toString() {
			return "add";
		}
	},
	
	SUB {
		public String toString() {
			return "sub";
		}
	},

	NEG {
		public String toString() {
			return "neg";
		}
	},

	EQ {
		public String toString() {
			return "eq";
		}
	},

	GT { 
		public String toString() {
			return "gt";
		}
	},

	LT {
		public String toString() {
			return "lt";
		}
	},

	AND {
		public String toString() {
			return "and";
		}
	},

	OR {
		public String toString() {
			return "or";
		}
	},

	NOT {
		public String toString() {
			return "not";
		}
	},

	MULT {
		public String toString() {
			return "mult";
		}
	},

	DIVIDE {
		public String toString() {
			return "divide";
		}
	},
}	 
