package sintatico;

import java.util.ArrayList;

import lexico.Constantes;
import lexico.Constantes.TOKEN_CODIGO;
import lexico.IToken;
import lexico.Lexico;

public class AnalizadorSintatico {
	private IToken tokenAtual;
	private ArrayList<IToken> tokens;
	private int pos;

	public void consumir(String s) {
		System.out.println(tokenAtual + "AAAAAAAA " + s);
		this.pos++;
		tokenAtual = tokens.get(this.pos);

	}

	public AnalizadorSintatico(String entrada) {
		tokens = new Lexico(entrada).getAllTokens();
		Constantes.getInstance();
		this.pos = -1;
		// IToken t = lexico.getNextToken();
		// while (t != null) {
		// System.out.println(t);
		// t = lexico.getNextToken();
		// }
		// for(IToken token:tokens) {
		// System.out.println(token);
		// }
	}

	private boolean match(String str) {
		return tokenAtual.match(str);
	}

	public void analizar() {

		compilationUnit();
	}

	private void compilationUnit() {
		consumir("compilationUnit");
		if (match("package")) {
			qualifiedIdentifier();
		}
		while (match("import")) {
			qualifiedIdentifier();
		}
		// type declaration
		modifier();
		classDeclaration();

		// EOF
		if (match("EOF")) {
			return;
		}
	}

