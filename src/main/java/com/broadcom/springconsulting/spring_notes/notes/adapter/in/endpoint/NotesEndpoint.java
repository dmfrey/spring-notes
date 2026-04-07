package com.broadcom.springconsulting.spring_notes.notes.adapter.in.endpoint;

import com.broadcom.springconsulting.spring_notes.notes.application.domain.model.NoteSlice;
import com.broadcom.springconsulting.spring_notes.notes.application.port.in.LoadNotesUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping( "/notes" )
class NotesEndpoint {

    private final LoadNotesUseCase loadNotesUseCase;

    NotesEndpoint( LoadNotesUseCase loadNotesUseCase ) {
        this.loadNotesUseCase = loadNotesUseCase;
    }

    @GetMapping( version = "1+" )
    NoteSlice loadNotes(
            @RequestParam( required = false ) UUID cursor,
            @RequestParam( defaultValue = "25" ) int limit
    ) {
        return loadNotesUseCase.execute( new LoadNotesUseCase.LoadNotesCommand( cursor, limit ) );
    }

}