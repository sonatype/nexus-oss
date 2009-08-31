package org.sonatype.nexus.util.test;

import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.sonatype.nexus.util.RegexUtil;

public class RegexUtilTest
    extends TestCase
{

    public void testPositiveAndMinusOne()
    {
        Pattern p = Pattern.compile( RegexUtil.NUMBERS_POSITIVE_AND_MINUS_ONE );
        assertTrue( p.matcher( "123" ).matches() );
        assertTrue( p.matcher( "552" ).matches() );
        assertTrue( p.matcher( "-1" ).matches() );
        // no zero
        assertFalse( p.matcher( "0" ).matches() );
        // negatives not allowed
        assertFalse( p.matcher( "-5" ).matches() );
        assertFalse( p.matcher( "-25" ).matches() );
    }

}
