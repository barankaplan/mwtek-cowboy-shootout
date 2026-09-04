package com.mwtek.shootout.cli;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** The finite set of value-taking command-line options accepted by the app. */
enum CliOption {
    SEED("--seed"),
    OUTPUT("--output"),
    BATCH("--batch"),
    STARTER("--starter"),
    SUMMARY("--summary");

    private static final Map<String, CliOption> BY_TOKEN = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(CliOption::token, Function.identity()));

    private final String token;

    CliOption(String token) {
        this.token = token;
    }

    String token() {
        return token;
    }

    static CliOption fromToken(String token) {
        final CliOption option = BY_TOKEN.get(token);
        if (option == null) {
            throw new IllegalArgumentException("Unknown option: " + token);
        }
        return option;
    }
}