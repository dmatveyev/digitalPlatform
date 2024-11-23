package com.example.digitalplatform.repository;

import com.example.digitalplatform.db.model.SubjectArea;
import com.example.digitalplatform.db.model.TeacherInfo;
import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.db.repository.TeacherInfoRepository;
import com.example.digitalplatform.db.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TeacherInfoTest {

    @Autowired
    TeacherInfoRepository teacherInfoRepository;
    @Autowired
    UserRepository userRepository;

    @Test
    void success() {
        User teacher = userRepository.findByLogin("teacher");
        assertNotNull(teacher);
        TeacherInfo byUser = teacherInfoRepository.findByUser(teacher);
        assertNotNull(byUser);
        List<SubjectArea> subjectAreas = byUser.getSubjectAreas();
        assertEquals(2,subjectAreas.size());

    }

}
