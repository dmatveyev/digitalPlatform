package com.example.digitalplatform.core;

import com.example.digitalplatform.db.model.Request;
import com.example.digitalplatform.db.model.TeacherInfo;

import java.util.List;

public interface GeneratorDecision {
    List<Request> execute(List<Request> requests, TeacherInfo user);

    GeneratorType generatorType();
}
