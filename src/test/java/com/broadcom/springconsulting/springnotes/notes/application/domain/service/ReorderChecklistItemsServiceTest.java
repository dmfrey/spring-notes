package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistAction;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItem;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteNotFoundException;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteType;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.ChecklistUpdated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.ReorderChecklistItemsUseCase.ReorderChecklistItemsCommand;
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
class ReorderChecklistItemsServiceTest {

    static final String TEST_OWNER = "test-user-sub";

    @Mock
    LoadNoteEventsPort loadNoteEventsPort;

    @Mock
    AppendNoteEventPort appendNoteEventPort;

    @Mock
    UpdateNoteItemsProjectionPort updateNoteItemsProjectionPort;

    @Captor
    ArgumentCaptor<ChecklistUpdated> eventCaptor;

    ReorderChecklistItemsService service;

    UUID noteId;
    ChecklistItem item1;
    ChecklistItem item2;

    @BeforeEach
    void setUp() {
        service = new ReorderChecklistItemsService( loadNoteEventsPort, appendNoteEventPort, updateNoteItemsProjectionPort );
        noteId = UuidCreator.getTimeOrderedEpoch();
        item1 = new ChecklistItem( UuidCreator.getTimeOrderedEpoch(), "Milk", false );
        item2 = new ChecklistItem( UuidCreator.getTimeOrderedEpoch(), "Eggs", false );
    }

    @Test
    void execute_withValidPermutation_appendsReorderEventAndReturnsNewOrder() {

        when( loadNoteEventsPort.loadEvents( noteId, TEST_OWNER ) )
                .thenReturn( withItems( item1, item2 ) )
                .thenReturn( List.of(
                        withItems( item1, item2 ).get( 0 ),
                        new ChecklistUpdated( noteId, ChecklistAction.ITEMS_REORDERED, null, null, null, List.of( item2.id(), item1.id() ), Instant.now() )
                ) );

        var result = service.execute( new ReorderChecklistItemsCommand( noteId, TEST_OWNER, List.of( item2.id(), item1.id() ) ) );

        assertThat( result.items() ).containsExactly( item2, item1 );

        verify( appendNoteEventPort ).append( eventCaptor.capture(), eq( TEST_OWNER ) );
        assertThat( eventCaptor.getValue().action() ).isEqualTo( ChecklistAction.ITEMS_REORDERED );
        assertThat( eventCaptor.getValue().orderedItemIds() ).containsExactly( item2.id(), item1.id() );

    }

    @Test
    void execute_whenNoteNotOwnedOrMissing_throwsNoteNotFoundException() {

        when( loadNoteEventsPort.loadEvents( noteId, TEST_OWNER ) ).thenReturn( List.of() );

        assertThatThrownBy( () -> service.execute( new ReorderChecklistItemsCommand( noteId, TEST_OWNER, List.of( item1.id() ) ) ) )
                .isInstanceOf( NoteNotFoundException.class );

        verify( appendNoteEventPort, never() ).append( any(), eq( TEST_OWNER ) );

    }

    @Test
    void execute_withSubsetOfItemIds_throwsIllegalArgumentExceptionAndDoesNotAppend() {

        when( loadNoteEventsPort.loadEvents( noteId, TEST_OWNER ) ).thenReturn( withItems( item1, item2 ) );

        assertThatThrownBy( () -> service.execute( new ReorderChecklistItemsCommand( noteId, TEST_OWNER, List.of( item1.id() ) ) ) )
                .isInstanceOf( IllegalArgumentException.class );

        verify( appendNoteEventPort, never() ).append( any(), eq( TEST_OWNER ) );

    }

    @Test
    void execute_withForeignItemId_throwsIllegalArgumentExceptionAndDoesNotAppend() {

        when( loadNoteEventsPort.loadEvents( noteId, TEST_OWNER ) ).thenReturn( withItems( item1, item2 ) );

        var foreignId = UuidCreator.getTimeOrderedEpoch();

        assertThatThrownBy( () -> service.execute( new ReorderChecklistItemsCommand( noteId, TEST_OWNER, List.of( item1.id(), foreignId ) ) ) )
                .isInstanceOf( IllegalArgumentException.class );

        verify( appendNoteEventPort, never() ).append( any(), eq( TEST_OWNER ) );

    }

    private List<NoteEvent> withItems( ChecklistItem... items ) {
        return List.of( new NoteCreated( noteId, TEST_OWNER, "Groceries", null, NoteType.LIST, List.of( items ), Instant.now() ) );
    }

}
