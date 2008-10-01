package org.sonatype.nexus.proxy.wastebasket;

/**
 * A Smart Wastebasket, that is able to do some housecleaning if some limits are reached.
 * 
 * @author cstamas
 */
public interface SmartWastebasket
    extends Wastebasket
{
    /**
     * Set treshold for Wastebasket, from where it delete it's items.
     * 
     * @param bytes
     */
    void setMaxSizeInBytes( long bytes );
}
