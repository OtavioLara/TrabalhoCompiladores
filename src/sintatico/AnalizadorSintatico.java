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
//		for(IToken token:tokens) {
//			System.out.println(token);
//		}
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
					this.pos = pos-1;
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
		if (match(")")){
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
	
	private void variableInitializer() {//obede
		
	}
	
	private void arrayInitializer() {//otavio
		
	}
	
	private void arguments() {//obede
		if (match("(")){
			consumir("arguments");
			expression();
			while(match(",")){
				consumir("arguments");
				expression();
			}
			if(match(")")){
				consumir("arguments");
			}
		}
	}
	
	private void type() {//otavio
		consumir("type");
	}
	
	private void basicType() {//obede
		if (match("boolean") || match("char") || match("int")){
			consumir("basicType");
		} else {
			//erro
		}
	}
	
	private void referenceType() {//otavio
		
	}
	
	private void statementExpression() {//obede
		expression();
	}
	
	private void expression() {//otavio
		
	}
	
	private void assignmentExpression() {//obede
		conditionalAndExpression();
		if (match("=") || match("+=")){
			consumir("assignmentExpression");
			assignmentExpression();
		}
	}
	
	private void conditionalAndExpression() {//otavio
		
	}
	
	private void equalityExpression() {//obede
		relationalExpression();
		while (match("==")){
			consumir("equalityExpression");
			relationalExpression();
		}
	}
	
	private void relationalExpression(){//otavio
		
	}
	
	private void additiveExpression() {//obede
		multiplicativeExpression();
		while (match("+") || match("-")){
			consumir("equalityExpression");
			relationalExpression();
		}
	}
	
	private void multiplicativeExpression() {//otavio
		
	}
	
	private void unaryExpression() {//obede
		if (match("++")){
			consumir("unaryExpression");
			unaryExpression();
		}
		if (match("-")){
			consumir("unaryExpression");
			unaryExpression();
		} else {
			simpleUnaryExpression();
		}
	}
	
	private void simpleUnaryExpression() {//otavio
		
	}
	
	private void postfixExpression() {//obede
		primary();
		//selector
		//--
	}
	
	private void selector() {//otavio
		
	}
	
	private void primary() {//obede
		//this
		//super
		literal();
		creator();
		qualifiedIdentifier();
	}
	
	private void creator() {//otavio
		
	}
	
	private void newArrayDeclarator() {//obede
		match("[");
		consumir("newArrayDeclarator");
		expression();
		match("]");
		consumir("newArrayDeclarator");
		if (match("[")){
			consumir("newArrayDeclarator");
			if (match("]")){
				consumir("newArrayDeclarator");
			} else {
				expression();
				if (match("]")){
					consumir("newArrayDeclarator");
				}
			}
		}
	}
	
	private void literal() {//otavio
		
	}

	public static void main(String[] args) {
		AnalizadorSintatico as = new AnalizadorSintatico("entradas/entrada_errada_Durelli");
		as.analizar();
	}
}
