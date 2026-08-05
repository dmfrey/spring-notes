package com.broadcom.springconsulting.springnotes.notes.application.port.in;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;

import java.util.List;
import java.util.UUID;

public interface LoadNoteHistoryUseCase {

    List<NoteEvent> execute( LoadNoteHistoryCommand command );

    record LoadNoteHistoryCommand( UUID id, String owner ) {}

}
