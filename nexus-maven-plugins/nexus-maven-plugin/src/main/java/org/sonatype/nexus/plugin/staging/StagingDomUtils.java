/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugin.staging;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class StagingDomUtils
{
    /**
     * Create an error message from failed repository action's response XML document.
     * 
     * @param e
     * @param msg
     * @return
     */
    public static String repositoryActionFailureMessage( final Document document, final String msg )
    {
        if ( document != null )
        {
            final String name = document.getRootElement().getName();
            if ( "stagingRuleFailures".equals( name ) )
            {
                return ruleFailureMessage( document, msg );
            }
            else
            {
                final StringBuilder sb = new StringBuilder();
                // unknown error format, can only print the xml
                sb.append( "Repository action failed with an unknown detail message.\n" ).append( msg );
                try
                {
                    final StringWriter out = new StringWriter();
                    new XMLOutputter( Format.getPrettyFormat() ).output( document, out );
                    sb.append( "\n" + out.toString() );
                }
                catch ( IOException e1 )
                {
                    // cannot write to StringWriter - unlikely, but we cannot do anything here anyway.
                }
                return sb.toString();
            }
        }

        return "";
    }

    /**
     * Create an error message from a staging rule failure's XML document.
     * 
     * @throws NullPointerException if the given document is {@code null}
     */
    public static String ruleFailureMessage( final Document document, final String mesg )
    {
        final StringBuilder msg =
            new StringBuilder( mesg + ": There were failed staging rules when finishing the repository.\n" );

        final Element failuresNode = document.getRootElement().getChild( "failures" );
        if ( failuresNode == null )
        {
            return "No failures recorded.";
        }

        final List<Element> failures = failuresNode.getChildren( "failure" );

        for ( Element entry : failures )
        {
            msg.append( " * " ).append( entry.getChild( "ruleName" ).getText() ).append( "\n" );
            final Element messageList = entry.getChild( "messages" );
            List<Element> messages = (List<Element>) messageList.getChildren();
            for ( Element message : messages )
            {
                msg.append( "      " ).append( message.getText() ).append( "\n" );
            }
        }

        final String htmlString = msg.toString();

        // staging rules return HTML markup in their results. Get rid of it.
        // Usually this should not be done with a regular expression (b/c HTML is not a regular language)
        // but this is (to date...) just stuff like '<b>$item</b>', so all will be well.
        // FIXME we should change staging rules etc. server-side to return all the necessary information to build
        // messages.
        return StringEscapeUtils.unescapeHtml( htmlString.replaceAll( "<[^>]*>", "" ) );
    }

}
