drop table if exists USERS cascade;
create table USERS
(
    ID   UUID not null
        primary key,
    LOGIN CHARACTER VARYING(255),
    PASSWORD CHARACTER VARYING(255),
    ENABLED BOOLEAN,
    TOKEN_EXPIRED BOOLEAN
);

drop table if exists ROLES cascade;
create table ROLES
(
    ID   UUID not null
        primary key,
    NAME CHARACTER VARYING(255)
);

drop table if exists CUSTOMERS cascade;
create table CUSTOMERS
(
    SCORE      DOUBLE PRECISION not null,
    ID         UUID             not null
        primary key,
    USER_ID    UUID
        unique,
    FIRST_NAME CHARACTER VARYING(255),
    LAST_NAME  CHARACTER VARYING(255),
    SCHOOL     CHARACTER VARYING(255),
    constraint customers_user_fk
        foreign key (USER_ID) references USERS
);
drop table if exists PRIVILEGES cascade ;
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

drop table if exists WORKERS cascade;
create table WORKERS
(
    SCORE      DOUBLE PRECISION not null,
    ID         UUID             not null
        primary key,
    USER_ID    UUID
        unique,
    DEGREE     CHARACTER VARYING(255),
    FIRST_NAME CHARACTER VARYING(255),
    LAST_NAME  CHARACTER VARYING(255),
    INSTITUTE     CHARACTER VARYING(255),
    constraint workers_users_fk
        foreign key (USER_ID) references USERS
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
    RATING       DOUBLE PRECISION not null,
    TIME         INTEGER          not null,
    CUSTOMER_ID  UUID,
    ID           UUID             not null
        primary key,
    WORKER_ID    UUID,
    DESCRIPTION  CHARACTER VARYING(255),
    STATUS       CHARACTER VARYING(255),
    SUBJECT_AREA_ID UUID,
    TITLE        CHARACTER VARYING(255),
    constraint requests_customers_fk
        foreign key (CUSTOMER_ID) references CUSTOMERS,
    constraint request_workers_fk
        foreign key (WORKER_ID) references WORKERS,
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


