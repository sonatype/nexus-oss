package org.sonatype.nexus.restlight.testharness;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link RESTTestFixture} implementation that captures an entire HTTP conversation between the
 * client under test and the test-harness {@link Server} instance. To do so, this implementation
 * encapsulates a series of other {@link RESTTestFixture} instances in a particular order. As client
 * requests are handled, the next fixture in order is used to validate the request and send back
 * a response. Each traversed fixture is tracked, allowing this fixture to determine whether the
 * full expected conversation took place, as an additional validation step for the client test. 
 */
public class ConversationalFixture
extends AbstractRESTTestFixture
{

    private List<RESTTestFixture> conversation;

    private List<RESTTestFixture> traversedConversation;

    /**
     * Retrieve the list of {@link RESTTestFixture} instances that represent the ordered script of exchanges expected
     * between the client and the test-harness server.
     */
    public List<RESTTestFixture> getConversation()
    {
        return conversation;
    }

    /**
     * Set the list of {@link RESTTestFixture} instances that represent the ordered script of exchanges expected between
     * the client and the test-harness server.
     */
    public void setConversation( final List<RESTTestFixture> conversation )
    {
        this.conversation = conversation;
        this.traversedConversation = new ArrayList<RESTTestFixture>();
    }

    /**
     * Verify that all conversation fixtures have been traversed through client requests. If some fixtures have not been
     * used, return that list to the caller. Otherwise, return an empty list.
     */
    public List<RESTTestFixture> verifyConversationWasFinished()
    {
        if ( conversation == null )
        {
            return Collections.emptyList();
        }

        List<RESTTestFixture> remaining = new ArrayList<RESTTestFixture>( conversation );
        remaining.removeAll( traversedConversation );

        return remaining;
    }

    /**
     * {@inheritDoc}
     * 
     * The {@link Handler} instance returned here tracks an index in the fixture's conversation list. Each successive
     * client request it receives, it attempts to pull out a corresponding fixture instance, and delegate the request to
     * that fixture. If the client makes more requests than there are fixtures in the conversation, the handler returns
     * a 501 HTTP response status. If the conversation is empty or null, the handler returns a 500 HTTP response status.
     * For each fixture traversed, the handler adds the fixture to the list of traversed fixtures, to allow this
     * conversational fixture to validate that the conversation as a whole was completed as expected later.
     */
    public Handler getTestHandler()
    {
        return new AbstractHandler()
        {

            private int conversationIndex = 0;

            public void handle( final String target, final HttpServletRequest request, final HttpServletResponse response, final int dispatch )
            throws IOException, ServletException
            {
                Logger logger = LogManager.getLogger( ConversationalFixture.class );

                if ( conversation == null || conversation.isEmpty() )
                {
                    logger.error( "Missing conversation." );

                    response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No conversation specified." );

                    ((Request) request).setHandled( true );
                }
                else if ( conversation.size() <= conversationIndex )
                {
                    logger.error( "Out of conversation elements. No conversation element at index: " + conversationIndex );

                    response.sendError( HttpServletResponse.SC_NOT_IMPLEMENTED, "Out of conversation elements. No conversation element at index: " + conversationIndex );

                    ((Request) request).setHandled( true );
                }
                else
                {
                    RESTTestFixture fixture = conversation.get( conversationIndex++ );

                    fixture.getTestHandler().handle( target, request, response, dispatch );

                    traversedConversation.add( fixture );
                }
            }

        };
    }

    /**
     * {@inheritDoc}
     */
    public ConversationalFixture copy()
    {
        ConversationalFixture fixture = new ConversationalFixture();

        fixture.conversation = conversation == null ? null : new ArrayList<RESTTestFixture>( conversation );
        fixture.traversedConversation = conversation == null ? null : new ArrayList<RESTTestFixture>();

        return fixture;
    }

}
