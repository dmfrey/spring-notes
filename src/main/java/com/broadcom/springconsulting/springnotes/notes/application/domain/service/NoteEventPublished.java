package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;

// Published after a NoteEvent has been durably appended, so listeners can react once the
// surrounding transaction has committed.
record NoteEventPublished( NoteEvent event, String owner ) {
}
