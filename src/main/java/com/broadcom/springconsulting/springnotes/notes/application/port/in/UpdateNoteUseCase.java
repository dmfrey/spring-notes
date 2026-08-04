package com.broadcom.springconsulting.springnotes.notes.application.port.in;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;

import java.util.UUID;

public interface UpdateNoteUseCase {

    Note execute( UpdateNoteCommand command );

    record UpdateNoteCommand( UUID id, String owner, String title, String content ) {

        public UpdateNoteCommand {
            if( title == null || title.isBlank() ) throw new IllegalArgumentException( "title must not be blank" );
            if( content == null || content.isBlank() ) throw new IllegalArgumentException( "content must not be blank" );
        }

    }

}
