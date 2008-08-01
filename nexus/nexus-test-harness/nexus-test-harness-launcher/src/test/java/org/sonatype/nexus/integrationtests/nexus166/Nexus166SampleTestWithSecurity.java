package org.sonatype.nexus.integrationtests.nexus166;

import org.sonatype.nexus.integrationtests.SecurityTest;

/**
 * Normally test run with no security, so the functionality can be tested, with out security getting in the way. We can
 * run the same test again with Security, just by extending the class and implementing SecurityTest. You will want to
 * add more tests to your class to test permissions and things, but you get the idea.
 */
public class Nexus166SampleTestWithSecurity
    extends Nexus166SampleTest
    implements SecurityTest
{

}
