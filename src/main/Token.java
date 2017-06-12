package main;

import main.Constantes.TOKEN_CODIGO;

public class Token extends IToken {
	private TOKEN_CODIGO cod;

	public Token(String lexema, TOKEN_CODIGO cod, int linha) {
		this.lexema = lexema;
		this.cod = cod;
		this.linha = linha;
	}

	@Override
	public String toString() {
		return "<" + this.cod + ",'" + this.lexema + "', Linha: " + this.linha + ">";
	}

}
