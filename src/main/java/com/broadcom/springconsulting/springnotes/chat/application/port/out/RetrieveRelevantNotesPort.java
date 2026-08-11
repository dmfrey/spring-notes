package com.broadcom.springconsulting.springnotes.chat.application.port.out;

import com.broadcom.springconsulting.springnotes.chat.application.domain.model.RetrievedNote;

import java.util.List;

public interface RetrieveRelevantNotesPort {

    List<RetrievedNote> retrieve( String owner, String query, int topK );

}
