package org.sonatype.nexus.test.utils;

import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.sonatype.nexus.test.utils.NexusRequestMatchers.InError;
import org.sonatype.nexus.test.utils.NexusRequestMatchers.IsRedirecting;
import org.sonatype.nexus.test.utils.NexusRequestMatchers.IsSuccessful;
import org.sonatype.nexus.test.utils.NexusRequestMatchers.RedirectLocationMatches;
import org.sonatype.nexus.test.utils.NexusRequestMatchers.RespondsWithStatusCode;
import org.sonatype.nexus.test.utils.NexusRequestMatchers.ResponseTextMatches;

public class ResponseMatchers
{

    @Factory
    public static RespondsWithStatusCode respondsWithStatusCode( final int expectedStatusCode )
    {
        return new RespondsWithStatusCode( expectedStatusCode );
    }

    @Factory
    public static InError inError()
    {
        return new InError();
    }

    @Factory
    public static IsSuccessful isSuccessful()
    {
        return new IsSuccessful();
    }

    @Factory
    public static ResponseTextMatches responseText( Matcher<String> matcher )
    {
        return new ResponseTextMatches( matcher );
    }

    @Factory
    public static IsRedirecting isRedirecting()
    {
        return new IsRedirecting();
    }

    @Factory
    public static RedirectLocationMatches redirectLocation( Matcher<String> matcher )
    {
        return new RedirectLocationMatches( matcher );
    }

}
