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
package org.sonatype.nexus.testharness.nexus1749

import org.testng.annotations.Test
import static org.testng.Assert.*
import org.sonatype.nexus.groovytest.NexusCompatibility
import org.codehaus.plexus.component.annotations.Component;

@Component(role = SkipTest.class)
public class SkipTest{

    @Test
    @NexusCompatibility (minVersion = "1.5")
    void skipMin() 
    {
       fail "should not run!"
    }

    @Test
    @NexusCompatibility (maxVersion = "1.2")
    void skipMax() 
    {
       fail "should not run!"
    }

    @Test
    @NexusCompatibility (minVersion = "1.1", maxVersion = "1.2")
    void skipRange() 
    {
       fail "should not run!"
    }
    
}
