package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteSlice;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.LoadNotesUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.LoadNotesUseCase.LoadNotesCommand;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNotesPort;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
class LoadNotesServiceTest {

    @Mock
    LoadNotesPort loadNotesPort;

    LoadNotesService service;

    @BeforeEach
    void setUp() {
        service = new LoadNotesService( loadNotesPort );
    }

    @Test
    void execute_withNoCursor_delegatesToPort() {

        var command = new LoadNotesCommand( null, 25 );
        var expected = new NoteSlice( List.of(), null );
        when( loadNotesPort.loadNotes( null, 25 ) ).thenReturn( expected );

        var result = service.execute( command );

        assertThat( result ).isEqualTo( expected );
        verify( loadNotesPort ).loadNotes( null, 25 );

    }

    @Test
    void execute_withCursor_delegatesToPort() {

        UUID cursor = UuidCreator.getTimeOrderedEpoch();
        var command = new LoadNotesCommand( cursor, 10 );
        var expected = new NoteSlice( List.of(), null );
        when( loadNotesPort.loadNotes( cursor, 10 ) ).thenReturn( expected );

        var result = service.execute( command );

        assertThat( result ).isEqualTo( expected );
        verify( loadNotesPort ).loadNotes( cursor, 10 );

    }

    @Test
    void loadNotesCommand_withZeroLimit_defaultsTo25() {
        var command = new LoadNotesCommand( null, 0 );
        assertThat( command.limit() ).isEqualTo( LoadNotesUseCase.LoadNotesCommand.DEFAULT_LIMIT );
    }

    @Test
    void loadNotesCommand_withNegativeLimit_defaultsTo25() {
        var command = new LoadNotesCommand( null, -5 );
        assertThat( command.limit() ).isEqualTo( LoadNotesUseCase.LoadNotesCommand.DEFAULT_LIMIT );
    }

}