-- Пользователи
INSERT INTO PUBLIC.USERS (ID, LOGIN, PASSWORD, ENABLED, TOKEN_EXPIRED) VALUES ('4c1e8ca4-c820-43c9-97e2-186edf346523', 'teacher', null, true, false);
INSERT INTO PUBLIC.USERS (ID, LOGIN, PASSWORD, ENABLED, TOKEN_EXPIRED) VALUES ('aa25d9af-f18b-4bf1-bef6-f70176b89b52', 'user', '{bcrypt}$2a$10$AsgwBmSDITYg8KEkQWfPXeiTy9mcJRPHY1esw2bHyrmPny8PCZhIy', true, false);
-- Роли
INSERT INTO PUBLIC.ROLES (ID, NAME) VALUES ('fba1ffbb-5e6c-4abf-8d1d-aff59ca895b9', 'ROLE_ADMIN');
INSERT INTO PUBLIC.ROLES (ID, NAME) VALUES ('e15f209c-6398-4647-9c8d-d3101349e198', 'ROLE_USER');
--Привилегии
INSERT INTO PUBLIC.PRIVILEGES (ID, NAME) VALUES ('10fad87d-ac18-44ac-a75b-38d0fb1e6351', 'READ_PRIVILEGE');
INSERT INTO PUBLIC.PRIVILEGES (ID, NAME) VALUES ('54f7f41e-e83d-4f40-b719-e5d400d0566f', 'WRITE_PRIVILEGE');
--Привилегии ролей
INSERT INTO PUBLIC.ROLES_PRIVILEGES (PRIVILEGE_ID, ROLE_ID) VALUES ('10fad87d-ac18-44ac-a75b-38d0fb1e6351', 'fba1ffbb-5e6c-4abf-8d1d-aff59ca895b9');
INSERT INTO PUBLIC.ROLES_PRIVILEGES (PRIVILEGE_ID, ROLE_ID) VALUES ('54f7f41e-e83d-4f40-b719-e5d400d0566f', 'fba1ffbb-5e6c-4abf-8d1d-aff59ca895b9');
INSERT INTO PUBLIC.ROLES_PRIVILEGES (PRIVILEGE_ID, ROLE_ID) VALUES ('10fad87d-ac18-44ac-a75b-38d0fb1e6351', 'e15f209c-6398-4647-9c8d-d3101349e198');
--Назначения
INSERT INTO PUBLIC.USERS_ROLES (ROLE_ID, USER_ID) VALUES ('fba1ffbb-5e6c-4abf-8d1d-aff59ca895b9', 'aa25d9af-f18b-4bf1-bef6-f70176b89b52');
-- Заказчики
insert into CUSTOMERS (score, id, user_id, first_name, last_name, school) values ( '4.5', '5c1e8ca4-c820-43c9-97e2-186edf346522', 'aa25d9af-f18b-4bf1-bef6-f70176b89b52', 'Den', 'Matveev', 'МБОУ Школа №112');
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