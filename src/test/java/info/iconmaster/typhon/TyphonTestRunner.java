package info.iconmaster.typhon;

import org.junit.Ignore;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;

/**
 * The test runner for {@link TyphonTest}.
 * 
 * @author iconmaster
 *
 */
@Ignore
public class TyphonTestRunner extends Parameterized {
	public TyphonTestRunner(Class<?> klass) throws Throwable {
		super(klass);
	}

	@Override
	public void run(RunNotifier notifier) {
		//System.out.println("==== "+getDescription().getTestClass().getSimpleName()+" ====");
		int i = 0;
		for (Runner test : getChildren()) {
			//System.out.println("== test "+(i+1)+" ==");
			test.run(notifier);
			i++;
		}
		//System.out.println();
	}
}
