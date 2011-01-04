/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.mock;


/**
 * <p>
 * A special JUnit runner that provides additional behaviors specific to Selenium test cases. This runner adds the
 * following behavior:
 * </p>
 * <ul>
 * <li>The test class gets it's JUnit description injected prior to test evaluation (see
 * {@link SeleniumTest#setDescription(org.junit.runner.Description)}).</li>
 * <li>If a test fails, a screenshot is automatically taken with the name "FAILURE" appended to the end of it.</li>
 * </ul>
 * <p>
 * NOTE: This runner only works for tests that extend {@link SeleniumTest}.
 * </p>
 */
public class SeleniumJUnitRunner
    //extends BlockJUnit4ClassRunner
{
/*
    protected static Logger log = Logger.getLogger( SeleniumJUnitRunner.class );

    public SeleniumJUnitRunner( Class<?> klass )
        throws InitializationError
    {
        super( klass );
    }

    @Override
    protected Statement methodInvoker( FrameworkMethod method, Object test )
    {
        if ( !( test instanceof SeleniumTest ) )
        {
            throw new RuntimeException( "Only works with SeleniumTest" );
        }

        final SeleniumTest stc = ( (SeleniumTest) test );
        stc.setDescription( describeChild( method ) );

        return new InvokeMethod( method, test )
        {
            @Override
            public void evaluate()
                throws Throwable
            {
                try
                {
                    super.evaluate();
                }
                catch ( Throwable throwable )
                {
                //TODO
                    stc.takeScreenshot( "FAILURE" );
                    throw throwable;
                }
                finally
                {
                //TODO
                    stc.captureNetworkTraffic();
                }
            }
        };
    }
*/



}
