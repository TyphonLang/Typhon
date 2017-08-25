grammar Typhon;

root: tnDecls+=decl* EOF;

decl:
	tnDoc=DOC_COMMENT? tnAnnots+=annotation* tnRetType=types tnName=WORD tnTemplate=templateDecls? '(' tnArgs=paramsDecl ')' ('=>' tnExprForm=exprs | '{' tnBlockForm+=stat* '}' | tnStubForm=';')			#methodDecl
|	tnDoc=DOC_COMMENT? tnType=type tnNames+=paramName (',' tnNames+=paramName)* ('=' tnValues+=expr (',' tnValues+=expr)*)? ';'																				#fieldDecl
|	tnAnnots+=annotation* '{' tnBlock+=stat* '}'																																							#staticInitDecl
|	tnDoc=DOC_COMMENT? tnAnnots+=annotation* 'class' tnName=WORD tnTemplate=templateDecls? (':' tnExtends+=type (',' tnExtends+=type)*)? '{' tnDecls+=decl* '}'												#classDecl
|	tnDoc=DOC_COMMENT? tnAnnots+=annotation* 'enum' tnName=WORD (':' tnExtends+=type (',' tnExtends+=type)*)? '{' (tnValues+=enumValueDecl (',' tnValues+=enumValueDecl)* ','?)? ';'? tnDecls+=decl* '}'	#enumDecl
|	tnDoc=DOC_COMMENT? tnAnnots+=annotation* 'package' tnName=packageName ';'																																#simplePackageDecl
|	tnDoc=DOC_COMMENT? tnAnnots+=annotation* 'package' tnName=packageName '{' tnDecls+=decl* '}'																											#packageDecl
|	tnAnnots+=annotation* 'import' (tnName=packageName | tnRawName=STRING) ('as' tnAlias=packageName)? ';'																									#importDecl
|	tnDoc=DOC_COMMENT? tnAnnots+=annotation* 'new' '(' (tnArgs+=constructorParam (',' tnArgs+=constructorParam)*)? ')' ('=>' tnExprForm=exprs | '{' tnBlockForm+=stat* '}' | tnStubForm=';')				#constructorDecl
|	tnGlobalAnnot=globalAnnotation																																											#globalAnnotDecl
;
enumValueDecl: tnDoc=DOC_COMMENT? tnAnnots+=annotation* tnName=WORD ('(' tnArgs=argsDecl ')')?;
constructorParam:
	tnAnnots+=annotation* 'this' '.' tnName=WORD ('=' tnDefaultValue=expr)?		#constructorParamThis
|	tnType=type tnAnnots+=annotation* tnName=WORD ('=' tnDefaultValue=expr)?	#constructorParamTyped
;

type:
	tnAnnots+=annotation* tnTemplate=templateDecls? '(' (tnArgTypes+=type (',' tnArgTypes+=type)*)? ')' '->' tnRetType=types	#funcType
|	tnAnnots+=annotation* '[' tnBaseType=type ']'																				#arrayType
|	tnAnnots+=annotation* '{' tnKeyType=type ':' tnValueType=type '}'															#mapType
|	tnAnnots+=annotation* 'var'																									#varType
|	tnAnnots+=annotation* 'const' tnType=type																					#constType
|	(tnLookup+=typeMemberItem '.')* tnLookup+=typeMemberItem																	#basicType
;
types:
	tnType=type										#singleTypes
|	'(' (tnTypes+=type (',' tnTypes+=type)*)? ')'	#multiTypes
|	'void'											#voidTypes
;
typeMemberItem: tnAnnots+=annotation* tnName=WORD tnTemplate=templateArgs?;

expr:
	tnLhs=expr tnLookup+=memberItem* (tnOp='.'|tnOp='?.') tnAnnots+=annotation* tnValue=WORD												#memberExpr
