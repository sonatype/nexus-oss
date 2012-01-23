/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testng;

import org.sonatype.nexus.mock.SeleniumTest;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ScreenShotListener
    implements ITestListener
{

    public void onFinish( ITestContext context )
    {
    }

    public void onStart( ITestContext context )
    {
    }

    public void onTestFailedButWithinSuccessPercentage( ITestResult result )
    {
    }

    public void onTestFailure( ITestResult result )
    {
        Object[] tests = result.getTestClass().getInstances( false );
        for ( Object test : tests )
        {
            if ( test instanceof SeleniumTest )
            {
                ( (SeleniumTest) test ).captureNetworkTraffic();
                ( (SeleniumTest) test ).takeScreenshot( "Failure" );
            }
        }

    }

    public void onTestSkipped( ITestResult result )
    {
    }

    public void onTestStart( ITestResult result )
    {
    }

    public void onTestSuccess( ITestResult result )
    {
        Object[] tests = result.getTestClass().getInstances( false );
        for ( Object test : tests )
        {
            if ( test instanceof SeleniumTest )
            {
                ( (SeleniumTest) test ).captureNetworkTraffic();
            }
        }
    }

}
