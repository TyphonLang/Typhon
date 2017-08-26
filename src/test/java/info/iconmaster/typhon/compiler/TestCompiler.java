package info.iconmaster.typhon.compiler;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.runners.Parameterized;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.TyphonTest;
import info.iconmaster.typhon.compiler.Instruction.OpCode;
import info.iconmaster.typhon.linker.TyphonLinker;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.TyphonModelReader;
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
			
			Assert.assertEquals(1, code.ops.size());
			
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(0).op);
			Assert.assertEquals(1, code.ops.get(0).<List<Variable>>arg(0).size());
			Assert.assertEquals("a", code.ops.get(0).<Function>arg(1).getName());
			Assert.assertEquals(0, code.ops.get(0).<List<Variable>>arg(2).size());
		}),new TestCase("int a; void f() {int x = a;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(0).op);
			Assert.assertEquals(1, code.ops.get(0).<List<Variable>>arg(0).size());
			Assert.assertEquals("a", code.ops.get(0).<Function>arg(1).getName());
			Assert.assertEquals(0, code.ops.get(0).<List<Variable>>arg(2).size());
		}),new TestCase("var a; void f() {int x = a;}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("package p {var a;} void f() {var x = p.a;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(2, code.ops.size());
			
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(0).op);
			Assert.assertEquals(1, code.ops.get(0).<List<Variable>>arg(0).size());
			Assert.assertEquals("a", code.ops.get(0).<Function>arg(1).getName());
			Assert.assertEquals(0, code.ops.get(0).<List<Variable>>arg(2).size());
			
			Assert.assertEquals(OpCode.MOV, code.ops.get(1).op);
		}),new TestCase("package p {var a;} void f() {var x = (p).a;}", (code)->{
			Assert.assertEquals(2, code.tni.errors.size());
		}),new TestCase("class a {int b;} a x; void f() {int y = x.b;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(3, code.ops.size());
			
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(0).op);
			Assert.assertEquals(1, code.ops.get(0).<List<Variable>>arg(0).size());
			Assert.assertEquals("x", code.ops.get(0).<Function>arg(1).getName());
			Assert.assertEquals(0, code.ops.get(0).<List<Variable>>arg(2).size());
			
			Assert.assertEquals(OpCode.CALL, code.ops.get(1).op);
			Assert.assertEquals(1, code.ops.get(1).<List<Variable>>arg(0).size());
			Assert.assertEquals("b", code.ops.get(1).<Function>arg(2).getName());
			Assert.assertEquals(0, code.ops.get(1).<List<Variable>>arg(3).size());
			
			Assert.assertEquals(OpCode.MOV, code.ops.get(2).op);
		}),new TestCase("class a {int b;} a x; void f() {int y = (x).b;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(3, code.ops.size());
			
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(0).op);
			Assert.assertEquals(1, code.ops.get(0).<List<Variable>>arg(0).size());
			Assert.assertEquals("x", code.ops.get(0).<Function>arg(1).getName());
			Assert.assertEquals(0, code.ops.get(0).<List<Variable>>arg(2).size());
			
			Assert.assertEquals(OpCode.CALL, code.ops.get(1).op);
			Assert.assertEquals(1, code.ops.get(1).<List<Variable>>arg(0).size());
			Assert.assertEquals("b", code.ops.get(1).<Function>arg(2).getName());
			Assert.assertEquals(0, code.ops.get(1).<List<Variable>>arg(3).size());
			
			Assert.assertEquals(OpCode.MOV, code.ops.get(2).op);
		}),new TestCase("void f() {int x; x = 1;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(2, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals("1", code.ops.get(0).arg(1));
			
			Assert.assertEquals(OpCode.MOV, code.ops.get(1).op);
		}),new TestCase("void f() {[int] x; x = 1;}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("void f() {int x, y; x, y = 1, 2;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(4, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals("1", code.ops.get(0).arg(1));
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(1).op);
			Assert.assertEquals("2", code.ops.get(1).arg(1));
			
			Assert.assertEquals(OpCode.MOV, code.ops.get(2).op);
			Assert.assertEquals(OpCode.MOV, code.ops.get(3).op);
		}),new TestCase("void f() {int x, y = 1, 2; x, y = y, x;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(6, code.ops.size());
		}),new TestCase("int x; void f() {x = 1;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(3, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals("1", code.ops.get(0).arg(1));
			
			Assert.assertEquals(OpCode.MOV, code.ops.get(1).op);
			
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(2).op);
			Assert.assertEquals(0, code.ops.get(2).<List<Variable>>arg(0).size());
			Assert.assertEquals("x", code.ops.get(2).<Function>arg(1).getName());
			Assert.assertEquals(1, code.ops.get(2).<List<Variable>>arg(2).size());
		}),new TestCase("class a {int x; void f() {x = 1;}}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(3, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals("1", code.ops.get(0).arg(1));
			
			Assert.assertEquals(OpCode.MOV, code.ops.get(1).op);
			
			Assert.assertEquals(OpCode.CALL, code.ops.get(2).op);
			Assert.assertEquals(0, code.ops.get(2).<List<Variable>>arg(0).size());
			Assert.assertEquals("x", code.ops.get(2).<Function>arg(2).getName());
			Assert.assertEquals(1, code.ops.get(2).<List<Variable>>arg(3).size());
		}),new TestCase("class a {int x; void f() {var y = x;}}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			
			Assert.assertEquals(OpCode.CALL, code.ops.get(0).op);
			Assert.assertEquals(1, code.ops.get(0).<List<Variable>>arg(0).size());
			Assert.assertEquals("x", code.ops.get(0).<Function>arg(2).getName());
			Assert.assertEquals(0, code.ops.get(0).<List<Variable>>arg(3).size());
		}),new TestCase("package p {int a;} void f() {p.a = 1}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(2, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals("1", code.ops.get(0).arg(1));
			
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(1).op);
			Assert.assertEquals(0, code.ops.get(1).<List<Variable>>arg(0).size());
			Assert.assertEquals("a", code.ops.get(1).<Function>arg(1).getName());
			Assert.assertEquals(1, code.ops.get(1).<List<Variable>>arg(2).size());
		}),new TestCase("class a {int y;} a x; void f() {x.y = 1}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(3, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals("1", code.ops.get(0).arg(1));
			
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(1).op);
			Assert.assertEquals(0, code.ops.get(1).<List<Variable>>arg(0).size());
			Assert.assertEquals("x", code.ops.get(1).<Function>arg(1).getName());
			Assert.assertEquals(1, code.ops.get(1).<List<Variable>>arg(2).size());
			
			Assert.assertEquals(OpCode.CALL, code.ops.get(1).op);
			Assert.assertEquals(0, code.ops.get(1).<List<Variable>>arg(0).size());
			Assert.assertEquals("y", code.ops.get(1).<Function>arg(2).getName());
			Assert.assertEquals(1, code.ops.get(1).<List<Variable>>arg(3).size());
		}),new TestCase("int g() {} void f() {int x = g();}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(0).op);
			Assert.assertEquals(1, code.ops.get(0).<List<Variable>>arg(0).size());
			Assert.assertEquals("g", code.ops.get(0).<Function>arg(1).getName());
			Assert.assertEquals(0, code.ops.get(0).<List<Variable>>arg(2).size());
		}),new TestCase("package p {int g() {}} void f() {int x = p.g();}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(0).op);
			Assert.assertEquals(1, code.ops.get(0).<List<Variable>>arg(0).size());
			Assert.assertEquals("g", code.ops.get(0).<Function>arg(1).getName());
			Assert.assertEquals(0, code.ops.get(0).<List<Variable>>arg(2).size());
		}),new TestCase("int g(int x) {} void f() {int x = g(1);}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(2, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals("1", code.ops.get(0).arg(1));
			
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(1).op);
			Assert.assertEquals(1, code.ops.get(1).<List<Variable>>arg(0).size());
			Assert.assertEquals("g", code.ops.get(1).<Function>arg(1).getName());
			Assert.assertEquals(1, code.ops.get(1).<List<Variable>>arg(2).size());
		}),new TestCase("int g(int x, int y) {} void f() {int x = g(1, 2);}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("int g(int x, int y) {} void f() {int x = g(y: 1, 2);}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("int g(int x, int y = 2) {} void f() {int x = g(1);}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("int g() {} void f() {int x = g(1);}", (code)->{
			Assert.assertEquals(2, code.tni.errors.size());
		}),new TestCase("int g(int x) {} void f() {int x = g();}", (code)->{
			Assert.assertEquals(2, code.tni.errors.size());
		}),new TestCase("int g(int x) {} void f() {var y; int x = g(y);}", (code)->{
			Assert.assertEquals(2, code.tni.errors.size());
		}),new TestCase("void f() {1;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(0, code.ops.size());
		}),new TestCase("int g() {} void f() {g();}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(0).op);
			Assert.assertEquals(0, code.ops.get(0).<List<Variable>>arg(0).size());
			Assert.assertEquals("g", code.ops.get(0).<Function>arg(1).getName());
			Assert.assertEquals(0, code.ops.get(0).<List<Variable>>arg(2).size());
		}),new TestCase("int g() {} void f() {int x = g(), g(1);}", (code)->{
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
			
			// the test is based on a function called 'f', in this package or a subpackage.
			
			if (p.getFunctionsWithName("f").isEmpty()) {
				for (Package pp : p.getSubpackges()) {
					if (!pp.getFunctionsWithName("f").isEmpty()) {
						test.accept(pp.getFunctionsWithName("f").get(0).getCode());
					}
				}
			} else {
				test.accept(p.getFunctionsWithName("f").get(0).getCode());
			}
		}
    }
}
