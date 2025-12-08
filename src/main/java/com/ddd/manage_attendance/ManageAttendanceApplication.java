package com.ddd.manage_attendance;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ManageAttendanceApplication {

    public static void main(String[] args) {
        // .env 파일 로드
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        // 환경 변수로 설정
        dotenv.entries()
                .forEach(
                        entry -> {
                            System.setProperty(entry.getKey(), entry.getValue());
                        });

        SpringApplication.run(ManageAttendanceApplication.class, args);
    }
}
