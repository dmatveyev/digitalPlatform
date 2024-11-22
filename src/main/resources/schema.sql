drop table if exists USERS cascade;
create table USERS
(
    ID   UUID not null
        primary key,
    LOGIN CHARACTER VARYING(255),
    PASSWORD CHARACTER VARYING(255),
    ENABLED BOOLEAN,
    TOKEN_EXPIRED BOOLEAN,
    SCORE      DOUBLE PRECISION not null,
    DEGREE     CHARACTER VARYING(255),
    FIRST_NAME CHARACTER VARYING(255),
    LAST_NAME  CHARACTER VARYING(255),
    INSTITUTE     CHARACTER VARYING(255),
    LIMIT_OURS  INTEGER
);

drop table if exists ROLES cascade;
create table ROLES
(
    ID   UUID not null
        primary key,
    NAME CHARACTER VARYING(255),
    CODE CHARACTER VARYING(255),
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
        foreign key (ROLE_ID) references ROLES
);


drop table if exists SUBJECTAREAS cascade ;
create table SUBJECTAREAS
(
    ID           UUID             not null
        primary key,
    NAME  CHARACTER VARYING(255),
    DESCRIPTION       CHARACTER VARYING(255)
);

drop table if exists REQUESTS;
create table REQUESTS
(
    ID           UUID             not null
        primary key,
    TITLE        CHARACTER VARYING(255),
    DESCRIPTION  CHARACTER VARYING(255),
    RATING       DOUBLE PRECISION not null,
    STATUS       CHARACTER VARYING(255),
    WORK_TYPE       CHARACTER VARYING(255),
    PERIODICAL BOOLEAN,
    TIME         INTEGER          not null,
    CUSTOMER_ID  UUID,
    WORKER_ID    UUID,
    SUBJECT_AREA_ID UUID,
    CREATION_DATE        TIMESTAMP,
    PLANED_FINISH_DATE        TIMESTAMP,
    ACTUAL_FINISH_DATE        TIMESTAMP,
    constraint requests_customers_fk
        foreign key (CUSTOMER_ID) references USERS,
    constraint request_workers_fk
        foreign key (WORKER_ID) references USERS,
    constraint request_subject_areas_fk
        foreign key (SUBJECT_AREA_ID) references SUBJECTAREAS
);


drop table if exists rating_parameters cascade ;
create table rating_parameters
(
    ID           UUID             not null
        primary key,
    CODE  CHARACTER VARYING(255),
    DESCRIPTION       CHARACTER VARYING(255),
    MIN_VALUE DOUBLE PRECISION,
    MAX_VALUE    DOUBLE PRECISION,
    COEFFICIENT       DOUBLE PRECISION
);


