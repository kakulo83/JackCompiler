public enum Kind {

	STATIC {
		public String toString() {
			return "static";
		}
	},

	FIELD {
		public String toString() {
			return "field";		
		}
	},

	ARG {
		public String toString() {
			return "arg";
		}
	},

	VAR {
		public String toString() {
			return "var";
		}
	},

	NONE {
		public String toString() {
			return "none";
		}
	},
}
