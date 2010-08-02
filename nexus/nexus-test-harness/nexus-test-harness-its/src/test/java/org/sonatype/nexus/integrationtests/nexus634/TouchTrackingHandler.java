package org.sonatype.nexus.integrationtests.nexus634;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.AbstractHandler;

public class TouchTrackingHandler
    extends AbstractHandler
    implements Handler
{
    private final List<String> touchedTargets;

    public TouchTrackingHandler()
    {
        this.touchedTargets = new ArrayList<String>();
    }

    public void handle( String target, HttpServletRequest request, HttpServletResponse response, int dispatch )
        throws IOException, ServletException
    {
        touchedTargets.add( target );
    }

    public List<String> getTouchedTargets()
    {
        return Collections.unmodifiableList( touchedTargets );
    }
}
