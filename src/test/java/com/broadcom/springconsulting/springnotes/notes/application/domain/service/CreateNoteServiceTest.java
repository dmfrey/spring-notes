package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItem;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteType;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.CreateNoteUseCase.CreateNoteCommand;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.AppendNoteEventPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.SaveNotePort;
import com.github.f4b6a3.uuid.UuidCreator;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.assertj.core.groups.Tuple;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
class CreateNoteServiceTest {

    static final String TEST_OWNER = "test-user-sub";

    @Mock
    SaveNotePort saveNotePort;

    @Mock
    AppendNoteEventPort appendNoteEventPort;

    @Captor
    ArgumentCaptor<NoteCreated> eventCaptor;

    CreateNoteService service;

    @BeforeEach
    void setUp() {
        service = new CreateNoteService( saveNotePort, appendNoteEventPort, ObservationRegistry.NOOP );
    }

    @Test
    void execute_delegatesToPortAndReturnsNote() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var expected = new Note( id, "My Title", "Some content", NoteType.TEXT, List.of() );
        when( saveNotePort.saveNote( TEST_OWNER, "My Title", "Some content", NoteType.TEXT, List.of() ) ).thenReturn( expected );

        var result = service.execute( new CreateNoteCommand( TEST_OWNER, "My Title", "Some content", NoteType.TEXT, null ) );

        assertThat( result ).isEqualTo( expected );
        verify( saveNotePort ).saveNote( TEST_OWNER, "My Title", "Some content", NoteType.TEXT, List.of() );

    }

    @Test
    void execute_appendsNoteCreatedEvent() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var expected = new Note( id, "My Title", "Some content", NoteType.TEXT, List.of() );
        when( saveNotePort.saveNote( TEST_OWNER, "My Title", "Some content", NoteType.TEXT, List.of() ) ).thenReturn( expected );

        service.execute( new CreateNoteCommand( TEST_OWNER, "My Title", "Some content", NoteType.TEXT, null ) );

        verify( appendNoteEventPort ).append( eventCaptor.capture(), eq( TEST_OWNER ) );
        assertThat( eventCaptor.getValue().noteId() ).isEqualTo( id );
        assertThat( eventCaptor.getValue().owner() ).isEqualTo( TEST_OWNER );
        assertThat( eventCaptor.getValue().title() ).isEqualTo( "My Title" );
        assertThat( eventCaptor.getValue().content() ).isEqualTo( "Some content" );
        assertThat( eventCaptor.getValue().type() ).isEqualTo( NoteType.TEXT );
        assertThat( eventCaptor.getValue().items() ).isEmpty();

    }

    @Test
    void execute_withListType_assignsIdsToItemsAndAppendsThem() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var expected = new Note( id, "Groceries", null, NoteType.LIST, List.of( new ChecklistItem( UuidCreator.getTimeOrderedEpoch(), "Milk", false ) ) );
        when( saveNotePort.saveNote( eq( TEST_OWNER ), eq( "Groceries" ), eq( null ), eq( NoteType.LIST ), any() ) ).thenReturn( expected );

        var result = service.execute( new CreateNoteCommand( TEST_OWNER, "Groceries", null, NoteType.LIST, List.of( "Milk", "Eggs" ) ) );

        assertThat( result ).isEqualTo( expected );

        verify( appendNoteEventPort ).append( eventCaptor.capture(), eq( TEST_OWNER ) );
        assertThat( eventCaptor.getValue().type() ).isEqualTo( NoteType.LIST );
        assertThat( eventCaptor.getValue().items() )
                .extracting( ChecklistItem::text, ChecklistItem::checked )
                .containsExactly( Tuple.tuple( "Milk", false ), Tuple.tuple( "Eggs", false ) );
        assertThat( eventCaptor.getValue().items() ).extracting( ChecklistItem::id ).doesNotContainNull();

    }

    @Test
    void createNoteCommand_withBlankTitle_throwsIllegalArgumentException() {

        assertThatThrownBy( () -> new CreateNoteCommand( TEST_OWNER, "  ", "Some content", NoteType.TEXT, null ) )
                .isInstanceOf( IllegalArgumentException.class )
                .hasMessageContaining( "title" );

    }

    @Test
    void createNoteCommand_withNullTitle_throwsIllegalArgumentException() {

        assertThatThrownBy( () -> new CreateNoteCommand( TEST_OWNER, null, "Some content", NoteType.TEXT, null ) )
                .isInstanceOf( IllegalArgumentException.class )
                .hasMessageContaining( "title" );

    }

    @Test
    void createNoteCommand_withBlankContent_throwsIllegalArgumentException() {

        assertThatThrownBy( () -> new CreateNoteCommand( TEST_OWNER, "My Title", "  ", NoteType.TEXT, null ) )
                .isInstanceOf( IllegalArgumentException.class )
                .hasMessageContaining( "content" );

    }

    @Test
    void createNoteCommand_withNullContent_throwsIllegalArgumentException() {

        assertThatThrownBy( () -> new CreateNoteCommand( TEST_OWNER, "My Title", null, NoteType.TEXT, null ) )
                .isInstanceOf( IllegalArgumentException.class )
                .hasMessageContaining( "content" );

    }

    @Test
    void createNoteCommand_withListTypeAndNoItems_throwsIllegalArgumentException() {

        assertThatThrownBy( () -> new CreateNoteCommand( TEST_OWNER, "My Title", null, NoteType.LIST, null ) )
                .isInstanceOf( IllegalArgumentException.class )
                .hasMessageContaining( "items" );

    }

    @Test
    void createNoteCommand_withListTypeAndEmptyItems_throwsIllegalArgumentException() {

        assertThatThrownBy( () -> new CreateNoteCommand( TEST_OWNER, "My Title", null, NoteType.LIST, List.of() ) )
                .isInstanceOf( IllegalArgumentException.class )
                .hasMessageContaining( "items" );

    }

}
