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
public class TestCommonType extends TyphonTest {
	@Parameterized.Parameters
    public static Collection<Object[]> data() {
    	TyphonInput tni = new TyphonInput();
    	CorePackage p = tni.corePackage;
    	
    	UserType a = new UserType(tni, "a");
    	a.getParentTypes().add(new TypeRef(p.TYPE_NUMBER));
    	
    	UserType b = new UserType(tni, "b");
    	b.getParentTypes().add(new TypeRef(p.TYPE_NUMBER));
    	
    	UserType c = new UserType(tni, "c");
    	c.getParentTypes().add(new TypeRef(a));
    	c.getParentTypes().add(new TypeRef(b));
    	
    	UserType d = new UserType(tni, "d");
    	d.getParentTypes().add(new TypeRef(a));
    	d.getParentTypes().add(new TypeRef(b));
    	
    	UserType e = new UserType(tni, "e");
    	e.getParentTypes().add(new TypeRef(a));
    	e.getParentTypes().add(new TypeRef(b));
    	e.getParentTypes().add(new TypeRef(p.TYPE_LIST));
    	
		return TyphonTest.makeData(
				new TestCase(p.TYPE_ANY, p.TYPE_BOOL, p.TYPE_ANY),
				new TestCase(p.TYPE_BOOL, p.TYPE_ANY, p.TYPE_ANY),
				new TestCase(p.TYPE_INT, p.TYPE_INTEGER, p.TYPE_INTEGER),
				new TestCase(p.TYPE_INT, p.TYPE_NUMBER, p.TYPE_NUMBER),
				new TestCase(a, p.TYPE_INT, p.TYPE_NUMBER),
				new TestCase(a, p.TYPE_NUMBER, p.TYPE_NUMBER),
				new TestCase(a, p.TYPE_ANY, p.TYPE_ANY),
				new TestCase(a, p.TYPE_BOOL, p.TYPE_ANY),
				new TestCase(a, b, p.TYPE_NUMBER),
				new TestCase(c, a, a),
				new TestCase(c, b, b),
				new TestCase(a, c, a),
				new TestCase(b, c, b),
				new TestCase(c, d, new ComboType(tni, a, b)),
				new TestCase(c, e, new ComboType(tni, a, b)),
				new TestCase(e, d, new ComboType(tni, a, b)),
				new TestCase(new ComboType(tni, a, b), d, new ComboType(tni, a, b)),
				new TestCase(new ComboType(tni, a, b), e, new ComboType(tni, a, b)),
				new TestCase(new ComboType(tni, a, b), p.TYPE_NUMBER, p.TYPE_NUMBER),
				new TestCase(p.TYPE_ANY, p.TYPE_ANY, p.TYPE_ANY)
		);
	}
    
    private static class TestCase implements Runnable {
    	TypeRef a;
    	TypeRef b;
    	TypeRef expected;
    	
		public TestCase(TypeRef a, TypeRef b, TypeRef expected) {
			this.a = a;
			this.b = b;
			this.expected = expected;
		}
		
		public TestCase(Type a, Type b, Type expected) {
			this.a = new TypeRef(a);
			this.b = new TypeRef(b);
			this.expected = new TypeRef(expected);
		}
		
		@Override
		public void run() {
			Assert.assertEquals(expected, a.commonType(b));
		}
    }
    
    private static class InvalidCase implements Runnable {
    	TypeRef a;
    	TypeRef b;
    	TypeRef expected;
    	
		public InvalidCase(TypeRef a, TypeRef b, TypeRef expected) {
			this.a = a;
			this.b = b;
			this.expected = expected;
		}
		
		public InvalidCase(Type a, Type b, Type expected) {
			this.a = new TypeRef(a);
			this.b = new TypeRef(b);
			this.expected = new TypeRef(expected);
		}
		
		@Override
		public void run() {
			Assert.assertNotEquals(expected, a.commonType(b));
		}
    }
}
