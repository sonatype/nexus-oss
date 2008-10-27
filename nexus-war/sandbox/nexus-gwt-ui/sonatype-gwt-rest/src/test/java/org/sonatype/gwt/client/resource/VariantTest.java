package org.sonatype.gwt.client.resource;

import junit.framework.TestCase;

public class VariantTest
    extends TestCase
{

    public void testVariant()
    {
        Variant variant = new Variant("application/json; charset=UTF-8");
        assertEquals( "application/json", variant.getMediaType() );
    }

}
