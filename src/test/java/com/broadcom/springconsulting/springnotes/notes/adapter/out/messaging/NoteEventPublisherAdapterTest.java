package com.broadcom.springconsulting.springnotes.notes.adapter.out.messaging;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteDeleted;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteUpdated;
import com.broadcom.springconsulting.springnotes.notes.configuration.NotesConfiguration;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith( MockitoExtension.class )
class NoteEventPublisherAdapterTest {

    static final String TEST_OWNER = "test-user-sub";

    @Mock
    RabbitTemplate rabbitTemplate;

    @Captor
    ArgumentCaptor<Message> messageCaptor;

    NoteEventPublisherAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new NoteEventPublisherAdapter( rabbitTemplate, JsonMapper.builder().build() );
    }

    @Test
    void publish_withNoteCreated_sendsToNotesEventsExchangeWithCreatedRoutingKey() {

        var eventId = UuidCreator.getTimeOrderedEpoch();
        var noteId = UuidCreator.getTimeOrderedEpoch();
        var event = new NoteCreated( noteId, TEST_OWNER, "Title", "Content", Instant.now() );

        adapter.publish( eventId, event );

        verify( rabbitTemplate ).send( eq( NotesConfiguration.NOTES_EVENTS_EXCHANGE ), eq( "note.created" ), messageCaptor.capture() );

    }

    @Test
    void publish_withNoteUpdated_usesUpdatedRoutingKey() {

        var eventId = UuidCreator.getTimeOrderedEpoch();
        var noteId = UuidCreator.getTimeOrderedEpoch();
        var event = new NoteUpdated( noteId, "New Title", "New content", Instant.now() );

        adapter.publish( eventId, event );

        verify( rabbitTemplate ).send( eq( NotesConfiguration.NOTES_EVENTS_EXCHANGE ), eq( "note.updated" ), messageCaptor.capture() );

    }

    @Test
    void publish_withNoteDeleted_usesDeletedRoutingKey() {

        var eventId = UuidCreator.getTimeOrderedEpoch();
        var noteId = UuidCreator.getTimeOrderedEpoch();
        var event = new NoteDeleted( noteId, Instant.now() );

        adapter.publish( eventId, event );

        verify( rabbitTemplate ).send( eq( NotesConfiguration.NOTES_EVENTS_EXCHANGE ), eq( "note.deleted" ), messageCaptor.capture() );

    }

    @Test
    void publish_setsMessageIdAndJsonContentType() {

        var eventId = UuidCreator.getTimeOrderedEpoch();
        var noteId = UuidCreator.getTimeOrderedEpoch();
        var event = new NoteCreated( noteId, TEST_OWNER, "Title", "Content", Instant.now() );

        adapter.publish( eventId, event );

        verify( rabbitTemplate ).send( eq( NotesConfiguration.NOTES_EVENTS_EXCHANGE ), eq( "note.created" ), messageCaptor.capture() );
        assertThat( messageCaptor.getValue().getMessageProperties().getMessageId() ).isEqualTo( eventId.toString() );
        assertThat( messageCaptor.getValue().getMessageProperties().getContentType() ).isEqualTo( MessageProperties.CONTENT_TYPE_JSON );

    }

    @Test
    void publish_bodyDeserializesBackToTheSameEvent() {

        var eventId = UuidCreator.getTimeOrderedEpoch();
        var noteId = UuidCreator.getTimeOrderedEpoch();
        var event = new NoteCreated( noteId, TEST_OWNER, "Title", "Content", Instant.now() );

        adapter.publish( eventId, event );

        verify( rabbitTemplate ).send( eq( NotesConfiguration.NOTES_EVENTS_EXCHANGE ), eq( "note.created" ), messageCaptor.capture() );
        var json = new String( messageCaptor.getValue().getBody(), StandardCharsets.UTF_8 );
        var roundTripped = JsonMapper.builder().build().readValue( json, NoteEvent.class );
        assertThat( roundTripped ).isEqualTo( event );

    }

}
