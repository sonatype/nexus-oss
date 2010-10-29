package org.sonatype.nexus.rest.indexng;

/**
 * A simple class holding Extension (non-null) and Classifier (may be null).
 * 
 * @author cstamas
 */
public class ECHolder
{
    private final String extension;

    private final String classifier;

    public ECHolder( String extension, String classifier )
    {
        assert extension != null : "Extension cannot be null!";

        this.extension = extension;
        this.classifier = classifier;
    }

    public String getExtension()
    {
        return extension;
    }

    public String getClassifier()
    {
        return classifier;
    }

    // ==

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( classifier == null ) ? 0 : classifier.hashCode() );
        result = prime * result + ( ( extension == null ) ? 0 : extension.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        ECHolder other = (ECHolder) obj;
        if ( classifier == null )
        {
            if ( other.classifier != null )
                return false;
        }
        else if ( !classifier.equals( other.classifier ) )
            return false;
        if ( extension == null )
        {
            if ( other.extension != null )
                return false;
        }
        else if ( !extension.equals( other.extension ) )
            return false;
        return true;
    }
}
