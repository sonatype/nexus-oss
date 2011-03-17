package org.sonatype.security.sample.web.services;

import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;

@At( "/test" )
@Service
public class SampleService
{
    @Get
    public Reply<String> get()
        throws Exception
    {
        return Reply.with( "Hello" );
    }
}
