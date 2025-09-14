package it.quaderno.note;

import it.quaderno.note.config.AsyncSyncConfiguration;
import it.quaderno.note.config.EmbeddedElasticsearch;
import it.quaderno.note.config.EmbeddedRedis;
import it.quaderno.note.config.EmbeddedSQL;
import it.quaderno.note.config.JacksonConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { NoteServiceApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class })
@EmbeddedRedis
@EmbeddedElasticsearch
@EmbeddedSQL
public @interface IntegrationTest {
}
