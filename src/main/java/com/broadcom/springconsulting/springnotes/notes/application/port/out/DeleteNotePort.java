package com.broadcom.springconsulting.springnotes.notes.application.port.out;

import java.util.UUID;

public interface DeleteNotePort {

    boolean deleteNote( UUID id, String owner );

}
