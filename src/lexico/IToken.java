package lexico;

import lexico.Constantes.TOKEN_CODIGO;

abstract public class IToken {
	protected String lexema;
	protected int linha;
	public String getLexema() {
		return this.lexema;
	}
	
	public boolean match(String lex) {
		return this.getLexema().equals(lex);
	}
	
	public int getLine(){
		return linha;
	}
	/**
	 * Descobr o tipo de um token
	 * @return
	 */
	public TOKEN_CODIGO tokenTipo() {
		if(this instanceof TokenIdentificador) {
			return TOKEN_CODIGO.IDENTIFICADOR;
		}else if(this instanceof TokenErro) {
			return TOKEN_CODIGO.ERRO;
		}else if(this instanceof Token) {
			if(((Token)this).getCodigo() == TOKEN_CODIGO.EOF) {
				return TOKEN_CODIGO.EOF;
			}
			return ((Token)this).cod;
		}
		return null;
	}
	
}
