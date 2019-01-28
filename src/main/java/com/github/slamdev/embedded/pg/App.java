package com.github.slamdev.embedded.pg;


import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;
import com.google.devtools.common.options.OptionsParser;
import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        OptionsParser parser = OptionsParser.newOptionsParser(Options.class);
        parser.parseAndExitUponError(args);
        Options options = parser.getOptions(Options.class);
        if (options == null) {
            throw new IllegalStateException("options are null");
        }
        LOGGER.debug("{}", options);

        if (options.help) {
            LOGGER.info("Usage: java -jar embedded-pg.jar OPTIONS");
            LOGGER.info("{}", parser.describeOptions(Collections.emptyMap(), OptionsParser.HelpVerbosity.LONG));
            return;
        }

        EmbeddedPostgres pg = EmbeddedPostgres.builder()
                .setPort(options.port)
                .start();
        pg.getPostgresDatabase().getConnection().prepareCall("create database " + options.database).execute();
        LOGGER.info("{}", pg.getJdbcUrl("postgres", options.database));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("shutting down postgres");
            try {
                pg.close();
                LOGGER.info("done");
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        }));
        while (true) {
            TimeUnit.SECONDS.sleep(1);
        }
    }

    public static class Options extends OptionsBase {
        @Option(
                name = "help",
                abbrev = 'h',
                help = "Prints usage info.",
                defaultValue = "false"
        )
        public boolean help;

        @Option(
                name = "port",
                abbrev = 'p',
                help = "Postgres port.",
                defaultValue = "5435"
        )
        public int port;

        @Option(
                name = "database",
                abbrev = 'd',
                help = "Postgres database that will be created.",
                defaultValue = "test"
        )
        public String database;
    }
}
