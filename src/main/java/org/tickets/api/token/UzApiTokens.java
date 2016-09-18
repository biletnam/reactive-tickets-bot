package org.tickets.api.token;

import java.util.function.Function;
import java.util.function.Supplier;

public class UzApiTokens implements ApiTokens {

    private final Supplier<String> content;
    private final Function<String, String> tokenFn;

    public UzApiTokens(Supplier<String> content, Function<String, String> tokenFn) {
        this.content = content;
        this.tokenFn = tokenFn;
    }

    @Override
    public String token() {
        String s = content.get();
        return tokenFn.apply(s);
    }
}
