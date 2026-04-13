package com.broadcom.springconsulting.springnotes.notes.application.port.out;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;

public interface SaveNotePort {

    Note saveNote( String owner, String title, String content );

}