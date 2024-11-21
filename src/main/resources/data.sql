-- Пользователи
insert into users (id, login, enabled, token_expired) values ('4c1e8ca4-c820-43c9-97e2-186edf346522', 'user', true, false);
insert into users (id, login, enabled, token_expired) values ('4c1e8ca4-c820-43c9-97e2-186edf346523', 'teacher', true, false);
-- Заказчики
insert into CUSTOMERS (score, id, user_id, first_name, last_name, school) values ( '4.5', '5c1e8ca4-c820-43c9-97e2-186edf346522', '4c1e8ca4-c820-43c9-97e2-186edf346522', 'Den', 'Matveev', 'МБОУ Школа №112');
-- Исполнители
insert into WORKERS (SCORE, ID, USER_ID, DEGREE, FIRST_NAME, LAST_NAME, INSTITUTE) values ( '4.5', '5c1e8ca4-c820-43c9-97e2-186edf346523', '4c1e8ca4-c820-43c9-97e2-186edf346523', 'Professor', 'Ivanov', 'Ivan', 'PSUTI'  );
-- Предметная область
insert into SUBJECTAREAS (id, name, description) values ('cc1e8ca4-c820-43c9-97e2-186edf346521', 'Математика', '');
insert into SUBJECTAREAS (id, name, description) values ('cc1e8ca4-c820-43c9-97e2-186edf346522', 'Физика', '');
insert into SUBJECTAREAS (id, name, description) values ('cc1e8ca4-c820-43c9-97e2-186edf346523', 'Литература', '');
-- Параметры рейтинга
insert into rating_parameters (id, code, description, min_value, max_value, coefficient) values ('dc1e8ca4-c820-43c9-97e2-186edf346521', 'AVERAGE_SCORE', 'Средний балл автора заявки', 0.0, 5.0, 0.4);
insert into rating_parameters (id, code, description, min_value, max_value, coefficient) values ('dc1e8ca4-c820-43c9-97e2-186edf346522', 'REQUEST_TITLE', 'Тема заявки', 0.0, 10.0, 0.1);
insert into rating_parameters (id, code, description, min_value, max_value, coefficient) values ('dc1e8ca4-c820-43c9-97e2-186edf346523', 'TYPE_ORGANIZATION', 'Тип учреждения автора заявки', 0.0, 10.0, 0.3);
insert into rating_parameters (id, code, description, min_value, max_value, coefficient) values ('dc1e8ca4-c820-43c9-97e2-186edf346524', 'AUTHOR_RATING', 'Рейтинг автора', 0.0, 10.0, 0.7);
insert into rating_parameters (id, code, description, min_value, max_value, coefficient) values ('dc1e8ca4-c820-43c9-97e2-186edf346525', 'DEADLINE', 'Близость срока исполнения', 0.0, 10.0, 0.5);