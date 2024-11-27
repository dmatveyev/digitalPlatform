package com.example.digitalplatform.service;

import com.example.digitalplatform.controller.ReportType;
import com.example.digitalplatform.db.model.*;
import com.example.digitalplatform.db.repository.RequestRepository;
import com.example.digitalplatform.db.repository.SubjectAreaRepository;
import com.example.digitalplatform.db.repository.UserRepository;
import com.example.digitalplatform.dto.ReportData;
import com.example.digitalplatform.service.handlers.reporttype.ByTeacherGenerator;
import com.example.digitalplatform.service.handlers.reporttype.ReportTypeGenerator;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
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
    List<ReportTypeGenerator> reportTypeGenerators;

    @NonFinal
    Map<ReportType, ReportTypeGenerator> map;
    @PostConstruct
    public void init() {
        map = reportTypeGenerators.stream().collect(Collectors.toMap(ReportTypeGenerator::getReportType, Function.identity()));
    }

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

    public byte[] generate(User user, ReportData reportData) {
        List<ReportModel> byPrincipalAndReportType = findByPrincipalAndReportType(user, reportData.getReportType());
        reportData.setData(byPrincipalAndReportType);
        ReportTypeGenerator reportTypeGenerator = map.get(ReportType.BY_TEACHERS);
        byte[] generate = reportTypeGenerator.generate(user, reportData);
        return generate;
    }
}
