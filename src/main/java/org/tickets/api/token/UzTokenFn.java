package org.tickets.api.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class UzTokenFn implements Function<String, String> {
    private static final Pattern tokenEncodedDataPattern = Pattern.compile("\\$\\$_=.*~\\[\\];.*\"\"\\)\\(\\)\\)\\(\\);");
    private static final Pattern tokenPattern = Pattern.compile("[0-9a-f]{32}");
    private static final Logger LOG = LoggerFactory.getLogger(UzTokenFn.class);

    @Override
    public String apply(String s) {
        return token(s);
    }

    private String token(String content) {
        Matcher matcher = tokenEncodedDataPattern.matcher(content);
        if (!matcher.find()) {
            throw new IllegalStateException("Can't find encoded token data");
        }
        String encodedTokenData = content.substring(matcher.start(), matcher.end());
        LOG.debug("encodedTokenData={}", encodedTokenData);
        String decodedTokenData = new JJEncoder().decode(encodedTokenData);
        LOG.debug("decodedTokenData={}", decodedTokenData);
        matcher = tokenPattern.matcher(decodedTokenData);
        if (!matcher.find()) {
            throw new IllegalStateException("Can't find token in decoded token data");
        }
        String token = decodedTokenData.substring(matcher.start(), matcher.end());
        LOG.info("Got new token: {}", token);
        return token;
    }
}
