package com.broadcom.springconsulting.springnotes.notes.adapter.in.endpoint;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteSlice;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.LoadNotesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping( "/notes" )
class NotesEndpoint {

    private static final Logger log = LoggerFactory.getLogger( NotesEndpoint.class );

    private final LoadNotesUseCase loadNotesUseCase;

    NotesEndpoint( LoadNotesUseCase loadNotesUseCase ) {
        this.loadNotesUseCase = loadNotesUseCase;
    }

    @GetMapping( version = "1+" )
    NoteSlice loadNotes(
            @RequestParam( required = false ) UUID cursor,
            @RequestParam( defaultValue = "25" ) int limit,
            @AuthenticationPrincipal Jwt jwt
    ) {
        log.debug( "Loading notes with cursor {} and limit {}", cursor, limit );

        return loadNotesUseCase.execute( new LoadNotesUseCase.LoadNotesCommand( jwt.getSubject(), cursor, limit ) );
    }

}