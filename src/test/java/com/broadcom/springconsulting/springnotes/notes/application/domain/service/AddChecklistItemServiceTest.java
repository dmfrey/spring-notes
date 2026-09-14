package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistAction;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItem;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteNotFoundException;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteType;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.ChecklistUpdated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.AddChecklistItemUseCase.AddChecklistItemCommand;
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
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
class AddChecklistItemServiceTest {

    static final String TEST_OWNER = "test-user-sub";

    @Mock
    LoadNoteEventsPort loadNoteEventsPort;

    @Mock
    AppendNoteEventPort appendNoteEventPort;

    @Mock
    UpdateNoteItemsProjectionPort updateNoteItemsProjectionPort;

    @Captor
    ArgumentCaptor<ChecklistUpdated> eventCaptor;

    AddChecklistItemService service;

    @BeforeEach
    void setUp() {
        service = new AddChecklistItemService( loadNoteEventsPort, appendNoteEventPort, updateNoteItemsProjectionPort );
    }

    @Test
    void execute_appendsItemAddedEventAndReturnsUpdatedNote() {

        var id = UuidCreator.getTimeOrderedEpoch();
        when( loadNoteEventsPort.loadEvents( id, TEST_OWNER ) )
                .thenReturn( listNoteEvents( id ) )
                .thenReturn( listNoteEventsWithNewItem( id, "Milk" ) );

        var result = service.execute( new AddChecklistItemCommand( id, TEST_OWNER, "Milk" ) );

        assertThat( result.items() ).extracting( ChecklistItem::text ).containsExactly( "Milk" );

        verify( appendNoteEventPort ).append( eventCaptor.capture(), eq( TEST_OWNER ) );
        assertThat( eventCaptor.getValue().action() ).isEqualTo( ChecklistAction.ITEM_ADDED );
        assertThat( eventCaptor.getValue().text() ).isEqualTo( "Milk" );

        verify( updateNoteItemsProjectionPort ).updateItems( eq( id ), any(), eq( TEST_OWNER ), any() );

    }

    @Test
    void execute_whenNoteNotOwnedOrMissing_throwsNoteNotFoundExceptionAndDoesNotAppendEvent() {

        var id = UuidCreator.getTimeOrderedEpoch();
        when( loadNoteEventsPort.loadEvents( id, TEST_OWNER ) ).thenReturn( List.of() );

        assertThatThrownBy( () -> service.execute( new AddChecklistItemCommand( id, TEST_OWNER, "Milk" ) ) )
                .isInstanceOf( NoteNotFoundException.class );

        verify( appendNoteEventPort, never() ).append( any(), eq( TEST_OWNER ) );

    }

    @Test
    void execute_onSequenceNumberConflict_retriesAndSucceeds() {

        var id = UuidCreator.getTimeOrderedEpoch();
        when( loadNoteEventsPort.loadEvents( id, TEST_OWNER ) )
                .thenReturn( listNoteEvents( id ) )
                .thenReturn( listNoteEvents( id ) )
                .thenReturn( listNoteEventsWithNewItem( id, "Milk" ) );

        doThrow( new DataIntegrityViolationException( "sequence conflict" ) )
                .doNothing()
                .when( appendNoteEventPort ).append( any(), eq( TEST_OWNER ) );

        var result = service.execute( new AddChecklistItemCommand( id, TEST_OWNER, "Milk" ) );

        assertThat( result.items() ).extracting( ChecklistItem::text ).containsExactly( "Milk" );
        verify( appendNoteEventPort, times( 2 ) ).append( any(), eq( TEST_OWNER ) );

    }

    private static List<NoteEvent> listNoteEvents( UUID id ) {
        return List.of( new NoteCreated( id, TEST_OWNER, "Groceries", null, NoteType.LIST, List.of(), Instant.now() ) );
    }

    private static List<NoteEvent> listNoteEventsWithNewItem( UUID id, String text ) {
        return List.of(
                new NoteCreated( id, TEST_OWNER, "Groceries", null, NoteType.LIST, List.of(), Instant.now() ),
                new ChecklistUpdated( id, ChecklistAction.ITEM_ADDED, UuidCreator.getTimeOrderedEpoch(), text, null, null, Instant.now() )
        );
    }

}
