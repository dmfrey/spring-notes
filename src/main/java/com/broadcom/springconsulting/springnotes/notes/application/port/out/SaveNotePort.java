package com.broadcom.springconsulting.springnotes.notes.application.port.out;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItem;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteType;

import java.util.List;

public interface SaveNotePort {

    Note saveNote( String owner, String title, String content, NoteType type, List<ChecklistItem> items );

}
