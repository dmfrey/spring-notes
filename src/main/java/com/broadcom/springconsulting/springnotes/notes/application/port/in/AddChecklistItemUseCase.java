package com.broadcom.springconsulting.springnotes.notes.application.port.in;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;

import java.util.UUID;

public interface AddChecklistItemUseCase {

    Note execute( AddChecklistItemCommand command );

    record AddChecklistItemCommand( UUID id, String owner, String text ) {

        public AddChecklistItemCommand {
            if ( text == null || text.isBlank() ) throw new IllegalArgumentException( "text must not be blank" );
        }

    }

}
