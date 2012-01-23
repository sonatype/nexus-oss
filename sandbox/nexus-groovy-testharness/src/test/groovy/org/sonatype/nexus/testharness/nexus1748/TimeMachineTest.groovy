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
package org.sonatype.nexus.testharness.nexus1748

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.component.annotations.*
import org.testng.annotations.*

import org.sonatype.nexus.groovytest.NexusCompatibility
import static org.testng.Assert.*@Component(role = TimeMachineTest.class)
public class TimeMachineTest implements Contextualizable {


	@Requirement(role = ColdFusionReactor.class, hint = "java")
	def javaReactor;

	@Requirement(role = ColdFusionReactor.class, hint = "groovy")
	def groovyReactor;
	
	def context;

	@Test
    @NexusCompatibility (minVersion = "1.3")
	void testPlexusContext(){
		assertNotNull context
	}

	@Test
    @NexusCompatibility (minVersion = "1.3")
	void testPlexusJavaWiring()
	{
		assertNotNull javaReactor
		assertTrue javaReactor.givePower( 10000 );
		assertFalse javaReactor.givePower( Integer.MAX_VALUE );
	}

	@Test
    @NexusCompatibility (minVersion = "1.3")
	void testPlexusGroovyWiring()
	{
		assertNotNull groovyReactor
		assertTrue groovyReactor.givePower( 10000 );
		assertFalse groovyReactor.givePower( Integer.MAX_VALUE );
	}

	@Test(expectedExceptions = [IllegalArgumentException.class])
    @NexusCompatibility (minVersion = "1.3")
	void testJavaException()
	{
		assertTrue javaReactor.givePower( -1 );
	}

	@Test(expectedExceptions = [IllegalArgumentException.class])
    @NexusCompatibility (minVersion = "1.3")
	void testGroovyException()
	{
		assertTrue groovyReactor.givePower( -1 );
	}

	void contextualize( org.codehaus.plexus.context.Context context ) 
	{
	    this.context = context;
	}
}