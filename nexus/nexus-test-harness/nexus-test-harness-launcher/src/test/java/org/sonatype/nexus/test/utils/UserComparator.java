package org.sonatype.nexus.test.utils;

import java.util.Comparator;

import org.sonatype.nexus.configuration.security.model.CUser;


public class UserComparator implements Comparator<CUser>
{

    public int compare( CUser user1, CUser user2 )
    {
        // quick outs
        if( user1 == null || user2 == null)
        {
            return -1;
        }
        
        if( user1 == user2 || user1.equals( user2 ))
        {
            return 0;
        }
        
        if ( user1.getEmail() == null )
        {
            if ( user2.getEmail() != null )
                return -1;
        }
        else if ( !user1.getEmail().equals( user2.getEmail() ) )
            return -1;
        if ( user1.getModelEncoding() == null )
        {
            if ( user2.getModelEncoding() != null )
                return -1;
        }
        else if ( !user1.getModelEncoding().equals( user2.getModelEncoding() ) )
            return -1;
        if ( user1.getName() == null )
        {
            if ( user2.getName() != null )
                return -1;
        }
        else if ( !user1.getName().equals( user2.getName() ) )
            return -1;
//        if ( user1.getPassword() == null )
//        {
//            if ( user2.getPassword() != null )
//                return -1;
//        }
//        else if ( !user1.getPassword().equals( user2.getPassword() ) )
//            return -1;
        if ( user1.getRoles() == null )
        {
            if ( user2.getRoles() != null )
                return -1;
        }
        else if ( !user1.getRoles().equals( user2.getRoles() ) )
            return -1;
        if ( user1.getStatus() == null )
        {
            if ( user2.getStatus() != null )
                return -1;
        }
        else if ( !user1.getStatus().equals( user2.getStatus() ) )
            return -1;
        if ( user1.getUserId() == null )
        {
            if ( user2.getUserId() != null )
                return -1;
        }
        else if ( !user1.getUserId().equals( user2.getUserId() ) )
            return -1;
        return 0;
    }
}
