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
package org.sonatype.nexus.proxy.attributes.perf;

import org.junit.runner.RunWith;
import org.sonatype.nexus.proxy.attributes.AttributeStorage;
import org.sonatype.nexus.proxy.attributes.DefaultFSAttributeStorage;
import org.sonatype.nexus.proxy.attributes.JacksonJSONMarshaller;
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
public class JacksonJSONFSAttributeStoragePerformanceLRTest
    extends AttributeStoragePerformanceTestSupport
{

//    @Rule
//    public MethodRule benchmarkRun = new BenchmarkRule();
    
    public AttributeStorage getAttributeStorage()
    {
        DefaultFSAttributeStorage attributeStorage =
            new DefaultFSAttributeStorage( applicationConfiguration,
                new JacksonJSONMarshaller() );
        attributeStorage.initializeWorkingDirectory();
        return attributeStorage;
    }
}
