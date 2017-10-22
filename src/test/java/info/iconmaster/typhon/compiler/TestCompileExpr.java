package info.iconmaster.typhon.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.Assert;
import org.junit.runners.Parameterized;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.TyphonTest;
import info.iconmaster.typhon.antlr.TyphonLexer;
import info.iconmaster.typhon.antlr.TyphonParser;
import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.compiler.Instruction.OpCode;
import info.iconmaster.typhon.linker.TyphonLinker;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.types.TypeRef;

/**
 * Tests <tt>{@link TyphonLinker}</tt>.
 * 
 * @author iconmaster
 *
 */
public class TestCompileExpr extends TyphonTest {
	@Parameterized.Parameters
    public static Collection<Object[]> data() {
		return TyphonTest.makeData(new TestCase("1", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(1, (int) code.ops.get(0).arg(1));
		}),new TestCase("x", 1, (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("(1)", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(1, (int) code.ops.get(0).arg(1));
		}),new TestCase("(((1)))", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(1, (int) code.ops.get(0).arg(1));
		}),new TestCase("1 + 2", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(3, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(1, (int) code.ops.get(0).arg(1));
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(1).op);
			Assert.assertEquals(2, (int) code.ops.get(1).arg(1));
			
			Assert.assertEquals(OpCode.CALL, code.ops.get(2).op);
			Assert.assertEquals(1, code.ops.get(2).<List<Variable>>arg(0).size());
			Assert.assertEquals("add", code.ops.get(2).<Function>arg(2).getName());
			Assert.assertEquals(1, code.ops.get(2).<List<Variable>>arg(3).size());
		}),new TestCase("1 - 2", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(3, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(1, (int) code.ops.get(0).arg(1));
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(1).op);
			Assert.assertEquals(2, (int) code.ops.get(1).arg(1));
			
			Assert.assertEquals(OpCode.CALL, code.ops.get(2).op);
			Assert.assertEquals(1, code.ops.get(2).<List<Variable>>arg(0).size());
			Assert.assertEquals("sub", code.ops.get(2).<Function>arg(2).getName());
			Assert.assertEquals(1, code.ops.get(2).<List<Variable>>arg(3).size());
		}),new TestCase("1+1.0-2*3.0/4%5.0", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 & 2", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 & 2.0", 1, (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("1 >> 2", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 >> 2.0", 1, (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("1 & 2 | 3 << 4 >> 5", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 < 2", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1.0 >= 2", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 > 2 <= 3", 1, (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("1 as int", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 as Any", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 as List<int>", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 as? int", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 as? Any", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 as? List<int>", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("-1", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("~1", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("-+-1", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("-1+-2.0", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("\"\"", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("\"Hello, World!\"", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("\"\\r\\n\"", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("\"Line one\\nLine two\"", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("\"\\\"\\\'\\\\\"", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("'a'", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("'\\n'", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("'\\\''", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("'\\\\'", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("\"\\q\"", 1, (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("'\\q'", 1, (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("true", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("false", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("null", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("true && false", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("true || false", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("true || false && false || true", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("true && 1", 1, (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("1 || false", 1, (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("1 && 2", 1, (code)->{
			Assert.assertEquals(2, code.tni.errors.size());
		}),new TestCase("1 == 2", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 == 'c'", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 != 2", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 != 'c'", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 === 2", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 !== 2", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 is int", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 !is int", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("null is List<Any>", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("null !is List<Any>", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 is int is bool", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("null ?? null", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("1 ?? 'c'", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("() => 1", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("(int i) => i", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("(int x, int y) => (x, y)", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("(int x, int y) {}", 1, (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}));
	}
    
    private static class TestCase implements Runnable {
    	String input;
    	int n;
    	Consumer<CodeBlock> test;
    	
		public TestCase(String input, int n, Consumer<CodeBlock> test) {
			this.input = input;
			this.n = n;
			this.test = test;
		}
		
		@Override
		public void run() {
			TyphonInput tni = new TyphonInput();
			
			TyphonLexer lexer = new TyphonLexer(new ANTLRInputStream(input));
			TyphonParser parser = new TyphonParser(new CommonTokenStream(lexer));
			parser.removeErrorListeners();
			parser.addErrorListener(new BaseErrorListener() {
				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
					Assert.fail("parse of '"+input+"' failed: "+msg);
				}
			});
			
			ExprContext root = parser.expr();
			CodeBlock code = new CodeBlock(tni, tni.corePackage);
			Scope scope = new Scope(code);
			
			List<Variable> vars = new ArrayList<>(n);
			for (int i = 0; i < n; i++) {
				vars.add(scope.addTempVar(new TypeRef(tni.corePackage.TYPE_ANY), null));
			}
			
			TyphonCompiler.compileExpr(scope, root, vars);
			test.accept(code);
		}
    }
}
