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
package org.sonatype.nexus.proxy.attributes.perf;

import org.junit.runner.RunWith;
import org.sonatype.nexus.proxy.attributes.AttributeStorage;
import org.sonatype.nexus.proxy.attributes.HashMapAttributeStorage;
import org.sonatype.nexus.proxy.attributes.perf.internal.OrderedRunner;

import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

/**
 * Performance test for DefaultFSAttributeStorage
 */
@BenchmarkHistoryChart
@BenchmarkMethodChart
@AxisRange( min = 0 )
@RunWith( OrderedRunner.class )
public class HashMapAttributeStoragePerformanceLRTest
    extends AttributeStoragePerformanceTestSupport
{
//    @Rule
//    public MethodRule benchmarkRun = new BenchmarkRule();

    private AttributeStorage attributeStorage =  new HashMapAttributeStorage();

    public void setup()
        throws Exception
    {
        super.setup();

        test1PutAttribute();
        test2PutAttributeX100();
    }

    public AttributeStorage getAttributeStorage()
    {
        return attributeStorage;
    }
}
