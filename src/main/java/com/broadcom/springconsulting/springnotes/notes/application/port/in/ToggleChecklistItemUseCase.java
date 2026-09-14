package com.broadcom.springconsulting.springnotes.notes.application.port.in;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;

import java.util.UUID;

public interface ToggleChecklistItemUseCase {

    Note execute( ToggleChecklistItemCommand command );

    // checked is the desired end state (idempotent "set to X"), not "flip current value" - safe
    // to retry after a timeout without double-flipping.
    record ToggleChecklistItemCommand( UUID id, String owner, UUID itemId, boolean checked ) {}

}
