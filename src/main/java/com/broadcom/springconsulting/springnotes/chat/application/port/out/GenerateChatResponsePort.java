package com.broadcom.springconsulting.springnotes.chat.application.port.out;

import com.broadcom.springconsulting.springnotes.chat.application.domain.model.RetrievedNote;
import reactor.core.publisher.Flux;

import java.util.List;

public interface GenerateChatResponsePort {

    Flux<String> generate( String owner, String message, List<RetrievedNote> context );

}
