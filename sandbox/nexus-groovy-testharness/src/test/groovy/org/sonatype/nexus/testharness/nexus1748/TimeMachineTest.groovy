package org.sonatype.nexus.testharness.nexus1748

import org.codehaus.plexus.component.annotations.Component
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.component.annotations.*
import org.testng.annotations.*

import org.sonatype.nexus.groovytest.NexusCompatibility
import org.sonatype.nexus.groovytest.plexus.ColdFusionReactor
import static org.testng.Assert.*import org.codehaus.plexus.component.annotations.Component;
@Component(role = TimeMachineTest.class)
public class TimeMachineTest implements Contextualizable {


	@Requirement(role = ColdFusionReactor.class, hint = "java")
	def reactor;
	
	def context;

	@Test
    @NexusCompatibility (minVersion = "1.3")
	void testPlexusContext(){
		assertNotNull context
	}

	@Test
    @NexusCompatibility (minVersion = "1.3")
	void testPlexusWiring()
	{
		assertNotNull reactor
		assertTrue reactor.givePower( 10000 );
		assertFalse reactor.givePower( Integer.MAX_VALUE );
	}

	@Test(expectedExceptions = [IllegalArgumentException.class])
    @NexusCompatibility (minVersion = "1.3")
	void testException()
	{
		assertTrue reactor.givePower( -1 );
	}

	void contextualize( org.codehaus.plexus.context.Context context ) 
	{
	    this.context = context;
	}
}