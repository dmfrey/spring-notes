package com.broadcom.springconsulting.springnotes.chat.application.port.out;

import java.util.List;
import java.util.UUID;

public interface LoadNotesMissingIndexPort {

    List<NoteToIndex> loadNotesMissingIndex();

    record NoteToIndex( UUID id, String owner, String title, String content ) {}

}
