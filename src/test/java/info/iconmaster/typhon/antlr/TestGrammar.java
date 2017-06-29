package info.iconmaster.typhon.antlr;

import java.util.Collection;
import java.util.function.Consumer;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.Assert;
import org.junit.runners.Parameterized;

import info.iconmaster.typhon.TyphonTest;

/**
 * Tests the ANTLR grammar.
 * 
 * @author iconmaster
 *
 */
public class TestGrammar extends TyphonTest {
	@Parameterized.Parameters
    public static Collection<Object[]> data() {
		return TyphonTest.makeData(
			//methodDecl
			new CaseValid("void main() {}"),
			new CaseValid("void main() {0;1;}"),
			new CaseValid("/**doc*/ @a @b void main() {}"),
			new CaseInvalid("void main() {};"),
			new CaseInvalid("main() {}"),
			//methodStubDecl
			new CaseValid("void main();"),
			new CaseValid("void main(var x);"),
			new CaseValid("void main(var x, var y);"),
			new CaseValid("void main<T>();"),
			new CaseValid("/**doc*/ @a @b void main();"),
			new CaseInvalid("void main()"),
			new CaseInvalid("main();"),
			//fieldDecl
			new CaseValid("int x;"),
			new CaseValid("int x = 3;"),
			new CaseValid("int x, y = 3;"),
			new CaseValid("int x, y = 3, 4;"),
			new CaseValid("/**doc*/ int @a @b x;"),
			new CaseValid("int x; int y; int z;"),
			new CaseInvalid("var x"),
			new CaseInvalid("var x, y"),
			//staticInitDecl
			new CaseValid("{}"),
			new CaseValid("{0;1;}"),
			new CaseValid("@a @b {}"),
			new CaseValid("@a(b) {}"),
			//classDecl
			new CaseValid("class x {}"),
			new CaseValid("class x {var y;}"),
			new CaseValid("class x : y,z {}"),
			new CaseValid("class x<t> {}"),
			new CaseValid("/**doc*/ @a @b class x {}"),
			new CaseInvalid("class x"),
			new CaseInvalid("class x;"),
			//enumDecl
			new CaseValid("enum x {}"),
			new CaseValid("enum x {a,b,c}"),
			new CaseValid("enum x {a,b,c,;var x;}"),
			new CaseValid("enum x {var x;}"),
			new CaseValid("enum x : y,z {}"),
			new CaseValid("/**doc*/ @a @b enum x {}"),
			new CaseInvalid("enum x"),
			new CaseInvalid("enum x;"),
			//simplePackageDecl
			new CaseValid("package x;"),
			new CaseValid("/**doc*/ @a @b package x;"),
			new CaseInvalid("package x"),
			//packageDecl
			new CaseValid("package x {}"),
			new CaseValid("package x {var x;var y;}"),
			new CaseValid("/**doc*/ @a @b package x {}"),
			new CaseInvalid("package {}"),
			//importDecl
			new CaseValid("import x;"),
			new CaseValid("import x as y;"),
			new CaseValid("@a @b import x;"),
			new CaseInvalid("import x"),
			//constructorDecl
			new CaseValid("new() {}"),
			new CaseValid("new() {0;1;}"),
			new CaseValid("new(var x,this.y,var z) {}"),
			new CaseValid("/**doc*/ @a @b new() {}"),
			new CaseInvalid("new {}"),
			//globalAnnotDecl
			new CaseValid("@@a"),
			new CaseValid("@x @y @@a"),
			//enumValueDecl
			new CaseValid("x", (parser)->{parser.enumValueDecl();}),
			new CaseValid("x()", (parser)->{parser.enumValueDecl();}),
			new CaseValid("x(1)", (parser)->{parser.enumValueDecl();}),
			new CaseValid("x(1,2,3)", (parser)->{parser.enumValueDecl();}),
			new CaseValid("/**doc*/ @a @b x", (parser)->{parser.enumValueDecl();}),
			//constructorParam
			new CaseValid("var x", (parser)->{parser.constructorParam();}),
			new CaseValid("this.x", (parser)->{parser.constructorParam();}),
			new CaseValid("@a @b var x", (parser)->{parser.constructorParam();}),
			new CaseValid("@a @b this.x", (parser)->{parser.constructorParam();}),
			new CaseInvalid("var this.x", (parser)->{parser.constructorParam();}),
			//typeofType
			new CaseValid("typeof 3", (parser)->{parser.type();}),
			new CaseValid("@a @b typeof 3", (parser)->{parser.type();}),
			new CaseInvalid("typeof ()", (parser)->{parser.type();}),
			//funcType
			new CaseValid("()->void", (parser)->{parser.type();}),
			new CaseValid("(int)->void", (parser)->{parser.type();}),
			new CaseValid("(int,int,int)->void", (parser)->{parser.type();}),
			new CaseValid("<t>()->void", (parser)->{parser.type();}),
			new CaseValid("@a @b ()->void", (parser)->{parser.type();}),
			new CaseInvalid("int -> ()", (parser)->{parser.type();}),
			//arrayType
			new CaseValid("[int]", (parser)->{parser.type();}),
			new CaseValid("[[int]]", (parser)->{parser.type();}),
			new CaseValid("@a @b [int]", (parser)->{parser.type();}),
			new CaseInvalid("[int,int]", (parser)->{parser.type();}),
			//mapType
			new CaseValid("{int:int}", (parser)->{parser.type();}),
			new CaseValid("{{int:int}:{string:string}}", (parser)->{parser.type();}),
			new CaseValid("@a @b {int:int}", (parser)->{parser.type();}),
			new CaseInvalid("{}", (parser)->{parser.type();}),
			new CaseInvalid("{int}", (parser)->{parser.type();}),
			//varType
			new CaseValid("var", (parser)->{parser.type();}),
			new CaseValid("@a @b var", (parser)->{parser.type();}),
			//constType
			new CaseValid("const var", (parser)->{parser.type();}),
			new CaseValid("@a @b const var", (parser)->{parser.type();}),
			new CaseInvalid("const", (parser)->{parser.type();}),
			//basicType
			new CaseValid("x", (parser)->{parser.type();}),
			new CaseValid("x<t>", (parser)->{parser.type();}),
			new CaseValid("@a @b x", (parser)->{parser.type();}),
			//singleTypes
			new CaseValid("var", (parser)->{parser.types();}),
			new CaseValid("const int", (parser)->{parser.types();}),
			new CaseValid("int", (parser)->{parser.types();}),
			new CaseValid("()->()", (parser)->{parser.types();}),
			//multiTypes
			new CaseValid("()", (parser)->{parser.types();}),
			new CaseValid("(var)", (parser)->{parser.types();}),
			new CaseValid("(var,var)", (parser)->{parser.types();}),
			new CaseValid("(var,var,var)", (parser)->{parser.types();}),
			new CaseInvalid("(var var)", (parser)->{parser.types();}),
			//voidTypes
			new CaseValid("void", (parser)->{parser.types();}),
			//memberExpr
			new CaseValid("a.b", (parser)->{parser.expr();}),
			new CaseValid("a?.b", (parser)->{parser.expr();}),
			new CaseValid("a::b", (parser)->{parser.expr();}),
			new CaseValid("a.b?.c::d", (parser)->{parser.expr();}),
			new CaseValid("'a'.b", (parser)->{parser.expr();}),
			new CaseInvalid("a.'b'", (parser)->{parser.expr();}),
			//funcCallExpr
			new CaseValid("a()", (parser)->{parser.expr();}),
			new CaseValid("a(b)", (parser)->{parser.expr();}),
			new CaseValid("a(b,c,d)", (parser)->{parser.expr();}),
			new CaseValid("a<int>()", (parser)->{parser.expr();}),
			new CaseValid("a.b()", (parser)->{parser.expr();}),
			//indexCallExpr
			new CaseValid("a[]", (parser)->{parser.expr();}),
			new CaseValid("a[b]", (parser)->{parser.expr();}),
			new CaseValid("a[b,c,d]", (parser)->{parser.expr();}),
			new CaseValid("a<int>[]", (parser)->{parser.expr();}),
			new CaseValid("a.b[]", (parser)->{parser.expr();}),
			//castExpr
			new CaseValid("a as int", (parser)->{parser.expr();}),
			new CaseValid("a as int as long", (parser)->{parser.expr();}),
			new CaseValid("(a+b) as int", (parser)->{parser.expr();}),
			new CaseInvalid("a as 3", (parser)->{parser.expr();}),
			//newExpr
			new CaseValid("new int()", (parser)->{parser.expr();}),
			new CaseValid("new int(a)", (parser)->{parser.expr();}),
			new CaseValid("new int(a,b,c)", (parser)->{parser.expr();}),
			new CaseValid("new int<int>()", (parser)->{parser.expr();}),
			new CaseValid("new int() {}", (parser)->{parser.expr();}),
			new CaseValid("new int() {var x; var y;}", (parser)->{parser.expr();}),
			new CaseInvalid("new 3()", (parser)->{parser.expr();}),
			new CaseInvalid("new int{}", (parser)->{parser.expr();}),
			//unOpsExpr
			new CaseValid("-1", (parser)->{parser.expr();}),
			new CaseValid("+1", (parser)->{parser.expr();}),
			new CaseValid("~1", (parser)->{parser.expr();}),
			new CaseValid("!1", (parser)->{parser.expr();}),
			new CaseValid("+-!!--~1", (parser)->{parser.expr();}),
			//binOps1Expr
			new CaseValid("1*1", (parser)->{parser.expr();}),
			new CaseValid("1/1", (parser)->{parser.expr();}),
			new CaseValid("1%1", (parser)->{parser.expr();}),
			//binOps2Expr
			new CaseValid("1+1", (parser)->{parser.expr();}),
			new CaseValid("1-1", (parser)->{parser.expr();}),
			//bitOpsExpr
			new CaseValid("1&1", (parser)->{parser.expr();}),
			new CaseValid("1|1", (parser)->{parser.expr();}),
			new CaseValid("1^1", (parser)->{parser.expr();}),
			new CaseValid("1<<1", (parser)->{parser.expr();}),
			new CaseValid("1>>1", (parser)->{parser.expr();}),
			//nullCoalesceExpr
			new CaseValid("1 ?? 1", (parser)->{parser.expr();}),
			//relOpsExpr
			new CaseValid("1<1", (parser)->{parser.expr();}),
			new CaseValid("1<=1", (parser)->{parser.expr();}),
			new CaseValid("1>1", (parser)->{parser.expr();}),
			new CaseValid("1>=1", (parser)->{parser.expr();}),
			//isExpr
			new CaseValid("1 is int", (parser)->{parser.expr();}),
			new CaseInvalid("1 is 3", (parser)->{parser.expr();}),
			//eqOpsExpr
			new CaseValid("1==1", (parser)->{parser.expr();}),
			new CaseValid("1!=1", (parser)->{parser.expr();}),
			new CaseValid("1===1", (parser)->{parser.expr();}),
			new CaseValid("1!==1", (parser)->{parser.expr();}),
			new CaseValid("!1!==!1", (parser)->{parser.expr();}),
			//logicOpsExpr
			new CaseValid("true && false", (parser)->{parser.expr();}),
			new CaseValid("true || false", (parser)->{parser.expr();}),
			//throwExpr
			new CaseValid("throw 1", (parser)->{parser.expr();}),
			new CaseValid("@a @b throw 1", (parser)->{parser.expr();}),
			//terneryOpExpr
			new CaseValid("true ? 1 : 0", (parser)->{parser.expr();}),
			new CaseInvalid("true ? 1", (parser)->{parser.expr();}),
			new CaseInvalid("1 : 0", (parser)->{parser.expr();}),
			new CaseInvalid("? 1 : 0", (parser)->{parser.expr();}),
			//typeConstExpr
			new CaseValid("type int", (parser)->{parser.expr();}),
			new CaseValid("@a @b type int", (parser)->{parser.expr();}),
			new CaseInvalid("type 3", (parser)->{parser.expr();}),
			//nullConstExpr
			new CaseValid("null", (parser)->{parser.expr();}),
			new CaseValid("@a @b null", (parser)->{parser.expr();}),
			//trueConstExpr
			new CaseValid("true", (parser)->{parser.expr();}),
			new CaseValid("@a @b true", (parser)->{parser.expr();}),
			//falseConstExpr
			new CaseValid("false", (parser)->{parser.expr();}),
			new CaseValid("@a @b false", (parser)->{parser.expr();}),
			//thisConstExpr
			new CaseValid("this", (parser)->{parser.expr();}),
			new CaseValid("@a @b this", (parser)->{parser.expr();}),
			//varExpr
			new CaseValid("x", (parser)->{parser.expr();}),
			new CaseValid("xyz", (parser)->{parser.expr();}),
			new CaseValid("_", (parser)->{parser.expr();}),
			new CaseValid("_123", (parser)->{parser.expr();}),
			new CaseValid("xyz123abc", (parser)->{parser.expr();}),
			new CaseValid("@a @b x", (parser)->{parser.expr();}),
			new CaseInvalid("123xyz", (parser)->{parser.expr();}),
			//numConstExpr
			new CaseValid("1", (parser)->{parser.expr();}),
			new CaseValid("1234", (parser)->{parser.expr();}),
			new CaseValid("1234.", (parser)->{parser.expr();}),
			new CaseValid(".1234", (parser)->{parser.expr();}),
			new CaseValid("1234.1234", (parser)->{parser.expr();}),
			new CaseValid("1234.1234e1234", (parser)->{parser.expr();}),
			new CaseValid("1234.1234e+1234", (parser)->{parser.expr();}),
			new CaseValid("1234.1234e-1234", (parser)->{parser.expr();}),
			new CaseValid("1234.e1234", (parser)->{parser.expr();}),
			new CaseValid(".1234e1234", (parser)->{parser.expr();}),
			new CaseValid("@a @b 1", (parser)->{parser.expr();}),
			new CaseInvalid("1e1.2", (parser)->{parser.expr();}),
			//stringConstExpr
			new CaseValid("\"\"", (parser)->{parser.expr();}),
			new CaseValid("\" \"", (parser)->{parser.expr();}),
			new CaseValid("\"abc 123\"", (parser)->{parser.expr();}),
			new CaseValid("\"\\\\\"", (parser)->{parser.expr();}),
			new CaseValid("\"\\\"\"", (parser)->{parser.expr();}),
			new CaseValid("\"abc\\n123\"", (parser)->{parser.expr();}),
			new CaseValid("@a @b \"\"", (parser)->{parser.expr();}),
			new CaseInvalid("\"", (parser)->{parser.expr();}),
			new CaseInvalid("\"\\\"", (parser)->{parser.expr();}),
			//charConstExpr
			new CaseValid("' '", (parser)->{parser.expr();}),
			new CaseValid("'x'", (parser)->{parser.expr();}),
			new CaseValid("'\"'", (parser)->{parser.expr();}),
			new CaseValid("'\\\\'", (parser)->{parser.expr();}),
			new CaseValid("'\\''", (parser)->{parser.expr();}),
			new CaseValid("@a @b ' '", (parser)->{parser.expr();}),
			new CaseInvalid("''", (parser)->{parser.expr();}),
			new CaseInvalid("' ", (parser)->{parser.expr();}),
			new CaseInvalid("'abc'", (parser)->{parser.expr();}),
			//funcConstExpr
			new CaseValid("()=>()", (parser)->{parser.expr();}),
			new CaseValid("@a @b ()=>()", (parser)->{parser.expr();}),
			new CaseInvalid("a=>()", (parser)->{parser.expr();}),
			//arrayConstExpr
			new CaseValid("[]", (parser)->{parser.expr();}),
			new CaseValid("[,]", (parser)->{parser.expr();}),
			new CaseValid("[1]", (parser)->{parser.expr();}),
			new CaseValid("[1,]", (parser)->{parser.expr();}),
			new CaseValid("[1,2,3]", (parser)->{parser.expr();}),
			new CaseValid("[1,2,3,]", (parser)->{parser.expr();}),
			new CaseValid("@a @b []", (parser)->{parser.expr();}),
			new CaseInvalid("[1 2 3]", (parser)->{parser.expr();}),
			new CaseInvalid("[1,2,3", (parser)->{parser.expr();}),
			//mapConstExpr
			new CaseValid("{}", (parser)->{parser.expr();}),
			new CaseValid("{,}", (parser)->{parser.expr();}),
			new CaseValid("{1=1}", (parser)->{parser.expr();}),
			new CaseValid("{1=1,}", (parser)->{parser.expr();}),
			new CaseValid("{1=1,2=2}", (parser)->{parser.expr();}),
			new CaseValid("{1=1,2=2,}", (parser)->{parser.expr();}),
			new CaseValid("@a @b {}", (parser)->{parser.expr();}),
			new CaseInvalid("{1}", (parser)->{parser.expr();}),
			new CaseInvalid("{1=2,3}", (parser)->{parser.expr();}),
			new CaseInvalid("{1=2,", (parser)->{parser.expr();}),
			//parensExpr
			new CaseValid("(1)", (parser)->{parser.expr();}),
			new CaseValid("(()=>())", (parser)->{parser.expr();}),
			new CaseValid("((((1))))", (parser)->{parser.expr();}),
			new CaseValid("@a @b (1)", (parser)->{parser.expr();}),
			new CaseInvalid("(1", (parser)->{parser.expr();}),
			new CaseInvalid("1)", (parser)->{parser.expr();}),
			//exprs
			new CaseValid("1", (parser)->{parser.exprs();}),
			new CaseValid("()", (parser)->{parser.exprs();}),
			new CaseValid("(1)", (parser)->{parser.exprs();}),
			new CaseValid("(1,2)", (parser)->{parser.exprs();}),
			new CaseValid("(1,2,3)", (parser)->{parser.exprs();}),
			//retStat
			new CaseValid("return;", (parser)->{parser.stat();}),
			new CaseValid("return 1;", (parser)->{parser.stat();}),
			new CaseValid("return 1,2,3;", (parser)->{parser.stat();}),
			new CaseInvalid("return 1", (parser)->{parser.stat();}),
			//defStat
			new CaseValid("var x;", (parser)->{parser.stat();}),
			new CaseValid("var x = 1;", (parser)->{parser.stat();}),
			new CaseValid("var x,y = 1;", (parser)->{parser.stat();}),
			new CaseValid("var x,y = 1,2;", (parser)->{parser.stat();}),
			new CaseValid("/**doc*/ @a @b var x;", (parser)->{parser.stat();}),
			new CaseInvalid("var x", (parser)->{parser.stat();}),
			//assignStat
			new CaseValid("x = 1;", (parser)->{parser.stat();}),
			new CaseValid("x, y = 1;", (parser)->{parser.stat();}),
			new CaseValid("x, y = 1, 2;", (parser)->{parser.stat();}),
			new CaseInvalid("x = 2", (parser)->{parser.stat();}),
			new CaseInvalid("= 3;", (parser)->{parser.stat();}),
			new CaseInvalid("x =;", (parser)->{parser.stat();}),
			//comboAssignStat
			new CaseValid("x += 1;", (parser)->{parser.stat();}),
			new CaseValid("x, y += 1;", (parser)->{parser.stat();}),
			new CaseValid("x -= 1;", (parser)->{parser.stat();}),
			new CaseValid("x *= 1;", (parser)->{parser.stat();}),
			new CaseValid("x /= 1;", (parser)->{parser.stat();}),
			new CaseValid("x %= 1;", (parser)->{parser.stat();}),
			new CaseInvalid("x += 1", (parser)->{parser.stat();}),
			new CaseInvalid("x += 1,2", (parser)->{parser.stat();}),
			//ifStat
			new CaseValid("if true {}", (parser)->{parser.stat();}),
			new CaseValid("if true {} else {}", (parser)->{parser.stat();}),
			new CaseValid("if true {} elseif true {}", (parser)->{parser.stat();}),
			new CaseValid("if true {} elseif true {} elseif true {}", (parser)->{parser.stat();}),
			new CaseValid("if true {} elseif true {} else {}", (parser)->{parser.stat();}),
			new CaseValid("if true {} elseif true {} elseif true {} else {}", (parser)->{parser.stat();}),
			new CaseInvalid("if true {};", (parser)->{parser.stat();}),
			new CaseInvalid("if true {} else {} elseif false {}", (parser)->{parser.stat();}),
			//forStat
			new CaseValid("for var x : y {}", (parser)->{parser.stat();}),
			new CaseValid("for var x,var y : z {}", (parser)->{parser.stat();}),
			new CaseInvalid("for var x : y,z {}", (parser)->{parser.stat();}),
			new CaseInvalid("for var x : y {};", (parser)->{parser.stat();}),
			//whileStat
			new CaseValid("while true {}", (parser)->{parser.stat();}),
			new CaseInvalid("while true {};", (parser)->{parser.stat();}),
			//repeatStat
			new CaseValid("repeat {} until true;", (parser)->{parser.stat();}),
			new CaseInvalid("repeat {} until true", (parser)->{parser.stat();}),
			new CaseInvalid("repeat until true;", (parser)->{parser.stat();}),
			//tryStat
			new CaseValid("try {}", (parser)->{parser.stat();}),
			new CaseValid("try {} catch var x {}", (parser)->{parser.stat();}),
			new CaseValid("try {} catch var x {} catch var y {}", (parser)->{parser.stat();}),
			new CaseInvalid("catch {}", (parser)->{parser.stat();}),
			//breakStat
			new CaseValid("break;", (parser)->{parser.stat();}),
			new CaseValid("break x;", (parser)->{parser.stat();}),
			new CaseInvalid("break", (parser)->{parser.stat();}),
			new CaseInvalid("break x,y;", (parser)->{parser.stat();}),
			//contStat
			new CaseValid("continue;", (parser)->{parser.stat();}),
			new CaseValid("continue x;", (parser)->{parser.stat();}),
			new CaseInvalid("continue", (parser)->{parser.stat();}),
			new CaseInvalid("continue x,y;", (parser)->{parser.stat();}),
			//switchStat
			new CaseValid("switch x {}", (parser)->{parser.stat();}),
			new CaseValid("switch x {case 1 {}}", (parser)->{parser.stat();}),
			new CaseValid("switch x {case 1 {} case 2 {}}", (parser)->{parser.stat();}),
			new CaseValid("switch x {default {}}", (parser)->{parser.stat();}),
			new CaseValid("switch x {case 1 {} default {}}", (parser)->{parser.stat();}),
			new CaseValid("switch x {case 1 {} case 2 {} default {}}", (parser)->{parser.stat();}),
			new CaseInvalid("switch {}", (parser)->{parser.stat();}),
			new CaseInvalid("switch x {};", (parser)->{parser.stat();}),
			//globalAnnotStat
			new CaseValid("@@a", (parser)->{parser.stat();}),
			new CaseValid("@x @y @@a", (parser)->{parser.stat();}),
			//exprStat
			new CaseValid("1;", (parser)->{parser.stat();}),
			new CaseValid("a.b();", (parser)->{parser.stat();}),
			new CaseInvalid("1", (parser)->{parser.stat();}),
			//blockStat
			new CaseValid("{0;1;}", (parser)->{parser.stat();}),
			//nullStat
			new CaseValid(";", (parser)->{parser.stat();}),
			new CaseInvalid(";;;", (parser)->{parser.stat();}),
			//forLvalue
			new CaseValid("var x", (parser)->{parser.forLvalue();}),
			new CaseValid("@a @b var x", (parser)->{parser.forLvalue();}),
			new CaseInvalid("x", (parser)->{parser.forLvalue();}),
			//catchBlock
			new CaseValid("catch var x {}", (parser)->{parser.catchBlock();}),
			new CaseValid("@a @b catch var x {}", (parser)->{parser.catchBlock();}),
			new CaseInvalid("catch x {}", (parser)->{parser.catchBlock();}),
			new CaseInvalid("catch var x,y {}", (parser)->{parser.catchBlock();}),
			//caseBlock
			new CaseValid("case 1 {}", (parser)->{parser.caseBlock();}),
			new CaseValid("case 1,2,3 {}", (parser)->{parser.caseBlock();}),
			new CaseValid("@a @b case 1 {}", (parser)->{parser.caseBlock();}),
			new CaseInvalid("case {}", (parser)->{parser.caseBlock();}),
			//memberLvalue
			new CaseValid("a.b", (parser)->{parser.lvalue();}),
			new CaseValid("a.b.c.d", (parser)->{parser.lvalue();}),
			new CaseValid("a.b(c).d", (parser)->{parser.lvalue();}),
			new CaseInvalid("a.1", (parser)->{parser.lvalue();}),
			//indexLvalue
			new CaseValid("a[]", (parser)->{parser.lvalue();}),
			new CaseValid("a[b]", (parser)->{parser.lvalue();}),
			new CaseValid("a[b,c,d]", (parser)->{parser.lvalue();}),
			//varLvalue
			new CaseValid("a", (parser)->{parser.lvalue();}),
			new CaseInvalid("1", (parser)->{parser.lvalue();}),
			//packageName
			new CaseValid("a", (parser)->{parser.packageName();}),
			new CaseValid("a.b", (parser)->{parser.packageName();}),
			new CaseValid("a.b.c", (parser)->{parser.packageName();}),
			new CaseValid("\"\"", (parser)->{parser.packageName();}),
			new CaseValid("\"abc\"", (parser)->{parser.packageName();}),
			new CaseInvalid("1", (parser)->{parser.packageName();}),
			new CaseInvalid("a.1", (parser)->{parser.packageName();}),
			new CaseInvalid("1.a", (parser)->{parser.packageName();}),
			//annotation
			new CaseValid("@a", (parser)->{parser.annotation();}),
			new CaseValid("@a()", (parser)->{parser.annotation();}),
			new CaseValid("@a.b", (parser)->{parser.annotation();}),
			new CaseValid("@a.b()", (parser)->{parser.annotation();}),
			new CaseValid("@a(b)", (parser)->{parser.annotation();}),
			new CaseValid("@a.b(c)", (parser)->{parser.annotation();}),
			new CaseValid("@a(b,c)", (parser)->{parser.annotation();}),
			new CaseValid("@a.b(c,d)", (parser)->{parser.annotation();}),
			new CaseValid("@a.b.c.d.e", (parser)->{parser.annotation();}),
			new CaseInvalid("@()", (parser)->{parser.annotation();}),
			//globalAnnotation
			new CaseValid("@@a", (parser)->{parser.globalAnnotation();}),
			new CaseValid("@@a()", (parser)->{parser.globalAnnotation();}),
			new CaseValid("@@a.b", (parser)->{parser.globalAnnotation();}),
			new CaseValid("@@a.b()", (parser)->{parser.globalAnnotation();}),
			new CaseValid("@@a(b)", (parser)->{parser.globalAnnotation();}),
			new CaseValid("@@a.b(c)", (parser)->{parser.globalAnnotation();}),
			new CaseValid("@@a(b,c)", (parser)->{parser.globalAnnotation();}),
			new CaseValid("@@a.b(c,d)", (parser)->{parser.globalAnnotation();}),
			new CaseValid("@@a.b.c.d.e", (parser)->{parser.globalAnnotation();}),
			new CaseInvalid("@@()", (parser)->{parser.annotation();}),
			//templateDecl
			new CaseValid("T", (parser)->{parser.templateDecl();}),
			new CaseValid("T : int", (parser)->{parser.templateDecl();}),
			new CaseValid("T = int", (parser)->{parser.templateDecl();}),
			new CaseValid("T : int = int", (parser)->{parser.templateDecl();}),
			new CaseValid("@a @b T", (parser)->{parser.templateDecl();}),
			new CaseInvalid("1", (parser)->{parser.templateDecl();}),
			//templateDecls
			new CaseValid("<>", (parser)->{parser.templateDecls();}),
			new CaseValid("<T>", (parser)->{parser.templateDecls();}),
			new CaseValid("<A,B,C>", (parser)->{parser.templateDecls();}),
			new CaseValid("<A : int,B = long,C : string = string>", (parser)->{parser.templateDecls();}),
			new CaseInvalid("<1>", (parser)->{parser.templateDecls();}),
			new CaseInvalid("<1 2>", (parser)->{parser.templateDecls();}),
			new CaseInvalid("<1", (parser)->{parser.templateDecls();}),
			new CaseInvalid("1>", (parser)->{parser.templateDecls();}),
			//templateInst
			new CaseValid("<>", (parser)->{parser.templateInst();}),
			new CaseValid("<var>", (parser)->{parser.templateInst();}),
			new CaseValid("<var,var,var>", (parser)->{parser.templateInst();}),
			new CaseInvalid("<1>", (parser)->{parser.templateInst();}),
			//paramName
			new CaseValid("x", (parser)->{parser.paramName();}),
			new CaseValid("@a @b x", (parser)->{parser.paramName();}),
			new CaseInvalid("3", (parser)->{parser.paramName();}),
			new CaseInvalid("@a @b 3", (parser)->{parser.paramName();}),
			//paramDecl
			new CaseValid("var x", (parser)->{parser.paramDecl();}),
			new CaseValid("var x = 3", (parser)->{parser.paramDecl();}),
			new CaseValid("@a @b var x", (parser)->{parser.paramDecl();}),
			new CaseInvalid("var 1", (parser)->{parser.paramDecl();}),
			new CaseInvalid("1", (parser)->{parser.paramDecl();}),
			new CaseInvalid("x", (parser)->{parser.paramDecl();}),
			//paramsDecl
			new CaseValid("var x", (parser)->{parser.paramsDecl();}),
			new CaseValid("var x, var y, var z", (parser)->{parser.paramsDecl();}),
			new CaseValid("var x", (parser)->{parser.paramsDecl();}),
			new CaseValid("var x, var y = 1, var z", (parser)->{parser.paramsDecl();}),
			//argDecl
			new CaseValid("1", (parser)->{parser.argDecl();}),
			new CaseValid("key: value", (parser)->{parser.argDecl();}),
			new CaseInvalid("1: value", (parser)->{parser.argDecl();}),
			//argsDecl
			new CaseValid("1", (parser)->{parser.argsDecl();}),
			new CaseValid("key: value", (parser)->{parser.argsDecl();}),
			new CaseValid("1, 2, 3", (parser)->{parser.argsDecl();}),
			new CaseValid("1, key: value, 3", (parser)->{parser.argsDecl();}),
			//block
			new CaseValid("{}", (parser)->{parser.block();}),
			new CaseValid("<label>{}", (parser)->{parser.block();}),
			new CaseValid("{1;2;}", (parser)->{parser.block();}),
			new CaseValid("<label>{1;2;}", (parser)->{parser.block();}),
			new CaseInvalid("{1;", (parser)->{parser.block();}),
			new CaseInvalid("1;}", (parser)->{parser.block();}),
			//anonFunc
			new CaseValid("()=>0", (parser)->{parser.anonFunc();}),
			new CaseValid("() {return 0;}", (parser)->{parser.anonFunc();}),
			new CaseValid("<T>()=>0", (parser)->{parser.anonFunc();}),
			new CaseValid("<T>() {return 0;}", (parser)->{parser.anonFunc();}),
			new CaseValid("()=>(1,2)", (parser)->{parser.anonFunc();}),
			new CaseValid("(var x)=>0", (parser)->{parser.anonFunc();}),
			new CaseValid("(var x, var y, var z)=>0", (parser)->{parser.anonFunc();}),
			new CaseInvalid("1=>()", (parser)->{parser.anonFunc();}),
			new CaseInvalid("x=>()", (parser)->{parser.anonFunc();}),
			new CaseInvalid("(1)=>()", (parser)->{parser.anonFunc();}),
			// misc. invalid cases
			new CaseInvalid("x"),
			new CaseInvalid("0"),
			//empty program
			new CaseValid("")
		);
	}
    
