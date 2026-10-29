package com.example.digitalplatform.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class SimpleErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            Object attribute = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

            // Ожидаемые ошибки ввода/статусов логируются на WARN, а не ERROR.
            if (statusCode == HttpStatus.BAD_REQUEST.value() || statusCode == HttpStatus.NOT_FOUND.value()
                    || statusCode == HttpStatus.FORBIDDEN.value() || statusCode == HttpStatus.CONFLICT.value()) {
                log.warn("Error with code: {} and message: {}", statusCode, attribute);
            } else {
                log.error("Error with code: {} and message: {}", statusCode, attribute);
            }
            if (statusCode == HttpStatus.BAD_REQUEST.value()) {
                return "400page";
            } else if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "404page";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "403page";
            } else if (statusCode == HttpStatus.CONFLICT.value()) {
                return "409page";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "500page";
            }
        }
        return "error";
    }
}