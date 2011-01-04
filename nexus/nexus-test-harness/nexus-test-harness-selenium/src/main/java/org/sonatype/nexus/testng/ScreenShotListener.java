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
