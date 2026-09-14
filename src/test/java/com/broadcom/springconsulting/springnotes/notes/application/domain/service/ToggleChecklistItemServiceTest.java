package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistAction;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItem;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItemNotFoundException;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteNotFoundException;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteType;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.ChecklistUpdated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.ToggleChecklistItemUseCase.ToggleChecklistItemCommand;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.AppendNoteEventPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNoteEventsPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.UpdateNoteItemsProjectionPort;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
class ToggleChecklistItemServiceTest {

    static final String TEST_OWNER = "test-user-sub";

    @Mock
    LoadNoteEventsPort loadNoteEventsPort;

    @Mock
    AppendNoteEventPort appendNoteEventPort;

    @Mock
    UpdateNoteItemsProjectionPort updateNoteItemsProjectionPort;

    @Captor
    ArgumentCaptor<ChecklistUpdated> eventCaptor;

    ToggleChecklistItemService service;

    @BeforeEach
    void setUp() {
        service = new ToggleChecklistItemService( loadNoteEventsPort, appendNoteEventPort, updateNoteItemsProjectionPort );
    }

    @Test
    void execute_withCheckedTrue_appendsToggleEventCarryingDesiredState() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var itemId = UuidCreator.getTimeOrderedEpoch();
        when( loadNoteEventsPort.loadEvents( id, TEST_OWNER ) )
                .thenReturn( withOneItem( id, itemId, false ) )
                .thenReturn( withOneItem( id, itemId, true ) );

        var result = service.execute( new ToggleChecklistItemCommand( id, TEST_OWNER, itemId, true ) );

        assertThat( result.items() ).extracting( ChecklistItem::checked ).containsExactly( true );

        verify( appendNoteEventPort ).append( eventCaptor.capture(), eq( TEST_OWNER ) );
        assertThat( eventCaptor.getValue().action() ).isEqualTo( ChecklistAction.ITEM_TOGGLED );
        assertThat( eventCaptor.getValue().checked() ).isTrue();

    }

    @Test
    void execute_isIdempotent_appendingTheSameDesiredStateTwiceStillSucceeds() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var itemId = UuidCreator.getTimeOrderedEpoch();
        when( loadNoteEventsPort.loadEvents( id, TEST_OWNER ) )
                .thenReturn( withOneItem( id, itemId, true ) )
                .thenReturn( withOneItem( id, itemId, true ) );

        var result = service.execute( new ToggleChecklistItemCommand( id, TEST_OWNER, itemId, true ) );

        assertThat( result.items() ).extracting( ChecklistItem::checked ).containsExactly( true );

    }

    @Test
    void execute_whenNoteNotOwnedOrMissing_throwsNoteNotFoundException() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var itemId = UuidCreator.getTimeOrderedEpoch();
        when( loadNoteEventsPort.loadEvents( id, TEST_OWNER ) ).thenReturn( List.of() );

        assertThatThrownBy( () -> service.execute( new ToggleChecklistItemCommand( id, TEST_OWNER, itemId, true ) ) )
                .isInstanceOf( NoteNotFoundException.class );

        verify( appendNoteEventPort, never() ).append( any(), eq( TEST_OWNER ) );

    }

    @Test
    void execute_whenItemDoesNotExist_throwsChecklistItemNotFoundException() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var realItemId = UuidCreator.getTimeOrderedEpoch();
        var missingItemId = UuidCreator.getTimeOrderedEpoch();
        when( loadNoteEventsPort.loadEvents( id, TEST_OWNER ) ).thenReturn( withOneItem( id, realItemId, false ) );

        assertThatThrownBy( () -> service.execute( new ToggleChecklistItemCommand( id, TEST_OWNER, missingItemId, true ) ) )
                .isInstanceOf( ChecklistItemNotFoundException.class );

        verify( appendNoteEventPort, never() ).append( any(), eq( TEST_OWNER ) );

    }

    private static List<NoteEvent> withOneItem( UUID id, UUID itemId, boolean checked ) {
        return List.of( new NoteCreated( id, TEST_OWNER, "Groceries", null, NoteType.LIST, List.of( new ChecklistItem( itemId, "Milk", checked ) ), Instant.now() ) );
    }

}
