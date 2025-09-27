package com.jb.urlShortner.urlShortner.exceptions;

import java.time.LocalDateTime;

public class UrlExpirationException extends RuntimeException {
    public UrlExpirationException(LocalDateTime expiredDate) {
        super("Short URL expired at : " + expiredDate.toString());
    }
}
