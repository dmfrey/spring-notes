package com.broadcom.springconsulting.springnotes.notes.application.domain.model.event;

import java.time.Instant;
import java.util.UUID;

public record NoteCreated( UUID noteId, String owner, String title, String content, Instant occurredAt ) implements NoteEvent {
}
