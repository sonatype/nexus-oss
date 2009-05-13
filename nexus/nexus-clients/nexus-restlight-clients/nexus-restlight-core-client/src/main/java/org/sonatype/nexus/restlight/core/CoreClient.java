package org.sonatype.nexus.restlight.core;

import java.util.ArrayList;
import java.util.Collections;
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

    public static final String ROLE_PATH = SVC_BASE + "/roles";

    public static final String PLEXUS_USER_PATH = SVC_BASE + "/plexus_user";

    public static final String USER_TO_ROLE_PATH = SVC_BASE + "/user_to_roles";

    /*
     * Element Name
     */
    private static final String RESOURCE_URI_ELEMENT = "resourceURI";

    // User
    private static final String USER_REQUEST_ELEMENT = "user-request";

    private static final String USER_ID_ELEMENT = "userId";

    private static final String USER_NAME_ELEMENT = "name";

    private static final String USER_STATUS_ELEMENT = "status";

    private static final String USER_EMAIL_ELEMENT = "email";

    private static final String USER_USER_MANAGED_ELEMENT = "userManaged";

    private static final String USER_ROLES_ELEMENT = "roles";

    private static final String USER_ROLE_ELEMENT = "role";

    // Plexus User
    private static final String PLEXUS_USER_ID_ELEMENT = "userId";

    private static final String PLEXUS_USER_NAME_ELEMENT = "name";

    private static final String PLEXUS_USER_EMAIL_ELEMENT = "email";

    private static final String PLEXUS_USER_SOURCE_ELEMENT = "source";

    private static final String PLEXUS_USER_ROLES_ELEMENT = "roles";

    // Plexus Role

    private static final String PLEXUS_ROLE_ID_ELEMENT = "roleId";

    private static final String PLEXUS_ROLE_NAME_ELEMENT = "name";

    private static final String PLEXUS_ROLE_SOURCE_ELEMENT = "source";

    // User To Role

    private static final String USER_TO_ROLE_REQUEST_ELEMENT = "user-to-role";

    // Role
    private static final String ROLE_REQUEST_ELEMENT = "role-request";

    private static final String ROLE_ID_ELEMENT = "id";

    private static final String ROLE_NAME_ELEMENT = "name";

    private static final String ROLE_DESCRIPTION_ELEMENT = "description";

    private static final String ROLE_SESSION_TIMEOUT_ELEMENT = "sessionTimeout";

    private static final String ROLE_USER_MANAGED_ELEMENT = "userManaged";

    private static final String ROLE_ROLES_ELEMENT = "roles";

    private static final String ROLE_ROLE_ELEMENT = "role";

    private static final String ROLE_PRIVILEGES_ELEMENT = "privileges";

    private static final String ROLE_PRIVILEGE_ELEMENT = "privilege";

    /*
     * XPath
     */
    private static final String USER_LIST_XPATH = "//users-list-item";

    private static final String USER_XPATH = "//data";

    private static final String ROLE_LIST_XPATH = "//roles-list-item";

    private static final String ROLE_XPATH = "//data";

    private static final String PLEXUS_USER_XPATH = "//data";

    public CoreClient( final String baseUrl, final String username, final String password )
        throws RESTLightClientException
    {
        super( baseUrl, username, password, "core/" );
    }

    public List<User> listUser()
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

    public PlexusUser getPlexusUser( String userId )
        throws RESTLightClientException
    {
        Document doc = get( PLEXUS_USER_PATH + "/" + userId );

        return parsePlexusUser( doc );
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

    public List<Role> listRole()
        throws RESTLightClientException
    {
        Document doc = get( ROLE_PATH );

        return parseRoleList( doc );
    }

    public Role postRole( Role role )
        throws RESTLightClientException
    {
        Document docReq = buildRole( role );

        Document docResp = postWithResponse( ROLE_PATH, null, docReq );

        return parseRole( docResp );
    }

    // TODO getRole()
    // TODO putRole()
    // TODO deleteRole()

    private List<Role> parseRoleList( final Document doc )
        throws RESTLightClientException
    {
        List<Role> result = new ArrayList<Role>();

        List<Element> elements = parseElements( newXPath( ROLE_LIST_XPATH ), doc.getRootElement() );

        for ( Element element : elements )
        {
            result.add( parseRole( element ) );
        }

        return result;
    }

    private Role parseRole( final Document doc )
        throws RESTLightClientException
    {
        List<Element> elements = parseElements( newXPath( ROLE_XPATH ), doc.getRootElement() );

        if ( elements.isEmpty() )
        {
            return null;
        }

        return parseRole( elements.get( 0 ) );
    }

    @SuppressWarnings( "unchecked" )
    private Role parseRole( Element element )
        throws RESTLightClientException
    {
        Role role = new Role();

        role.setResourceURI( element.getChildText( RESOURCE_URI_ELEMENT ) );
        role.setId( element.getChildText( ROLE_ID_ELEMENT ) );
        role.setName( element.getChildText( ROLE_NAME_ELEMENT ) );
        role.setDescription( element.getChildText( ROLE_DESCRIPTION_ELEMENT ) );
        role.setSessionTimeout( Integer.parseInt( element.getChildText( ROLE_SESSION_TIMEOUT_ELEMENT ) ) );

        Element subRoles = element.getChild( ROLE_ROLES_ELEMENT );

        if ( subRoles != null )
        {
            for ( Element subRole : (List<Element>) subRoles.getChildren( ROLE_ROLE_ELEMENT ) )
            {
                role.getRoles().add( subRole.getValue() );
            }
        }

        Element privileges = element.getChild( ROLE_PRIVILEGES_ELEMENT );

        if ( privileges != null )
        {
            for ( Element privilege : (List<Element>) privileges.getChildren( ROLE_PRIVILEGE_ELEMENT ) )
            {
                role.getPrivileges().add( privilege.getValue() );
            }
        }

        role.setUserManaged( element.getChildText( ROLE_USER_MANAGED_ELEMENT ).equals( "true" ) ? true : false );

        return role;
    }

    @SuppressWarnings( "unchecked" )
    private PlexusUser parsePlexusUser( Element element )
    {
        PlexusUser plexusUser = new PlexusUser();

        plexusUser.setUserId( element.getChildText( PLEXUS_USER_ID_ELEMENT ) );
        plexusUser.setName( element.getChildText( PLEXUS_USER_NAME_ELEMENT ) );
        plexusUser.setEmail( element.getChildText( PLEXUS_USER_EMAIL_ELEMENT ) );
        plexusUser.setSource( element.getChildText( PLEXUS_USER_SOURCE_ELEMENT ) );

        Element rolesElement = element.getChild( PLEXUS_USER_ROLES_ELEMENT );

        if ( rolesElement != null )
        {
            for ( Element roleElement : (List<Element>) rolesElement.getChildren() )
            {
                plexusUser.getPlexusRoles().add( parsePlexusRole( roleElement ) );
            }
        }

        return plexusUser;
    }

    private PlexusRole parsePlexusRole( Element element )
    {
        PlexusRole plexusRole = new PlexusRole();

        plexusRole.setRoleId( element.getChildText( PLEXUS_ROLE_ID_ELEMENT ) );
        plexusRole.setName( element.getChildText( PLEXUS_ROLE_NAME_ELEMENT ) );
        plexusRole.setSource( element.getChildText( PLEXUS_ROLE_SOURCE_ELEMENT ) );

        return plexusRole;
    }

    private List<User> parseUserList( final Document doc )
        throws RESTLightClientException
    {
        List<User> result = new ArrayList<User>();

        List<Element> elements = parseElements( newXPath( USER_LIST_XPATH ), doc.getRootElement() );

        for ( Element element : elements )
        {
            result.add( parseUser( element ) );
        }

        return result;
    }

    private User parseUser( final Document doc )
        throws RESTLightClientException
    {
        List<Element> elements = parseElements( newXPath( USER_XPATH ), doc.getRootElement() );

        if ( elements.isEmpty() )
        {
            return null;
        }

        return parseUser( elements.get( 0 ) );
    }

    private PlexusUser parsePlexusUser( final Document doc )
        throws RESTLightClientException
    {
        List<Element> elements = parseElements( newXPath( PLEXUS_USER_XPATH ), doc.getRootElement() );

        if ( elements.isEmpty() )
        {
            return null;
        }

        return parsePlexusUser( elements.get( 0 ) );
    }

    @SuppressWarnings( "unchecked" )
    private User parseUser( final Element element )
        throws RESTLightClientException
    {
        User user = new User();

        user.setResourceURI( element.getChildText( RESOURCE_URI_ELEMENT ) );
        user.setUserId( element.getChildText( USER_ID_ELEMENT ) );
        user.setName( element.getChildText( USER_NAME_ELEMENT ) );
        user.setStatus( element.getChildText( USER_STATUS_ELEMENT ) );
        user.setEmail( element.getChildText( USER_EMAIL_ELEMENT ) );

        Element roles = element.getChild( USER_ROLES_ELEMENT );

        if ( roles != null )
        {
            for ( Element role : (List<Element>) roles.getChildren( USER_ROLE_ELEMENT ) )
            {
                user.getRoles().add( role.getValue() );
            }
        }

        user.setUserManaged( element.getChildText( USER_USER_MANAGED_ELEMENT ).equals( "true" ) ? true : false );

        return user;
    }

    private Document buildUser( User user )
    {
        Element root = new Element( USER_REQUEST_ELEMENT );

        Document doc = new Document().setRootElement( root );

        Element data = new Element( "data" );

        root.addContent( data );

        data.addContent( new Element( USER_ID_ELEMENT ).setText( user.getUserId() ) );
        data.addContent( new Element( USER_NAME_ELEMENT ).setText( user.getName() ) );
        data.addContent( new Element( USER_STATUS_ELEMENT ).setText( user.getStatus() ) );
        data.addContent( new Element( USER_EMAIL_ELEMENT ).setText( user.getEmail() ) );

        Element roles = new Element( USER_ROLES_ELEMENT );

        for ( String role : user.getRoles() )
        {
            roles.addContent( new Element( USER_ROLE_ELEMENT ).setText( role ) );
        }

        data.addContent( roles );

        data.addContent( new Element( USER_USER_MANAGED_ELEMENT ).setText( user.isUserManaged() ? "true" : "false" ) );

        return doc;
    }

    private Document buildUserToRole( UserToRole userToRole )
    {
        Element root = new Element( USER_TO_ROLE_REQUEST_ELEMENT );

        Document doc = new Document().setRootElement( root );

        Element data = new Element( "data" );

        root.addContent( data );

        data.addContent( new Element( "userId" ).setText( userToRole.getUserId() ) );

        data.addContent( new Element( "source" ).setText( userToRole.getSource() ) );

        Element rolesElement = new Element( "roles" );

        for ( String role : userToRole.getRoles() )
        {
            rolesElement.addContent( new Element( "role" ).setText( role ) );
        }

        data.addContent( rolesElement );

        return doc;
    }

    private Document buildRole( Role role )
    {
        Element root = new Element( ROLE_REQUEST_ELEMENT );

        Document doc = new Document().setRootElement( root );

        Element data = new Element( "data" );

        root.addContent( data );

        data.addContent( new Element( ROLE_ID_ELEMENT ).setText( role.getId() ) );
        data.addContent( new Element( ROLE_NAME_ELEMENT ).setText( role.getName() ) );
        data.addContent( new Element( ROLE_DESCRIPTION_ELEMENT ).setText( role.getDescription() ) );
        data.addContent( new Element( ROLE_SESSION_TIMEOUT_ELEMENT ).setText( role.getSessionTimeout() + "" ) );

        Element subRoles = new Element( ROLE_ROLES_ELEMENT );
        for ( String subRole : role.getRoles() )
        {
            subRoles.addContent( new Element( ROLE_ROLE_ELEMENT ).setText( subRole ) );
        }
        data.addContent( subRoles );

        Element privileges = new Element( ROLE_PRIVILEGES_ELEMENT );
        for ( String privilege : role.getPrivileges() )
        {
            privileges.addContent( new Element( ROLE_PRIVILEGE_ELEMENT ).setText( privilege ) );
        }
        data.addContent( privileges );

        data.addContent( new Element( ROLE_USER_MANAGED_ELEMENT ).setText( role.isUserManaged() ? "true" : "false" ) );

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

    @SuppressWarnings( "unchecked" )
    private List<Element> parseElements( XPath xPath, Element root )
        throws RESTLightClientException
    {
        List<Element> elements = null;

        try
        {
            elements = xPath.selectNodes( root );
        }
        catch ( JDOMException e )
        {
            throw new RESTLightClientException( "XPath selection failed: '" + xPath + "' (Root node: " + root.getName()
                + ").", e );
        }

        return (List<Element>) ( elements == null ? Collections.emptyList() : elements );
    }

    public void putUserToRole( UserToRole userToRole )
        throws RESTLightClientException
    {
        Document doc = buildUserToRole( userToRole );

        this.put( USER_TO_ROLE_PATH + "/" + userToRole.getSource() + "/" + userToRole.getUserId(), null, doc );
    }
}
