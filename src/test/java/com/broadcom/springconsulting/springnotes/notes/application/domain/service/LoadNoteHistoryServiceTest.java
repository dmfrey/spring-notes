package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.LoadNoteHistoryUseCase.LoadNoteHistoryCommand;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNoteEventsPort;
import com.github.f4b6a3.uuid.UuidCreator;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
class LoadNoteHistoryServiceTest {

    static final String TEST_OWNER = "test-user-sub";

    @Mock
    LoadNoteEventsPort loadNoteEventsPort;

    LoadNoteHistoryService service;

    @BeforeEach
    void setUp() {
        service = new LoadNoteHistoryService( loadNoteEventsPort, ObservationRegistry.NOOP );
    }

    @Test
    void execute_delegatesToPort() {

        var id = UuidCreator.getTimeOrderedEpoch();
        List<NoteEvent> expected = List.of( new NoteCreated( id, TEST_OWNER, "My Title", "Some content", Instant.now() ) );
        when( loadNoteEventsPort.loadEvents( id, TEST_OWNER ) ).thenReturn( expected );

        var result = service.execute( new LoadNoteHistoryCommand( id, TEST_OWNER ) );

        assertThat( result ).isEqualTo( expected );
        verify( loadNoteEventsPort ).loadEvents( id, TEST_OWNER );

    }

}
