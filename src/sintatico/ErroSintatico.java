package sintatico;

import lexico.IToken;

public class ErroSintatico {
	
	private String desc;
	private IToken token;
	
	public ErroSintatico(String desc, IToken token) {
		this.desc = desc;
		this.token = token;
	}
	
	@Override
	public String toString() {
		return "Erro Line " + token.getLine() + " " + desc;
	}

}
