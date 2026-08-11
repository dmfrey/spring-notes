package com.broadcom.springconsulting.springnotes.chat.application.domain.service;

import com.broadcom.springconsulting.springnotes.chat.application.port.in.ChatUseCase;
import com.broadcom.springconsulting.springnotes.chat.application.port.out.GenerateChatResponsePort;
import com.broadcom.springconsulting.springnotes.chat.application.port.out.RetrieveRelevantNotesPort;
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

    ChatService( RetrieveRelevantNotesPort retrieveRelevantNotesPort, GenerateChatResponsePort generateChatResponsePort ) {
        this.retrieveRelevantNotesPort = retrieveRelevantNotesPort;
        this.generateChatResponsePort = generateChatResponsePort;
    }

    @Override
    public Flux<String> execute( ChatCommand command ) {
        log.debug( "Handling chat message for owner {}", command.owner() );

        var context = retrieveRelevantNotesPort.retrieve( command.owner(), command.message(), TOP_K );

        return generateChatResponsePort.generate( command.owner(), command.message(), context );
    }

}
