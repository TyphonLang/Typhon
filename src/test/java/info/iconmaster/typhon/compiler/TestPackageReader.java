package info.iconmaster.typhon.compiler;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.runners.Parameterized;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.TyphonTest;
import info.iconmaster.typhon.errors.TyphonError;
import info.iconmaster.typhon.language.Package;

/**
 * Tests <tt>{@link TyphonSourceReader}.readPackage()</tt>.
 * 
 * @author iconmaster
 *
 */
public class TestPackageReader extends TyphonTest {
	@Parameterized.Parameters
    public static Collection<Object[]> data() {
		return TyphonTest.makeData(new CaseValid("", (p)->{
			Assert.assertEquals("", p.getName());
			Assert.assertNotNull(p.getParent());
			
			Assert.assertEquals("core", p.getParent().getName());
			Assert.assertNull(p.getParent().getParent());
			
			Assert.assertEquals(0, p.getFields().size());
			Assert.assertEquals(0, p.getFunctions().size());
			Assert.assertEquals(0, p.getImports().size());
			Assert.assertEquals(0, p.getSubpackges().size());
			Assert.assertEquals(0, p.getTypes().size());
		}),new CaseValid("\n", (p)->{
			Assert.assertEquals("", p.getName());
			Assert.assertNotNull(p.getParent());
			
			Assert.assertEquals("core", p.getParent().getName());
			Assert.assertNull(p.getParent().getParent());
			
			Assert.assertEquals(0, p.getFields().size());
			Assert.assertEquals(0, p.getFunctions().size());
			Assert.assertEquals(0, p.getImports().size());
			Assert.assertEquals(0, p.getSubpackges().size());
			Assert.assertEquals(0, p.getTypes().size());
		}),new CaseValid("package q;", (p)->{
			Assert.assertEquals("", p.getName());
			Assert.assertNotNull(p.getParent());
			
			Assert.assertEquals(0, p.getFields().size());
			Assert.assertEquals(0, p.getFunctions().size());
			Assert.assertEquals(0, p.getImports().size());
			Assert.assertEquals(1, p.getSubpackges().size());
			Assert.assertEquals(0, p.getTypes().size());
			
			Package q = p.getSubpackges().get(0);
			
			Assert.assertEquals("q", q.getName());
			Assert.assertEquals(p, q.getParent());
			
			Assert.assertEquals(0, q.getFields().size());
			Assert.assertEquals(0, q.getFunctions().size());
			Assert.assertEquals(0, q.getImports().size());
			Assert.assertEquals(0, q.getSubpackges().size());
			Assert.assertEquals(0, q.getTypes().size());
		}),new CaseValid("package q {}", (p)->{
			Assert.assertEquals("", p.getName());
			Assert.assertNotNull(p.getParent());
			
			Assert.assertEquals(0, p.getFields().size());
			Assert.assertEquals(0, p.getFunctions().size());
			Assert.assertEquals(0, p.getImports().size());
			Assert.assertEquals(1, p.getSubpackges().size());
			Assert.assertEquals(0, p.getTypes().size());
			
			Package q = p.getSubpackges().get(0);
			
			Assert.assertEquals("q", q.getName());
			Assert.assertEquals(p, q.getParent());
			
			Assert.assertEquals(0, q.getFields().size());
			Assert.assertEquals(0, q.getFunctions().size());
			Assert.assertEquals(0, q.getImports().size());
			Assert.assertEquals(0, q.getSubpackges().size());
			Assert.assertEquals(0, q.getTypes().size());
		}),new CaseValid("package q; package r;", (p)->{
			Assert.assertEquals("", p.getName());
			Assert.assertNotNull(p.getParent());
			
			Assert.assertEquals(0, p.getFields().size());
			Assert.assertEquals(0, p.getFunctions().size());
			Assert.assertEquals(0, p.getImports().size());
			Assert.assertEquals(1, p.getSubpackges().size());
			Assert.assertEquals(0, p.getTypes().size());
			
			Package q = p.getSubpackges().get(0);
			
			Assert.assertEquals("q", q.getName());
			Assert.assertEquals(p, q.getParent());
			
			Assert.assertEquals(0, q.getFields().size());
			Assert.assertEquals(0, q.getFunctions().size());
			Assert.assertEquals(0, q.getImports().size());
			Assert.assertEquals(1, q.getSubpackges().size());
			Assert.assertEquals(0, q.getTypes().size());
			
			Package r = q.getSubpackges().get(0);
			
			Assert.assertEquals("r", r.getName());
			Assert.assertEquals(q, r.getParent());
			
			Assert.assertEquals(0, r.getFields().size());
			Assert.assertEquals(0, r.getFunctions().size());
			Assert.assertEquals(0, r.getImports().size());
			Assert.assertEquals(0, r.getSubpackges().size());
			Assert.assertEquals(0, r.getTypes().size());
		}),new CaseValid("package q {package r {}}", (p)->{
			Assert.assertEquals("", p.getName());
			Assert.assertNotNull(p.getParent());
			
			Assert.assertEquals(0, p.getFields().size());
			Assert.assertEquals(0, p.getFunctions().size());
			Assert.assertEquals(0, p.getImports().size());
			Assert.assertEquals(1, p.getSubpackges().size());
			Assert.assertEquals(0, p.getTypes().size());
			
			Package q = p.getSubpackges().get(0);
			
			Assert.assertEquals("q", q.getName());
			Assert.assertEquals(p, q.getParent());
			
			Assert.assertEquals(0, q.getFields().size());
			Assert.assertEquals(0, q.getFunctions().size());
			Assert.assertEquals(0, q.getImports().size());
			Assert.assertEquals(1, q.getSubpackges().size());
			Assert.assertEquals(0, q.getTypes().size());
			
			Package r = q.getSubpackges().get(0);
			
			Assert.assertEquals("r", r.getName());
			Assert.assertEquals(q, r.getParent());
			
			Assert.assertEquals(0, r.getFields().size());
			Assert.assertEquals(0, r.getFunctions().size());
			Assert.assertEquals(0, r.getImports().size());
			Assert.assertEquals(0, r.getSubpackges().size());
			Assert.assertEquals(0, r.getTypes().size());
		}),new CaseValid("package q.r;", (p)->{
			Assert.assertEquals("", p.getName());
			Assert.assertNotNull(p.getParent());
			
			Assert.assertEquals(0, p.getFields().size());
			Assert.assertEquals(0, p.getFunctions().size());
			Assert.assertEquals(0, p.getImports().size());
			Assert.assertEquals(1, p.getSubpackges().size());
			Assert.assertEquals(0, p.getTypes().size());
			
			Package q = p.getSubpackges().get(0);
			
			Assert.assertEquals("q", q.getName());
			Assert.assertEquals(p, q.getParent());
			
			Assert.assertEquals(0, q.getFields().size());
			Assert.assertEquals(0, q.getFunctions().size());
			Assert.assertEquals(0, q.getImports().size());
			Assert.assertEquals(1, q.getSubpackges().size());
			Assert.assertEquals(0, q.getTypes().size());
			
			Package r = q.getSubpackges().get(0);
			
			Assert.assertEquals("r", r.getName());
			Assert.assertEquals(q, r.getParent());
			
			Assert.assertEquals(0, r.getFields().size());
			Assert.assertEquals(0, r.getFunctions().size());
			Assert.assertEquals(0, r.getImports().size());
			Assert.assertEquals(0, r.getSubpackges().size());
			Assert.assertEquals(0, r.getTypes().size());
		}),new CaseValid("package q.r {}", (p)->{
			Assert.assertEquals("", p.getName());
			Assert.assertNotNull(p.getParent());
			
			Assert.assertEquals(0, p.getFields().size());
			Assert.assertEquals(0, p.getFunctions().size());
			Assert.assertEquals(0, p.getImports().size());
			Assert.assertEquals(1, p.getSubpackges().size());
			Assert.assertEquals(0, p.getTypes().size());
			
			Package q = p.getSubpackges().get(0);
			
			Assert.assertEquals("q", q.getName());
			Assert.assertEquals(p, q.getParent());
			
			Assert.assertEquals(0, q.getFields().size());
			Assert.assertEquals(0, q.getFunctions().size());
			Assert.assertEquals(0, q.getImports().size());
			Assert.assertEquals(1, q.getSubpackges().size());
			Assert.assertEquals(0, q.getTypes().size());
			
			Package r = q.getSubpackges().get(0);
			
			Assert.assertEquals("r", r.getName());
			Assert.assertEquals(q, r.getParent());
			
			Assert.assertEquals(0, r.getFields().size());
			Assert.assertEquals(0, r.getFunctions().size());
			Assert.assertEquals(0, r.getImports().size());
			Assert.assertEquals(0, r.getSubpackges().size());
			Assert.assertEquals(0, r.getTypes().size());
		}),new CaseValid("package q.r.s {}", (p)->{
			Assert.assertEquals("", p.getName());
			Assert.assertNotNull(p.getParent());
			
			Assert.assertEquals(0, p.getFields().size());
			Assert.assertEquals(0, p.getFunctions().size());
			Assert.assertEquals(0, p.getImports().size());
			Assert.assertEquals(1, p.getSubpackges().size());
			Assert.assertEquals(0, p.getTypes().size());
			
			Package q = p.getSubpackges().get(0);
			
			Assert.assertEquals("q", q.getName());
			Assert.assertEquals(p, q.getParent());
			
			Assert.assertEquals(0, q.getFields().size());
			Assert.assertEquals(0, q.getFunctions().size());
			Assert.assertEquals(0, q.getImports().size());
			Assert.assertEquals(1, q.getSubpackges().size());
			Assert.assertEquals(0, q.getTypes().size());
			
			Package r = q.getSubpackges().get(0);
			
			Assert.assertEquals("r", r.getName());
			Assert.assertEquals(q, r.getParent());
			
			Assert.assertEquals(0, r.getFields().size());
			Assert.assertEquals(0, r.getFunctions().size());
			Assert.assertEquals(0, r.getImports().size());
			Assert.assertEquals(1, r.getSubpackges().size());
			Assert.assertEquals(0, r.getTypes().size());
			
			Package s = r.getSubpackges().get(0);
			
			Assert.assertEquals("s", s.getName());
			Assert.assertEquals(r, s.getParent());
			
			Assert.assertEquals(0, s.getFields().size());
			Assert.assertEquals(0, s.getFunctions().size());
			Assert.assertEquals(0, s.getImports().size());
			Assert.assertEquals(0, s.getSubpackges().size());
			Assert.assertEquals(0, s.getTypes().size());
		}),new CaseValid("package q;", (p)->{
			Assert.assertEquals(1, p.getSubpackges().size());
			
			Assert.assertNotNull(p.source);
			Assert.assertEquals(0, p.source.begin);
			Assert.assertEquals(9, p.source.end);
			Assert.assertEquals("<unknown>", p.source.file);
			
			Package q = p.getSubpackges().get(0);
			
			Assert.assertNotNull(q.source);
			Assert.assertEquals(0, q.source.begin);
			Assert.assertEquals(9, q.source.end);
			Assert.assertEquals("<unknown>", q.source.file);
		}),new CaseValid("package q {package r;}", (p)->{
			Assert.assertEquals(1, p.getSubpackges().size());
			
			Assert.assertNotNull(p.source);
			Assert.assertEquals(0, p.source.begin);
			Assert.assertEquals(21, p.source.end);
			Assert.assertEquals("<unknown>", p.source.file);
			
			Package q = p.getSubpackges().get(0);
			Assert.assertEquals(1, q.getSubpackges().size());
			
			Assert.assertNotNull(q.source);
			Assert.assertEquals(0, q.source.begin);
			Assert.assertEquals(21, q.source.end);
			Assert.assertEquals("<unknown>", q.source.file);
			
			Package r = q.getSubpackges().get(0);
			
			Assert.assertNotNull(r.source);
			Assert.assertEquals(11, r.source.begin);
			Assert.assertEquals(20, r.source.end);
			Assert.assertEquals("<unknown>", r.source.file);
		}),new CaseValid("package q {} package r {}", (p)->{
			Assert.assertEquals("", p.getName());
			Assert.assertNotNull(p.getParent());
			
			Assert.assertEquals(0, p.getFields().size());
			Assert.assertEquals(0, p.getFunctions().size());
			Assert.assertEquals(0, p.getImports().size());
			Assert.assertEquals(2, p.getSubpackges().size());
			Assert.assertEquals(0, p.getTypes().size());
			
			List<Package> ps = p.getSubpackges();
			Assert.assertTrue(ps.stream().anyMatch((pp)->pp.getName().equals("q")));
			Assert.assertTrue(ps.stream().anyMatch((pp)->pp.getName().equals("r")));
			Assert.assertTrue(ps.stream().allMatch((pp)->pp.getSubpackges().size() == 0));
		}),new CaseValid("package q {} package q {}", (p)->{
			Assert.assertEquals("", p.getName());
			Assert.assertNotNull(p.getParent());
			
			Assert.assertEquals(0, p.getFields().size());
			Assert.assertEquals(0, p.getFunctions().size());
			Assert.assertEquals(0, p.getImports().size());
			Assert.assertEquals(2, p.getSubpackges().size());
			Assert.assertEquals(0, p.getTypes().size());
			
			List<Package> ps = p.getSubpackges();
			Assert.assertTrue(ps.stream().allMatch((pp)->pp.getName().equals("q")));
			Assert.assertTrue(ps.stream().allMatch((pp)->pp.getSubpackges().size() == 0));
		}),new CaseValid("package q {package r {}} package q {package r {}}", (p)->{
			Assert.assertEquals("", p.getName());
			Assert.assertNotNull(p.getParent());
			
			Assert.assertEquals(0, p.getFields().size());
			Assert.assertEquals(0, p.getFunctions().size());
			Assert.assertEquals(0, p.getImports().size());
			Assert.assertEquals(2, p.getSubpackges().size());
			Assert.assertEquals(0, p.getTypes().size());
			
			List<Package> ps = p.getSubpackges();
			Assert.assertTrue(ps.stream().allMatch((pp)->pp.getName().equals("q")));
			Assert.assertTrue(ps.stream().allMatch((pp)->pp.getSubpackges().size() == 1));
			Assert.assertTrue(ps.stream().allMatch((pp)->pp.getSubpackges().get(0).getName().equals("r")));
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
