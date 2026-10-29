package com.example.digitalplatform.service;

import com.example.digitalplatform.db.model.RatingParameters;
import com.example.digitalplatform.db.repository.RatingParametersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Единый сервис CRUD-логики параметров рейтинга (RatingParameters).
 * Контроллер становится тонким: маппинг DTO + redirect, вся бизнес-логика здесь.
 *
 * <h2>Контракт статусов</h2>
 * <ul>
 *   <li><b>400 Bad Request</b> — невалидный формат id (мусор вместо UUID); пустое/отсутствующее
 *       значение обязательного параметра (проверяется Bean Validation на уровне MVC:
 *       @NotBlank в DTO, MethodArgumentNotValidException -> 400).</li>
 *   <li><b>409 Conflict</b> — дубликат кода при создании (запись с тем же code уже существует).</li>
 *   <li><b>404 Not Found</b> — валидный id, но запись отсутствует в БД.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class RatingParametersService {

    private final RatingParametersRepository ratingParametersRepository;

    /**
     * Создаёт новый параметр рейтинга.
     * - дубликат кода (запись с тем же code уже существует) -> 409 Conflict;
     * - успешное создание -> сохранённая запись.
     */
    public RatingParameters create(RatingParameters toAdd) {
        if (ratingParametersRepository.existsByCode(toAdd.getCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Parameter with code '" + toAdd.getCode() + "' already exists");
        }
        return ratingParametersRepository.save(toAdd);
    }

    /**
     * Удаляет параметр рейтинга по строковому id (MVC-вариант: query-параметр мапится в String через DTO).
     * - невалидный UUID (мусор, например "abc") -> 400 Bad Request;
     * - пустой/отсутствующий id -> 400 Bad Request (Bean Validation @NotBlank на уровне MVC);
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
        deleteById(uuid);
    }

    /**
     * Удаляет параметр рейтинга по UUID (overload для будущих вызовов вне MVC).
     * - валидный UUID без записи в БД -> 404 Not Found;
     * - успешное удаление -> запись удалена.
     */
    public void deleteById(UUID uuid) {
        ratingParametersRepository.findById(uuid).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rating parameter with id '" + uuid + "' not found"));

        ratingParametersRepository.deleteById(uuid);
    }

    /**
     * Возвращает страницу параметров рейтинга с сортировкой по code.
     */
    public Page<RatingParameters> getPaged(int page, int size) {
        return ratingParametersRepository.findAll(
                PageRequest.of(page - 1, size, Sort.by("code")));
    }

    /**
     * Возвращает параметр рейтинга по id или бросает 404 Not Found.
     */
    public RatingParameters findByIdOrThrow(UUID uuid) {
        return ratingParametersRepository.findById(uuid).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rating parameter with id '" + uuid + "' not found"));
    }

    /**
     * Обновляет существующий параметр рейтинга по id.
     * Парсинг формата UUID передан на уровень MVC (@RequestParam("id") UUID в контроллере):
     * мусорный id -> TypeMismatchException -> 400 Bad Request (SimpleErrorController).
     * - валидный UUID без записи в БД -> 404 Not Found;
     * - успешное сохранение -> сохранённая запись.
     */
    public RatingParameters save(UUID uuid, RatingParameters updated) {
        RatingParameters existing = findByIdOrThrow(uuid);
        existing.setCoefficient(updated.getCoefficient());
        existing.setMaxValue(updated.getMaxValue());
        existing.setMinValue(updated.getMinValue());
        existing.setDescription(updated.getDescription());
        return ratingParametersRepository.save(existing);
    }
}
