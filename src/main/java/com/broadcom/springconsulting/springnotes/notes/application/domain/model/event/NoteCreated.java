package com.broadcom.springconsulting.springnotes.notes.application.domain.model.event;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItem;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// type's JSON key is "noteType", not "type" - NoteEvent's own @JsonTypeInfo already claims
// "type" as the polymorphic class discriminator (the literal string "NoteCreated"), and without
// this annotation Jackson would serialize both the discriminator and this field under the same
// JSON key, corrupting the discriminator with whatever NoteType.name() happened to serialize
// last. The Java accessor stays type() - only the wire-format key differs.
public record NoteCreated( UUID noteId, String owner, String title, String content, @JsonProperty( "noteType" ) NoteType type,
                            List<ChecklistItem> items, Instant occurredAt ) implements NoteEvent {

    // type/items are absent on NoteCreated JSON persisted before this field existed - Jackson
    // binds them null on replay of those older events, and this constructor runs on every such
    // deserialization, so every consumer of a hydrated NoteCreated can treat both as non-null.
    public NoteCreated {
        type = type == null ? NoteType.TEXT : type;
        items = items == null ? List.of() : items;
    }

}
