package org.sonatype.nexus.proxy.repository.validator;

import org.sonatype.nexus.proxy.item.StorageFileItem;

public interface FileTypeValidator
{
    enum FileTypeValidity
    {
        /**
         * We can say for sure that the content and the expected content does match.
         */
        VALID,

        /**
         * We cannot state nothing for sure.
         */
        NEUTRAL,

        /**
         * We can say for sure that the content and the expected content does not match.
         */
        INVALID;
    };

    FileTypeValidity isExpectedFileType( StorageFileItem file );
}
