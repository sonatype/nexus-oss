/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.dao;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

public final class LdapUtils
{

    private LdapUtils()
    {
    }

    public static String getLabeledUriValue( Attributes attributes, String attrName, String label,
        String attributeDescription )
        throws LdapDAOException
    {
        Attribute attribute = attributes.get( attrName );
        if ( attribute != null )
        {
            NamingEnumeration attrs;
            try
            {
                attrs = attribute.getAll();
            }
            catch ( NamingException e )
            {
                throw new LdapDAOException( "Failed to retrieve " + attributeDescription + " (attribute: \'"
                    + attrName + "\').", e );
            }

            while ( attrs.hasMoreElements() )
            {
                Object value = attrs.nextElement();

                String val = String.valueOf( value );

                if ( val.endsWith( " " + label ) )
                {
                    return val.substring( 0, val.length() - ( label.length() + 1 ) );
                }
            }
        }

        return null;
    }

    public static String getAttributeValue( Attributes attributes, String attrName, String attributeDescription )
        throws LdapDAOException
    {
        Attribute attribute = attributes.get( attrName );
        if ( attribute != null )
        {
            try
            {
                Object value = attribute.get();

                return String.valueOf( value );
            }
            catch ( NamingException e )
            {
                throw new LdapDAOException( "Failed to retrieve " + attributeDescription + " (attribute: \'"
                    + attrName + "\').", e );
            }
        }

        return null;
    }
    
    public static Set<String> getAttributeValues( Attributes attributes, String attrName, String attributeDescription )
    throws LdapDAOException
    {
        Set<String> results = new HashSet<String>();
        Attribute attribute = attributes.get( attrName );
        if ( attribute != null )
        {
            try
            {   
                for ( Enumeration<?> values = attribute.getAll(); values.hasMoreElements(); )
                {
                    results.add( String.valueOf( values.nextElement() ) );
                }
            }
            catch ( NamingException e )
            {
                throw new LdapDAOException( "Failed to retrieve " + attributeDescription + " (attribute: \'"
                    + attrName + "\').", e );
            }
        }
    
        return results;
    }

    public static String getAttributeValueFromByteArray( Attributes attributes, String attrName,
        String attributeDescription )
        throws LdapDAOException
    {
        if( attrName != null)
        {
            Attribute attribute = attributes.get( attrName );
            if ( attribute != null )
            {
                try
                {
                    byte[] value = (byte[]) attribute.get();
    
                    return new String( value );
                }
                catch ( NamingException e )
                {
                    throw new LdapDAOException( "Failed to retrieve " + attributeDescription + " (attribute: \'"
                        + attrName + "\').", e );
                }
            }
        }

        return null;
    }
}
