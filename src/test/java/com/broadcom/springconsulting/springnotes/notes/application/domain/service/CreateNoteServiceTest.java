package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.CreateNoteUseCase.CreateNoteCommand;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.SaveNotePort;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
class CreateNoteServiceTest {

    static final String TEST_OWNER = "test-user-sub";

    @Mock
    SaveNotePort saveNotePort;

    CreateNoteService service;

    @BeforeEach
    void setUp() {
        service = new CreateNoteService( saveNotePort );
    }

    @Test
    void execute_delegatesToPortAndReturnsNote() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var expected = new Note( id, "My Title", "Some content" );
        when( saveNotePort.saveNote( TEST_OWNER, "My Title", "Some content" ) ).thenReturn( expected );

        var result = service.execute( new CreateNoteCommand( TEST_OWNER, "My Title", "Some content" ) );

        assertThat( result ).isEqualTo( expected );
        verify( saveNotePort ).saveNote( TEST_OWNER, "My Title", "Some content" );

    }

    @Test
    void createNoteCommand_withBlankTitle_throwsIllegalArgumentException() {

        assertThatThrownBy( () -> new CreateNoteCommand( TEST_OWNER, "  ", "Some content" ) )
                .isInstanceOf( IllegalArgumentException.class )
                .hasMessageContaining( "title" );

    }

    @Test
    void createNoteCommand_withNullTitle_throwsIllegalArgumentException() {

        assertThatThrownBy( () -> new CreateNoteCommand( TEST_OWNER, null, "Some content" ) )
                .isInstanceOf( IllegalArgumentException.class )
                .hasMessageContaining( "title" );

    }

    @Test
    void createNoteCommand_withBlankContent_throwsIllegalArgumentException() {

        assertThatThrownBy( () -> new CreateNoteCommand( TEST_OWNER, "My Title", "  " ) )
                .isInstanceOf( IllegalArgumentException.class )
                .hasMessageContaining( "content" );

    }

    @Test
    void createNoteCommand_withNullContent_throwsIllegalArgumentException() {

        assertThatThrownBy( () -> new CreateNoteCommand( TEST_OWNER, "My Title", null ) )
                .isInstanceOf( IllegalArgumentException.class )
                .hasMessageContaining( "content" );

    }

}