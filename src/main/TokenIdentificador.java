package main;

import main.Constantes.TOKEN_CODIGO;

public class TokenIdentificador extends IToken {
	private Constantes.TOKEN_CODIGO cod;
	private Integer posicaoMemoria;
	
	public TokenIdentificador(String lexema, TOKEN_CODIGO cod, Integer posMemoria, int linha) {
		this.cod = cod;
		this.lexema = lexema;
		this.posicaoMemoria = posMemoria;
		this.linha = linha;
	}
	
	@Override
	public String toString() {
		return "<" + this.cod + ", '" + this.lexema + "', Mem:" + this.posicaoMemoria + ", Linha:" + this.linha +">";
	}

}
