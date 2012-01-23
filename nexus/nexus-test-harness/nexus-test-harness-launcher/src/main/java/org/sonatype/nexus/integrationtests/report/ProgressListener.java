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

        printer.println( String.format( "Result: %s.%s() ===> %s", result.getTestClass().getName(),
            result.getName(), status ) );
    }

}
