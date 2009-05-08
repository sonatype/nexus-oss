package org.sonatype.nexus.restlight.core;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.sonatype.nexus.restlight.common.AbstractRESTLightClient;
import org.sonatype.nexus.restlight.common.RESTLightClientException;

public class CoreClient
    extends AbstractRESTLightClient
{
    /*
     * REST Resource Path
     */
    public static final String USER_PATH = SVC_BASE + "/users";

    /*
     * Element Name
     */
    private static final String RESOURCE_URI_ELEMENT = "resourceURI";

    private static final String USER_REQUEST_ELEMENT = "user-request";

    private static final String USER_ID_ELEMENT = "userId";

    private static final String USER_NAME_ELEMENT = "name";

    private static final String USER_STATUS_ELEMENT = "status";

    private static final String USER_EMAIL_ELEMENT = "email";

    private static final String USER_USER_MANAGED_ELEMENT = "userManaged";

    private static final String USER_ROLES = "roles";

    private static final String USER_ROLE = "role";

    /*
     * XPath
     */
    private static final String USER_LIST_XPATH = "//users-list-item";

    private static final String USER_XPATH = "//data";

    private static final String USER_ROLE_XPATH = "//roles/role";

    public CoreClient( final String baseUrl, final String username, final String password )
        throws RESTLightClientException
    {
        super( baseUrl, username, password, "core/" );
    }

    public List<User> getUserList()
        throws RESTLightClientException
    {
        Document doc = get( USER_PATH );

        return parseUserList( doc );
    }

    public User getUser( String userId )
        throws RESTLightClientException
    {
        Document doc = get( USER_PATH + "/" + userId );

        return parseUser( doc );
    }

    public User putUser( User user )
        throws RESTLightClientException
    {
        Document docReq = buildUser( user );

        Document docResp = putWithResponse( USER_PATH + "/" + user.getUserId(), null, docReq );

        return parseUser( docResp );
    }

    public User postUser( User user )
        throws RESTLightClientException
    {
        Document docReq = buildUser( user );

        Document docResp = postWithResponse( USER_PATH, null, docReq );

        return parseUser( docResp );
    }

    public void deleteUser( String userId )
        throws RESTLightClientException
    {
        delete( USER_PATH + "/" + userId, null );
    }

    @SuppressWarnings( "unchecked" )
    private List<User> parseUserList( final Document doc )
        throws RESTLightClientException
    {
        XPath userListXPath = newXPath( USER_LIST_XPATH );

        List<Element> elements;

        try
        {
            elements = userListXPath.selectNodes( doc.getRootElement() );
        }
        catch ( JDOMException e )
        {
            throw new RESTLightClientException( "XPath selection failed: '" + userListXPath + "' (Root node: "
                + doc.getRootElement().getName() + ").", e );
        }

        if ( elements == null )
        {
            return null;
        }

        List<User> result = new ArrayList<User>();

        for ( Element element : elements )
        {
            result.add( parseUser( element ) );
        }

        return result;
    }

    @SuppressWarnings( "unchecked" )
    private User parseUser( final Document doc )
        throws RESTLightClientException
    {
        XPath userXPath = newXPath( USER_XPATH );

        List<Element> elements;

        try
        {
            elements = userXPath.selectNodes( doc.getRootElement() );
        }
        catch ( JDOMException e )
        {
            throw new RESTLightClientException( "XPath selection failed: '" + userXPath + "' (Root node: "
                + doc.getRootElement().getName() + ").", e );
        }

        if ( elements == null || elements.size() == 0 )
        {
            return null;
        }

        return parseUser( elements.get( 0 ) );
    }

    @SuppressWarnings( "unchecked" )
    private User parseUser( final Element element )
        throws RESTLightClientException
    {
        User user = new User();

        user.setResourceURI( element.getChildText( RESOURCE_URI_ELEMENT ) );
        user.setUserId( element.getChildText( USER_ID_ELEMENT ) );
        user.setName( element.getChildText( USER_NAME_ELEMENT ) );
        user.setEmail( element.getChildText( USER_EMAIL_ELEMENT ) );
        user.setStatus( element.getChildText( USER_STATUS_ELEMENT ) );
        user.setUserManaged( element.getChildText( USER_USER_MANAGED_ELEMENT ).equals( "true" ) ? true : false );

        XPath userRoleXPath = newXPath( USER_ROLE_XPATH );

        try
        {
            List<Element> elements = userRoleXPath.selectNodes( element );

            if ( elements != null && !elements.isEmpty() )
            {
                for ( Element roleElement : elements )
                {
                    user.getRoles().add( roleElement.getValue() );
                }
            }
        }
        catch ( JDOMException e )
        {
            throw new RESTLightClientException( "XPath selection failed: '" + userRoleXPath + "' (Root node: "
                + element.getName() + ").", e );
        }

        return user;
    }

    private Document buildUser( User user )
        throws RESTLightClientException
    {
        Element root = new Element( USER_REQUEST_ELEMENT );

        Document doc = new Document().setRootElement( root );

        Element data = new Element( "data" );

        root.addContent( data );

        data.addContent( new Element( USER_ID_ELEMENT ).setText( user.getUserId() ) );
        data.addContent( new Element( USER_NAME_ELEMENT ).setText( user.getName() ) );
        data.addContent( new Element( USER_STATUS_ELEMENT ).setText( user.getStatus() ) );
        data.addContent( new Element( USER_EMAIL_ELEMENT ).setText( user.getEmail() ) );
        data.addContent( new Element( USER_USER_MANAGED_ELEMENT ).setText( user.isUserManaged() ? "true" : "false" ) );

        Element roles = new Element( USER_ROLES );

        for ( String role : user.getRoles() )
        {
            roles.addContent( new Element( USER_ROLE ).setText( role ) );
        }

        data.addContent( roles );

        return doc;
    }

    private XPath newXPath( final String xpath )
        throws RESTLightClientException
    {
        try
        {
            return XPath.newInstance( xpath );
        }
        catch ( JDOMException e )
        {
            throw new RESTLightClientException( "Failed to build xpath: '" + xpath + "'.", e );
        }
    }

}
