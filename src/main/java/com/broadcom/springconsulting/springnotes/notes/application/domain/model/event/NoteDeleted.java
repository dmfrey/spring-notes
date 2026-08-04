package com.broadcom.springconsulting.springnotes.notes.application.domain.model.event;

import java.time.Instant;
import java.util.UUID;

public record NoteDeleted( UUID noteId, Instant occurredAt ) implements NoteEvent {
}
