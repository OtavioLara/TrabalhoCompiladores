package lexico;

import java.util.ArrayList;

public class Constantes {

	public static final ArrayList<String> reserved_word = new ArrayList<>();
	private Constantes(){
		init();
	}
	public static Constantes getInstance() {
        return constantesHolder.INSTANCE;
    }
	public enum MODFIERS {
		PUBLIC, PRIVATE, PROTECTED, STATIC, ABSTRACT 
	}
	public enum TOKEN_CODIGO {
		RESERVED_WORD, OPERATOR, SEPARATOR, IDENTIFICADOR, INT_LITERAL, CHAR_LITERAL, STRING_LITERAL, ERRO, EOF
	}
	
	public enum TOKEN_CODIGO_ERRO {
		OPERADOR_INDEFINIDO, STRING_INDEFINIDA, SIMBOLO_INDEFINIDO, INDEFINIDO
	}

	private void init() {
		reserved_word.add("abstract");
		reserved_word.add("boolean");
		reserved_word.add("char");
		reserved_word.add("class");
		reserved_word.add("else");
		reserved_word.add("extends");
		reserved_word.add("false");
		reserved_word.add("import");
		reserved_word.add("if");
		reserved_word.add("instanceof");
		reserved_word.add("int");
		reserved_word.add("new");
		reserved_word.add("null");
		reserved_word.add("package");
		reserved_word.add("private");
		reserved_word.add("protected");
		reserved_word.add("public");
		reserved_word.add("return");
		reserved_word.add("static");
		reserved_word.add("super");
		reserved_word.add("this");
		reserved_word.add("true");
		reserved_word.add("void");
		reserved_word.add("while");
	}
	private static class constantesHolder {
        private static final Constantes INSTANCE = new Constantes();
    }
}
