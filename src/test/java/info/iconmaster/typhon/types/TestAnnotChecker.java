package info.iconmaster.typhon.types;

import java.util.Collection;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.runners.Parameterized;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.TyphonTest;
import info.iconmaster.typhon.linker.TyphonLinker;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.TyphonModelReader;

/**
 * Tests <tt>{@link TyphonLinker}</tt>.
 * 
 * @author iconmaster
 *
 */
public class TestAnnotChecker extends TyphonTest {
	@Parameterized.Parameters
    public static Collection<Object[]> data() {
		return TyphonTest.makeData(new TestCase("int @main x;", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertEquals(p.tni.corePackage.getAnnotDefsWithName("main").get(0), p.getField("x").getAnnots().get(0).getDefinition());
		}),new TestCase("int @nonexist x;", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("void f([int] @vararg x);", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("void f(int @vararg x);", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("void f([int] @vararg @vararg x);", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("void f([int] @vararg x, [float] @vararg y);", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("void f([int] @vararg x = []);", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("void f(int a, [int] @vararg x, int b);", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("void f({string:int} @varflag x);", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("void f(int @varflag x);", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("void f({string:int} @varflag @varflag x);", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("void f({string:int} @varflag x, {string:float} @varflag y);", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("void f({string:int} @varflag x = {});", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("void f(int a, {string:int} @varflag x, int b);", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("void f({int:string} @varflag x);", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("void f(var @vararg @varflag x);", (p)->{
			Assert.assertEquals(2, p.tni.errors.size());
		}),new TestCase("void f(int a, [int] @vararg x, {string:int} @varflag y, int b);", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a {void virtual() {}} class b : a {@override void virtual() {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a {void virtual(int a) {}} class b : a {@override void virtual(int a) {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a {void virtual(int a) {}} class b : a {@override void virtual(int b) {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a {void virtual(int a, int b) {}} class b : a {@override void virtual(int a, int b) {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a {int virtual() {}} class b : a {@override int virtual() {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a {(int, float) virtual() {}} class b : a {@override (int, float) virtual() {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a {void virtual(int a) {}} class b : a {@override void virtual() {}}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class a {void virtual() {}} class b : a {@override void virtual(int a) {}}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class a {void virtual() {}} class b : a {@override void override() {}}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class a {int virtual() {}} class b : a {@override void virtual() {}}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class a {void virtual() {}} class b : a {@override int virtual() {}}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class a {int virtual() {}} class b : a {@override float virtual() {}}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class a {void virtual(int a) {}} class b : a {@override void virtual(float a) {}}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class a {void virtual() {}} class b : a {@override void virtual() {}} class c : b {@override void virtual() {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a {void virtual() {}} class b : a {} class c : b {@override void virtual() {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("int x; @getter int x() {}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a {int x; @getter int x() {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("@getter int x() {}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a {@getter int x() {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("int x; @setter void x(int arg) {}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a{int x; @setter void x(int arg) {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("@setter void x(int arg) {}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a{@setter void x(int arg) {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("int x; @getter float x() {}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("int x; @getter int x(Any arg) {}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("int x; @getter (int,int) x() {}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("int x; @getter void x() {}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class a {int @static x; @getter int x() {}}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class a {int x; @static @getter int x() {}}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class a {@static @getter int x() {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("int x; @setter void x(float arg) {}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("int x; @setter Any x(int arg) {}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("int x; @setter void x(int a, int b) {}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("int x; @setter void x() {}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class a {int @static x; @setter x(int arg) {}}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class a {int x; @static @setter x(int arg) {}}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class a {@static @setter void x(int arg) {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a<T> {T virtual() {}} class b : a<int> {@override int virtual() {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a<T> {void virtual(T t) {}} class b : a<int> {@override void virtual(int t) {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a<T> {T virtual() {}} class b : a<int> {@override float virtual() {}}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class a<T> {T virtual() {}} class b<T> : a<T> {@override T virtual() {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("import operator; class a : Iterable<int> {@override Iterator<int> iterator() {} @loop int f(Iterator<int> iter) {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("import operator; class a {@loop int f(Iterator<int> iter) {}}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("import operator; class a : Iterable<int> {@override Iterator<int> iterator() {} @loop int f() {}}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("import operator; class a : Iterable<int> {@override Iterator<int> iterator() {} @loop int f(Iterator<int> iter, int b) {}}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("import operator; class a : Iterable<int> {@override Iterator<int> iterator() {} @loop void f(Iterator<int> iter) {}}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("import operator; @loop int f(Iterator<int> iter) {}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("@abstract class a {@abstract void f();}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a {@abstract void f();}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("@abstract void f();", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class b {void f() {}} @abstract class a : b {@abstract @override void f();}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("@abstract class a {@abstract void f();} @abstract class b : a {}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("@abstract class a {@abstract void f();} class b : a {}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("@abstract class a {@abstract void f();} class b : a {@override void f() {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}));
	}
    
    private static class TestCase implements Runnable {
    	String input;
    	Consumer<Package> test;
    	
		public TestCase(String input, Consumer<Package> test) {
			this.input = input;
			this.test = test;
		}
		
		@Override
		public void run() {
			TyphonInput tni = new TyphonInput();
			Package p = TyphonModelReader.parseString(tni, input);
			TyphonLinker.link(p);
			TyphonTypeResolver.resolve(p);
			TyphonAnnotChecker.check(p);
			test.accept(p);
		}
    }
}
