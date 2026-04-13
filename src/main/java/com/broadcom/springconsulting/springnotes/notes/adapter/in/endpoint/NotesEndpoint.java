package com.broadcom.springconsulting.springnotes.notes.adapter.in.endpoint;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteSlice;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.CreateNoteUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.LoadNotesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping( "/notes" )
class NotesEndpoint {

    private static final Logger log = LoggerFactory.getLogger( NotesEndpoint.class );

    private final LoadNotesUseCase loadNotesUseCase;
    private final CreateNoteUseCase createNoteUseCase;

    NotesEndpoint( LoadNotesUseCase loadNotesUseCase, CreateNoteUseCase createNoteUseCase ) {
        this.loadNotesUseCase = loadNotesUseCase;
        this.createNoteUseCase = createNoteUseCase;
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

    @PostMapping( version = "1+" )
    ResponseEntity<Note> createNote(
            @RequestBody CreateNoteRequest request,
            @AuthenticationPrincipal Jwt jwt,
            UriComponentsBuilder uriBuilder
    ) {
        log.debug( "Creating note for owner {}", jwt.getSubject() );

        var note = createNoteUseCase.execute( new CreateNoteUseCase.CreateNoteCommand( jwt.getSubject(), request.title(), request.content() ) );
        var location = uriBuilder.path( "/{id}" ).buildAndExpand( note.id() ).toUri();

        return ResponseEntity.created( location ).body( note );
    }

    @ExceptionHandler( IllegalArgumentException.class )
    ResponseEntity<Void> handleValidation() {
        return ResponseEntity.badRequest().build();
    }

    record CreateNoteRequest( String title, String content ) {}

}