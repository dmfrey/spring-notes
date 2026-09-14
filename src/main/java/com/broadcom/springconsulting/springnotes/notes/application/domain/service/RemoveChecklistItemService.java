package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistAction;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItemNotFoundException;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteAggregate;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteNotFoundException;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.ChecklistUpdated;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.RemoveChecklistItemUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.AppendNoteEventPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNoteEventsPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.UpdateNoteItemsProjectionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
class RemoveChecklistItemService implements RemoveChecklistItemUseCase {

    private static final Logger log = LoggerFactory.getLogger( RemoveChecklistItemService.class );

    private final LoadNoteEventsPort loadNoteEventsPort;
    private final AppendNoteEventPort appendNoteEventPort;
    private final UpdateNoteItemsProjectionPort updateNoteItemsProjectionPort;

    RemoveChecklistItemService( LoadNoteEventsPort loadNoteEventsPort, AppendNoteEventPort appendNoteEventPort, UpdateNoteItemsProjectionPort updateNoteItemsProjectionPort ) {
        this.loadNoteEventsPort = loadNoteEventsPort;
        this.appendNoteEventPort = appendNoteEventPort;
        this.updateNoteItemsProjectionPort = updateNoteItemsProjectionPort;
    }

    @Override
    @Transactional
    public Note execute( RemoveChecklistItemCommand command ) {
        log.debug( "Removing checklist item {} from note {}", command.itemId(), command.id() );

        return ChecklistMutationSupport.withRetry( () -> {
            var existing = NoteAggregate.hydrate( loadNoteEventsPort.loadEvents( command.id(), command.owner() ) )
                    .orElseThrow( () -> new NoteNotFoundException( command.id() ) );

            if ( existing.items().stream().noneMatch( item -> item.id().equals( command.itemId() ) ) ) {
                throw new ChecklistItemNotFoundException( command.id(), command.itemId() );
            }

            var event = new ChecklistUpdated(
                    command.id(), ChecklistAction.ITEM_REMOVED,
                    command.itemId(), null, null, null,
                    Instant.now()
            );

            appendNoteEventPort.append( event, command.owner() );

            var updated = NoteAggregate.hydrate( loadNoteEventsPort.loadEvents( command.id(), command.owner() ) )
                    .orElseThrow( () -> new NoteNotFoundException( command.id() ) );

            updateNoteItemsProjectionPort.updateItems( command.id(), updated.items(), command.owner(), event.occurredAt() );

            return updated;
        } );
    }

}
