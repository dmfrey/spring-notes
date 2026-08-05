package com.broadcom.springconsulting.springnotes.notes.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LoadNotesMissingEventsPort {

    List<NoteSnapshot> loadNotesMissingEvents();

    record NoteSnapshot( UUID id, String owner, String title, String content, Instant createdDate ) {}

}
