package dev.langchain4j.model.openai.internal.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public final class Reasoning {

    @JsonProperty("effort")
    private final String effort;

    public Reasoning(String effort) {
        this.effort = effort;
    }

    public String effort() {
        return effort;
    }

    @Override
    public boolean equals(Object another) {
        if (this == another) return true;
        return another instanceof Reasoning && Objects.equals(effort, ((Reasoning) another).effort);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(effort);
    }

    @Override
    public String toString() {
        return "Reasoning{effort=" + effort + "}";
    }
}