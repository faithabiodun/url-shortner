package com.example.urlshortener.exception;

// I made this exception for when the url given is invalid
// like when it does not start with http:// or https://
// it will return a 400 through my global handler
public class InvalidUrlException extends RuntimeException {

    // I use this when I just want to say why the url is bad
    public InvalidUrlException(String message) {
        super(message);
    }

    // I use this when I also want to keep the original cause
    public InvalidUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
