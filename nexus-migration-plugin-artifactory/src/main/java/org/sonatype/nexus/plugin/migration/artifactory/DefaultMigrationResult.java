package org.sonatype.nexus.plugin.migration.artifactory;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;

@Component(role=MigrationResult.class)
public class DefaultMigrationResult
    implements MigrationResult
{

    List<String> errorMessages = new ArrayList<String>();

    List<String> warningMessages = new ArrayList<String>();

    public List<String> getErrorMessages()
    {
        return errorMessages;
    }

    public void setErrorMessages( List<String> errorMessages )
    {
        this.errorMessages = errorMessages;
    }

    public void addErrorMessage( String errorMessage )
    {
        this.errorMessages.add( errorMessage );
    }

    public List<String> getWarningMessages()
    {
        return warningMessages;
    }

    public void setWarningMessages( List<String> warningMessages )
    {
        this.warningMessages = warningMessages;
    }

    public void addWarningMessage( String warningMessage )
    {
        this.warningMessages.add( warningMessage );
    }

    public void mergeResult( MigrationResult migrationResult )
    {
        this.errorMessages.addAll( migrationResult.getErrorMessages() );
        this.warningMessages.addAll( migrationResult.getWarningMessages() );
    }

    @Override
    public String toString()
    {
        StringWriter sw = new StringWriter();

        if ( this.errorMessages.size() > 0 )
        {
            sw.append( "\nMigration errors follows:\n" );

            for ( String error : this.errorMessages )
            {
                sw.append( error ).append( "\n" );
            }
        }

        if ( this.warningMessages.size() > 0 )
        {
            sw.append( "\nMigration warnings follows:\n" );

            for ( String warning : this.warningMessages )
            {
                sw.append( warning ).append( "\n" );
            }
        }

        return sw.toString();
    }

    public void clear()
    {
        this.errorMessages.clear();
        this.warningMessages.clear();
    }
}
