package info.iconmaster.typhon.compiler;

import java.util.Collection;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.runners.Parameterized;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.TyphonTest;
import info.iconmaster.typhon.linker.TyphonLinker;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.TyphonModelReader;
import info.iconmaster.typhon.tnil.CodeBlock;
import info.iconmaster.typhon.tnil.Instruction.OpCode;
import info.iconmaster.typhon.types.TyphonTypeResolver;

/**
 * Tests <tt>{@link TyphonLinker}</tt>.
 * 
 * @author iconmaster
 *
 */
public class TestCompiler extends TyphonTest {
	@Parameterized.Parameters
    public static Collection<Object[]> data() {
		return TyphonTest.makeData(new TestCase("void f() {}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("void f() {var x = 1;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals("1", code.ops.get(0).arg(1));
		}),new TestCase("int f() => 1", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals("1", code.ops.get(0).arg(1));
		}),new TestCase("void f() {var x = 1; var y = x;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(2, code.ops.size());
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals("1", code.ops.get(0).arg(1));
			Assert.assertEquals(OpCode.MOV, code.ops.get(1).op);
		}),new TestCase("var a; void f() {var x = a;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("int a; void f() {int x = a;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("var a; void f() {int x = a;}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
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
			Package p = TyphonModelReader.parseString(tni, input);
			TyphonLinker.link(p);
			TyphonTypeResolver.resolve(p);
			TyphonCompiler.compile(p);
			
			test.accept(p.getFunctionsWithName("f").get(0).getCode());
		}
    }
}
