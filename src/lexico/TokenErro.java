package lexico;

import lexico.Constantes.TOKEN_CODIGO_ERRO;

public class TokenErro extends IToken {
	private TOKEN_CODIGO_ERRO erro;

	public TokenErro(String lexema, TOKEN_CODIGO_ERRO e, int linha) {
		this.lexema = lexema;
		this.erro = e;
		this.linha = linha;
	}

	@Override
	public String toString() {
		return "Erro " + this.erro + " na linha " + this.linha + " no lexema " + this.lexema;
	}


}
