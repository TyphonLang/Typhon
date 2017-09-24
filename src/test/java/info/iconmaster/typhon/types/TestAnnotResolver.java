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
public class TestAnnotResolver extends TyphonTest {
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
			test.accept(p);
		}
    }
}
