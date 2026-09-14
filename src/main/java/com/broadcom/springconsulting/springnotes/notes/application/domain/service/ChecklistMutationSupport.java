package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.function.Supplier;

// NoteEventStoreAdapter.append()'s sequence-number computation is an unlocked correlated
// subquery - two concurrent appends on the same note can compute the same next sequence number,
// and whichever commits second gets a DataIntegrityViolationException from the unique
// constraint on (aggregate_id, sequence_number). Rare for the original single-textarea
// NoteUpdated flow, but rapid same-note item mutations (double-tap a checkbox, two tabs open)
// are the checklist feature's normal interaction pattern, so item-CRUD services retry instead
// of surfacing a 500. Each retry re-runs the whole load-validate-append-project sequence, so it
// re-reads current state rather than blindly reattempting a stale computation.
final class ChecklistMutationSupport {

    private static final int MAX_ATTEMPTS = 3;

    private ChecklistMutationSupport() {
    }

    static <T> T withRetry( Supplier<T> action ) {
        DataIntegrityViolationException lastFailure = null;

        for ( int attempt = 0; attempt < MAX_ATTEMPTS; attempt++ ) {
            try {
                return action.get();
            } catch ( DataIntegrityViolationException e ) {
                lastFailure = e;
            }
        }

        throw lastFailure;
    }

}
