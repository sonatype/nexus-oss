/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.yum.internal.support;

import static org.custommonkey.xmlunit.DifferenceConstants.ATTR_VALUE_ID;
import static org.custommonkey.xmlunit.DifferenceConstants.CHILD_NODELIST_LENGTH_ID;
import static org.custommonkey.xmlunit.DifferenceConstants.CHILD_NODELIST_SEQUENCE_ID;
import static org.custommonkey.xmlunit.DifferenceConstants.TEXT_VALUE_ID;

import org.apache.commons.lang.ArrayUtils;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.NodeDetail;
import org.w3c.dom.Node;

public class TimeStampIgnoringDifferenceListener
    implements DifferenceListener
{

    @Override
    public void skippedComparison( Node node1, Node node2 )
    {
    }

    @Override
    public int differenceFound( Difference difference )
    {
        if ( isRpmLibEntry( difference ) )
        {
            return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
        }

        if ( testNodeXpathContains( difference, "/requires[", "/provides[", "/format[1]" )
            || testNodeXpathMatches( difference, "^/repomd.{4,5}data.{4,5}(open-)?size.*$",
                                     "^/repomd.{4,5}(data.{4,5})?text.{5,6}$", "^/repomd.{4,5}revision.*$" ) )
        {
            return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
        }

        if ( isNodeName( difference, "checksum", "timestamp", "packager", "open-checksum", "time@build", "time@file",
                         "size@package", "summary", "description", "buildhost", "header-range@end", "checksum@type",
                         "open-checksum@type", "location@href" ) )
        {
            return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
        }

        if ( ( difference.getId() == TEXT_VALUE_ID ) && hasSameText( difference ) )
        {
            return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
        }

        if ( ( difference.getId() == CHILD_NODELIST_LENGTH_ID || difference.getId() == CHILD_NODELIST_SEQUENCE_ID )
            && difference.getTestNodeDetail().getXpathLocation().startsWith( "/repomd[1]" ) )
        {
            return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
        }

        if ( isRpmRequiresWithOnlyRpmLibEntries( difference.getControlNodeDetail() ) )
        {
            return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
        }

        if ( CHILD_NODELIST_SEQUENCE_ID == difference.getId() )
        {
            return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
        }

        return RETURN_ACCEPT_DIFFERENCE;
    }

    private boolean isRpmRequiresWithOnlyRpmLibEntries( NodeDetail controlNode )
    {
        return controlNode != null && controlNode.getNode() != null && controlNode.getNode().getLocalName() != null
            && controlNode.getNode().getLocalName().equals( "requires" )
            && hasOnlyRpmLibEntries( controlNode.getNode() );
    }

    private boolean hasOnlyRpmLibEntries( Node node )
    {
        for ( int index = 0; index < node.getChildNodes().getLength(); index++ )
        {
            Node childNode = node.getChildNodes().item( index );
            if ( childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getLocalName().equals( "entry" )
                && !childNode.getAttributes().getNamedItem( "name" ).getNodeValue().startsWith( "rpmlib" ) )
            {
                return false;
            }
        }
        return true;
    }

    private boolean isRpmLibEntry( Difference difference )
    {
        return difference.getControlNodeDetail().getXpathLocation() != null
            && difference.getControlNodeDetail().getXpathLocation().matches(
            "/metadata.{4,5}package.{4,5}format.{4,5}requires.{4,5}entry.{3}" )
            && controlNode( difference ).getAttributes().getNamedItem( "name" ) != null
            && controlNode( difference ).getAttributes().getNamedItem( "name" ).getTextContent().startsWith(
            "rpmlib(" );
    }

    private boolean hasSameText( Difference difference )
    {
        if ( difference.getControlNodeDetail().getValue() == null )
        {
            return difference.getTestNodeDetail().getValue() == null;
        }

        if ( difference.getTestNodeDetail() == null )
        {
            return false;
        }

        return difference.getControlNodeDetail().getValue().trim().equals(
            difference.getTestNodeDetail().getValue().trim() );
    }

    private boolean isNodeName( Difference difference, String... nodeNames )
    {
        if ( difference.getId() == ATTR_VALUE_ID )
        {
            return isNodeName( attributeName( difference.getControlNodeDetail() ), nodeNames )
                && isNodeName( attributeName( difference.getTestNodeDetail() ), nodeNames );
        }
        if ( difference.getId() == TEXT_VALUE_ID )
        {
            return isNodeName( controlNode( difference ).getParentNode().getLocalName(), nodeNames )
                && isNodeName( testNode( difference ).getParentNode().getLocalName(), nodeNames );
        }

        return isNodeName( localName( controlNode( difference ) ), nodeNames )
            && isNodeName( localName( testNode( difference ) ), nodeNames );
    }

    private boolean testNodeXpathContains( Difference difference, String... contains )
    {
        if ( difference.getTestNodeDetail().getXpathLocation() != null && contains != null && contains.length > 0 )
        {
            for ( String containedString : contains )
            {
                if ( difference.getTestNodeDetail().getXpathLocation().contains( containedString ) )
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean testNodeXpathMatches( Difference difference, String... patterns )
    {
        if ( difference.getTestNodeDetail().getXpathLocation() != null && patterns != null && patterns.length > 0 )
        {
            for ( String pattern : patterns )
            {
                if ( difference.getTestNodeDetail().getXpathLocation().matches( pattern ) )
                {
                    return true;
                }
            }
        }
        return false;
    }

    private String localName( Node node )
    {
        return node == null ? "null" : node.getLocalName();
    }

    private String attributeName( NodeDetail nodeDetail )
    {
        return nodeName( nodeDetail.getXpathLocation() ) + "@" + nodeDetail.getNode().getLocalName();
    }

    private String nodeName( String attrXPathLocation )
    {
        return attrXPathLocation.replaceAll( "^(.*\\/)([\\w\\-]+)(\\[\\d+\\]\\/@[\\w\\-]+)", "$2" );
    }

    private Node testNode( Difference difference )
    {
        return difference.getTestNodeDetail().getNode();
    }

    private Node controlNode( Difference difference )
    {
        return difference.getControlNodeDetail().getNode();
    }

    private boolean isNodeName( String name, String... nodeNames )
    {
        return ArrayUtils.contains( nodeNames, name );
    }
}
