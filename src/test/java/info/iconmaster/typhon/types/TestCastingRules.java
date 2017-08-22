package info.iconmaster.typhon.types;

import java.util.Collection;

import org.junit.Assert;
import org.junit.runners.Parameterized;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.TyphonTest;
import info.iconmaster.typhon.linker.TyphonLinker;
import info.iconmaster.typhon.model.CorePackage;
import info.iconmaster.typhon.model.TemplateArgument;

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
    	
    	UserType a = new UserType(tni, "a");
    	a.getTemplates().add(new TemplateType(tni, "T"));
    	a.getParentTypes().add(new TypeRef(p.TYPE_LIST, new TemplateArgument(a.getTemplates().get(0))));
    	
		return TyphonTest.makeData(
				new TestCase(p.TYPE_INT, p.TYPE_INT),
				new TestCase(p.TYPE_NUMBER, p.TYPE_NUMBER),
				new TestCase(p.TYPE_INT, p.TYPE_NUMBER),
				new TestCase(p.TYPE_INT, p.TYPE_ANY),
				new TestCase(p.TYPE_NUMBER, p.TYPE_ANY),
				new TestCase(a, p.TYPE_ANY),
				new TestCase(new TypeRef(a, new TemplateArgument(p.TYPE_INT)), new TypeRef(p.TYPE_LIST, new TemplateArgument(p.TYPE_INT))),
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
