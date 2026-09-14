package com.broadcom.springconsulting.springnotes.notes.application.domain.model;

import java.util.UUID;

public record ChecklistItem( UUID id, String text, boolean checked ) {
}
