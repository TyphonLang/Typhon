package info.iconmaster.typhon.compiler;

import java.util.Collection;
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
import info.iconmaster.typhon.antlr.TyphonParser.StatContext;
import info.iconmaster.typhon.compiler.Instruction.OpCode;
import info.iconmaster.typhon.linker.TyphonLinker;

/**
 * Tests <tt>{@link TyphonLinker}</tt>.
 * 
 * @author iconmaster
 *
 */
public class TestCompileStat extends TyphonTest {
	@Parameterized.Parameters
    public static Collection<Object[]> data() {
		return TyphonTest.makeData(new TestCase("var x;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(0, code.ops.size());
		}),new TestCase("var x, y;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(0, code.ops.size());
		}),new TestCase("var x = 1;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals("1", code.ops.get(0).arg(1));
		}),new TestCase("var x, y = 1;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
		}),new TestCase("var x, y = 1, 2;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(2, code.ops.size());
		}),new TestCase("var x, y = 1, 2, 3;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(2, code.ops.size());
		}),new TestCase("1;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(0, code.ops.size());
		}));
	}
    
    private static class TestCase implements Runnable {
    	String input;
    	Consumer<CodeBlock> test;
    	
		public TestCase(String input, Consumer<CodeBlock> test) {
			this.input = input;
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
			
			StatContext root = parser.stat();
			CodeBlock code = new CodeBlock(tni, tni.corePackage);
			Scope scope = new Scope(code);
			
			TyphonCompiler.compileStat(scope, root, null);
			test.accept(code);
		}
    }
}