|	tnLhs=expr '::' tnValue=WORD tnTemplate=templateArgs? '(' (tnFuncPtrArg+=templateArg (',' tnFuncPtrArg+=templateArg)*)? ')'				#funcPtrExpr
|	tnCallee=expr tnAnnots+=annotation* tnTemplate=templateArgs? '(' tnArgs=argsDecl ')'													#funcCallExpr
|	tnCallee=expr tnAnnots+=annotation* tnTemplate=templateArgs? '[' tnArgs=argsDecl ']'													#indexCallExpr
|	tnLhs=expr (tnOp='as'|tnOp='as?') tnRhs=type																							#castExpr
|	tnAnnots+=annotation* 'new' tnType=type '(' tnArgs=argsDecl ')' ('{' tnDecls+=decl* '}')?												#newExpr
|	tnAnnots+=annotation* (tnOp='-'|tnOp='+'|tnOp='!'|tnOp='~') tnArg=expr																	#unOpsExpr
|	tnLhs=expr (tnOp='*'|tnOp='/'|tnOp='%') tnRhs=expr																						#binOps1Expr
|	tnLhs=expr (tnOp='+'|tnOp='-') tnRhs=expr																								#binOps2Expr
|	tnLhs=expr (tnOp='&'|tnOp='|'|tnOp='^'|tnOp='<<'|tnOp='>>') tnRhs=expr																	#bitOpsExpr
|	tnLhs=expr '??' tnRhs=expr																												#nullCoalesceExpr
|	tnLhs=expr (tnOp='<'|tnOp='>'|tnOp='<='|tnOp='>=') tnRhs=expr																			#relOpsExpr
|	tnLhs=expr 'is' tnRhs=type																												#isExpr
|	tnLhs=expr (tnOp='=='|tnOp='!='|tnOp='==='|tnOp='!==') tnRhs=expr																		#eqOpsExpr
|	tnLhs=expr (tnOp='&&'|tnOp='||') tnRhs=expr																								#logicOpsExpr
|	tnAnnots+=annotation* 'throw' tnArg=expr																								#throwExpr
|	tnIf=expr '?' tnThen=expr ':' tnElse=expr																								#terneryOpExpr
|	tnAnnots+=annotation* 'class' tnType=type																								#typeConstExpr
|	tnAnnots+=annotation* 'null'																											#nullConstExpr
|	tnAnnots+=annotation* 'true'																											#trueConstExpr
|	tnAnnots+=annotation* 'false'																											#falseConstExpr
|	tnAnnots+=annotation* 'this'																											#thisConstExpr
|	tnAnnots+=annotation* tnValue=WORD																										#varExpr
|	tnAnnots+=annotation* tnValue=NUMBER																									#numConstExpr
|	tnAnnots+=annotation* tnValue=STRING																									#stringConstExpr
|	tnAnnots+=annotation* tnValue=CHAR																										#charConstExpr
|	tnAnnots+=annotation* tnFuncTemplate=templateDecls? '(' tnFuncArgs=paramsDecl ')' ('=>' tnExprForm=exprs | '{' tnBlockForm+=stat* '}')	#funcConstExpr
|	tnAnnots+=annotation* '[' (tnValues+=expr (',' tnValues+=expr)*)? ','? ']'																#arrayConstExpr
|	tnAnnots+=annotation* '{' (tnKeys+=expr '=' tnValues+=expr (',' tnKeys+=expr '=' tnValues+=expr)*)? ','? '}'							#mapConstExpr
|	tnAnnots+=annotation* '(' tnExpr=expr ')'																								#parensExpr
;
exprs: tnExprs+=expr | '(' (tnExprs+=expr (',' tnExprs+=expr)*)? ')';
memberItem: (tnOp='.'|tnOp='?.') tnAnnots+=annotation* tnName=WORD tnTemplate=templateArgs?;

stat:
	tnAnnots+=annotation* 'return' (tnValues+=expr (',' tnValues+=expr)*)? ';'																							#retStat
