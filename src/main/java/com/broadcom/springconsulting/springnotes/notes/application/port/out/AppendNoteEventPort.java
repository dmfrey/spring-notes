package com.broadcom.springconsulting.springnotes.notes.application.port.out;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;

public interface AppendNoteEventPort {

    void append( NoteEvent event, String owner );

}
