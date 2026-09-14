package com.broadcom.springconsulting.springnotes.notes.application.port.in;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;

import java.util.UUID;

public interface RemoveChecklistItemUseCase {

    Note execute( RemoveChecklistItemCommand command );

    record RemoveChecklistItemCommand( UUID id, String owner, UUID itemId ) {}

}
