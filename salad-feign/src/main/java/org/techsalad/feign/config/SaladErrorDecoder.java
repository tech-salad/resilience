package org.techsalad.feign.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.log4j.Log4j2;
import org.techsalad.feign.exception.DressingException;
import org.techsalad.feign.exception.SaladException;

@Log4j2
public class SaladErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 400:
                return new SaladException("You have made a bad request!");
            case 403:
                return new DressingException("Dressing unavailable!");
            default:
                return new Exception("Unknown error!");
        }
    }
}
