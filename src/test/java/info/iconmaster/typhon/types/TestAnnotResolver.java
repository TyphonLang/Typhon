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
