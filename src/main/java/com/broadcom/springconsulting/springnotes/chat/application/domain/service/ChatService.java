package com.broadcom.springconsulting.springnotes.chat.application.domain.service;

import com.broadcom.springconsulting.springnotes.chat.application.port.in.ChatUseCase;
import com.broadcom.springconsulting.springnotes.chat.application.port.out.GenerateChatResponsePort;
import com.broadcom.springconsulting.springnotes.chat.application.port.out.RetrieveRelevantNotesPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
class ChatService implements ChatUseCase {

    private static final Logger log = LoggerFactory.getLogger( ChatService.class );
    private static final int TOP_K = 5;

    private final RetrieveRelevantNotesPort retrieveRelevantNotesPort;
    private final GenerateChatResponsePort generateChatResponsePort;
    private final Counter emptyRetrievalCounter;

    ChatService( RetrieveRelevantNotesPort retrieveRelevantNotesPort, GenerateChatResponsePort generateChatResponsePort, MeterRegistry meterRegistry ) {
        this.retrieveRelevantNotesPort = retrieveRelevantNotesPort;
        this.generateChatResponsePort = generateChatResponsePort;
        // Divide by http.server.requests{uri="/chat"} to chart this as a rate - a climbing
        // ratio means users are asking about topics with nothing matching in their notes,
        // which reads as "the assistant doesn't know anything" from the outside.
        this.emptyRetrievalCounter = Counter.builder( "chat.retrieval.empty" )
                .description( "Chat requests where retrieval found no matching notes for the owner" )
                .register( meterRegistry );
    }

    @Override
    public Flux<String> execute( ChatCommand command ) {
        log.debug( "Handling chat message for owner {}", command.owner() );

        var context = retrieveRelevantNotesPort.retrieve( command.owner(), command.message(), TOP_K );

        if ( context.isEmpty() ) {
            emptyRetrievalCounter.increment();
        }

        return generateChatResponsePort.generate( command.owner(), command.message(), context );
    }

}
