package com.example.coursemanagementsystem.config;

import com.example.coursemanagementsystem.model.entity.Role;
import com.example.coursemanagementsystem.model.entity.User;
import com.example.coursemanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@coursemanagement.com")
                    .fullName("System Administrator")
                    .role(Role.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            System.out.println(">>> Đã tạo tài khoản ADMIN mặc định: admin / admin123");
        } else {
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setPassword(passwordEncoder.encode("admin123"));
            userRepository.save(admin);
            System.out.println(">>> Đã cập nhật tài khoản ADMIN mặc định: admin / admin123");
        }

        if (!userRepository.existsByUsername("lecturer01")) {
            User lecturer = User.builder()
                    .username("lecturer01")
                    .password(passwordEncoder.encode("lecturer123"))
                    .email("lecturer01@coursemanagement.com")
                    .fullName("Nguyễn Văn Giảng Viên")
                    .role(Role.LECTURER)
                    .active(true)
                    .build();
            userRepository.save(lecturer);
            System.out.println(">>> Đã tạo tài khoản LECTURER mẫu: lecturer01 / lecturer123");
        }
    }
}