    private static class CaseValid implements Runnable {
    	public String input;
    	public Consumer<TyphonParser> code;
    	
		public CaseValid(String input) {
			this.input = input;
			this.code = (parser)->{
				parser.root();
			};
		}
		
		public CaseValid(String input, Consumer<TyphonParser> code) {
			this.input = input;
			this.code = code;
		}
		
		@Override
		public void run() {
			TyphonLexer lexer = new TyphonLexer(new ANTLRInputStream(input));
			TyphonParser parser = new TyphonParser(new CommonTokenStream(lexer));
			parser.removeErrorListeners();
			parser.addErrorListener(new BaseErrorListener() {
				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
					Assert.fail("parse of '"+input+"' failed: "+msg);
				}
			});
			code.accept(parser);
			Assert.assertEquals("parse of '"+input+"' did not match all the input: '"+parser.getCurrentToken().getText()+"' at position "+parser.getCurrentToken().getCharPositionInLine()+"\n", parser.getCurrentToken().getType(), Recognizer.EOF);
		}
    }
    
    private static class CaseInvalid implements Runnable {
    	public String input;
    	public Consumer<TyphonParser> code;
    	boolean errorOccured = false;
    	
		public CaseInvalid(String input) {
			this.input = input;
			this.code = (parser)->{
				parser.root();
			};
		}
		
		public CaseInvalid(String input, Consumer<TyphonParser> code) {
			this.input = input;
			this.code = code;
		}
		
		@Override
		public void run() {
			TyphonLexer lexer = new TyphonLexer(new ANTLRInputStream(input));
			TyphonParser parser = new TyphonParser(new CommonTokenStream(lexer));
			parser.removeErrorListeners();
			parser.addErrorListener(new BaseErrorListener() {
				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
					errorOccured = true;
				}
			});
			code.accept(parser);
			if (parser.getCurrentToken().getType() != Recognizer.EOF) errorOccured = true;
			
			if (!errorOccured) {
				Assert.fail("parse of '" + input + "' had no errors; errors expected");
			}
		}
    }
}
