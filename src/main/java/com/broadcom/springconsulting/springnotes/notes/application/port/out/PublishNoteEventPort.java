package com.broadcom.springconsulting.springnotes.notes.application.port.out;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;

import java.util.UUID;

public interface PublishNoteEventPort {

    void publish( UUID eventId, NoteEvent event );

}
