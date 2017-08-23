package sintatico;

import java.util.ArrayList;
import lexico.Constantes.TOKEN_CODIGO;
import semantica.ConstantesSemantica.TIPO_DADOS;
import semantica.Escopo;
import semantica.PilhaEscopo;
import semantica.TabelaSimbolosSemantica;
import lexico.IToken;
import lexico.Lexico;
import lexico.Token;

public class AnalizadorSemantico {
	private IToken tokenAtual;
	private ArrayList<IToken> tokens;
	private int pos;
	private ArrayList<ErroSintatico> erros;
	private int indexEscopo = -1;
	private TabelaSimbolosSemantica tabela = new TabelaSimbolosSemantica();
	
	//-----------------------

	public void consumir(String s) {
		//System.out.println(tokenAtual + " " + s);
		this.pos++;
		if (pos < tokens.size()) {
			tokenAtual = tokens.get(this.pos);
		} else {
			tokenAtual = null;
			//mostrarErros();
		}
	}

	private void erro(String desc, String s) {
		ErroSintatico e = new ErroSintatico(desc, tokenAtual);
		System.out.println(e + "---" + tokenAtual + "---" + s);
		erros.add(e);
		consumir("ERRO");
	}

	private boolean match(String str) {
		if (tokenAtual == null) return false;
		return tokenAtual.match(str);
	}

	//----------------------------------
	
	private String getObjectName(){
		String name = tokenAtual.getLexema();
		int pos_ant = this.pos;
		consumir("");
		while(match(".")){
			consumir("");
			name = tokenAtual.getLexema();
			consumir("");
		}
		this.pos = pos_ant -1;
		consumir("");		
		return name;
	}
	
	private boolean isArray(){
		String name = tokenAtual.getLexema();
		boolean isArray = false;
		int pos_ant = this.pos;
		consumir("");
		while(match(".")){
			consumir("");
			name = tokenAtual.getLexema();
			consumir("");
		}
		if (match("[")){
			consumir("");
			if (match("]")){
				isArray = true;
			}
		}
		this.pos = pos_ant -1;
		consumir("");		
		return isArray;
	}
	
	public void createEscopo(String name){
		Escopo esc = new Escopo();
		esc.name = name;
		this.indexEscopo = PilhaEscopo.insertEscopo(esc);
	}
	
	public void insertTabela(String lexema, TIPO_DADOS type){
		this.tabela.insert(lexema, PilhaEscopo.getLastEscopo(), type);
	}
	
	public void removeEscopo(){
		PilhaEscopo.removeLastEscopo();
	}
	
	//--------------------------------------------

	private void compilationUnit() { //ok
		
		createEscopo("GLOBAL");
//		insertTabela("String", "String"); @obede: tabela de import
		
		
		if (tokenAtual == null) return;
		if (match("package")) {
			consumir("compilationUnit");
			qualifiedIdentifier();
			if (match(";")) {
				consumir("compilationUnit");
			} else {
				erro("Esperado ';'","compilationUnit");
			}
		}
		while (match("import")) {
			consumir("compilationUnit");
//			String name = getObjectName();
//			insertTabela(name, name); @obede: tabela de import
			qualifiedIdentifier();
			if (match(";")) {
				consumir("compilationUnit");
			} else {
				erro("Esperado ';'","compilationUnit");
			}
		}
		while (!match("EOF")) {
			typeDeclaration();
		}

		if (match("EOF")) {
			System.out.println(tabela);
			removeEscopo();
			consumir("compilationUnit");
			return;
		} else {
			erro("Esperado 'EOF'","compilationUnit");
		}
	}

