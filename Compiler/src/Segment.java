public enum Segment {

	CONST {
		public String toString() {
			return "constant";
		}
	},

	ARGUMENT {
		public String toString() {
			return "argument";
		}
	},

	LOCAL {
		public String toString() {
			return "local";
		}
	},

	STATIC { 
		public String toString() {
			return "static";
		}
	},

	THIS {
		public String toString() {
			return "this";
		}
	},

	THAT {
		public String toString() {
			return "that";
		}
	},

	POINTER {
		public String toString() {
			return "pointer";
		}
	},

	TEMP {
		public String toString() {
			return "temp";
		}
	},
}	
