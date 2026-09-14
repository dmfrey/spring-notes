package com.broadcom.springconsulting.springnotes.chat.application.port.out;

import java.util.UUID;

public interface IndexNotePort {

    void index( UUID noteId, String owner, String title, String content );

    // NoteUpdated events don't carry an owner (see NoteEvent), so the implementing adapter
    // must recover it from the note's existing indexed row rather than the caller supplying it.
    void reindex( UUID noteId, String title, String content );

    // ChecklistUpdated events only carry a delta, not the note's full current items or title -
    // the implementing adapter re-reads current state from notes itself rather than the caller
    // reconstructing it. No ChecklistItem in this signature: chat.application stays unaware of
    // notes' checklist shape, per the ArchUnit rule that chat's domain/ports never depend on notes.
    void reindexFromSource( UUID noteId );

}
