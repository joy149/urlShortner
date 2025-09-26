package com.jb.urlShortner.urlShortner.exceptions;

public class DuplicateAliasException extends RuntimeException {
    public DuplicateAliasException(String alias) {
        super("Custom alias already exist for : " + alias);
    }
}
