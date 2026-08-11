package com.broadcom.springconsulting.springnotes.chat.application.port.out;

import java.util.UUID;

public interface IndexNotePort {

    void index( UUID noteId, String owner, String title, String content );

    // NoteUpdated events don't carry an owner (see NoteEvent), so the implementing adapter
    // must recover it from the note's existing indexed row rather than the caller supplying it.
    void reindex( UUID noteId, String title, String content );

}
