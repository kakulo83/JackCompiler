import java.util.HashMap;

public class ASCIITable {	

	public ASCIITable() {
  		ascii_table = new HashMap <String, Integer>(); 

		//	Alphabet Characters 
		ascii_table.put("A",65);		ascii_table.put("a",97);
		ascii_table.put("B",66);		ascii_table.put("b",98);
		ascii_table.put("C",67);		ascii_table.put("c",99);
		ascii_table.put("D",68);		ascii_table.put("d",100);

		ascii_table.put("E",69);		ascii_table.put("e",101);
		ascii_table.put("F",70);		ascii_table.put("f",102);
		ascii_table.put("G",71);		ascii_table.put("g",103);
		ascii_table.put("H",72);		ascii_table.put("h",104);

		ascii_table.put("I",73);		ascii_table.put("i",105);
		ascii_table.put("J",74);		ascii_table.put("j",106);
		ascii_table.put("K",75);		ascii_table.put("k",107);
		ascii_table.put("L",76);		ascii_table.put("l",108);

		ascii_table.put("M",77);		ascii_table.put("m",109);
		ascii_table.put("N",78);		ascii_table.put("n",110);
		ascii_table.put("O",79);		ascii_table.put("o",111);
		ascii_table.put("P",80);		ascii_table.put("p",112);

		ascii_table.put("Q",81); 		ascii_table.put("q",113);
		ascii_table.put("R",82);		ascii_table.put("r",114);
		ascii_table.put("S",83);		ascii_table.put("s",115);
		ascii_table.put("T",84);		ascii_table.put("t",116);

		ascii_table.put("U",85);		ascii_table.put("u",117);
		ascii_table.put("V",86);		ascii_table.put("v",118);
		ascii_table.put("W",87);		ascii_table.put("w",119);
		ascii_table.put("X",88);		ascii_table.put("x",120);
	
		ascii_table.put("Y",89);		ascii_table.put("y",121);
		ascii_table.put("Z",90);		ascii_table.put("z",122);

		//	Punctuation Characters			
		ascii_table.put("!",33);
		ascii_table.put("\"",34);
		ascii_table.put("#",35);
		ascii_table.put("$",36);
		ascii_table.put("%",37);
		ascii_table.put("&",38);
		ascii_table.put("'",39);
		ascii_table.put("(",40);
		ascii_table.put(")",41);
		ascii_table.put("*",42);
		ascii_table.put("+",43);
		ascii_table.put(",",44);
		ascii_table.put("-",45);
		ascii_table.put(".",46);
		ascii_table.put("/",47);
		ascii_table.put(":",58);
		ascii_table.put(";",59);
		ascii_table.put("<",60);
		ascii_table.put("=",61);
		ascii_table.put(">",62);
		ascii_table.put("?",63);
		ascii_table.put("@",64);
		ascii_table.put("[",91);
		ascii_table.put("\\",92);
		ascii_table.put("]",93);
		ascii_table.put("^",94);
		ascii_table.put("_",95);
		ascii_table.put("`",96);
		ascii_table.put("{",123);
		ascii_table.put("¦",124);
		ascii_table.put("}",125);
		ascii_table.put("~",126);
	
		//  Control Characters
		ascii_table.put(" ",32);

		//	Digits 0-9
		ascii_table.put("0",48);
		ascii_table.put("1",49);
		ascii_table.put("2",50);
		ascii_table.put("3",51);
		ascii_table.put("4",52);
		ascii_table.put("5",53);
		ascii_table.put("6",54);
		ascii_table.put("7",55);
		ascii_table.put("8",56);
		ascii_table.put("9",57);
	}

	private HashMap<String, Integer> ascii_table;

	public int getDecimalCode(String character) {
		if (ascii_table.containsKey(character)) {
			return ascii_table.get(character);	
		}
		else {
			return -1;
		}
	}
}
