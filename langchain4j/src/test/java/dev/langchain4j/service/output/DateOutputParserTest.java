package dev.langchain4j.service.output;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateOutputParserTest {

    private final DateOutputParser parser = new DateOutputParser();

    @ParameterizedTest
    @MethodSource
    void should_parse_valid_input(String input, int year, int month, int day) {

        // when
        Date actual = parser.parse(input);

        // then
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(actual);
        assertThat(calendar.get(Calendar.YEAR)).isEqualTo(year);
        assertThat(calendar.get(Calendar.MONTH)).isEqualTo(month - 1);
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(day);
    }

    static Stream<Arguments> should_parse_valid_input() {
        return Stream.of(
                Arguments.of("2024-01-15", 2024, 1, 15),
                Arguments.of("2000-12-31", 2000, 12, 31),
                Arguments.of("1999-06-01", 1999, 6, 1),
                Arguments.of("  2024-01-15  ", 2024, 1, 15)
        );
    }

    @Test
    void should_fail_to_parse_null_input() {

        assertThatThrownBy(() -> parser.parse(null))
                .isExactlyInstanceOf(OutputParsingException.class)
                .hasMessage("Cannot parse null into java.util.Date")
                .hasNoCause();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "15-01-2024",
            "01-15-2024",
            "2024/01/15",
            "20240115",
            "not-a-date",
            "2024-1-5"
    })
    void should_fail_to_parse_invalid_input(String input) {

        assertThatThrownBy(() -> parser.parse(input))
                .isExactlyInstanceOf(OutputParsingException.class)
                .hasMessageContainingAll("Cannot parse", input, "into java.util.Date")
                .hasCauseExactlyInstanceOf(DateTimeParseException.class);
    }

    @Test
    void should_be_thread_safe() throws Exception {
        // given
        DateOutputParser sharedParser = new DateOutputParser();
        String[] dates = {"2024-01-15", "2000-12-31", "1999-06-01", "2023-07-20", "2021-03-10"};
        int threads = 20;
        int iterations = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // when
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int t = 0; t < threads; t++) {
            final int threadIndex = t;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < iterations; i++) {
                        String date = dates[(threadIndex + i) % dates.length];
                        try {
                            Date result = sharedParser.parse(date);
                            if (result != null) successCount.incrementAndGet();
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        assertThat(completed).isTrue();
        assertThat(errorCount.get()).isZero();
        assertThat(successCount.get()).isEqualTo(threads * iterations);
    }

    @Test
    void format_instructions() {

        // when
        String instructions = parser.formatInstructions();

        // then
        assertThat(instructions).isEqualTo("yyyy-MM-dd");
    }
}
