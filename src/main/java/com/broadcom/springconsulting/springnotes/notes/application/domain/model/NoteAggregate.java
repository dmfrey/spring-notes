package com.broadcom.springconsulting.springnotes.notes.application.domain.model;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteDeleted;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteUpdated;

import java.util.List;
import java.util.Optional;

// Rebuilds a Note by folding its event stream. Assumes events are ordered by sequence number
// and belong to a single aggregate, as guaranteed by LoadNoteEventsPort.
public final class NoteAggregate {

    private NoteAggregate() {
    }

    public static Optional<Note> hydrate( List<NoteEvent> events ) {

        Note state = null;

        for ( NoteEvent event : events ) {
            state = switch ( event ) {
                case NoteCreated created -> new Note( created.noteId(), created.title(), created.content() );
                case NoteUpdated updated -> new Note( state.id(), updated.title(), updated.content() );
                case NoteDeleted ignored -> null;
            };
        }

        return Optional.ofNullable( state );

    }

}
