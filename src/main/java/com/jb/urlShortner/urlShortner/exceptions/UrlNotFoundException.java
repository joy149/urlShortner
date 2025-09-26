package com.jb.urlShortner.urlShortner.exceptions;

public class UrlNotFoundException extends RuntimeException{
    public UrlNotFoundException(String hash) {
        super("No URL found for hash: " + hash);
    }
}
