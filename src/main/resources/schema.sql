drop table if exists USERS cascade;
create table USERS
(
    ID            UUID             not null
        primary key,
    LOGIN         CHARACTER VARYING(255),
    PASSWORD      CHARACTER VARYING(255),
    FIRST_NAME CHARACTER VARYING(255),
    LAST_NAME  CHARACTER VARYING(255),
    MIDDLE_NAME  CHARACTER VARYING(255),
    EMAIL  CHARACTER VARYING(255),
    ENABLED       BOOLEAN,
    TOKEN_EXPIRED BOOLEAN
);

drop table if exists TEACHER_INFO cascade;
create table TEACHER_INFO
(
    ID         UUID             not null
        primary key,
    USER_ID    UUID,
    SCORE      FLOAT not null,
    DEGREE     CHARACTER VARYING(255),
    INSTITUTE  CHARACTER VARYING(255),
    LIMIT_HOURS INTEGER,
    constraint teacher_info_users_fk
        foreign key (USER_ID) references USERS
);

drop table if exists STUDENT_INFO cascade;
create table STUDENT_INFO
(
    ID         UUID             not null
        primary key,
    USER_ID    UUID,
    SCORE      FLOAT not null,
    DEGREE     CHARACTER VARYING(255),
    INSTITUTE  CHARACTER VARYING(255),
    CLAZZ  CHARACTER VARYING(255),
    constraint student_info_users_fk
        foreign key (USER_ID) references USERS
);

drop table if exists ROLES cascade;
create table ROLES
(
    ID          UUID not null
        primary key,
    NAME        CHARACTER VARYING(255),
    CODE        CHARACTER VARYING(255),
    DESCRIPTION CHARACTER VARYING(255)
);

drop table if exists PRIVILEGES cascade;
create table PRIVILEGES
(
    ID   UUID not null
        primary key,
    NAME CHARACTER VARYING(255)
);

drop table if exists ROLES_PRIVILEGES;
create table ROLES_PRIVILEGES
(
    PRIVILEGE_ID UUID not null,
    ROLE_ID      UUID not null,
    constraint roles_privileges_privileges_fk
        foreign key (PRIVILEGE_ID) references PRIVILEGES,
    constraint roles_privileges_roles_fk
        foreign key (ROLE_ID) references ROLES
);

drop table if exists USERS_ROLES;
create table USERS_ROLES
(
    ROLE_ID UUID not null,
    USER_ID UUID not null,
    constraint users_roles_roles_fk
        foreign key (ROLE_ID) references ROLES,
   constraint users_roles_users_fk
            foreign key (USER_ID) references USERS
);


drop table if exists SUBJECTAREAS cascade;
create table SUBJECTAREAS
(
    ID          UUID not null
        primary key,
    NAME        CHARACTER VARYING(255),
    DESCRIPTION CHARACTER VARYING(255)
);

drop table if exists TEACHERS_SUBJECT_AREAS cascade;
create table TEACHERS_SUBJECT_AREAS
(
    TEACHER_ID    UUID,
    SUBJECTAREA_ID    UUID,
    constraint teachers_subject_areas_teacher_info_fk
        foreign key (TEACHER_ID) references TEACHER_INFO,
    constraint teachers_subject_areas_subject_areas_fk
        foreign key (SUBJECTAREA_ID) references SUBJECTAREAS
);

drop table if exists REQUESTS;
create table REQUESTS
(
    ID                 UUID             not null
        primary key,
    TITLE              CHARACTER VARYING(255),
    DESCRIPTION        CHARACTER VARYING(255),
    RATING             FLOAT not null,
    STATUS             CHARACTER VARYING(255),
    WORK_TYPE          CHARACTER VARYING(255),
    PERIODICAL         BOOLEAN,
    TIME               INTEGER          not null,
    CUSTOMER_ID        UUID,
    WORKER_ID          UUID,
    SUBJECT_AREA_ID    UUID,
    CREATION_DATE      TIMESTAMP,
    PLANED_FINISH_DATE TIMESTAMP,
    PARTICIPANTS_SCORE INTEGER,
    TEACHER_SCORE      INTEGER,
    ACTUAL_FINISH_DATE TIMESTAMP,
    constraint requests_customers_fk
        foreign key (CUSTOMER_ID) references USERS,
    constraint request_workers_fk
        foreign key (WORKER_ID) references USERS,
    constraint request_subject_areas_fk
        foreign key (SUBJECT_AREA_ID) references SUBJECTAREAS
);


drop table if exists rating_parameters cascade;
create table rating_parameters
(
    ID          UUID not null
        primary key,
    CODE        CHARACTER VARYING(255),
    DESCRIPTION CHARACTER VARYING(255),
    MIN_VALUE   FLOAT,
    MAX_VALUE   FLOAT,
    COEFFICIENT FLOAT
);


