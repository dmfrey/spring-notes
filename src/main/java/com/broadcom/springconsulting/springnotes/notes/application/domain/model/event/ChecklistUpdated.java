package com.broadcom.springconsulting.springnotes.notes.application.domain.model.event;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistAction;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// A delta, not a full item-list snapshot - hydrate() applies this onto whatever state resulted
// from folding all prior events, never onto a client-read copy, so two concurrent item
// mutations (e.g. one tab adding an item, another toggling a different one) can never silently
// clobber each other regardless of append order. Only the field(s) relevant to `action` are
// populated; the rest are null.
public record ChecklistUpdated(
        UUID noteId,
        ChecklistAction action,
        UUID itemId,               // the affected item; null only for ITEMS_REORDERED
        String text,                // set only for ITEM_ADDED
        Boolean checked,            // set only for ITEM_TOGGLED
        List<UUID> orderedItemIds,  // set only for ITEMS_REORDERED
        Instant occurredAt
) implements NoteEvent {
}
