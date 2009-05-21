package org.sonatype.nexus.mock;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.FrameworkMethod;
import org.junit.internal.runners.statements.InvokeMethod;

/**
 * <p>
 * A special JUnit runner that provides additional behaviors specific to Selenium test cases. This runner adds the
 * following behavior:
 * </p>
 *
 * <ul>
 * <li>The test class gets it's JUnit description injected prior to test evaluation (see {@link SeleniumTest#setDescription(org.junit.runner.Description)}).</li>
 * <li>If a test fails, a screenshot is automatically taken with the name "FAILURE" appended to the end of it.</li>
 * </ul>
 *
 * <p>
 * NOTE: This runner only works for tests that extend {@link SeleniumTest}.
 * </p>
 */
public class SeleniumJUnitRunner extends BlockJUnit4ClassRunner {
    public SeleniumJUnitRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }


    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        if (!(test instanceof SeleniumTest)) {
            throw new RuntimeException("Only works with SeleniumTest");
        }

        final SeleniumTest stc = ((SeleniumTest) test);
        stc.setDescription(describeChild(method));

        return new InvokeMethod(method, test) {
            @Override
            public void evaluate() throws Throwable {
                try {
                    super.evaluate();
                } catch (Throwable throwable) {
                    stc.takeScreenshot("FAILURE");
                    throw throwable;
                } finally {
                    stc.captureNetworkTraffic();
                }
            }
        };
    }

}