	private void qualifiedIdentifier() { //ok
		if (tokenAtual == null) return;
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			consumir("qualifiedIdentifier");
			while (match(".")) {
				consumir("qualifiedIdentifier");
				if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
					consumir("qualifiedIdentifier");
				} else {
					erro("Esperado um <identificador>","qualifiedIdentifier");
				}
			}
		}
	}

	private void typeDeclaration() { //ok
		if (tokenAtual == null) return;
		modifiers();
		classDeclaration();
	}

	private void modifiers() { //ok
		if (tokenAtual == null) return;
		while (match("public") || match("private") || match("protected") || match("abstract") || match("static")) {
			consumir("modifier");
		}
	}

	private void classDeclaration() { //ok
		if (tokenAtual == null) return;
		if (match("class")) {
			consumir("classDeclaration");
		} else {
			erro("Esperado 'class'","classDeclaration");
		}
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			createEscopo(tokenAtual.getLexema());
			consumir("classDeclaration");
		} else {
			erro("Esperado um identificador","classDeclaration");
		}
		if (match("extends")) {
//			analise semantica? @obede
			consumir("classDeclaration");
			qualifiedIdentifier();
		}
		classBody();
	}

	private void classBody() { //ok
		if (tokenAtual == null) return;
		if (match("{")) {
			consumir("classBody");
		} else {
			erro("Esperado '{'","classBody");
		}
		while (!match("}")) {
			modifiers();
			memberDecl(); //colocalos em lista ?? @obede
		}
		if (match("}")) {
			consumir("classBody");
			removeEscopo();
		} else {
			erro("Esperado '{'","classBody");
		}
	}

	private void memberDecl() { //ok
		if (tokenAtual == null) return;
		boolean constructor = false;
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			String name = tokenAtual.getLexema();
			constructor = true;
			int pos_ant = this.pos;
			consumir("memberDecl");
			if (match("(")){
				if (!name.equals(PilhaEscopo.getLastEscopo().name)){
					System.out.println("Construtor não válido");
				}
				formalParameters();
				block(TIPO_DADOS.NONE);
			} else {
				this.pos = pos_ant - 1;
				consumir("memberDecl_backtracking");
				constructor = false;
			}
		}
		if (match("void") && !constructor) {
			consumir("memberDecl");
			if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) { // void
				consumir("memberDecl");
				formalParameters();
				if (match(";")) {
					consumir("memberDecl");
				} else {
					block(TIPO_DADOS.NONE);
				}
			} else {
				erro("Esperado um <identificador>","memberDecl");
			}
		} else if (!constructor){
			String type = getObjectName();
			boolean isArray = isArray();
			TIPO_DADOS esperada = getType(type, isArray);
			type();
			int pos_ant = this.pos;
			if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {// nvoid
				consumir("memberDecl");
				if (match("(")) {
					formalParameters();
					if (match(";")) {
						consumir("memberDecl");
					} else {
						block(esperada);
					}
				} else {
					this.pos = pos_ant - 1;
					consumir("memberDecl_backtracking"); //field
					variableDeclarators(esperada);
					if (match(";")) {
						consumir("memberDecl");
					} else {
						erro("Esperado ';'","memberDecl");
					}
				}
			} else {
				erro("Esperado um <identificador>","memberDecl");
			}
		}
	}

	private void block(TIPO_DADOS esperado) { //ok
		if (tokenAtual == null) return;
		if (match("{")) {
			consumir("block");
		} else {
			erro("Esperado '{'","block");
		}
		createEscopo("block");
		while (!match("}")) {
			blockStatement(esperado);
		}
		if (match("}")) {
			removeEscopo();
			consumir("block");
		} else {
			erro("Esperado '}'","block");
		}
	}

	private void blockStatement(TIPO_DADOS esperado) { //ok
		if (tokenAtual == null) return;
		if (isLocalVariableDeclaratiomStatement()) {
			localVariableDeclaratiomStatement();
		} else {
			statement(esperado);
		}
	}

	private boolean isLocalVariableDeclaratiomStatement() { //ok
		boolean isLocalVD = false;
		int pos_ant = this.pos;
		if (isBasicType()) {
			isLocalVD = true;
		} else if (match("char") || match("boolean") || match("int")) {
			consumir(""); // .
			if (match("[")) {
				consumir("["); // .
				if (match("]")) {
					isLocalVD = true;
				}
			}
		} else if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			consumir("OR");
			while (match(".")) {
				consumir("OR");
				if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
					consumir("OR");
				}
			}
			if (match("[")) {
				consumir("");
				if (match("]")) {
					isLocalVD = true;
				}
			}

		}
		this.pos = pos_ant - 1;
		consumir("OK");
		return isLocalVD;

	}

	private void statement(TIPO_DADOS esperado) { //ok
		if (tokenAtual == null) return;
		if (match("{")) {
			block(esperado);
		} else if (match("if")) {
			consumir("statement");
			parExpression(TIPO_DADOS.BOOLEAN);
			statement(TIPO_DADOS.NONE);
			if (match("else")) {
				consumir("statement");
				statement(TIPO_DADOS.NONE);
			}
		} else if (match("while")) {
			consumir("statement");
			parExpression(TIPO_DADOS.BOOLEAN);
			statement(TIPO_DADOS.NONE);
		} else if (match("return")) {
			consumir("statement");
			if (!match(";")) {
				if (esperado == TIPO_DADOS.NONE){
					System.out.println("Não é possível retornar nenhum valor" + tokenAtual);
				}
				expression(esperado);
			}
			if (match(";")) {
				consumir("statement");
			} else {
				erro("Esperado ';'","statement");
			}
		} else if (match(";")) {
			consumir("statement");
		} else {
			statementExpression(esperado);
			if (match(";")) {
				consumir("statement");
			}
		}

	}

	private void formalParameters() { //ok
		if (tokenAtual == null) return;
		if (match("(")) {
			consumir("formalParameters");
		} else {
			erro("Esperado '('","formalParameters");
		}
		if (!match(")")) {
			formalParameter();
			while (match(",")) {
				consumir("formalParameters");
				formalParameter();
			}
		}
		if (match(")")) {
			consumir("formalParameters");
		} else {
			erro("Esperado ')'","formalParameters");
		}
	}
	
	public TIPO_DADOS getType(String type, boolean isArray){ //ok
		if (isArray){
			if (type.equals("int")){
				return TIPO_DADOS.INT_ARRAY;
			} else if (type.equals("char")){
				return TIPO_DADOS.CHAR_ARRAY;
			} else if (type.equals("boolean")){
				return TIPO_DADOS.BOOLEAN_ARRAY;
			} else if (type.equals("String")){
				return TIPO_DADOS.STRING_ARRAY;
			} else {
				return TIPO_DADOS.OBJECT_ARRAY;
			}
		} else {
			if (type.equals("int")){
				return TIPO_DADOS.INT;
			} else if (type.equals("char")){
				return TIPO_DADOS.CHAR;
			} else if (type.equals("boolean")){
				return TIPO_DADOS.BOOLEAN;
			} else if (type.equals("String")){
				return TIPO_DADOS.STRING;
			} else {
				return TIPO_DADOS.OBJECT;
			}
		}
	}

	private void formalParameter() { //ok
		if (tokenAtual == null) return;
		String type = getObjectName();
		boolean isArray = isArray();
		TIPO_DADOS esperado = getType(type, isArray);
		type();
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			insertTabela(tokenAtual.getLexema(), esperado);
			consumir("formalParameter");
		} else {
			erro("Esperado <identificador>","formalParameter");
		}
	}

	private void parExpression(TIPO_DADOS esperado) { //ok
		if (tokenAtual == null) return;
		if (match("(")) {
			consumir("parExpression");
			expression(esperado);
		} else {
			erro("Esperado '('","parExpression");
		}
		if (match(")")) {
			consumir("parExpression");
		} else {
			erro("Esperado ')'","parExpression");
		}
	}

	private void localVariableDeclaratiomStatement() { //ok
		if (tokenAtual == null) return;
		String type = getObjectName();
		boolean isArray = isArray();
		TIPO_DADOS esperado = getType(type, isArray);
		type();
		variableDeclarators(esperado);
		if (match(";")) {
			consumir("localVariableDeclaratiomStatement");
		} else {
			erro("Esperado ';'","localVariableDeclaratiomStatement");
		}
	}

	private void variableDeclarators(TIPO_DADOS esperado) { //ok
		if (tokenAtual == null)
			return;
		variableDeclarator(esperado);
		while (match(",")) {
			consumir("variableDeclarators");
			variableDeclarator(esperado);
		}
	}

	private void variableDeclarator(TIPO_DADOS esperado) { //ok
		if (tokenAtual == null)
			return;
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			insertTabela(tokenAtual.getLexema(), esperado);
			consumir("variableDeclarator");
			if (match("=")) {
				consumir("variableDeclarator");
				variableInitializer(esperado);
			}
		} else {
			erro("Esperado <identificador>", "variableDeclarator");
		}
	}

	private void variableInitializer(TIPO_DADOS esperado) { //ok
		if (tokenAtual == null) return;
		if (match("{")) {
			arrayInitializer(esperado);
		} else {
			expression(esperado);
		}
	}

	private void arrayInitializer(TIPO_DADOS esperado) { //ok
		if (tokenAtual == null) return;
		if (match("{")) {
			consumir("arrayInitializer");
			if (!match("}")) {
				variableInitializer(esperado);
				while (match(",")) {
					consumir("arrayInitializer");
					variableInitializer(esperado);
				}
			}
			if (match("}")) {
				consumir("arrayInitializer");
			} else {
				erro("Esperado '}'","arrayInitializer");
			}
		} else {
			erro("Esperado '{'","arrayInitializer");
		}
	}

	private void arguments() { //ok
		if (tokenAtual == null) return;
		if (match("(")) {
			consumir("arguments");
			if (!match(")")) {
				expression(TIPO_DADOS.ANY);
				while (match(",")) {
					consumir("arguments");
					expression(TIPO_DADOS.ANY);
				}
			}
			if (match(")")) {
				consumir("arguments");
			} else {
				erro("Esperado ')'","arguments");
			}
		} else {
			erro("Esperado '('","arguments");
		}
	}

	private void type() { //ok
		if (tokenAtual == null) return;
		if (isBasicType()) {
			basicType();
		} else {
			referenceType();
		}
	}

	private boolean isBasicType() { //ok
		boolean isBasicType = false;
		int pos_ant = this.pos;
		if (match("boolean") || match("char") || match("int")) {
			consumir("isBasicType");
			if (!match("[")) {
				isBasicType = true;
			}
		}
		this.pos = pos_ant - 1;
		consumir("OK");
		return isBasicType;
	}

	private void basicType() { //ok
		if (tokenAtual == null) return;
		if (match("boolean") || match("char") || match("int")) {
			consumir("basicType");
		} else {
			erro("Esperado <basicType>","basicType");
		}
	}

	private void referenceType() { //ok
		if (tokenAtual == null) return;
		if (match("boolean") || match("int") || match("char")) {
			basicType();
			if (match("[")) {
				consumir("referenceType");
				if (match("]")) {
					consumir("referenceType");
				} else {
					erro("Esperado ']'","referenceType");
				}
			} else {
				erro("Esperado '['","referenceType");
			}
			while (match("[")) {
				consumir("referenceType");
				if (match("]")) {
					consumir("referenceType");
				} else {
					erro("Esperado ']'","referenceType");
				}
			}
		} else {
			qualifiedIdentifier();
			while (match("[")) {
				consumir("referenceType");
				if (match("]")) {
					consumir("referenceType");
				} else {
					erro("Esperado ']'","referenceType");
				}
			}
		}
	}

	private void statementExpression(TIPO_DADOS esperado) { // ok
		if (tokenAtual == null)
			return;
		expression(esperado);
	}

	private void expression(TIPO_DADOS esperado) { //ok
		if (tokenAtual == null)
			return;
		assignmentExpression(esperado);
	}

	private void assignmentExpression(TIPO_DADOS esperado) { //ok 
		if (tokenAtual == null)
			return;
		conditionalAndExpression(esperado);
		if (match("=")) {
			consumir("assignmentExpression");
			assignmentExpression(esperado);
		} else if (match("+=")) {
			if (TIPO_DADOS.INT == esperado) {
				System.out.println("Não é possível operações +=." + tokenAtual);
			}
			consumir("assignmentExpression");
			assignmentExpression(TIPO_DADOS.INT);
		}
	}

	private void conditionalAndExpression(TIPO_DADOS esperado) { // ok
		if (tokenAtual == null)
			return;
		equalityExpression(esperado);
		while (match("&&")) {
			if (TIPO_DADOS.BOOLEAN == esperado) {
				System.out.println("Não é possível operações &&." + tokenAtual);
			}
			consumir("conditionalAndExpression");
			equalityExpression(TIPO_DADOS.BOOLEAN);
		}
	}

	private void equalityExpression(TIPO_DADOS esperado) { // ok
		if (tokenAtual == null)
			return;
		relationalExpression(esperado);
		while (match("==")) {
			if (TIPO_DADOS.BOOLEAN == esperado) {
				System.out.println("Não é possível ==." + tokenAtual);
			}
			consumir("equalityExpression");
			relationalExpression(TIPO_DADOS.BOOLEAN);
		}
	}

	private void relationalExpression(TIPO_DADOS esperado) { // ok
		if (tokenAtual == null)
			return;
		additiveExpression(esperado);
		if (match(">") || match("<=")) {
			if (TIPO_DADOS.BOOLEAN == esperado) {
				System.out.println("Não é possível operações > ou <=." + tokenAtual);
			}
			consumir("relationalExpression");
			additiveExpression(TIPO_DADOS.INT);
		} else if (match("instanceof")) {
			if (TIPO_DADOS.BOOLEAN == esperado) {
				System.out.println("Não é possível operação instanceof" + tokenAtual);
			}
			consumir("relationalExpression");
			referenceType();
		}
	}

	private void additiveExpression(TIPO_DADOS esperado) { // ok
		if (tokenAtual == null)
			return;
		multiplicativeExpression(esperado);
		while (match("+") || match("-")) {
			if (esperado != TIPO_DADOS.INT) {
				System.out.println("Não é possível somar/subtrair não inteiros" + tokenAtual);
			}
			consumir("equalityExpression");
			multiplicativeExpression(TIPO_DADOS.INT);
		}
	}

	private void multiplicativeExpression(TIPO_DADOS esperado) { //ok
		if (tokenAtual == null)
			return;
		unaryExpression(esperado);
		while (match("*")) {
			if (esperado == TIPO_DADOS.INT) {
				System.out.println("Não é possível multiplicar não inteiros" + tokenAtual);
			}
			consumir("multiplicativeExpression");
			unaryExpression(TIPO_DADOS.INT);
		}
	}

	private void unaryExpression(TIPO_DADOS esperado) { // ok
		if (tokenAtual == null)
			return;
		if (match("++")) {
			consumir("unaryExpression");
			unaryExpression(TIPO_DADOS.INT);
		} else if (match("-")) {
			consumir("unaryExpression");
			unaryExpression(TIPO_DADOS.INT);
		} else {
			simpleUnaryExpression(esperado);
		}
	}
	
	public boolean isCast() {
		int pos_ant = this.pos;
		boolean isCast = false;
		consumir("("); // (
		if (isBasicType()) {
			consumir(""); // basicType
			if (match(")")) {
				isCast = true;
			}
		} else if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			consumir(""); // .
			while (match(".")) {
				consumir("."); // .
				consumir("<id>");
			}
			while (match("[")) {
				consumir("["); // .
				consumir("]");
			}
			if (match(")")) {
				isCast = true;
			}
		} else if (match("char") || match("boolean") || match("int")) {
			consumir(""); // .
			if (match("[")) {
				consumir("["); // .
				consumir("]");
				while (match("[")) {
					consumir("["); // .
					consumir("]");
				}
				if (match(")")) {
					isCast = true;
				}
			}
		}
		this.pos = pos_ant - 1;
		consumir("isCast");
		return isCast;
	}

	private void simpleUnaryExpression(TIPO_DADOS esperado) { // ok
		if (tokenAtual == null)
			return;
		if (match("!")) {
			consumir("simpleUnaryExpression");
			unaryExpression(TIPO_DADOS.ANY);
		} else if (match("(") && isCast()) {
			consumir("simpleUnaryExpression");
			if (isBasicType()) {
				String type = getObjectName();
				TIPO_DADOS create = getType(type, false);
				if (create != esperado) {
					System.out.println("Cast não premitido" + tokenAtual);
				}
				basicType();
				consumir("simpleUnaryExpression");
				if (match(")")) {
					consumir("simpleUnaryExpression");
					unaryExpression(esperado);
				} else {
					erro("Esperado ')'", "simpleUnaryExpression");
				}
			} else {
				String type = getObjectName();
				boolean isArray = isArray();
				TIPO_DADOS create = getType(type, isArray);
				if (create != esperado) {
					System.out.println("Cast não premitido" + tokenAtual);
				}
				referenceType();
				if (match(")")) {
					consumir("simpleUnaryExpression");
					simpleUnaryExpression(esperado);
				} else {
					erro("Esperado ')'", "simpleUnaryExpression");
				}
			}
		} else {
			postfixExpression(esperado);
		}
	}

	private void postfixExpression(TIPO_DADOS esperado) {
		if (tokenAtual == null) return;
		primary(esperado);
		while (match(".") || match("[")) { //é array ou objeto
			selector();
		}
		while (match("--")) {
			if (esperado != TIPO_DADOS.INT){
				System.out.println("Operação não permitida --" + tokenAtual);
			}
			consumir("postifixExpression");
		}
	}

	private void selector() { //ok
		if (tokenAtual == null)
			return;
		if (match(".")) {
			consumir("selector");
			qualifiedIdentifier();
			if (match("(")) {
				arguments();
			}
		} else if (match("[")) {
			consumir("selector");
			expression(TIPO_DADOS.INT);
			if (match("]")) {
				consumir("selector");
			} else {
				erro("Esperado ']'", "selector");
			}
		} else {
			erro("Esperado '.' ou '['", "selector");
		}
	}

	private void primary(TIPO_DADOS esperado) {
		if (tokenAtual == null)
			return;
		if (match("(")) {
			parExpression(esperado);
		} else if (match("this")) {
			consumir("primary");
			if (match("(")) {
				arguments();
			}
		} else if (match("super")) {
			consumir("primary");
			if (match("(")) {
				arguments();
			} else if (match(".")) {
				consumir("primary");
				if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) { // verificar se tem no super @obede
					consumir("primary");
				} else {
					erro("Esperado <identificador>", "primary");
				}
				if (match("(")) {
					arguments();
				}
			} else {
				erro("Esperado '(' ou '.'", "primary");
			}
		} else if (match("new")) {
			consumir("primary");
			creator(esperado);

		} else if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			String name = getObjectName();
			System.out.println(tabela.buscar(name));
			qualifiedIdentifier();
			if (match("(")) {
				arguments();
			}
		} else {
			literal(esperado);
		}
	}

	private void creator(TIPO_DADOS esperado) {// ok
		if (tokenAtual == null)
			return;
		String type = getObjectName();
		boolean isArray = isArray();
		TIPO_DADOS typeCreate = getType(type, isArray);
		if (typeCreate != esperado) {
			System.out.println("Tipo inesperado" + tokenAtual);
		}
		if (match("boolean") || match("int") || match("char")) {
			basicType();
		} else {
			qualifiedIdentifier();
		}
		if (match("(")) {
			arguments();
		} else if (match("[")) {
			int pos_ant = this.pos;
			consumir("creator");
			if (match("]")) {
				consumir("creator");
				while (match("[")) {
					consumir("creator");
					if (match("]")) {
						consumir("creator");
					} else {
						erro("Esperado ']'", "creator");
					}
				}
				if (match("{")) {
					arrayInitializer(esperado);
				}
			} else {
				this.pos = pos_ant - 1;
				consumir("creator");
				newArrayDeclarator();
			}
		}
	}

	private void newArrayDeclarator() { // ok
		if (tokenAtual == null)
			return;
		if (match("[")) {
			consumir("newArrayDeclarator");
			expression(TIPO_DADOS.INT);
		} else {
			erro("Esperado '['", "newArrayDeclarator");
		}
		if (match("]")) {
			consumir("newArrayDeclarator");
		} else {
			erro("Esperado ']'", "newArrayDeclarator");
		}
		while (match("[")) {
			consumir("newArrayDeclarator");
			expression(TIPO_DADOS.INT);
			if (match("]")) {
				consumir("newArrayDeclarator");
			} else {
				erro("Esperado ']'", "newArrayDeclarator");
			}
		}
		while (match("[")) {
			consumir("newArrayDeclarator");
			if (match("]")) {
				consumir("newArrayDeclarator");
			} else {
				erro("Esperado ']'", "newArrayDeclarator");
			}
		}
	}

	private void literal(TIPO_DADOS esperado) { //ok
		if (tokenAtual == null)
			return;
		if (((Token) tokenAtual).cod == TOKEN_CODIGO.INT_LITERAL) {
			if (TIPO_DADOS.ANY != esperado && TIPO_DADOS.INT != esperado) {
				System.out.println("Esperado int" + tokenAtual);
			}
			consumir("literal");
		} else if (((Token) tokenAtual).cod == TOKEN_CODIGO.CHAR_LITERAL) {
			if (TIPO_DADOS.ANY != esperado && TIPO_DADOS.CHAR != esperado) {
				System.out.println("Esperado char" + tokenAtual);
			}
			consumir("literal");
		} else if (((Token) tokenAtual).cod == TOKEN_CODIGO.STRING_LITERAL) {
			if (TIPO_DADOS.ANY != esperado && TIPO_DADOS.STRING != esperado) {
				System.out.println("Esperado String" + tokenAtual);
			}
			consumir("literal");
		} else if (match("true")) {
			if (TIPO_DADOS.ANY != esperado && TIPO_DADOS.BOOLEAN != esperado) {
				System.out.println("Esperado boolean" + tokenAtual);
			}
			consumir("literal");
		} else if (match("false")) {
			if (TIPO_DADOS.ANY != esperado && TIPO_DADOS.BOOLEAN != esperado) {
				System.out.println("Esperado boolean" + tokenAtual);
			}
			consumir("literal");
		} else if (match("null")) {
			if (TIPO_DADOS.ANY != esperado && TIPO_DADOS.NULL != esperado) {
				System.out.println("Esperado null" + tokenAtual);
			}
			consumir("literal");
		} else {
			erro("Esperado <literal>", "literal");
			return;
		}
	}

	
	//------------------------------------------------------
	
	
	public static void main(String[] args) {
		AnalizadorSemantico as = new AnalizadorSemantico("entradas/SyntaxErrors.txt");
		//AnalizadorSintatico as = new AnalizadorSintatico("entradas/entrada_certa.txt");
		as.analizar();
		//as.mostrarErros();
	}
	
	public void analizar() {
		compilationUnit();
	}
	
	private void mostrarErros(){
		if (erros.size() == 0){
			System.out.println("\n\n\n******* Não há erros *************");
		}
		for (ErroSintatico e : this.erros){
			System.out.println(e);
		}
		System.exit(0);
	}

	public AnalizadorSemantico(String entrada) {
		tokens = new Lexico(entrada).getAllTokens();
		this.pos = 0;
		tokenAtual = tokens.get(0);
		erros = new ArrayList<>();
//		for (IToken t : tokens){
//			System.out.println(t);
//		}
	}
	
	public String toString(){ //deletar
		return tokenAtual.getLine() + "";
	}
}
