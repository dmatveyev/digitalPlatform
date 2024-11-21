package com.example.digitalplatform;

import com.example.digitalplatform.db.model.Privilege;
import com.example.digitalplatform.db.model.Role;
import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.db.repository.PrivilegeRepository;
import com.example.digitalplatform.db.repository.RoleRepository;
import com.example.digitalplatform.db.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class DigitalPlatformApplication {

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PrivilegeRepository privilegeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;
    private static boolean alreadySetup = true;

    public static void main(String[] args) {
        SpringApplication.run(DigitalPlatformApplication.class, args);
    }


    @Transactional
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (!alreadySetup) {
            Privilege readPrivilege
                    = createPrivilegeIfNotFound("READ_PRIVILEGE");
            Privilege writePrivilege
                    = createPrivilegeIfNotFound("WRITE_PRIVILEGE");

            List<Privilege> adminPrivileges = Arrays.asList(
                    readPrivilege, writePrivilege);
            createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);
            createRoleIfNotFound("ROLE_USER", Collections.singletonList(readPrivilege));

            Role adminRole = roleRepository.findByName("ROLE_ADMIN");
            User user = new User();
            user.setLogin("user");
            user.setPassword(passwordEncoder.encode("user"));
            user.setRoles(Collections.singletonList(adminRole));
            user.setEnabled(true);
            user.setTokenExpired(false);
            userRepository.save(user);

            alreadySetup = true;
        }

    }

    @Transactional
    Privilege createPrivilegeIfNotFound(String name) {

        Privilege privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = new Privilege();
            privilege.setName(name);
            privilegeRepository.save(privilege);
        }
        return privilege;
    }

    @Transactional
    Role createRoleIfNotFound(
            String name, List<Privilege> privileges) {

        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role();
            role.setName(name);
            role.setPrivileges(privileges);
            roleRepository.save(role);
        }
        return role;
    }

}
