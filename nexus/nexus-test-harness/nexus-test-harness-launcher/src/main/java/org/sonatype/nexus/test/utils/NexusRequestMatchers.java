package org.sonatype.nexus.test.utils;

import java.io.IOException;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.TypeSafeMatcher;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

/**
 * Matchers for {@link NexusRequest}
 */
public class NexusRequestMatchers
{

    // **************** Status Matchers ********************

    public static abstract class BaseStatusMatcher
        extends TypeSafeMatcher<Status>
    {
        @Override
        protected void describeMismatchSafely( Status status, Description mismatchDescription )
        {
            mismatchDescription.appendText( "was " ).appendText( status.toString() );
        }
    }

    public static class HasCode
        extends BaseStatusMatcher
    {
        private int expectedCode;

        public HasCode( final int expectedCode )
        {
            this.expectedCode = expectedCode;
        }

        @Override
        protected boolean matchesSafely( Status item )
        {
            return item.getCode() == expectedCode;
        }

        @Override
        public void describeTo( Description description )
        {
            description.appendText( "status code of " ).appendValue( this.expectedCode );
        }
    }

    public static class IsSuccess
        extends BaseStatusMatcher
    {

        @Override
        protected boolean matchesSafely( Status item )
        {
            return item.isSuccess();
        }

        @Override
        public void describeTo( Description description )
        {
            description.appendText( "success" );
        }
    }

    public static class IsError
        extends BaseStatusMatcher
    {

        @Override
        protected boolean matchesSafely( Status item )
        {
            return item.isError();
        }

        @Override
        public void describeTo( Description description )
        {
            description.appendText( "error" );
        }
    }

    // **************** Response Matchers ******************

    public static abstract class BaseResponseMatcher
        extends TypeSafeMatcher<Response>
    {
        @Override
        protected void describeMismatchSafely( Response item, Description mismatchDescription )
        {
            mismatchDescription.appendText( item.getStatus().toString() );
        }
    }

    public static class RespondsWithStatusCode
        extends BaseResponseMatcher
    {

        private int expectedStatusCode;

        public RespondsWithStatusCode( int expectedStatusCode )
        {
            this.expectedStatusCode = expectedStatusCode;
        }

        @Override
        protected boolean matchesSafely( Response item )
        {
            return item.getStatus().getCode() == expectedStatusCode;
        }

        @Override
        public void describeTo( Description description )
        {
            description.appendText( "response successful" );
        }
    }

    public static class InError
        extends BaseResponseMatcher
    {

        @Override
        protected boolean matchesSafely( Response resp )
        {
            return resp.getStatus().isError();
        }

        @Override
        public void describeTo( Description description )
        {
            description.appendText( "response status in error" );
        }

    }

    public static class IsSuccessful
        extends BaseResponseMatcher
    {

        @Override
        protected boolean matchesSafely( Response resp )
        {
            return resp.getStatus().isSuccess();
        }

        @Override
        public void describeTo( Description description )
        {
            description.appendText( "response status to indicate success status 200" );
        }

    }

    public static class TextMatches
        extends BaseResponseMatcher
    {

        private final Matcher<String> matcher;

        private IOException exception;

        public TextMatches( Matcher<String> matcher )
        {
            this.matcher = matcher;
        }

        @Override
        public void describeTo( Description description )
        {
            if ( exception != null )
            {
                description.appendText( "got exception " + exception.getMessage() );
            }
            else
            {
                description.appendText( "response text is " ).appendDescriptionOf( matcher );
            }
        }

        @Override
        protected boolean matchesSafely( Response item )
        {
            final Representation entity = item.getEntity();
            MatcherAssert.assertThat( entity, CoreMatchers.notNullValue() );
            String responseText;
            try
            {
                responseText = entity.getText();
            }
            catch ( IOException e )
            {
                this.exception = e;
                return false;
            }
            return matcher.matches( responseText );
        }

    }

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

    @Factory
    public static <T> RespondsWithStatusCode respondsWithStatusCode( final int expectedStatusCode )
    {
        return new RespondsWithStatusCode( expectedStatusCode );
    }

    @Factory
    public static <T> InError inError()
    {
        return new InError();
    }

    @Factory
    public static <T> IsSuccessful isSuccessful()
    {
        return new IsSuccessful();
    }

    @Factory
    public static <T> TextMatches textMatches( Matcher<String> matcher )
    {
        return new TextMatches( matcher );
    }

}
