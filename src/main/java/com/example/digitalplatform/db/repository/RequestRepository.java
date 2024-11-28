package com.example.digitalplatform.db.repository;

import com.example.digitalplatform.db.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RequestRepository extends JpaRepository<Request, UUID>, PagingAndSortingRepository<Request, UUID> {
    List<Request> findByCustomer(User user);

    List<Request> findByWorker(User user);

    List<Request> findByStatus(RequestStatus requestStatus);

    Page<Request> findByCustomer(User user, Pageable pageable);

    Page<Request> findByWorker(User user, Pageable pageable);

    Page<Request> findByCustomerAndStatusInAndSubjectAreaIn(User user, Pageable pageable, List<RequestStatus> requestStatuses, List<SubjectArea> subjectArea);

    Page<Request> findByWorkerAndStatusInAndSubjectAreaIn(User user, Pageable pageable, List<RequestStatus> requestStatuses, List<SubjectArea> subjectArea);

    Page<Request> findByStatusInAndSubjectAreaIn(Pageable pageable, List<RequestStatus> requestStatuses, List<SubjectArea> subjectArea);

    List<Request> findByCustomerAndStatusIn(User customer, List<RequestStatus> statuses);

    @Query(value = """
            select
                (u.id::text) as userId,
                u.LAST_NAME as lastName,
                u.FIRST_NAME as firstName,
                ti.DEGREE as degree,
                ti.INSTITUTE as institute,
                count(case when r.STATUS = 'FINISHED' then 1 end) as countDone,
                count(case when r.STATUS = 'DECLINED' then 1 end) as countDeclined,
                count(case when r.PLANED_FINISH_DATE < r.ACTUAL_FINISH_DATE then 1 end) as countExpired,
                count(u.id) as countAssigned
            from REQUESTS r
            left join TEACHER_INFO ti on ti.USER_ID = r.WORKER_ID
            left join USERS u on u.ID= ti.USER_ID
            where r.status in ('FINISHED', 'DECLINED') and r.CREATION_DATE > :start
            group by r.WORKER_ID
            """, nativeQuery = true)
    List<IReport> findTerminatedRequestsGroupByTeacher(@Param("start") LocalDateTime startDate);

    @Query(value = """
            select (u.id::text) as userId,
                   u.LAST_NAME as lastName,
                   u.FIRST_NAME as firstName,
                   ti.DEGREE as degree,
                   ti.INSTITUTE as institute,
                   s.NAME as subjectArea,
                   count(case when r.STATUS = 'FINISHED' then 1 end) as countDone,
                   count(case when r.STATUS = 'DECLINED' then 1 end) as countDeclined,
                   count(case when r.PLANED_FINISH_DATE < r.ACTUAL_FINISH_DATE then 1 end) as countExpired,
                   count(u.id) as countAssigned
            from PUBLIC.REQUESTS r
            left join PUBLIC.TEACHER_INFO ti on ti.USER_ID = r.WORKER_ID
            left join PUBLIC.USERS u on u.ID= ti.USER_ID
            left join SUBJECTAREAS s on s.ID = r.SUBJECT_AREA_ID
            where status in ('FINISHED', 'DECLINED') and r.CREATION_DATE > :start
            group by r.WORKER_ID, s.NAME
            """, nativeQuery = true)
    List<IReport> findTerminatedRequestsGroupByTeacherAndSubjectArea(@Param("start") LocalDateTime startDate);

    @Query(value = """
            select  s.NAME as subjectArea,
                    count(case when r.STATUS = 'FINISHED' then 1 end) as countDone,
                    count(case when r.STATUS = 'DECLINED' then 1 end) as countDeclined,
                    count(case when r.PLANED_FINISH_DATE < r.ACTUAL_FINISH_DATE then 1 end) as countExpired,
                    count(s.NAME) as countAssigned
            from REQUESTS r
            left join SUBJECTAREAS S on S.ID = r.SUBJECT_AREA_ID
            where status in ('FINISHED', 'DECLINED') and r.CREATION_DATE > :start
            group by s.NAME
            """, nativeQuery = true)
    List<IReport> findTerminatedRequestsGroupBySubjectArea(@Param("start") LocalDateTime startDate);

    @Query(value = """
            select s.NAME as subjectArea,
                   count(case when r.STATUS = 'FINISHED' then 1 end) as countDone,
                   count(case when r.STATUS = 'DECLINED' then 1 end) as countDeclined,
                   count(case when r.PLANED_FINISH_DATE < r.ACTUAL_FINISH_DATE then 1 end) as countExpired,
                   count(s.NAME) as countAssigned
            from PUBLIC.REQUESTS r
            left join PUBLIC.USERS u on u.ID= r.WORKER_ID
            left join SUBJECTAREAS s on s.ID = r.SUBJECT_AREA_ID
            where status in ('FINISHED', 'DECLINED') and u.id = :id and r.CREATION_DATE > :start
            group by s.NAME;
            """, nativeQuery = true)
    List<IReport> findTerminatedRequestsByTeacherGroupBySubjectArea(@Param("id") UUID id, @Param("start") LocalDateTime startDate);


}
