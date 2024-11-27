package com.example.digitalplatform.service;

import com.example.digitalplatform.controller.ReportType;
import com.example.digitalplatform.db.model.*;
import com.example.digitalplatform.db.repository.RequestRepository;
import com.example.digitalplatform.db.repository.SubjectAreaRepository;
import com.example.digitalplatform.db.repository.UserRepository;
import com.example.digitalplatform.service.handlers.reporttype.ByTeacherGenerator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ReportService {

    RequestRepository reportRepository;
    SubjectAreaRepository subjectAreaRepository;
    UserRepository userService;
    ByTeacherGenerator byTeacherGenerator;

    public List<ReportModel> findByPrincipalAndReportType(User byLogin, ReportType reportType) {
        RoleType code = byLogin.getRole().getCode();
        List<IReport> terminatedRequests =
        switch (code) {
            case ADMIN -> getAdminReports(reportType);
            case TEACHER -> reportRepository.findTerminatedRequestsByTeacherGroupBySubjectArea(byLogin.getId());
            default -> Collections.emptyList();
        };
        Map<String, SubjectArea> collect = subjectAreaRepository.findAll().stream().collect(Collectors.toMap(SubjectArea::getName, Function.identity()));

        List<ReportModel> reportModels = terminatedRequests.stream()
                .map(rep -> new ReportModel(
                        rep.getUserId() != null ? UUID.fromString(rep.getUserId()) : null,
                        rep.getLastName(),
                        rep.getFirstName(),
                        rep.getDegree(),
                        rep.getInstitute(),
                        collect.get(rep.getSubjectArea()),
                        rep.getCountDone(),
                        rep.getCountExpired(),
                        rep.getCountDeclined(),
                        rep.getCountAssigned()
                )).toList();
        return reportModels;
    }

    private List<IReport> getAdminReports(ReportType reportType) {

        List<IReport> terminatedRequests = reportType == null? Collections.emptyList() :
                switch (reportType) {
            case BY_SUBJECT_AREA -> reportRepository.findTerminatedRequestsGroupBySubjectArea();
            case BY_TEACHERS -> reportRepository.findTerminatedRequestsGroupByTeacher();
            case BY_TEACHES_AND_SUBJECT_AREAS -> reportRepository.findTerminatedRequestsGroupByTeacherAndSubjectArea();
        };
        return terminatedRequests;
    }

    public byte[] generate(User user, ReportType reportType) {
        List<ReportModel> byPrincipalAndReportType = findByPrincipalAndReportType(user, reportType);
        byte[] generate = byTeacherGenerator.generate(user, byPrincipalAndReportType);
        return generate;
    }
}