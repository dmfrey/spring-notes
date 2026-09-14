package com.broadcom.springconsulting.springnotes.notes.application.domain.model;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.ChecklistUpdated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteDeleted;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteUpdated;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NoteAggregateTest {

    static final String TEST_OWNER = "test-user-sub";

    @Test
    void hydrate_withNoEvents_returnsEmpty() {

        var result = NoteAggregate.hydrate( List.of() );

        assertThat( result ).isEmpty();

    }

    @Test
    void hydrate_withOnlyCreatedEvent_returnsNote() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var events = List.<NoteEvent>of(
                new NoteCreated( id, TEST_OWNER, "My Title", "Some content", NoteType.TEXT, List.of(), Instant.now() )
        );

        var result = NoteAggregate.hydrate( events );

        assertThat( result ).contains( new Note( id, "My Title", "Some content", NoteType.TEXT, List.of() ) );

    }

    @Test
    void hydrate_withCreatedThenUpdated_returnsNoteWithUpdatedFields() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var events = List.<NoteEvent>of(
                new NoteCreated( id, TEST_OWNER, "My Title", "Some content", NoteType.TEXT, List.of(), Instant.now() ),
                new NoteUpdated( id, "New Title", "New content", Instant.now() )
        );

        var result = NoteAggregate.hydrate( events );

        assertThat( result ).contains( new Note( id, "New Title", "New content", NoteType.TEXT, List.of() ) );

    }

    @Test
    void hydrate_withCreatedThenDeleted_returnsEmpty() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var events = List.<NoteEvent>of(
                new NoteCreated( id, TEST_OWNER, "My Title", "Some content", NoteType.TEXT, List.of(), Instant.now() ),
                new NoteDeleted( id, Instant.now() )
        );

        var result = NoteAggregate.hydrate( events );

        assertThat( result ).isEmpty();

    }

    @Test
    void hydrate_withCreatedUpdatedThenDeleted_returnsEmpty() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var events = List.<NoteEvent>of(
                new NoteCreated( id, TEST_OWNER, "My Title", "Some content", NoteType.TEXT, List.of(), Instant.now() ),
                new NoteUpdated( id, "New Title", "New content", Instant.now() ),
                new NoteDeleted( id, Instant.now() )
        );

        var result = NoteAggregate.hydrate( events );

        assertThat( result ).isEmpty();

    }

    @Test
    void hydrate_withMultipleUpdates_returnsNoteWithLatestFields() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var events = List.<NoteEvent>of(
                new NoteCreated( id, TEST_OWNER, "My Title", "Some content", NoteType.TEXT, List.of(), Instant.now() ),
                new NoteUpdated( id, "Second Title", "Second content", Instant.now() ),
                new NoteUpdated( id, "Third Title", "Third content", Instant.now() )
        );

        var result = NoteAggregate.hydrate( events );

        assertThat( result ).contains( new Note( id, "Third Title", "Third content", NoteType.TEXT, List.of() ) );

    }

    @Test
    void hydrate_withListNoteAndItemAdded_returnsNoteWithNewItem() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var itemId = UuidCreator.getTimeOrderedEpoch();
        var events = List.<NoteEvent>of(
                new NoteCreated( id, TEST_OWNER, "Groceries", null, NoteType.LIST, List.of(), Instant.now() ),
                new ChecklistUpdated( id, ChecklistAction.ITEM_ADDED, itemId, "Milk", null, null, Instant.now() )
        );

        var result = NoteAggregate.hydrate( events );

        assertThat( result ).contains( new Note( id, "Groceries", null, NoteType.LIST, List.of( new ChecklistItem( itemId, "Milk", false ) ) ) );

    }

    @Test
    void hydrate_withItemToggled_returnsNoteWithItemChecked() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var itemId = UuidCreator.getTimeOrderedEpoch();
        var events = List.<NoteEvent>of(
                new NoteCreated( id, TEST_OWNER, "Groceries", null, NoteType.LIST, List.of( new ChecklistItem( itemId, "Milk", false ) ), Instant.now() ),
                new ChecklistUpdated( id, ChecklistAction.ITEM_TOGGLED, itemId, null, true, null, Instant.now() )
        );

        var result = NoteAggregate.hydrate( events );

        assertThat( result ).contains( new Note( id, "Groceries", null, NoteType.LIST, List.of( new ChecklistItem( itemId, "Milk", true ) ) ) );

    }

    @Test
    void hydrate_withItemRemoved_returnsNoteWithoutItem() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var itemId = UuidCreator.getTimeOrderedEpoch();
        var events = List.<NoteEvent>of(
                new NoteCreated( id, TEST_OWNER, "Groceries", null, NoteType.LIST, List.of( new ChecklistItem( itemId, "Milk", false ) ), Instant.now() ),
                new ChecklistUpdated( id, ChecklistAction.ITEM_REMOVED, itemId, null, null, null, Instant.now() )
        );

        var result = NoteAggregate.hydrate( events );

        assertThat( result ).contains( new Note( id, "Groceries", null, NoteType.LIST, List.of() ) );

    }

    @Test
    void hydrate_withItemsReordered_returnsNoteWithNewOrder() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var item1 = new ChecklistItem( UuidCreator.getTimeOrderedEpoch(), "Milk", false );
        var item2 = new ChecklistItem( UuidCreator.getTimeOrderedEpoch(), "Eggs", false );
        var events = List.<NoteEvent>of(
                new NoteCreated( id, TEST_OWNER, "Groceries", null, NoteType.LIST, List.of( item1, item2 ), Instant.now() ),
                new ChecklistUpdated( id, ChecklistAction.ITEMS_REORDERED, null, null, null, List.of( item2.id(), item1.id() ), Instant.now() )
        );

        var result = NoteAggregate.hydrate( events );

        assertThat( result ).contains( new Note( id, "Groceries", null, NoteType.LIST, List.of( item2, item1 ) ) );

    }

    @Test
    void hydrate_withUpdatedAfterListCreation_carriesForwardTypeAndItems() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var item = new ChecklistItem( UuidCreator.getTimeOrderedEpoch(), "Milk", false );
        var events = List.<NoteEvent>of(
                new NoteCreated( id, TEST_OWNER, "Groceries", null, NoteType.LIST, List.of( item ), Instant.now() ),
                new NoteUpdated( id, "Renamed Groceries", null, Instant.now() )
        );

        var result = NoteAggregate.hydrate( events );

        assertThat( result ).contains( new Note( id, "Renamed Groceries", null, NoteType.LIST, List.of( item ) ) );

    }

}
