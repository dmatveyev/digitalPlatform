package com.example.digitalplatform.db.repository;

import com.example.digitalplatform.db.model.TeacherInfo;
import com.example.digitalplatform.db.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TeacherInfoRepository extends JpaRepository<TeacherInfo, UUID> {

    TeacherInfo findByUser(User user);

    List<TeacherInfo> findByUserIn(List<User> users);
}
