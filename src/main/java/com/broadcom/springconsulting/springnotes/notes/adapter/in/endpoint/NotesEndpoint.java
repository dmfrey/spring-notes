package com.broadcom.springconsulting.springnotes.notes.adapter.in.endpoint;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteSlice;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.CreateNoteUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.DeleteNoteUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.LoadNotesUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.UpdateNoteUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final UpdateNoteUseCase updateNoteUseCase;
    private final DeleteNoteUseCase deleteNoteUseCase;

    NotesEndpoint( LoadNotesUseCase loadNotesUseCase, CreateNoteUseCase createNoteUseCase, UpdateNoteUseCase updateNoteUseCase, DeleteNoteUseCase deleteNoteUseCase ) {
        this.loadNotesUseCase = loadNotesUseCase;
        this.createNoteUseCase = createNoteUseCase;
        this.updateNoteUseCase = updateNoteUseCase;
        this.deleteNoteUseCase = deleteNoteUseCase;
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

    @PutMapping( value = "/{id}", version = "1+" )
    ResponseEntity<Note> updateNote(
            @PathVariable UUID id,
            @RequestBody UpdateNoteRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        log.debug( "Updating note {}", id );

        var note = updateNoteUseCase.execute( new UpdateNoteUseCase.UpdateNoteCommand( id, jwt.getSubject(), request.title(), request.content() ) );

        return ResponseEntity.ok( note );
    }

    @DeleteMapping( value = "/{id}", version = "1+" )
    ResponseEntity<Void> deleteNote(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        log.debug( "Deleting note {}", id );

        deleteNoteUseCase.execute( new DeleteNoteUseCase.DeleteNoteCommand( id, jwt.getSubject() ) );

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler( IllegalArgumentException.class )
    ResponseEntity<Void> handleValidation() {
        return ResponseEntity.badRequest().build();
    }

    record CreateNoteRequest( String title, String content ) {}

    record UpdateNoteRequest( String title, String content ) {}

}