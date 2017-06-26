grammar Typhon;

root: tnDecls+=decl* EOF;

decl:
	tnDoc=DOC_COMMENT? tnAnnots+=annotation* tnRetType=types tnName=WORD tnFunc=anonFunc																													#methodDecl
|	tnDoc=DOC_COMMENT? tnAnnots+=annotation* tnRetType=types tnName=WORD tnTemplate=templateDecls? '(' tnArgs=paramsDecl ')' ';'																			#methodStubDecl
|	tnDoc=DOC_COMMENT? tnAnnots+=annotation* tnType=type tnNames+=WORD (',' tnNames+=WORD)* ('=' tnValues+=expr (',' tnValues+=expr)*)? ';'																	#fieldDecl
|	tnAnnots+=annotation* tnBlock=block																																										#staticInitDecl
|	tnDoc=DOC_COMMENT? tnAnnots+=annotation* 'class' tnName=WORD tnTemplate=templateDecls? (':' tnExtends+=type (',' tnExtends+=type)*)? '{' tnDecls+=decl* '}'												#classDecl
|	tnDoc=DOC_COMMENT? tnAnnots+=annotation* 'enum' tnName=WORD (':' tnExtends+=type (',' tnExtends+=type)*)? '{' (tnValues+=enumValueDecl (',' tnValues+=enumValueDecl)* ','?)? ';'? tnDecls+=decl* '}'	#enumDecl
|	tnDoc=DOC_COMMENT? tnAnnots+=annotation* 'package' tnName=packageName ';'																																#simplePackageDecl
|	tnDoc=DOC_COMMENT? tnAnnots+=annotation* 'package' tnName=packageName '{' tnDecls+=decl* '}'																											#packageDecl
|	tnAnnots+=annotation* 'import' tnName=packageName ('as' tnAlias=packageName)? ';'																														#importDecl
|	tnDoc=DOC_COMMENT? tnAnnots+=annotation* 'new' '(' (tnArgs+=constructorParam (',' tnArgs+=constructorParam)*)? ')' ('=>' tnExprForm=exprs | tnBlockForm=block)											#constructorDecl
|	tnAnnots+=annotation* tnGlobalAnnot=globalAnnotation																																					#globalAnnotDecl
;
enumValueDecl: tnDoc=DOC_COMMENT? tnAnnots+=annotation* tnName=WORD ('(' tnArgs=argsDecl ')')?;
constructorParam:
	tnAnnots+=annotation* 'this' '.' tnName=WORD	#constructorParamThis
|	tnAnnots+=annotation* tnType=type tnName=WORD	#constructorParamTyped
;

type:
	tnAnnots+=annotation* 'typeof' tnExpr=expr																					#typeofType
|	tnAnnots+=annotation* tnTemplate=templateDecls? '(' (tnArgTypes+=type (',' tnArgTypes+=type)*)? ')' '->' tnRetType=types	#funcType
|	tnAnnots+=annotation* '[' tnBaseType=type ']'																				#arrayType
|	tnAnnots+=annotation* '{' tnKeyType=type ':' tnValueType=type '}'															#mapType
|	tnAnnots+=annotation* 'void'																								#voidType
|	tnAnnots+=annotation* 'var'																									#varType
|	tnAnnots+=annotation* 'const' tnType=type																					#constType
|	tnAnnots+=annotation* tnName=WORD tnTemplate=templateInst?																	#basicType
;
types: tnTypes+=type | '(' (tnTypes+=type (',' tnTypes+=type)*)? ')';

expr:
	tnLhs=expr (tnOp='.'|tnOp='?.') tnRhs=expr																		#memberExpr
