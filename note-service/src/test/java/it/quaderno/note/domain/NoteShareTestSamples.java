package it.quaderno.note.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class NoteShareTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static NoteShare getNoteShareSample1() {
        return new NoteShare().id(1L);
    }

    public static NoteShare getNoteShareSample2() {
        return new NoteShare().id(2L);
    }

    public static NoteShare getNoteShareRandomSampleGenerator() {
        return new NoteShare().id(longCount.incrementAndGet());
    }
}
