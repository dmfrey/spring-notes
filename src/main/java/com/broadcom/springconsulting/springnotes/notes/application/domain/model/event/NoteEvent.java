package com.broadcom.springconsulting.springnotes.notes.application.domain.model.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.UUID;

// DEDUCTION was considered but rejected: NoteDeleted's fields are a structural subset of
// NoteUpdated's, which are in turn a subset of NoteCreated's, so Jackson can't disambiguate
// between them by shape alone. An explicit "type" discriminator avoids that ambiguity and
// matches the note_events.type column used for querying.
@JsonTypeInfo( use = JsonTypeInfo.Id.NAME, property = "type" )
@JsonSubTypes( {
        @JsonSubTypes.Type( value = NoteCreated.class, name = "NoteCreated" ),
        @JsonSubTypes.Type( value = NoteUpdated.class, name = "NoteUpdated" ),
        @JsonSubTypes.Type( value = NoteDeleted.class, name = "NoteDeleted" )
} )
public sealed interface NoteEvent permits NoteCreated, NoteUpdated, NoteDeleted {

    UUID noteId();

    Instant occurredAt();

}
