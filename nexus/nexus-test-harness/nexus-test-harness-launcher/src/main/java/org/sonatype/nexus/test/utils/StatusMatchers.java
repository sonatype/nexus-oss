package org.sonatype.nexus.test.utils;

import org.hamcrest.Factory;
import org.sonatype.nexus.test.utils.NexusRequestMatchers.HasCode;
import org.sonatype.nexus.test.utils.NexusRequestMatchers.IsError;
import org.sonatype.nexus.test.utils.NexusRequestMatchers.IsSuccess;

public class StatusMatchers
{

    @Factory
    public static <T> IsSuccess isSuccess()
    {
        return new IsSuccess();
    }

    @Factory
    public static <T> IsError isError()
    {
        return new IsError();
    }

    @Factory
    public static <T> HasCode hasStatusCode( int expectedCode )
    {
        return new HasCode( expectedCode );
    }

    @Factory
    public static <T> HasCode isNotFound()
    {
        return new HasCode( 404 );
    }
}
