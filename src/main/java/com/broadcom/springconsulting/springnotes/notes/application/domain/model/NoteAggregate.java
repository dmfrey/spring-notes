package com.broadcom.springconsulting.springnotes.notes.application.domain.model;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.ChecklistUpdated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteDeleted;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteUpdated;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Rebuilds a Note by folding its event stream. Assumes events are ordered by sequence number
// and belong to a single aggregate, as guaranteed by LoadNoteEventsPort.
public final class NoteAggregate {

    private NoteAggregate() {
    }

    public static Optional<Note> hydrate( List<NoteEvent> events ) {

        Note state = null;

        for ( NoteEvent event : events ) {
            state = switch ( event ) {
                case NoteCreated created -> new Note( created.noteId(), created.title(), created.content(), created.type(), created.items() );
                case NoteUpdated updated -> new Note( state.id(), updated.title(), updated.content(), state.type(), state.items() );
                case ChecklistUpdated checklistUpdated -> new Note( state.id(), state.title(), state.content(), state.type(), applyChecklistDelta( state.items(), checklistUpdated ) );
                case NoteDeleted ignored -> null;
            };
        }

        return Optional.ofNullable( state );

    }

    // The single authoritative place a ChecklistUpdated delta gets applied to an item list -
    // reused both here (event replay) and by item-CRUD services (re-hydrating after their own
    // append to compute what to write to the read projection), so there's exactly one
    // implementation of "what a given action does" to keep correct.
    private static List<ChecklistItem> applyChecklistDelta( List<ChecklistItem> items, ChecklistUpdated event ) {
        return switch ( event.action() ) {
            case ITEM_ADDED -> {
                var updated = new ArrayList<>( items );
                updated.add( new ChecklistItem( event.itemId(), event.text(), false ) );
                yield List.copyOf( updated );
            }
            case ITEM_REMOVED -> items.stream()
                    .filter( item -> !item.id().equals( event.itemId() ) )
                    .toList();
            case ITEM_TOGGLED -> items.stream()
                    .map( item -> item.id().equals( event.itemId() ) ? new ChecklistItem( item.id(), item.text(), event.checked() ) : item )
                    .toList();
            case ITEMS_REORDERED -> {
                var byId = items.stream().collect( Collectors.toMap( ChecklistItem::id, item -> item ) );
                yield event.orderedItemIds().stream().map( byId::get ).toList();
            }
        };
    }

}
