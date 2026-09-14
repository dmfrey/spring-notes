package com.broadcom.springconsulting.springnotes.notes.application.port.out;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItem;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LoadNotesMissingEventsPort {

    List<NoteSnapshot> loadNotesMissingEvents();

    record NoteSnapshot( UUID id, String owner, String title, String content, NoteType type, List<ChecklistItem> items, Instant createdDate ) {}

}
