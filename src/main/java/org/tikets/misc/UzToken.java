package org.tikets.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UzToken {
    private static final Pattern tokenEncodedDataPattern = Pattern.compile("\\$\\$_=.*~\\[\\];.*\"\"\\)\\(\\)\\)\\(\\);");
    private static final Pattern tokenPattern = Pattern.compile("[0-9a-f]{32}");
    private static final Logger LOG = LoggerFactory.getLogger(UzToken.class);

    public String get(String rootPageContent) {
        Matcher matcher = tokenEncodedDataPattern.matcher(rootPageContent);
        if (!matcher.find()) {
            throw new IllegalStateException("Can't find encoded token data");
        }
        String encodedTokenData = rootPageContent.substring(matcher.start(), matcher.end());
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
