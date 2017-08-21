package lexico;

import lexico.Constantes.TOKEN_CODIGO;

public class Token extends IToken {
	public TOKEN_CODIGO cod;

	public Token(String lexema, TOKEN_CODIGO cod, int linha) {
		this.lexema = lexema;
		this.cod = cod;
		this.linha = linha;
	}
	
	public TOKEN_CODIGO getCodigo() {
		return this.cod;
	}
	
	@Override
	public String toString() {
		return "<" + this.cod + ",'" + this.lexema + "', Linha: " + this.linha + ">";
	}

}