|	tnCallee=expr tnTemplate=templateInst? '(' tnArgs=argsDecl ')'													#funcCallExpr
|	tnCallee=expr tnTemplate=templateInst? '[' tnArgs=argsDecl ']'													#indexCallExpr
|	tnLhs=expr 'as' tnRhs=type																						#castExpr
|	'new' tnType=type '(' tnArgs=argsDecl ')' ('{' tnDecls+=decl* '}')?												#newExpr
|	(tnOp='-'|tnOp='+'|tnOp='!'|tnOp='~') tnArg=expr																#unOpsExpr
|	tnLhs=expr (tnOp='*'|tnOp='/'|tnOp='%') tnRhs=expr																#binOps1Expr
|	tnLhs=expr (tnOp='+'|tnOp='-') tnRhs=expr																		#binOps2Expr
|	tnLhs=expr (tnOp='&'|tnOp='|'|tnOp='^'|tnOp='<<'|tnOp='>>') tnRhs=expr											#bitOpsExpr
|	tnLhs=expr '??' tnRhs=expr																						#nullCoalesceExpr
|	tnLhs=expr (tnOp='<'|tnOp='>'|tnOp='<='|tnOp='>=') tnRhs=expr													#relOpsExpr
|	tnLhs=expr 'is' tnRhs=type																						#isExpr
|	tnLhs=expr (tnOp='=='|tnOp='!=') tnRhs=expr																		#eqOpsExpr
|	tnLhs=expr (tnOp='&&'|tnOp='||') tnRhs=expr																		#logicOpsExpr
|	tnAnnots+=annotation* 'throw' tnArg=expr																		#throwExpr
|	tnIf=expr '?' tnThen=expr ':' tnElse=expr																		#terneryOpExpr
|	tnAnnots+=annotation* 'type' tnType=type																		#typeConstExpr
|	tnAnnots+=annotation* 'null'																					#nullConstExpr
|	tnAnnots+=annotation* 'true'																					#trueConstExpr
|	tnAnnots+=annotation* 'false'																					#falseConstExpr
|	tnAnnots+=annotation* 'this'																					#thisConstExpr
|	tnAnnots+=annotation* tnValue=WORD																				#varExpr
|	tnAnnots+=annotation* tnValue=NUMBER																			#numConstExpr
|	tnAnnots+=annotation* tnValue=STRING																			#stringConstExpr
|	tnAnnots+=annotation* tnValue=CHAR																				#charConstExpr
|	tnAnnots+=annotation* tnFunc=anonFunc																			#funcConstExpr
|	tnAnnots+=annotation* '[' (tnValues+=expr (',' tnValues+=expr)*)? ','? ']'										#arrayConstExpr
|	tnAnnots+=annotation* '{' (tnKeys+=expr '=' tnValues+=expr (',' tnKeys+=expr '=' tnValues+=expr)*)? ','? '}'	#mapConstExpr
|	tnAnnots+=annotation* '(' tnExpr=expr ')'																		#parensExpr
;
exprs: tnExprs+=expr | '(' (tnExprs+=expr (',' tnExprs+=expr)*)? ')';

stat:
	'return' (tnValues+=expr (',' tnValues+=expr)*)? ';'																					#retStat
