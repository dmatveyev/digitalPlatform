-- Пользователи
INSERT INTO PUBLIC.USERS (ID, LOGIN, PASSWORD, ENABLED, TOKEN_EXPIRED, SCORE, DEGREE, FIRST_NAME, LAST_NAME, INSTITUTE, LIMIT_OURS)
VALUES ('4c1e8ca4-c820-43c9-97e2-186edf346523', 'teacher', '{bcrypt}$2a$10$pqz3eTPFJPtNoiYg3xC9yOfyekTM4yFDCz5d7visMU0gD7qGEK3gy', true, false, '4.5', 'Professor', 'Ivanov', 'Ivan', 'PSUTI', 20);
INSERT INTO PUBLIC.USERS (ID, LOGIN, PASSWORD, ENABLED, TOKEN_EXPIRED, SCORE, DEGREE, FIRST_NAME, LAST_NAME, INSTITUTE)
VALUES ('aa25d9af-f18b-4bf1-bef6-f70176b89b52', 'user', '{bcrypt}$2a$10$AsgwBmSDITYg8KEkQWfPXeiTy9mcJRPHY1esw2bHyrmPny8PCZhIy', true, false, '4.5','', 'Den', 'Matveev', 'МБОУ Школа №112');
-- Роли
INSERT INTO PUBLIC.ROLES (ID, CODE, NAME, DESCRIPTION) VALUES ('fba1ffbb-5e6c-4abf-8d1d-aff59ca895b9', 'ADMIN', 'Администратор', 'Администратор системя');
INSERT INTO PUBLIC.ROLES (ID, CODE, NAME, DESCRIPTION) VALUES ('e15f209c-6398-4647-9c8d-d3101349e199', 'TEACHER','Преподаватель','Преподаватель');
INSERT INTO PUBLIC.ROLES (ID, CODE, NAME, DESCRIPTION) VALUES ('e15f209c-6398-4647-9c8d-d3101349e197', 'STUDENT','Ученик','Ученик');
INSERT INTO PUBLIC.ROLES (ID, CODE, NAME, DESCRIPTION) VALUES ('e15f209c-6398-4647-9c8d-d3101349e198', 'USER','Пользователь','');
--Привилегии
INSERT INTO PUBLIC.PRIVILEGES (ID, NAME) VALUES ('10fad87d-ac18-44ac-a75b-38d0fb1e6351', 'READ_PRIVILEGE');
INSERT INTO PUBLIC.PRIVILEGES (ID, NAME) VALUES ('54f7f41e-e83d-4f40-b719-e5d400d0566f', 'WRITE_PRIVILEGE');
--Привилегии ролей
INSERT INTO PUBLIC.ROLES_PRIVILEGES (PRIVILEGE_ID, ROLE_ID) VALUES ('10fad87d-ac18-44ac-a75b-38d0fb1e6351', 'fba1ffbb-5e6c-4abf-8d1d-aff59ca895b9');
INSERT INTO PUBLIC.ROLES_PRIVILEGES (PRIVILEGE_ID, ROLE_ID) VALUES ('54f7f41e-e83d-4f40-b719-e5d400d0566f', 'fba1ffbb-5e6c-4abf-8d1d-aff59ca895b9');
INSERT INTO PUBLIC.ROLES_PRIVILEGES (PRIVILEGE_ID, ROLE_ID) VALUES ('10fad87d-ac18-44ac-a75b-38d0fb1e6351', 'e15f209c-6398-4647-9c8d-d3101349e198');
--Назначения
INSERT INTO PUBLIC.USERS_ROLES (ROLE_ID, USER_ID) VALUES ('fba1ffbb-5e6c-4abf-8d1d-aff59ca895b9', 'aa25d9af-f18b-4bf1-bef6-f70176b89b52');
INSERT INTO PUBLIC.USERS_ROLES (ROLE_ID, USER_ID) VALUES ('e15f209c-6398-4647-9c8d-d3101349e199', '4c1e8ca4-c820-43c9-97e2-186edf346523');
-- Предметная область
insert into PUBLIC.SUBJECTAREAS (id, name, description) values ('cc1e8ca4-c820-43c9-97e2-186edf346521', 'Математика', '');
insert into PUBLIC.SUBJECTAREAS (id, name, description) values ('cc1e8ca4-c820-43c9-97e2-186edf346522', 'Физика', '');
insert into PUBLIC.SUBJECTAREAS (id, name, description) values ('cc1e8ca4-c820-43c9-97e2-186edf346523', 'Литература', '');
-- Параметры рейтинга
insert into PUBLIC.rating_parameters (id, code, description, min_value, max_value, coefficient) values ('dc1e8ca4-c820-43c9-97e2-186edf346521', 'AVERAGE_SCORE', 'Средний балл автора заявки', 0.0, 5.0, 0.4);
insert into PUBLIC.rating_parameters (id, code, description, min_value, max_value, coefficient) values ('dc1e8ca4-c820-43c9-97e2-186edf346522', 'REQUEST_TITLE', 'Тема заявки', 0.0, 10.0, 0.1);
insert into PUBLIC.rating_parameters (id, code, description, min_value, max_value, coefficient) values ('dc1e8ca4-c820-43c9-97e2-186edf346523', 'TYPE_ORGANIZATION', 'Тип учреждения автора заявки', 0.0, 10.0, 0.3);
insert into PUBLIC.rating_parameters (id, code, description, min_value, max_value, coefficient) values ('dc1e8ca4-c820-43c9-97e2-186edf346524', 'AUTHOR_RATING', 'Рейтинг автора', 0.0, 10.0, 0.7);
insert into PUBLIC.rating_parameters (id, code, description, min_value, max_value, coefficient) values ('dc1e8ca4-c820-43c9-97e2-186edf346525', 'DEADLINE', 'Близость срока исполнения', 0.0, 10.0, 0.5);