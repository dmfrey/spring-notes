package com.broadcom.springconsulting.spring_notes.notes.adapter.out.persistence;

import com.broadcom.springconsulting.spring_notes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.spring_notes.notes.application.domain.model.NoteSlice;
import com.broadcom.springconsulting.spring_notes.notes.application.port.out.LoadNotesPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
class NotesPersistenceAdapter implements LoadNotesPort {

    private final NotesRepository notesRepository;

    NotesPersistenceAdapter( NotesRepository notesRepository ) {
        this.notesRepository = notesRepository;
    }

    @Override
    public NoteSlice loadNotes( UUID cursor, int limit ) {

        List<NoteEntity> entities = cursor == null
                ? notesRepository.findFirst( limit )
                : notesRepository.findAfterCursor( cursor, limit );

        List<Note> notes = entities.stream()
                .map( e -> new Note( e.id(), e.title(), e.content() ) )
                .toList();

        UUID nextCursor = notes.size() == limit ? notes.getLast().id() : null;

        return new NoteSlice( notes, nextCursor );
    }

}