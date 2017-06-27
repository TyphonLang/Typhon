package info.iconmaster.typhon.compiler;

import java.util.Collection;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.runners.Parameterized;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.TyphonTest;
import info.iconmaster.typhon.language.Package;

public class TestPackageReader extends TyphonTest {
	@Parameterized.Parameters
    public static Collection<Object[]> data() {
		return TyphonTest.makeData(new CaseValid("", (p)->{
			Assert.assertNull(p.name);
			Assert.assertNotNull(p.parent);
			
			Assert.assertNull(p.parent.name);
			Assert.assertNull(p.parent.parent);
			
			Assert.assertTrue(p.fields.isEmpty());
			Assert.assertTrue(p.functions.isEmpty());
			Assert.assertTrue(p.imports.isEmpty());
			Assert.assertTrue(p.subpackages.isEmpty());
			Assert.assertTrue(p.types.isEmpty());
		}));
	}
    
    private static class CaseValid implements Runnable {
    	String input;
    	Consumer<Package> test;
    	
		public CaseValid(String input, Consumer<Package> test) {
			this.input = input;
			this.test = test;
		}
		
		@Override
		public void run() {
			TyphonInput tni = new TyphonInput();
			test.accept(TyphonSourceReader.parseString(tni, input));
		}
    }
}
