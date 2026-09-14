package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistAction;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteAggregate;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteNotFoundException;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.ChecklistUpdated;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.AddChecklistItemUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.AppendNoteEventPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNoteEventsPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.UpdateNoteItemsProjectionPort;
import com.github.f4b6a3.uuid.UuidCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
class AddChecklistItemService implements AddChecklistItemUseCase {

    private static final Logger log = LoggerFactory.getLogger( AddChecklistItemService.class );

    private final LoadNoteEventsPort loadNoteEventsPort;
    private final AppendNoteEventPort appendNoteEventPort;
    private final UpdateNoteItemsProjectionPort updateNoteItemsProjectionPort;

    AddChecklistItemService( LoadNoteEventsPort loadNoteEventsPort, AppendNoteEventPort appendNoteEventPort, UpdateNoteItemsProjectionPort updateNoteItemsProjectionPort ) {
        this.loadNoteEventsPort = loadNoteEventsPort;
        this.appendNoteEventPort = appendNoteEventPort;
        this.updateNoteItemsProjectionPort = updateNoteItemsProjectionPort;
    }

    @Override
    @Transactional
    public Note execute( AddChecklistItemCommand command ) {
        log.debug( "Adding checklist item to note {}", command.id() );

        return ChecklistMutationSupport.withRetry( () -> {
            // Ownership is enforced entirely by loadEvents' own WHERE aggregate_id = :id AND
            // owner = :owner - an empty result means "not found or not yours", and the two
            // cases are deliberately indistinguishable to the caller (NoteNotFoundException).
            NoteAggregate.hydrate( loadNoteEventsPort.loadEvents( command.id(), command.owner() ) )
                    .orElseThrow( () -> new NoteNotFoundException( command.id() ) );

            var event = new ChecklistUpdated(
                    command.id(), ChecklistAction.ITEM_ADDED,
                    UuidCreator.getTimeOrderedEpoch(), command.text(), null, null,
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
