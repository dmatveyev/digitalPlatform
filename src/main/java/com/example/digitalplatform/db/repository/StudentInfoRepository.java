package com.example.digitalplatform.db.repository;

import com.example.digitalplatform.db.model.StudentInfo;
import com.example.digitalplatform.db.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StudentInfoRepository extends JpaRepository<StudentInfo, UUID> {

    StudentInfo findByUser(User user);

    List<StudentInfo> findByUserIn(List<User> users);

    List<StudentInfo> getByInstituteAndClazz(String institute, String clazz);
}