|	tnDoc=DOC_COMMENT? tnType=type tnNames+=paramName (',' tnNames+=paramName)* ('=' tnValues+=expr (',' tnValues+=expr)*)? ';'											#defStat
|	tnLvals+=lvalue (',' tnLvals+=lvalue)* ('=' tnValues+=expr (',' tnValues+=expr)*)? ';'																				#assignStat
|	tnLvals+=lvalue (',' tnLvals+=lvalue)* (tnOp='+='|tnOp='-='|tnOp='*='|tnOp='/='|tnOp='%=') tnRval=expr ';'															#comboAssignStat
|	tnAnnots+=annotation* 'if' tnIfExpr=expr tnIfBlock=block	('elseif' tnElseifExprs+=expr tnElseifBlocks+=block)* ('else' tnElseBlock=block)?						#ifStat
|	tnAnnots+=annotation* 'for' tnLvals+=forLvalue (',' tnLvals+=forLvalue)* ':' tnExpr=expr tnBlock=block																#forStat
|	tnAnnots+=annotation* 'while' tnExpr=expr tnBlock=block																												#whileStat
|	tnAnnots+=annotation* 'repeat' tnBlock=block 'until' tnExpr=expr ';'																								#repeatStat
|	tnAnnots+=annotation* 'try' tnTryBlock=block tnCatchBlocks+=catchBlock*																								#tryStat
|	tnAnnots+=annotation* 'break' tnLabel=WORD? ';'																														#breakStat
|	tnAnnots+=annotation* 'continue' tnLabel=WORD? ';'																													#contStat
|	tnAnnots+=annotation* 'switch' tnExpr=expr ('<' tnLabel=WORD '>')? '{' tnCaseBlocks+=caseBlock* (tnDefaultAnnots+=annotation* 'default' tnDefaultBlock=block)? '}'	#switchStat
|	tnGlobalAnnot=globalAnnotation																																		#globalAnnotStat
|	tnExpr=expr ';'																																						#exprStat
|	tnAnnots+=annotation* tnBlock=block																																	#blockStat
|	';'																																									#nullStat
;
forLvalue: tnAnnots+=annotation* tnType=type tnName=WORD;
catchBlock: tnAnnots+=annotation* 'catch' tnType=type tnName=WORD tnBlock=block;
caseBlock: tnAnnots+=annotation* 'case' tnExprs+=expr (',' tnExprs+=expr)* tnBlock=block;

lvalue:
	tnLhs=expr tnLookup+=memberItem* (tnOp='.'|tnOp='?.') tnRhs=lvalue	#memberLvalue
|	tnCallee=expr '[' tnArgs=argsDecl ']'								#indexLvalue
|	tnName=WORD															#varLvalue
;

packageName: tnName+=WORD ('.' tnName+=WORD)*;
annotation: '@' tnName=packageName ('(' tnArgs=argsDecl ')')?;
globalAnnotation: '@@' tnName=packageName ('(' tnArgs=argsDecl ')')?;

templateDecl: tnAnnots+=annotation* tnName=WORD (':' tnBaseType=type)? ('=' tnDefaultType=type)?;
templateDecls: '<' (tnArgs+=templateDecl (',' tnArgs+=templateDecl)*)? '>';

templateArg: (tnLabel=WORD ':')? tnType=type;
templateArgs: '<' (tnArgs+=templateArg (',' tnArgs+=templateArg)*)? '>';

paramName: tnAnnots+=annotation* tnName=WORD;
paramDecl: tnType=type tnAnnots+=annotation* tnName=WORD ('=' tnDefaultValue=expr)?;
paramsDecl: tnArgs+=paramDecl (',' tnArgs+=paramDecl)* |;

argDecl: (tnKey=WORD ':')? tnValue=expr;
argsDecl: tnArgs+=argDecl (',' tnArgs+=argDecl)* |;

block: ('<' tnLabel=WORD '>')? '{' tnBlock+=stat* '}';

fragment LETTER: [a-zA-Z] | '_';
fragment DIGIT: [0-9];
fragment WORD_PART: LETTER | DIGIT;

WORD: LETTER WORD_PART*;
NUMBER: (DIGIT+ | (DIGIT* '.' DIGIT+) | (DIGIT+ '.' DIGIT*)) ('e' ('+'|'-')? DIGIT+)?;
STRING: '"' (~["\\] | '\\' .)* '"';
CHAR: '\'' (~['\\] | '\\' .) '\'';
DOC_COMMENT: '/**' (BLOCK_COMMENT | DOC_COMMENT | .)*? '*/';

WHITESPACE: [ \t\r\n]+ -> channel(HIDDEN);
COMMENT: '//' (BLOCK_COMMENT | DOC_COMMENT | ~[\r\n])* [\r\n] -> channel(HIDDEN);
BLOCK_COMMENT: '/*' ((BLOCK_COMMENT | DOC_COMMENT | ~'*') (BLOCK_COMMENT | DOC_COMMENT | .)*?)? '*/' -> channel(HIDDEN);

UNKNOWN_TOKEN: .+?;
