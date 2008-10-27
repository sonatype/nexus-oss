package org.sonatype.nexus.artifact;

/**
 * The GavCalculator component.
 * 
 * @author cstamas
 */
public interface GavCalculator
{
    String ROLE = GavCalculator.class.getName();

    Gav pathToGav( String path );

    String gavToPath( Gav gav );
}
