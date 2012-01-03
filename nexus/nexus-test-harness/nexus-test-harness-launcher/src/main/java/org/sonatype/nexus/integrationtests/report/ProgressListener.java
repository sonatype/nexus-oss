/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.integrationtests.report;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.PrintStream;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class ProgressListener
    extends TestListenerAdapter
{
    @Override
    public void onStart( ITestContext testContext )
    {
        super.onStart( testContext );
    }

    @Override
    public void onFinish( ITestContext testContext )
    {
        super.onFinish( testContext );
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage( ITestResult tr )
    {
        super.onTestFailedButWithinSuccessPercentage( tr );

        showResult( tr, "partial success", System.out );
    }

    @Override
    public void onTestFailure( ITestResult tr )
    {
        super.onTestFailure( tr );

        showResult( tr, "FAILED", System.out );
    }

    @Override
    public void onTestSkipped( ITestResult tr )
    {
        super.onTestSkipped( tr );

        showResult( tr, "skipped", System.out );
    }

    @Override
    public void onTestSuccess( ITestResult tr )
    {
        super.onTestSuccess( tr );

        showResult( tr, "SUCCESS", System.out  );
    }

    private void showResult( ITestResult result, String status, PrintStream printer )
    {
        checkNotNull( result );
        checkNotNull( result.getTestClass() );
        checkNotNull( printer );
        long duration = result.getEndMillis() - result.getStartMillis();

        printer.println( String.format( "Result: %s.%s() ===> %s (duration %s ms)", result.getTestClass().getName(),
            result.getName(), status, duration ) );
    }

}
