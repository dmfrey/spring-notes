package com.broadcom.springconsulting.springnotes.notes.application.port.out;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItem;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Separate from UpdateNoteProjectionPort deliberately - item mutations touch neither title nor
// content, so reusing that method would risk threading stale values through on every toggle.
public interface UpdateNoteItemsProjectionPort {

    void updateItems( UUID id, List<ChecklistItem> items, String owner, Instant occurredAt );

}
