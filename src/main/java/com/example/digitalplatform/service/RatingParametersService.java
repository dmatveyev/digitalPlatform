package com.example.digitalplatform.service;

import com.example.digitalplatform.db.repository.RatingParametersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Бизнес-логика удаления параметров рейтинга, вынесенная из RatingParametersController.
 * Контроллер становится тонким и только делегирует сюда.
 */
@Service
@RequiredArgsConstructor
public class RatingParametersService {

    private final RatingParametersRepository ratingParametersRepository;

    /**
     * Удаляет параметр рейтинга по строковому id.
     * - невалидный UUID (мусор, например "abc") -> 400 Bad Request (единообразие с create/getAll: плохой ввод = 400);
     * - валидный UUID без записи в БД -> 404 Not Found;
     * - успешное удаление -> запись удалена.
     */
    public void deleteById(String id) {
        final UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid rating parameter id: '" + id + "'");
        }

        ratingParametersRepository.findById(uuid).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rating parameter with id '" + id + "' not found"));

        ratingParametersRepository.deleteById(uuid);
    }
}
