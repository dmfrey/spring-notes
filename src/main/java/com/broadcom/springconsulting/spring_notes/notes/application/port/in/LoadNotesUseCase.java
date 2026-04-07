package com.broadcom.springconsulting.spring_notes.notes.application.port.in;

import com.broadcom.springconsulting.spring_notes.notes.application.domain.model.NoteSlice;

import java.util.UUID;

public interface LoadNotesUseCase {

    NoteSlice execute( LoadNotesCommand command );

    record LoadNotesCommand( UUID cursor, int limit ) {

        public static final int DEFAULT_LIMIT = 25;

        public LoadNotesCommand {
            if( limit <= 0 ) limit = DEFAULT_LIMIT;
        }

    }

}