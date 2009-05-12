package org.sonatype.nexus.integrationtests.runner;

import java.lang.reflect.Method;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class ConsoleLoggingRunner extends org.junit.runners.BlockJUnit4ClassRunner
{

    public ConsoleLoggingRunner( Class<?> klass )
        throws InitializationError
    {
        super( klass );
    }

    @Override
    protected void runChild( FrameworkMethod frameworkMethod, RunNotifier runNotifier )
    {
        Method method = frameworkMethod.getMethod();
        String description = method.getDeclaringClass().getName() +": "+ frameworkMethod.getName();

        System.out.println( "Running: " + description );

        ConsoleLoggingRunListener listener = new ConsoleLoggingRunListener( description );
        runNotifier.addListener( listener );

        long startTime  = System.currentTimeMillis();

        super.runChild( frameworkMethod, runNotifier );

        String durration = (System.currentTimeMillis() - startTime) +"ms";

        System.out.println( "Test Finished in: " + durration );

        runNotifier.removeListener( listener );
    }


    class ConsoleLoggingRunListener extends RunListener
    {

        private String description;

        public ConsoleLoggingRunListener( String description )
        {
            this.description = description;
        }

        @Override
        public void testAssumptionFailure( Failure failure )
        {
            System.out.println( description + ": >>>>>> Failed");
        }

        @Override
        public void testFailure( Failure failure )
            throws Exception
        {
            System.out.println( description + ": >>>>>> Failed");
        }
    }

}