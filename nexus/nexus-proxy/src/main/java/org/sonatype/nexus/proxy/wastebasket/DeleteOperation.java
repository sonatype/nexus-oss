package org.sonatype.nexus.proxy.wastebasket;

/**
 * The possible actions taken on delete operation.
 * 
 * @author cstamas
 */
public enum DeleteOperation
{
    DELETE_PERMANENTLY,

    MOVE_TO_TRASH;
}
