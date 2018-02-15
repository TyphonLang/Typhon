package info.iconmaster.typhon.types;

import java.util.Collection;

import org.junit.Assert;
import org.junit.runners.Parameterized;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.TyphonTest;
import info.iconmaster.typhon.linker.TyphonLinker;
import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.model.libs.CorePackage;

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
    	a.getTemplates().add(new TemplateType(tni, "X"));
    	a.getParentTypes().add(new TypeRef(p.TYPE_LIST, new TemplateArgument(a.getTemplates().get(0))));
    	
    	TemplateType f1 = new TemplateType(tni, "F1");
    	TemplateType f2 = new TemplateType(tni, "F2");
    	
    	ComboType c = new ComboType(tni);
    	c.getParentTypes().add(new TypeRef(p.TYPE_INT));
    	c.getParentTypes().add(new TypeRef(p.TYPE_FLOAT));
    	
    	ComboType d = new ComboType(tni);
    	d.getParentTypes().add(new TypeRef(p.TYPE_LIST, new TemplateArgument(p.TYPE_ANY)));
    	d.getParentTypes().add(new TypeRef(p.TYPE_NUMBER));
    	
    	UserType b = new UserType(tni, "b");
    	b.getParentTypes().add(new TypeRef(p.TYPE_LIST, new TemplateArgument(p.TYPE_ANY)));
    	b.getParentTypes().add(new TypeRef(p.TYPE_NUMBER));
    	
		return TyphonTest.makeData(
				new TestCase(p.TYPE_INT, p.TYPE_INT),
				new TestCase(p.TYPE_NUMBER, p.TYPE_NUMBER),
				new TestCase(p.TYPE_INT, p.TYPE_NUMBER),
				new TestCase(p.TYPE_INT, p.TYPE_ANY),
				new TestCase(p.TYPE_NUMBER, p.TYPE_ANY),
				new TestCase(a, p.TYPE_ANY),
				new TestCase(new TypeRef(a, new TemplateArgument(p.TYPE_INT)), new TypeRef(p.TYPE_ANY)),
				new TestCase(new TypeRef(a, new TemplateArgument(p.TYPE_INT)), new TypeRef(p.TYPE_LIST, new TemplateArgument(p.TYPE_INT))),
				new TestCase(new FunctionType(tni, new Type[] {}, new Type[] {}), p.TYPE_ANY),
				new TestCase(new FunctionType(tni, new Type[] {}, new Type[] {}), new FunctionType(tni, new Type[] {}, new Type[] {})),
				new TestCase(new FunctionType(tni, new Type[] {p.TYPE_INT}, new Type[] {}), new FunctionType(tni, new Type[] {p.TYPE_INT}, new Type[] {})),
				new TestCase(new FunctionType(tni, new Type[] {}, new Type[] {p.TYPE_INT}), new FunctionType(tni, new Type[] {}, new Type[] {p.TYPE_INT})),
				new TestCase(new FunctionType(tni, new Type[] {p.TYPE_ANY}, new Type[] {}), new FunctionType(tni, new Type[] {p.TYPE_INT}, new Type[] {})),
				new TestCase(new FunctionType(tni, new Type[] {}, new Type[] {p.TYPE_INT}), new FunctionType(tni, new Type[] {}, new Type[] {p.TYPE_ANY})),
				new TestCase(new FunctionType(tni, new Type[] {f1}, new Type[] {}, f1), new FunctionType(tni, new Type[] {f2}, new Type[] {}, f2)),
				new TestCase(c, p.TYPE_ANY),
				new TestCase(c, p.TYPE_INT),
				new TestCase(c, p.TYPE_FLOAT),
				new TestCase(c, c),
				new TestCase(b, d),
				new InvalidCase(p.TYPE_ANY, p.TYPE_INT),
				new InvalidCase(p.TYPE_NUMBER, p.TYPE_INT),
				new InvalidCase(p.TYPE_INT, p.TYPE_FLOAT),
				new InvalidCase(p.TYPE_ANY, p.TYPE_NUMBER),
				new InvalidCase(p.TYPE_INT, p.TYPE_BYTE),
				new InvalidCase(new FunctionType(tni, new Type[] {}, new Type[] {}), p.TYPE_INT),
				new InvalidCase(new FunctionType(tni, new Type[] {p.TYPE_INT}, new Type[] {}), new FunctionType(tni, new Type[] {}, new Type[] {})),
				new InvalidCase(new FunctionType(tni, new Type[] {}, new Type[] {}), new FunctionType(tni, new Type[] {}, new Type[] {p.TYPE_INT})),
				new InvalidCase(new FunctionType(tni, new Type[] {p.TYPE_FLOAT}, new Type[] {}), new FunctionType(tni, new Type[] {p.TYPE_INT}, new Type[] {})),
				new InvalidCase(new FunctionType(tni, new Type[] {}, new Type[] {p.TYPE_FLOAT}), new FunctionType(tni, new Type[] {}, new Type[] {p.TYPE_INT})),
				new InvalidCase(c, p.TYPE_BOOL),
				new InvalidCase(p.TYPE_INT, c),
				new InvalidCase(d, b),
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
