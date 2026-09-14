package com.broadcom.springconsulting.springnotes.notes.application.port.in;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;

import java.util.List;
import java.util.UUID;

public interface ReorderChecklistItemsUseCase {

    Note execute( ReorderChecklistItemsCommand command );

    record ReorderChecklistItemsCommand( UUID id, String owner, List<UUID> orderedItemIds ) {

        public ReorderChecklistItemsCommand {
            if ( orderedItemIds == null || orderedItemIds.isEmpty() ) throw new IllegalArgumentException( "orderedItemIds must not be empty" );
        }

    }

}
