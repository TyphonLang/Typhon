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
		}),new TestCase("var x = 1;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals((Integer) 1, (Integer) code.ops.get(0).arg(1));
		}),new TestCase("byte x = 1;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVBYTE, code.ops.get(0).op);
			Assert.assertEquals((Byte) (byte) 1, (Byte) code.ops.get(0).arg(1));
		}),new TestCase("ubyte x = 1;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVBYTE, code.ops.get(0).op);
			Assert.assertEquals((Byte) (byte) 1, (Byte) code.ops.get(0).arg(1));
		}),new TestCase("short x = 1;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVSHORT, code.ops.get(0).op);
			Assert.assertEquals((Short) (short) 1, (Short) code.ops.get(0).arg(1));
		}),new TestCase("ushort x = 1;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVSHORT, code.ops.get(0).op);
			Assert.assertEquals((Short) (short) 1, (Short) code.ops.get(0).arg(1));
		}),new TestCase("int x = 1;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals((Integer) 1, (Integer) code.ops.get(0).arg(1));
		}),new TestCase("uint x = 1;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals((Integer) 1, (Integer) code.ops.get(0).arg(1));
		}),new TestCase("long x = 1;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVLONG, code.ops.get(0).op);
			Assert.assertEquals((Long) 1l, (Long) code.ops.get(0).arg(1));
		}),new TestCase("ulong x = 1;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVLONG, code.ops.get(0).op);
			Assert.assertEquals((Long) 1l, (Long) code.ops.get(0).arg(1));
		}),new TestCase("double x = 1;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVDOUBLE, code.ops.get(0).op);
			Assert.assertEquals((Double) 1.0, (Double) code.ops.get(0).arg(1));
		}),new TestCase("float x = 1;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVFLOAT, code.ops.get(0).op);
			Assert.assertEquals((Float) 1.0f, (Float) code.ops.get(0).arg(1));
		}),new TestCase("var x = 1.0;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVDOUBLE, code.ops.get(0).op);
			Assert.assertEquals((Double) 1.0, (Double) code.ops.get(0).arg(1));
		}),new TestCase("double x = 1.0;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVDOUBLE, code.ops.get(0).op);
			Assert.assertEquals((Double) 1.0, (Double) code.ops.get(0).arg(1));
		}),new TestCase("float x = 1.0;", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVFLOAT, code.ops.get(0).op);
			Assert.assertEquals((Float) 1.0f, (Float) code.ops.get(0).arg(1));
		}),new TestCase("{}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(2, code.ops.size());
			
			Assert.assertEquals(OpCode.LABEL, code.ops.get(0).op);
			Assert.assertEquals(OpCode.LABEL, code.ops.get(1).op);
		}),new TestCase("{var x = 1; {var x = 2;}}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(6, code.ops.size());
			
			Assert.assertEquals(OpCode.LABEL, code.ops.get(0).op);
			Assert.assertEquals(OpCode.LABEL, code.ops.get(5).op);
		}),new TestCase("while true {}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(6, code.ops.size());
			
			Assert.assertEquals(OpCode.LABEL, code.ops.get(0).op);
			Assert.assertEquals(OpCode.MOVTRUE, code.ops.get(1).op);
			Assert.assertEquals(OpCode.NOT, code.ops.get(2).op);
			Assert.assertEquals(OpCode.JUMPIF, code.ops.get(3).op);
			Assert.assertEquals(OpCode.JUMP, code.ops.get(4).op);
			Assert.assertEquals(OpCode.LABEL, code.ops.get(5).op);
		}),new TestCase("while true name: {}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(6, code.ops.size());
			
			Assert.assertEquals(OpCode.LABEL, code.ops.get(0).op);
			Assert.assertEquals(OpCode.MOVTRUE, code.ops.get(1).op);
			Assert.assertEquals(OpCode.NOT, code.ops.get(2).op);
			Assert.assertEquals(OpCode.JUMPIF, code.ops.get(3).op);
			Assert.assertEquals(OpCode.JUMP, code.ops.get(4).op);
			Assert.assertEquals(OpCode.LABEL, code.ops.get(5).op);
		}),new TestCase("while true {println();}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(7, code.ops.size());
			
			Assert.assertEquals(OpCode.LABEL, code.ops.get(0).op);
			Assert.assertEquals(OpCode.MOVTRUE, code.ops.get(1).op);
			Assert.assertEquals(OpCode.NOT, code.ops.get(2).op);
			Assert.assertEquals(OpCode.JUMPIF, code.ops.get(3).op);
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(4).op);
			Assert.assertEquals(OpCode.JUMP, code.ops.get(5).op);
			Assert.assertEquals(OpCode.LABEL, code.ops.get(6).op);
		}),new TestCase("while 0 {}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("while true {println(); break; println();}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(9, code.ops.size());
			
			Assert.assertEquals(OpCode.LABEL, code.ops.get(0).op);
			Assert.assertEquals(OpCode.MOVTRUE, code.ops.get(1).op);
			Assert.assertEquals(OpCode.NOT, code.ops.get(2).op);
			Assert.assertEquals(OpCode.JUMPIF, code.ops.get(3).op);
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(4).op);
			Assert.assertEquals(OpCode.JUMP, code.ops.get(5).op);
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(6).op);
			Assert.assertEquals(OpCode.JUMP, code.ops.get(7).op);
			Assert.assertEquals(OpCode.LABEL, code.ops.get(8).op);
			
			Assert.assertEquals(code.ops.get(8).args[0], code.ops.get(5).args[0]);
		}),new TestCase("while true name: {println(); break name; println();}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(9, code.ops.size());
			
			Assert.assertEquals(OpCode.LABEL, code.ops.get(0).op);
			Assert.assertEquals(OpCode.MOVTRUE, code.ops.get(1).op);
			Assert.assertEquals(OpCode.NOT, code.ops.get(2).op);
			Assert.assertEquals(OpCode.JUMPIF, code.ops.get(3).op);
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(4).op);
			Assert.assertEquals(OpCode.JUMP, code.ops.get(5).op);
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(6).op);
			Assert.assertEquals(OpCode.JUMP, code.ops.get(7).op);
			Assert.assertEquals(OpCode.LABEL, code.ops.get(8).op);
			
			Assert.assertEquals(code.ops.get(8).args[0], code.ops.get(5).args[0]);
		}),new TestCase("name: {while true {println(); break name; println();}}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(11, code.ops.size());
			
			Assert.assertEquals(OpCode.LABEL, code.ops.get(0).op);
			Assert.assertEquals(OpCode.LABEL, code.ops.get(1).op);
			Assert.assertEquals(OpCode.MOVTRUE, code.ops.get(2).op);
			Assert.assertEquals(OpCode.NOT, code.ops.get(3).op);
			Assert.assertEquals(OpCode.JUMPIF, code.ops.get(4).op);
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(5).op);
			Assert.assertEquals(OpCode.JUMP, code.ops.get(6).op);
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(7).op);
			Assert.assertEquals(OpCode.JUMP, code.ops.get(8).op);
			Assert.assertEquals(OpCode.LABEL, code.ops.get(9).op);
			Assert.assertEquals(OpCode.LABEL, code.ops.get(10).op);
			
			Assert.assertEquals(code.ops.get(10).args[0], code.ops.get(6).args[0]);
		}),new TestCase("name: {while true name: {println(); break name; println();}}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(11, code.ops.size());
			
			Assert.assertEquals(OpCode.LABEL, code.ops.get(0).op);
			Assert.assertEquals(OpCode.LABEL, code.ops.get(1).op);
			Assert.assertEquals(OpCode.MOVTRUE, code.ops.get(2).op);
			Assert.assertEquals(OpCode.NOT, code.ops.get(3).op);
			Assert.assertEquals(OpCode.JUMPIF, code.ops.get(4).op);
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(5).op);
			Assert.assertEquals(OpCode.JUMP, code.ops.get(6).op);
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(7).op);
			Assert.assertEquals(OpCode.JUMP, code.ops.get(8).op);
			Assert.assertEquals(OpCode.LABEL, code.ops.get(9).op);
			Assert.assertEquals(OpCode.LABEL, code.ops.get(10).op);
			
			Assert.assertEquals(code.ops.get(9).args[0], code.ops.get(6).args[0]);
		}),new TestCase("name: {while true name: {println(); break notaname; println();}}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("break;", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("break name;", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("{name: {} break name;}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		})
				
				
				
				,new TestCase("while true {println(); continue; println();}", (code)->{
					Assert.assertEquals(0, code.tni.errors.size());
					
					Assert.assertEquals(9, code.ops.size());
					
					Assert.assertEquals(OpCode.LABEL, code.ops.get(0).op);
					Assert.assertEquals(OpCode.MOVTRUE, code.ops.get(1).op);
					Assert.assertEquals(OpCode.NOT, code.ops.get(2).op);
					Assert.assertEquals(OpCode.JUMPIF, code.ops.get(3).op);
					Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(4).op);
					Assert.assertEquals(OpCode.JUMP, code.ops.get(5).op);
					Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(6).op);
					Assert.assertEquals(OpCode.JUMP, code.ops.get(7).op);
					Assert.assertEquals(OpCode.LABEL, code.ops.get(8).op);
					
					Assert.assertEquals(code.ops.get(0).args[0], code.ops.get(5).args[0]);
				}),new TestCase("while true name: {println(); continue name; println();}", (code)->{
					Assert.assertEquals(0, code.tni.errors.size());
					
					Assert.assertEquals(9, code.ops.size());
					
					Assert.assertEquals(OpCode.LABEL, code.ops.get(0).op);
					Assert.assertEquals(OpCode.MOVTRUE, code.ops.get(1).op);
					Assert.assertEquals(OpCode.NOT, code.ops.get(2).op);
					Assert.assertEquals(OpCode.JUMPIF, code.ops.get(3).op);
					Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(4).op);
					Assert.assertEquals(OpCode.JUMP, code.ops.get(5).op);
					Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(6).op);
					Assert.assertEquals(OpCode.JUMP, code.ops.get(7).op);
					Assert.assertEquals(OpCode.LABEL, code.ops.get(8).op);
					
					Assert.assertEquals(code.ops.get(0).args[0], code.ops.get(5).args[0]);
				}),new TestCase("name: {while true {println(); continue name; println();}}", (code)->{
					Assert.assertEquals(0, code.tni.errors.size());
					
					Assert.assertEquals(11, code.ops.size());
					
					Assert.assertEquals(OpCode.LABEL, code.ops.get(0).op);
					Assert.assertEquals(OpCode.LABEL, code.ops.get(1).op);
					Assert.assertEquals(OpCode.MOVTRUE, code.ops.get(2).op);
					Assert.assertEquals(OpCode.NOT, code.ops.get(3).op);
					Assert.assertEquals(OpCode.JUMPIF, code.ops.get(4).op);
					Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(5).op);
					Assert.assertEquals(OpCode.JUMP, code.ops.get(6).op);
					Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(7).op);
					Assert.assertEquals(OpCode.JUMP, code.ops.get(8).op);
					Assert.assertEquals(OpCode.LABEL, code.ops.get(9).op);
					Assert.assertEquals(OpCode.LABEL, code.ops.get(10).op);
					
					Assert.assertEquals(code.ops.get(0).args[0], code.ops.get(6).args[0]);
				}),new TestCase("name: {while true name: {println(); continue name; println();}}", (code)->{
					Assert.assertEquals(0, code.tni.errors.size());
					
					Assert.assertEquals(11, code.ops.size());
					
					Assert.assertEquals(OpCode.LABEL, code.ops.get(0).op);
					Assert.assertEquals(OpCode.LABEL, code.ops.get(1).op);
					Assert.assertEquals(OpCode.MOVTRUE, code.ops.get(2).op);
					Assert.assertEquals(OpCode.NOT, code.ops.get(3).op);
					Assert.assertEquals(OpCode.JUMPIF, code.ops.get(4).op);
					Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(5).op);
					Assert.assertEquals(OpCode.JUMP, code.ops.get(6).op);
					Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(7).op);
					Assert.assertEquals(OpCode.JUMP, code.ops.get(8).op);
					Assert.assertEquals(OpCode.LABEL, code.ops.get(9).op);
					Assert.assertEquals(OpCode.LABEL, code.ops.get(10).op);
					
					Assert.assertEquals(code.ops.get(1).args[0], code.ops.get(6).args[0]);
				}),new TestCase("name: {while true name: {println(); continue notaname; println();}}", (code)->{
					Assert.assertEquals(1, code.tni.errors.size());
				}),new TestCase("continue;", (code)->{
					Assert.assertEquals(1, code.tni.errors.size());
				}),new TestCase("continue name;", (code)->{
					Assert.assertEquals(1, code.tni.errors.size());
				}),new TestCase("{name: {} continue name;}", (code)->{
					Assert.assertEquals(1, code.tni.errors.size());
				}),new TestCase("repeat {} until true;", (code)->{
					Assert.assertEquals(0, code.tni.errors.size());
					
					Assert.assertEquals(5, code.ops.size());
					
					Assert.assertEquals(OpCode.LABEL, code.ops.get(0).op);
					Assert.assertEquals(OpCode.MOVTRUE, code.ops.get(1).op);
					Assert.assertEquals(OpCode.NOT, code.ops.get(2).op);
					Assert.assertEquals(OpCode.JUMPIF, code.ops.get(3).op);
					Assert.assertEquals(OpCode.LABEL, code.ops.get(4).op);
				}),new TestCase("repeat name: {} until true;", (code)->{
					Assert.assertEquals(0, code.tni.errors.size());
					
					Assert.assertEquals(5, code.ops.size());
					
					Assert.assertEquals(OpCode.LABEL, code.ops.get(0).op);
					Assert.assertEquals(OpCode.MOVTRUE, code.ops.get(1).op);
					Assert.assertEquals(OpCode.NOT, code.ops.get(2).op);
					Assert.assertEquals(OpCode.JUMPIF, code.ops.get(3).op);
					Assert.assertEquals(OpCode.LABEL, code.ops.get(4).op);
				}),new TestCase("repeat {println();} until true;", (code)->{
					Assert.assertEquals(0, code.tni.errors.size());
					
					Assert.assertEquals(6, code.ops.size());
					
					Assert.assertEquals(OpCode.LABEL, code.ops.get(0).op);
					Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(1).op);
					Assert.assertEquals(OpCode.MOVTRUE, code.ops.get(2).op);
					Assert.assertEquals(OpCode.NOT, code.ops.get(3).op);
					Assert.assertEquals(OpCode.JUMPIF, code.ops.get(4).op);
					Assert.assertEquals(OpCode.LABEL, code.ops.get(5).op);
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
