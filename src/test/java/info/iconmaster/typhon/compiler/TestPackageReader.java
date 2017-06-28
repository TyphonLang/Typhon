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
		}),new CaseValid("package q;", (p)->{
			Assert.assertNull(p.name);
			Assert.assertNotNull(p.parent);
			
			Assert.assertTrue(p.fields.isEmpty());
			Assert.assertTrue(p.functions.isEmpty());
			Assert.assertTrue(p.imports.isEmpty());
			Assert.assertEquals(1, p.subpackages.size());
			Assert.assertTrue(p.types.isEmpty());
			
			Package q = p.subpackages.get(0);
			
			Assert.assertEquals("q", q.name);
			Assert.assertEquals(p, q.parent);
			
			Assert.assertTrue(q.fields.isEmpty());
			Assert.assertTrue(q.functions.isEmpty());
			Assert.assertTrue(q.imports.isEmpty());
			Assert.assertTrue(q.subpackages.isEmpty());
			Assert.assertTrue(q.types.isEmpty());
		}),new CaseValid("package q {}", (p)->{
			Assert.assertNull(p.name);
			Assert.assertNotNull(p.parent);
			
			Assert.assertTrue(p.fields.isEmpty());
			Assert.assertTrue(p.functions.isEmpty());
			Assert.assertTrue(p.imports.isEmpty());
			Assert.assertEquals(1, p.subpackages.size());
			Assert.assertTrue(p.types.isEmpty());
			
			Package q = p.subpackages.get(0);
			
			Assert.assertEquals("q", q.name);
			Assert.assertEquals(p, q.parent);
			
			Assert.assertTrue(q.fields.isEmpty());
			Assert.assertTrue(q.functions.isEmpty());
			Assert.assertTrue(q.imports.isEmpty());
			Assert.assertTrue(q.subpackages.isEmpty());
			Assert.assertTrue(q.types.isEmpty());
		}),new CaseValid("package q; package r;", (p)->{
			Assert.assertNull(p.name);
			Assert.assertNotNull(p.parent);
			
			Assert.assertTrue(p.fields.isEmpty());
			Assert.assertTrue(p.functions.isEmpty());
			Assert.assertTrue(p.imports.isEmpty());
			Assert.assertEquals(1, p.subpackages.size());
			Assert.assertTrue(p.types.isEmpty());
			
			Package q = p.subpackages.get(0);
			
			Assert.assertEquals("q", q.name);
			Assert.assertEquals(p, q.parent);
			
			Assert.assertTrue(q.fields.isEmpty());
			Assert.assertTrue(q.functions.isEmpty());
			Assert.assertTrue(q.imports.isEmpty());
			Assert.assertEquals(1, q.subpackages.size());
			Assert.assertTrue(q.types.isEmpty());
			
			Package r = q.subpackages.get(0);
			
			Assert.assertEquals("r", r.name);
			Assert.assertEquals(q, r.parent);
			
			Assert.assertTrue(r.fields.isEmpty());
			Assert.assertTrue(r.functions.isEmpty());
			Assert.assertTrue(r.imports.isEmpty());
			Assert.assertEquals(0, r.subpackages.size());
			Assert.assertTrue(r.types.isEmpty());
		}),new CaseValid("package q {package r {}}", (p)->{
			Assert.assertNull(p.name);
			Assert.assertNotNull(p.parent);
			
			Assert.assertTrue(p.fields.isEmpty());
			Assert.assertTrue(p.functions.isEmpty());
			Assert.assertTrue(p.imports.isEmpty());
			Assert.assertEquals(1, p.subpackages.size());
			Assert.assertTrue(p.types.isEmpty());
			
			Package q = p.subpackages.get(0);
			
			Assert.assertEquals("q", q.name);
			Assert.assertEquals(p, q.parent);
			
			Assert.assertTrue(q.fields.isEmpty());
			Assert.assertTrue(q.functions.isEmpty());
			Assert.assertTrue(q.imports.isEmpty());
			Assert.assertEquals(1, q.subpackages.size());
			Assert.assertTrue(q.types.isEmpty());
			
			Package r = q.subpackages.get(0);
			
			Assert.assertEquals("r", r.name);
			Assert.assertEquals(q, r.parent);
			
			Assert.assertTrue(r.fields.isEmpty());
			Assert.assertTrue(r.functions.isEmpty());
			Assert.assertTrue(r.imports.isEmpty());
			Assert.assertEquals(0, r.subpackages.size());
			Assert.assertTrue(r.types.isEmpty());
		}),new CaseValid("package q.r;", (p)->{
			Assert.assertNull(p.name);
			Assert.assertNotNull(p.parent);
			
			Assert.assertTrue(p.fields.isEmpty());
			Assert.assertTrue(p.functions.isEmpty());
			Assert.assertTrue(p.imports.isEmpty());
			Assert.assertEquals(1, p.subpackages.size());
			Assert.assertTrue(p.types.isEmpty());
			
			Package q = p.subpackages.get(0);
			
			Assert.assertEquals("q", q.name);
			Assert.assertEquals(p, q.parent);
			
			Assert.assertTrue(q.fields.isEmpty());
			Assert.assertTrue(q.functions.isEmpty());
			Assert.assertTrue(q.imports.isEmpty());
			Assert.assertEquals(1, q.subpackages.size());
			Assert.assertTrue(q.types.isEmpty());
			
			Package r = q.subpackages.get(0);
			
			Assert.assertEquals("r", r.name);
			Assert.assertEquals(q, r.parent);
			
			Assert.assertTrue(r.fields.isEmpty());
			Assert.assertTrue(r.functions.isEmpty());
			Assert.assertTrue(r.imports.isEmpty());
			Assert.assertEquals(0, r.subpackages.size());
			Assert.assertTrue(r.types.isEmpty());
		}),new CaseValid("package q.r {}", (p)->{
			Assert.assertNull(p.name);
			Assert.assertNotNull(p.parent);
			
			Assert.assertTrue(p.fields.isEmpty());
			Assert.assertTrue(p.functions.isEmpty());
			Assert.assertTrue(p.imports.isEmpty());
			Assert.assertEquals(1, p.subpackages.size());
			Assert.assertTrue(p.types.isEmpty());
			
			Package q = p.subpackages.get(0);
			
			Assert.assertEquals("q", q.name);
			Assert.assertEquals(p, q.parent);
			
			Assert.assertTrue(q.fields.isEmpty());
			Assert.assertTrue(q.functions.isEmpty());
			Assert.assertTrue(q.imports.isEmpty());
			Assert.assertEquals(1, q.subpackages.size());
			Assert.assertTrue(q.types.isEmpty());
			
			Package r = q.subpackages.get(0);
			
			Assert.assertEquals("r", r.name);
			Assert.assertEquals(q, r.parent);
			
			Assert.assertTrue(r.fields.isEmpty());
			Assert.assertTrue(r.functions.isEmpty());
			Assert.assertTrue(r.imports.isEmpty());
			Assert.assertEquals(0, r.subpackages.size());
			Assert.assertTrue(r.types.isEmpty());
		}),new CaseValid("package q.r.s {}", (p)->{
			Assert.assertNull(p.name);
			Assert.assertNotNull(p.parent);
			
			Assert.assertTrue(p.fields.isEmpty());
			Assert.assertTrue(p.functions.isEmpty());
			Assert.assertTrue(p.imports.isEmpty());
			Assert.assertEquals(1, p.subpackages.size());
			Assert.assertTrue(p.types.isEmpty());
			
			Package q = p.subpackages.get(0);
			
			Assert.assertEquals("q", q.name);
			Assert.assertEquals(p, q.parent);
			
			Assert.assertTrue(q.fields.isEmpty());
			Assert.assertTrue(q.functions.isEmpty());
			Assert.assertTrue(q.imports.isEmpty());
			Assert.assertEquals(1, q.subpackages.size());
			Assert.assertTrue(q.types.isEmpty());
			
			Package r = q.subpackages.get(0);
			
			Assert.assertEquals("r", r.name);
			Assert.assertEquals(q, r.parent);
			
			Assert.assertTrue(r.fields.isEmpty());
			Assert.assertTrue(r.functions.isEmpty());
			Assert.assertTrue(r.imports.isEmpty());
			Assert.assertEquals(1, r.subpackages.size());
			Assert.assertTrue(r.types.isEmpty());
			
			Package s = r.subpackages.get(0);
			
			Assert.assertEquals("s", s.name);
			Assert.assertEquals(r, s.parent);
			
			Assert.assertTrue(s.fields.isEmpty());
			Assert.assertTrue(s.functions.isEmpty());
			Assert.assertTrue(s.imports.isEmpty());
			Assert.assertEquals(0, s.subpackages.size());
			Assert.assertTrue(s.types.isEmpty());
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
