package com.broadcom.springconsulting.springnotes.notes.adapter.out.persistence;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteSlice;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNotesPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
class NotesPersistenceAdapter implements LoadNotesPort {

    private static final Logger log = LoggerFactory.getLogger( NotesPersistenceAdapter.class );

    private final NotesRepository notesRepository;

    NotesPersistenceAdapter( NotesRepository notesRepository ) {
        this.notesRepository = notesRepository;
    }

    @Override
    public NoteSlice loadNotes( String owner, UUID cursor, int limit ) {
        log.debug( "Loading notes for owner {} with cursor {} and limit {}", owner, cursor, limit );

        List<NoteEntity> entities = cursor == null
                ? notesRepository.findFirst( owner, limit )
                : notesRepository.findAfterCursor( owner, cursor, limit );

        List<Note> notes = entities.stream()
                .map( e -> new Note( e.id(), e.title(), e.content() ) )
                .toList();

        UUID nextCursor = notes.size() == limit ? notes.getLast().id() : null;

        return new NoteSlice( notes, nextCursor );
    }

}