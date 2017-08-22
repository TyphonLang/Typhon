package info.iconmaster.typhon.types;

import java.util.Collection;

import org.junit.Assert;
import org.junit.runners.Parameterized;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.TyphonTest;
import info.iconmaster.typhon.linker.TyphonLinker;
import info.iconmaster.typhon.model.CorePackage;

/**
 * Tests <tt>{@link TyphonLinker}</tt>.
 * 
 * @author iconmaster
 *
 */
public class TestCastingRules extends TyphonTest {
	@Parameterized.Parameters
    public static Collection<Object[]> data() {
    	TyphonInput tni = new TyphonInput();
    	CorePackage p = tni.corePackage;
    	
		return TyphonTest.makeData(
				new TestCase(p.TYPE_INT, p.TYPE_INT),
				new TestCase(p.TYPE_NUMBER, p.TYPE_NUMBER),
				new TestCase(p.TYPE_INT, p.TYPE_NUMBER),
				new TestCase(p.TYPE_INT, p.TYPE_ANY),
				new TestCase(p.TYPE_NUMBER, p.TYPE_ANY),
				new InvalidCase(p.TYPE_ANY, p.TYPE_INT),
				new InvalidCase(p.TYPE_NUMBER, p.TYPE_INT),
				new InvalidCase(p.TYPE_INT, p.TYPE_FLOAT),
				new InvalidCase(p.TYPE_ANY, p.TYPE_NUMBER),
				new TestCase(p.TYPE_ANY, p.TYPE_ANY)
		);
	}
    
    private static class TestCase implements Runnable {
    	TypeRef a;
    	TypeRef b;
    	
		public TestCase(TypeRef a, TypeRef b) {
			this.a = a;
			this.b = b;
		}
		
		public TestCase(Type a, Type b) {
			this.a = new TypeRef(a);
			this.b = new TypeRef(b);
		}
		
		@Override
		public void run() {
			Assert.assertTrue(a.canCastTo(b));
		}
    }
    
    private static class InvalidCase implements Runnable {
    	TypeRef a;
    	TypeRef b;
    	
		public InvalidCase(TypeRef a, TypeRef b) {
			this.a = a;
			this.b = b;
		}
		
		public InvalidCase(Type a, Type b) {
			this.a = new TypeRef(a);
			this.b = new TypeRef(b);
		}
		
		@Override
		public void run() {
			Assert.assertFalse(a.canCastTo(b));
		}
    }
}
