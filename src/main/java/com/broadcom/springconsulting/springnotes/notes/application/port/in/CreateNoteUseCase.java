package com.broadcom.springconsulting.springnotes.notes.application.port.in;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;

public interface CreateNoteUseCase {

    Note execute( CreateNoteCommand command );

    record CreateNoteCommand( String owner, String title, String content ) {

        public CreateNoteCommand {
            if( title == null || title.isBlank() ) throw new IllegalArgumentException( "title must not be blank" );
            if( content == null || content.isBlank() ) throw new IllegalArgumentException( "content must not be blank" );
        }

    }

}