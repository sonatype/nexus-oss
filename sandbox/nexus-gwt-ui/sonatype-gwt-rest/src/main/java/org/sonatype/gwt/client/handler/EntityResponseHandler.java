package org.sonatype.gwt.client.handler;

import org.sonatype.gwt.client.resource.Representation;

public interface EntityResponseHandler
    extends ResponseHandler
{

    void onSuccess( Representation entity );

}