	private void qualifiedIdentifier() {

		consumir("qualifiedIdentifier");

		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			consumir("qualifiedIdentifier");
			if (match(".")) {
				qualifiedIdentifier();
			} else if (match(";")) {
				consumir("qualifiedIdentifier");
			}
		}
	}

	private void modifier() {
		if (!match("public") && !match("private") && !match("protected") && !match("abstract") && !match("static")) {
		} else {
			consumir("modifier");
			modifier();
		}
	}

	private void classDeclaration() {

		if (match("class")) {
			consumir("classDeclaration");
		} else {
			// no class found error;
		}
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			consumir("classDeclaration");
		} else {
			// erro, expected a identifier
		}
		if (match("extends")) {
			qualifiedIdentifier();
		}
		classBody();

	}

	private void classBody() {

		if (match("{")) {
			consumir("classBody");
		}
		while (!match("}")) {
			modifier();
			memberDecl();
		}
		if (match("}")) {
			consumir("classBody");
		}
	}

	private void memberDecl() {

		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) { // construtor
			consumir("memberDecl");
			formalParameters();
			block();
		} else if (match("void")) {
			consumir("memberDecl");
			if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) { // função void
				consumir("memberDecl");
				formalParameters();
				if (match(";")) {
					consumir("memberDecl");
				} else {
					block();
				}
			}
		} else {
			type();
			int pos = this.pos;
			if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) { // função void
				consumir("memberDecl");
				if (match("(")) {
					formalParameters();
					if (match(";")) {
						consumir("memberDecl");
					} else {
						block();
					}
				} else {
					this.pos = pos - 1;
					consumir("memberDecl");
					variableDeclarators();
				}
			}
		}
	}

	private void block() {

		if (match("{"))
			consumir("block");
		while (!match("}")) {
			blockStatement();
		}
		if (match("}"))
			consumir("block");
	}

	private void blockStatement() {
		consumir("blockStatement");
	}

	private void statement() {

	}

	private void formalParameters() {

		if (match("("))
			consumir("formalParameters");
		if (!match(")")) {
			formalParameter();
		}
		if (match(")")) {
			consumir("formalParameters");
		}
	}

	private void formalParameter() {
		type();
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			consumir("formalParameter");
		}
		if (match(",")) {
			consumir("formalParameter");
			formalParameter();
		}
	}

	private void parExpression() {
		match("(");
		expression();
		match(")");
	}

	private void localVariableDeclaratiomStatement() {
		type();
		variableDeclarators();
	}

	private void variableDeclarators() {
		variableDeclarator();
		if (match(",")) {
			consumir("variableDeclarators");
			variableDeclarator();
		}
	}

	private void variableDeclarator() {
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			consumir("variableDeclarator");
			if (match("=")) {
				variableInitializer();
			}
		}
	}

	private void variableInitializer() {// obede

	}

	private void arrayInitializer() {// otavio
		if (match("{")) {
			do {
				consumir("arrayInitializer");
				variableInitializer();
				consumir("arrayInitializer"); // consome a ","
			} while (!match("}"));
		}

	}

	private void arguments() {// obede

	}

	private void type() {// otavio
		if (match("boolean") || match("int") || match("char")) {
			consumir("type");
		} else {
			referenceType();
		}
	}

	@Deprecated
	private void basicType() {// obede

	}

	private void referenceType() {// otavio
		if (match("boolean") || match("int") || match("char")) {
			consumir("referenceType");
			do {
				consumir("referenceType");
				consumir("referenceType");
			} while (match("]"));
		} else {
			qualifiedIdentifier();
			while (match("]")) {
				consumir("referenceType");
				consumir("referenceType");
			}
		}
	}

	private void statementExpression() {// obede

	}

	private void expression() {// otavio
		assignmentExpression();
	}

	private void assignmentExpression() {// obede

	}

	private void conditionalAndExpression() {// otavio
		equalityExpression();
	}

	private void equalityExpression() {// obede

	}

	private void relationalExpression() {// otavio
		additiveExpression();
	}

	private void additiveExpression() {// obede

	}

	private void multiplicativeExpression() {// otavio
		unaryExpression();
	}

	private void unaryExpression() {// obede

	}

	private void simpleUnaryExpression() {// otavio
		if (match("!")) {
			unaryExpression();
		} else if (match("(")) {
			consumir("simpleUnaryExpression");
			if (match("boolean") || match("int") || match("char")) {
				consumir("simpleUnaryExpression");
				if (match(")")) {
					unaryExpression();
				}
			} else {
				consumir("simpleUnaryExpression");
				referenceType();
				if (match(")")) {
					simpleUnaryExpression();
				}
			}

		} else if (match("(")) {

		} else {
			postfixExpression();
		}
	}

	private void postfixExpression() {// obede

	}

	private void selector() {// otavio
		if (match(".")) {
			qualifiedIdentifier();
			arguments();
		} else if (match("[")) {
			consumir("selector");
			expression();
			if (match("]")) {
				consumir("selector");
			}
		}
	}

	private void primary() {// obede

	}

	private void creator() {// otavio
		if (match("(")) {
			if (match("boolean") || match("int") || match("char")) {
				consumir("creator");
			} else {
				qualifiedIdentifier();
			}
			if (match(")")) {
				consumir("creator");
				if (match("(")) {
					consumir("creator");
					if (match("(")) {
						arguments();
					} else if (match("[")) {
						consumir("creator");
						if (match("]")) {
							consumir("creator");
							while (match("[")) {
								consumir("creator");
								consumir("creator");
							}
							arrayInitializer();
						} else {
							newArrayDeclarator();
						}
					}
					if (match(")")) {
						consumir("creator");
					}
				}
			}
		}
	}

	private void newArrayDeclarator() {// obede

	}

	private void literal() {// otavio
		if(tokenAtual.tokenTipo() == TOKEN_CODIGO.INT_LITERAL) {
			consumir("literal");
		}else if(tokenAtual.tokenTipo() == TOKEN_CODIGO.CHAR_LITERAL) {
			consumir("literal");
		}else if(tokenAtual.tokenTipo() == TOKEN_CODIGO.STRING_LITERAL) {
			consumir("literal");
		}else if(match("true")) {
			consumir("literal");
		}else if(match("false")) {
			consumir("literal");
		}else if(match("null")) {
			consumir("literal");
		}
	}

	public static void main(String[] args) {
		AnalizadorSintatico as = new AnalizadorSintatico("entradas/entrada_errada_Durelli");
		as.analizar();
	}
}
