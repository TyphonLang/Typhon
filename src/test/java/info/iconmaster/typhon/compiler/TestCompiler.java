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
			Assert.assertEquals(1, (int) code.ops.get(0).arg(1));
		}),new TestCase("int f() => 1", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(1, (int) code.ops.get(0).arg(1));
		}),new TestCase("void f() {var x = 1; var y = x;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(2, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(1, (int) code.ops.get(0).arg(1));
			
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
			Assert.assertEquals(1, (int) code.ops.get(0).arg(1));
			
			Assert.assertEquals(OpCode.MOV, code.ops.get(1).op);
		}),new TestCase("void f() {[int] x; x = 1;}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("void f() {int x, y; x, y = 1, 2;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(4, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(1, (int) code.ops.get(0).arg(1));
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(1).op);
			Assert.assertEquals(2, (int) code.ops.get(1).arg(1));
			
			Assert.assertEquals(OpCode.MOV, code.ops.get(2).op);
			Assert.assertEquals(OpCode.MOV, code.ops.get(3).op);
		}),new TestCase("void f() {int x, y = 1, 2; x, y = y, x;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(6, code.ops.size());
		}),new TestCase("int x; void f() {x = 1;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(3, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(1, (int) code.ops.get(0).arg(1));
			
			Assert.assertEquals(OpCode.MOV, code.ops.get(1).op);
			
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(2).op);
			Assert.assertEquals(0, code.ops.get(2).<List<Variable>>arg(0).size());
			Assert.assertEquals("x", code.ops.get(2).<Function>arg(1).getName());
			Assert.assertEquals(1, code.ops.get(2).<List<Variable>>arg(2).size());
		}),new TestCase("class a {int x; void f() {x = 1;}}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(3, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(1, (int) code.ops.get(0).arg(1));
			
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
			Assert.assertEquals(1, (int) code.ops.get(0).arg(1));
			
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(1).op);
			Assert.assertEquals(0, code.ops.get(1).<List<Variable>>arg(0).size());
			Assert.assertEquals("a", code.ops.get(1).<Function>arg(1).getName());
			Assert.assertEquals(1, code.ops.get(1).<List<Variable>>arg(2).size());
		}),new TestCase("class a {int y;} a x; void f() {x.y = 1}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(3, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(1, (int) code.ops.get(0).arg(1));
			
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
		}),new TestCase("int g() {} void f() {int x; x = g();}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(2, code.ops.size());
			
			Assert.assertEquals(OpCode.CALLSTATIC, code.ops.get(0).op);
			Assert.assertEquals(1, code.ops.get(0).<List<Variable>>arg(0).size());
			Assert.assertEquals("g", code.ops.get(0).<Function>arg(1).getName());
			Assert.assertEquals(0, code.ops.get(0).<List<Variable>>arg(2).size());
			
			Assert.assertEquals(OpCode.MOV, code.ops.get(1).op);
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
			Assert.assertEquals(1, (int) code.ops.get(0).arg(1));
			
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
		}),new TestCase("class a<T> {T y;} class b {int z;} void f() {a<b> x; x.y.z;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("class a<T> {T y;} class b<T> : a<T> {} void f() {b<int> x; a<int> y = x;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("void g(byte b) {} void f() {g(1);}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("void g(byte b) {} void f() {int x = 1; g(x);}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("var x; void f() {var y = x as int;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("var x; void f() {int y = x as int;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("var x; void f() {var y = x as? int;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("var x; void f() {int y = x as? int;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("import operator; class a {@add int g(a other) {}} void f() {a b, c; int x = b + c;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("class a {int b;} void f() {a a; int x = a.?b;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(4, code.ops.size());
			
			Assert.assertEquals(OpCode.ISNULL, code.ops.get(0).op);
			Assert.assertEquals(OpCode.JUMPTRUE, code.ops.get(1).op);
			
			Assert.assertEquals(OpCode.CALL, code.ops.get(2).op);
			Assert.assertEquals(1, code.ops.get(2).<List<Variable>>arg(0).size());
			Assert.assertEquals("b", code.ops.get(2).<Function>arg(2).getName());
			Assert.assertEquals(0, code.ops.get(2).<List<Variable>>arg(3).size());
			
			Assert.assertEquals(OpCode.LABEL, code.ops.get(3).op);
		}),new TestCase("class a {float b; int c;} void f() {a a; int x = a..b.c;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(3, code.ops.size());
			
			Assert.assertEquals(OpCode.CALL, code.ops.get(0).op);
			Assert.assertEquals(1, code.ops.get(0).<List<Variable>>arg(0).size());
			Assert.assertEquals("b", code.ops.get(0).<Function>arg(2).getName());
			Assert.assertEquals(0, code.ops.get(0).<List<Variable>>arg(3).size());
			
			Assert.assertEquals(OpCode.CALL, code.ops.get(1).op);
			Assert.assertEquals(1, code.ops.get(1).<List<Variable>>arg(0).size());
			Assert.assertEquals("c", code.ops.get(1).<Function>arg(2).getName());
			Assert.assertEquals(0, code.ops.get(1).<List<Variable>>arg(3).size());
			
			Assert.assertEquals(OpCode.MOV, code.ops.get(2).op);
		}),new TestCase("class a {package p {float b;} package q {int c;}} void f() {a a; int x = a..p.b.q.c;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(3, code.ops.size());
			
			Assert.assertEquals(OpCode.CALL, code.ops.get(0).op);
			Assert.assertEquals(1, code.ops.get(0).<List<Variable>>arg(0).size());
			Assert.assertEquals("b", code.ops.get(0).<Function>arg(2).getName());
			Assert.assertEquals(0, code.ops.get(0).<List<Variable>>arg(3).size());
			
			Assert.assertEquals(OpCode.CALL, code.ops.get(1).op);
			Assert.assertEquals(1, code.ops.get(1).<List<Variable>>arg(0).size());
			Assert.assertEquals("c", code.ops.get(1).<Function>arg(2).getName());
			Assert.assertEquals(0, code.ops.get(1).<List<Variable>>arg(3).size());
			
			Assert.assertEquals(OpCode.MOV, code.ops.get(2).op);
		}),new TestCase("class a {float b; int c;} void f() {a a; a x = a..b..c..b;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("class y {int c;} class a {y b;} void f() {a a; int x = a.?b.?c;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("class a {int g(); int h();} void f() {a a; a b = a..g()..h();}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("class a {int b} void f() {a a; a.?b = 3;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("class a {int b} void f() {a a; a..b..b = a;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("class a {int g() {}} void f() {a a; a = a..g()..g();}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("class a {int g() {}} void f() {a a; int x = a.?g();}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("void f() {string s = \"Hello!\";}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVSTR, code.ops.get(0).op);
			Assert.assertEquals("Hello!", code.ops.get(0).args[1]);
		}),new TestCase("void f() {string s = \"One\\nTwo\";}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVSTR, code.ops.get(0).op);
			Assert.assertEquals("One\nTwo", code.ops.get(0).args[1]);
		}),new TestCase("void f() {char c = 'c';}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVCHAR, code.ops.get(0).op);
			Assert.assertEquals('c', code.ops.get(0).args[1]);
		}),new TestCase("void f() {char c = '\\n';}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVCHAR, code.ops.get(0).op);
			Assert.assertEquals('\n', code.ops.get(0).args[1]);
		}),new TestCase("void f() {bool b = true;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVTRUE, code.ops.get(0).op);
		}),new TestCase("void f() {bool b = false;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVFALSE, code.ops.get(0).op);
		}),new TestCase("void f() {var x = this;}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("class c {void f() {var x = this;}}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			
			Assert.assertEquals(OpCode.MOV, code.ops.get(0).op);
		}),new TestCase("void f() {var x = null;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVNULL, code.ops.get(0).op);
		}),new TestCase("void f() {return;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			
			Assert.assertEquals(OpCode.RET, code.ops.get(0).op);
			Assert.assertEquals(0, code.ops.get(0).<List>arg(0).size());
		}),new TestCase("int f() {return 0;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(2, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(OpCode.RET, code.ops.get(1).op);
			Assert.assertEquals(1, code.ops.get(1).<List>arg(0).size());
		}),new TestCase("(int,int) f() {return 0, 1;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(3, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(1).op);
			Assert.assertEquals(OpCode.RET, code.ops.get(2).op);
			Assert.assertEquals(2, code.ops.get(2).<List>arg(0).size());
		}),new TestCase("void f() {return 0;}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("void f() {return 0, 1;}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("(int,int) f() {return 0;}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("int f() {return 'c';}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("(int, int) f() {return 0, 'c';}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("void f() {bool x = 1 == 2;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(3, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(1).op);
			Assert.assertEquals(OpCode.CALL, code.ops.get(2).op);
		}),new TestCase("void f() {bool x = 1 == 'c';}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(3, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(OpCode.MOVCHAR, code.ops.get(1).op);
			Assert.assertEquals(OpCode.CALL, code.ops.get(2).op);
		}),new TestCase("void f() {bool x = 1 != 2;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(4, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(1).op);
			Assert.assertEquals(OpCode.CALL, code.ops.get(2).op);
			Assert.assertEquals(OpCode.NOT, code.ops.get(3).op);
		}),new TestCase("void f() {bool x = 1 === 2;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(3, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(1).op);
			Assert.assertEquals(OpCode.RAWEQ, code.ops.get(2).op);
		}),new TestCase("void f() {bool x = 1 !== 2;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(4, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(1).op);
			Assert.assertEquals(OpCode.RAWEQ, code.ops.get(2).op);
			Assert.assertEquals(OpCode.NOT, code.ops.get(3).op);
		}),new TestCase("void f() {int x = 1 == 2;}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("void f() {bool x = 1 is int}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(2, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(OpCode.INSTANCEOF, code.ops.get(1).op);
		}),new TestCase("void f() {bool x = 1 !is int}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(3, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(OpCode.INSTANCEOF, code.ops.get(1).op);
			Assert.assertEquals(OpCode.NOT, code.ops.get(2).op);
		}),new TestCase("void f() {int x = 1 is int}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("void f() {var v; int x = v is List<List<Number>>}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(1, code.ops.size());
			
			Assert.assertEquals(OpCode.INSTANCEOF, code.ops.get(0).op);
		}),new TestCase("void f() {int x = 1 ?? 2;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(5, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(OpCode.ISNULL, code.ops.get(1).op);
			Assert.assertEquals(OpCode.JUMPFALSE, code.ops.get(2).op);
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(3).op);
			Assert.assertEquals(OpCode.LABEL, code.ops.get(4).op);
		}),new TestCase("void f() {var x = 1 ?? 'c';}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
			
			Assert.assertEquals(5, code.ops.size());
			
			Assert.assertEquals(OpCode.MOVINT, code.ops.get(0).op);
			Assert.assertEquals(OpCode.ISNULL, code.ops.get(1).op);
			Assert.assertEquals(OpCode.JUMPFALSE, code.ops.get(2).op);
			Assert.assertEquals(OpCode.MOVCHAR, code.ops.get(3).op);
			Assert.assertEquals(OpCode.LABEL, code.ops.get(4).op);
		}),new TestCase("void f() {int x = 1 ?? 'c';}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("void f() {Number x = 1 ?? 1.0;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("void g<T>(T a) {} void f() {g(1);}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("void g<T : Number>(T a) {} void f() {g(1);}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("void g<T : int>(T a) {} void f() {g('c');}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		}),new TestCase("void g<T : Number>(T a, T b) {} void f() {g(1, 2);}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("void g<T : Number>(T a, T b) {} void f() {g(1, 2.0);}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}),new TestCase("void g<T : int>(T a, T b) {} void f() {g(1, 2.0);}", (code)->{
			Assert.assertEquals(1, code.tni.errors.size());
		})/*,new TestCase("T g<T>(T a) {} void f() {int x = g(1);}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		})*/);
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
