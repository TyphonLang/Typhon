package info.iconmaster.typhon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@Ignore
@RunWith(TyphonTestRunner.class)
public abstract class TyphonTest {
	@Parameterized.Parameters
    public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {});
	}
    
    public static Collection<Object[]> makeData(Runnable... testCases) {
    	ArrayList<Object[]> result = new ArrayList<>();
    	for (Runnable testCase : testCases) {
    		result.add(new Object[] {testCase});
    	}
    	return result;
    }
	
	@Parameterized.Parameter
	public Runnable r;
	
	@Test
	public void test() {
		r.run();
	}
}
