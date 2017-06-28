package info.iconmaster.typhon.compiler;

import java.util.Collection;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.runners.Parameterized;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.TyphonTest;
import info.iconmaster.typhon.errors.TyphonError;
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
		}),new CaseValid("\n", (p)->{
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
		}),new CaseValid("package q;", (p)->{
			Assert.assertEquals(1, p.subpackages.size());
			
			Assert.assertNotNull(p.source);
			Assert.assertEquals(0, p.source.begin);
			Assert.assertEquals(9, p.source.end);
			Assert.assertEquals("<unknown>", p.source.file);
			
			Package q = p.subpackages.get(0);
			
			Assert.assertNotNull(q.source);
			Assert.assertEquals(0, q.source.begin);
			Assert.assertEquals(9, q.source.end);
			Assert.assertEquals("<unknown>", q.source.file);
		}),new CaseValid("package q {package r;}", (p)->{
			Assert.assertEquals(1, p.subpackages.size());
			
			Assert.assertNotNull(p.source);
			Assert.assertEquals(0, p.source.begin);
			Assert.assertEquals(21, p.source.end);
			Assert.assertEquals("<unknown>", p.source.file);
			
			Package q = p.subpackages.get(0);
			Assert.assertEquals(1, q.subpackages.size());
			
			Assert.assertNotNull(q.source);
			Assert.assertEquals(0, q.source.begin);
			Assert.assertEquals(21, q.source.end);
			Assert.assertEquals("<unknown>", q.source.file);
			
			Package r = q.subpackages.get(0);
			
			Assert.assertNotNull(r.source);
			Assert.assertEquals(11, r.source.begin);
			Assert.assertEquals(20, r.source.end);
			Assert.assertEquals("<unknown>", r.source.file);
		}),
		new CaseInvalid("x", 0, 1),
		new CaseInvalid("aaa", 2, 3),
		new CaseInvalid("a.a", 1, 1),
		new CaseInvalid("package", 6, 7),
		new CaseInvalid("package 1", 8, 8),
		new CaseInvalid("package\n1", 8, 8),
		new CaseInvalid("package\n\n1", 9, 9));
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
    
    private static class CaseInvalid implements Runnable {
    	String input;
    	int begin,end;
    	
		public CaseInvalid(String input, int begin, int end) {
			this.input = input;
			this.begin = begin;
			this.end = end;
		}
		
		@Override
		public void run() {
			TyphonInput tni = new TyphonInput();
			TyphonSourceReader.parseString(tni, input);
			Assert.assertEquals("Input '"+input+"': Incorrect number of errors:", 1, tni.errors.size());
			TyphonError error = tni.errors.get(0);
			Assert.assertNotNull("Input '"+input+"': Source was null:", error.source);
			Assert.assertEquals("Input '"+input+"': Begin was incorrect:", begin, error.source.begin);
			Assert.assertEquals("Input '"+input+"': End was incorrect:", end, error.source.end);
		}
    }
}
