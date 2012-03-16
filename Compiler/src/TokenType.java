public enum TokenType {
	KEYWORD {
		public String toString() {
			return "Keyword";
		}
	},
	
	SYMBOL {
		public String toString() {
			return "Symbol";
		}
	},
	
	IDENTIFIER {
		public String toString() {
			return "Identifier";
		}
	},
	
	INT_CONST {
		public String toString() {
			return "Int_const";
		}
	},
	
	STRING_CONST {
		public String toString() {
			return "String_const";
		}
	},
}
