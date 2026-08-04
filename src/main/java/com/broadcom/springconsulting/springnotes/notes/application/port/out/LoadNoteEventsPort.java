package com.broadcom.springconsulting.springnotes.notes.application.port.out;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;

import java.util.List;
import java.util.UUID;

public interface LoadNoteEventsPort {

    List<NoteEvent> loadEvents( UUID noteId );

}
