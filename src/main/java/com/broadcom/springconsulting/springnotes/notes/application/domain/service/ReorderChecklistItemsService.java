package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistAction;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItem;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteAggregate;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteNotFoundException;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.ChecklistUpdated;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.ReorderChecklistItemsUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.AppendNoteEventPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNoteEventsPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.UpdateNoteItemsProjectionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
class ReorderChecklistItemsService implements ReorderChecklistItemsUseCase {

    private static final Logger log = LoggerFactory.getLogger( ReorderChecklistItemsService.class );

    private final LoadNoteEventsPort loadNoteEventsPort;
    private final AppendNoteEventPort appendNoteEventPort;
    private final UpdateNoteItemsProjectionPort updateNoteItemsProjectionPort;

    ReorderChecklistItemsService( LoadNoteEventsPort loadNoteEventsPort, AppendNoteEventPort appendNoteEventPort, UpdateNoteItemsProjectionPort updateNoteItemsProjectionPort ) {
        this.loadNoteEventsPort = loadNoteEventsPort;
        this.appendNoteEventPort = appendNoteEventPort;
        this.updateNoteItemsProjectionPort = updateNoteItemsProjectionPort;
    }

    @Override
    @Transactional
    public Note execute( ReorderChecklistItemsCommand command ) {
        log.debug( "Reordering checklist items on note {}", command.id() );

        return ChecklistMutationSupport.withRetry( () -> {
            var existing = NoteAggregate.hydrate( loadNoteEventsPort.loadEvents( command.id(), command.owner() ) )
                    .orElseThrow( () -> new NoteNotFoundException( command.id() ) );

            validatePermutation( existing.items(), command.orderedItemIds() );

            var event = new ChecklistUpdated(
                    command.id(), ChecklistAction.ITEMS_REORDERED,
                    null, null, null, command.orderedItemIds(),
                    Instant.now()
            );

            appendNoteEventPort.append( event, command.owner() );

            var updated = NoteAggregate.hydrate( loadNoteEventsPort.loadEvents( command.id(), command.owner() ) )
                    .orElseThrow( () -> new NoteNotFoundException( command.id() ) );

            updateNoteItemsProjectionPort.updateItems( command.id(), updated.items(), command.owner(), event.occurredAt() );

            return updated;
        } );
    }

    // Must be exactly a permutation of the note's current item ids - a subset would silently
    // drop items from the next snapshot, and a foreign id would inject garbage.
    private static void validatePermutation( List<ChecklistItem> currentItems, List<UUID> orderedItemIds ) {
        Set<UUID> currentIds = currentItems.stream().map( ChecklistItem::id ).collect( Collectors.toSet() );
        Set<UUID> requestedIds = Set.copyOf( orderedItemIds );

        if ( orderedItemIds.size() != currentItems.size() || !currentIds.equals( requestedIds ) ) {
            throw new IllegalArgumentException( "orderedItemIds must be exactly a permutation of the note's current item ids" );
        }
    }

}
