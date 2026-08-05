package com.broadcom.springconsulting.springnotes.notes.application.domain.model.event;

import java.time.Instant;
import java.util.UUID;

public record NoteUpdated( UUID noteId, String title, String content, Instant occurredAt ) implements NoteEvent {
}
