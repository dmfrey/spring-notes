package com.broadcom.springconsulting.springnotes.notes.application.port.in;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteType;

import java.util.List;

public interface CreateNoteUseCase {

    Note execute( CreateNoteCommand command );

    record CreateNoteCommand( String owner, String title, String content, NoteType type, List<String> items ) {

        public CreateNoteCommand {
            if ( title == null || title.isBlank() ) throw new IllegalArgumentException( "title must not be blank" );

            type = type == null ? NoteType.TEXT : type;

            if ( type == NoteType.TEXT ) {
                if ( content == null || content.isBlank() ) throw new IllegalArgumentException( "content must not be blank" );
            } else {
                if ( items == null || items.isEmpty() ) throw new IllegalArgumentException( "items must not be empty" );
            }

            items = items == null ? List.of() : items;
        }

    }

}
