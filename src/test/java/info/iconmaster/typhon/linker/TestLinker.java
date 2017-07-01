package info.iconmaster.typhon.linker;

import java.util.Collection;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.runners.Parameterized;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.TyphonTest;
import info.iconmaster.typhon.compiler.TyphonSourceReader;
import info.iconmaster.typhon.language.Package;

/**
 * Tests <tt>{@link TyphonLinker}</tt>.
 * 
 * @author iconmaster
 *
 */
public class TestLinker extends TyphonTest {
	@Parameterized.Parameters
    public static Collection<Object[]> data() {
		return TyphonTest.makeData(new TestCase("import p; package p {}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			Assert.assertEquals(1, p.getImports().get(0).getResolvedTo().size());
			Assert.assertEquals(p.getSubpackges().get(0), p.getImports().get(0).getResolvedTo().get(0));
		}),new TestCase("import p.q; package p {package q {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			Assert.assertEquals(1, p.getImports().get(0).getResolvedTo().size());
			Assert.assertEquals(p.getSubpackges().get(0).getSubpackges().get(0), p.getImports().get(0).getResolvedTo().get(0));
		}),new TestCase("package i {import p.q;} package p {package q {}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			Assert.assertEquals(1, p.getSubpackagesWithName("i").get(0).getImports().get(0).getResolvedTo().size());
			Assert.assertEquals(p.getSubpackagesWithName("p").get(0).getSubpackges().get(0), p.getSubpackagesWithName("i").get(0).getImports().get(0).getResolvedTo().get(0));
		}),new TestCase("import p; package p {} package p {}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			Assert.assertEquals(2, p.getImports().get(0).getResolvedTo().size());
		}),new TestCase("package i {package p {} import p;} package p {}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			Assert.assertEquals(1, p.getSubpackagesWithName("i").get(0).getImports().get(0).getResolvedTo().size());
			Assert.assertEquals(p.getSubpackagesWithName("i").get(0).getSubpackges().get(0), p.getSubpackagesWithName("i").get(0).getImports().get(0).getResolvedTo().get(0));
		}),new TestCase("package i {package p {} package p {} import p;} package p {}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			Assert.assertEquals(2, p.getSubpackagesWithName("i").get(0).getImports().get(0).getResolvedTo().size());
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
			Package p = TyphonSourceReader.parseString(tni, input);
			TyphonLinker.link(p);
			test.accept(p);
		}
    }
}
