package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistAction;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItem;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItemNotFoundException;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteNotFoundException;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteType;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.ChecklistUpdated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.RemoveChecklistItemUseCase.RemoveChecklistItemCommand;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.AppendNoteEventPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNoteEventsPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.UpdateNoteItemsProjectionPort;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class RemoveChecklistItemServiceTest {

    static final String TEST_OWNER = "test-user-sub";

    @Mock
    LoadNoteEventsPort loadNoteEventsPort;

    @Mock
    AppendNoteEventPort appendNoteEventPort;

    @Mock
    UpdateNoteItemsProjectionPort updateNoteItemsProjectionPort;

    RemoveChecklistItemService service;

    @BeforeEach
    void setUp() {
        service = new RemoveChecklistItemService( loadNoteEventsPort, appendNoteEventPort, updateNoteItemsProjectionPort );
    }

    @Test
    void execute_removesItemAndReturnsUpdatedNote() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var itemId = UuidCreator.getTimeOrderedEpoch();
        when( loadNoteEventsPort.loadEvents( id, TEST_OWNER ) )
                .thenReturn( withOneItem( id, itemId ) )
                .thenReturn( List.of( withOneItem( id, itemId ).get( 0 ), new ChecklistUpdated( id, ChecklistAction.ITEM_REMOVED, itemId, null, null, null, Instant.now() ) ) );

        var result = service.execute( new RemoveChecklistItemCommand( id, TEST_OWNER, itemId ) );

        assertThat( result.items() ).isEmpty();
        verify( updateNoteItemsProjectionPort ).updateItems( eq( id ), eq( List.of() ), eq( TEST_OWNER ), any() );

    }

    @Test
    void execute_whenNoteNotOwnedOrMissing_throwsNoteNotFoundException() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var itemId = UuidCreator.getTimeOrderedEpoch();
        when( loadNoteEventsPort.loadEvents( id, TEST_OWNER ) ).thenReturn( List.of() );

        assertThatThrownBy( () -> service.execute( new RemoveChecklistItemCommand( id, TEST_OWNER, itemId ) ) )
                .isInstanceOf( NoteNotFoundException.class );

        verify( appendNoteEventPort, never() ).append( any(), eq( TEST_OWNER ) );

    }

    @Test
    void execute_whenItemDoesNotExist_throwsChecklistItemNotFoundExceptionAndDoesNotAppend() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var realItemId = UuidCreator.getTimeOrderedEpoch();
        var missingItemId = UuidCreator.getTimeOrderedEpoch();
        when( loadNoteEventsPort.loadEvents( id, TEST_OWNER ) ).thenReturn( withOneItem( id, realItemId ) );

        assertThatThrownBy( () -> service.execute( new RemoveChecklistItemCommand( id, TEST_OWNER, missingItemId ) ) )
                .isInstanceOf( ChecklistItemNotFoundException.class );

        verify( appendNoteEventPort, never() ).append( any(), eq( TEST_OWNER ) );

    }

    private static List<NoteEvent> withOneItem( UUID id, UUID itemId ) {
        return List.of( new NoteCreated( id, TEST_OWNER, "Groceries", null, NoteType.LIST, List.of( new ChecklistItem( itemId, "Milk", false ) ), Instant.now() ) );
    }

}
