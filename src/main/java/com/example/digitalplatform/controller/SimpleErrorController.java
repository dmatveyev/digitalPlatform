package com.example.digitalplatform.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Objects;

@Controller
@Slf4j
public class SimpleErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            Object attribute = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

            log.error("Error with code: {} and message: {}", statusCode, attribute);
            Object ex = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
            if (Objects.nonNull(ex)) {
                log.error("Exception: {}", ex);
            }
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "404page";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "403page";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "500page";
            }
        }
        return "error";
    }
}