|	tnDoc=DOC_COMMENT? tnAnnots+=annotation* tnType=type tnNames+=WORD (',' tnNames+=WORD)* ('=' tnValues+=expr (',' tnValues+=expr)*)? ';'	#defStat
|	tnLvals+=assignLvalue (',' tnLvals+=assignLvalue)* ('=' tnValues+=expr (',' tnValues+=expr)*)? ';'										#assignStat
|	tnLvals+=assignLvalue (',' tnLvals+=assignLvalue)* (tnOp='+='|tnOp='-='|tnOp='*='|tnOp='/='|tnOp='%=') tnRval=expr ';'					#comboAssignStat
|	'if' tnIfExpr=expr tnIfBlock=block	('elseif' tnElseifExprs+=expr tnElseifBlocks+=block)* ('else' tnElseBlock=block)?					#ifStat
|	'for' tnLvals+=forLvalue (',' tnLvals+=forLvalue)* ':' tnExpr=expr tnBlock=block														#forStat
|	'while' tnExpr=expr tnBlock=block																										#whileStat
|	'repeat' tnBlock=block 'until' tnExpr=expr ';'																							#repeatStat
|	'try' tnTryBlock=block tnCatchBlocks+=catchBlock*																						#tryStat
|	'break' tnLabel=WORD? ';'																												#breakStat
|	'continue' tnLabel=WORD? ';'																											#contStat
|	'switch' tnExpr=expr ('<' tnLabel=WORD '>')? '{' ('case' tnCaseBlocks+=caseBlock)* ('default' tnDefaultBlock=block)? '}'				#switchStat
|	tnAnnots+=annotation* tnGlobalAnnot=globalAnnotation																					#globalAnnotStat
|	tnExpr=expr ';'																															#exprStat
|	tnAnnots+=annotation* tnBlock=block																										#blockStat
|	';'																																		#nullStat
;
assignLvalue: tnAnnots+=annotation* tnLval=lvalue;
forLvalue: tnAnnots+=annotation* tnType=type tnName=WORD;
catchBlock: tnAnnots+=annotation* 'catch' tnType=type tnName=WORD tnBlock=block;
caseBlock: tnExprs+=expr (',' tnExprs+=expr)* tnBlock=block;

lvalue:
	tnLhs=expr '.' tnRhs=lvalue				#memberLvalue
|	tnCallee=expr '[' tnArgs=argsDecl ']'	#indexLvalue
|	tnName=WORD								#varLvalue
;

packageName: tnName+=WORD ('.' tnName+=WORD)* | tnRawName=STRING;
annotation: '@' tnName=packageName ('(' tnArgs=argsDecl ')')?;
globalAnnotation: '@@' tnName=packageName ('(' tnArgs=argsDecl ')')?;

templateDecl: tnAnnots+=annotation* tnName=WORD (':' tnBaseType=type)? ('=' tnDefaultType=type)?;
templateDecls: '<' (tnArgs+=templateDecl (',' tnArgs+=templateDecl)*)? '>';
templateInst: '<' (tnArgs+=type (',' tnArgs+=type)*)? '>';

paramDecl: tnAnnots+=annotation* tnType=type tnName=WORD ('=' tnDefaultValue=expr)?;
paramsDecl: tnArgs+=paramDecl (',' tnArgs+=paramDecl)* |;

argDecl: (tnKey=WORD ':')? tnValue=expr;
argsDecl: tnArgs+=argDecl (',' tnArgs+=argDecl)* |;

block: ('<' tnLabel=WORD '>')? '{' tnBlock+=stat* '}';
anonFunc: tnTemplate=templateDecls? '(' tnArgs=paramsDecl ')' ('=>' tnExprForm=exprs | tnBlockForm=block);

fragment LETTER: [a-zA-Z] | '_';
fragment DIGIT: [0-9];
fragment WORD_PART: LETTER | DIGIT;

WORD: LETTER WORD_PART*;
NUMBER: (DIGIT+ | (DIGIT* '.' DIGIT+) | (DIGIT+ '.' DIGIT*)) ('e' ('+'|'-')? DIGIT+)?;
STRING: '"' (~["\\] | '\\' .)* '"';
CHAR: '\'' (~['\\] | '\\' .) '\'';
DOC_COMMENT: '/**' (BLOCK_COMMENT | DOC_COMMENT | .)*? '*/';

WHITESPACE: [ \t\r\n]+ -> skip;
COMMENT: '//' (BLOCK_COMMENT | DOC_COMMENT | ~[\r\n])* [\r\n] -> skip;
BLOCK_COMMENT: '/*' ((BLOCK_COMMENT | DOC_COMMENT | ~'*') (BLOCK_COMMENT | DOC_COMMENT | .)*?)? '*/' -> skip;

UNKNOWN_TOKEN: .+?;