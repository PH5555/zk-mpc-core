package com.zkrypto.zk_mpc_core.common.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Configuration
@Slf4j
public class TssLibraryConfig {
    @Value("classpath:libthreshold_ecdsa.dylib")
    private Resource libraryForMac;

    @Value("classpath:libthreshold_ecdsa.so")
    private Resource libraryForLinux;

    @Value("classpath:libthreshold_ecdsa.dll")
    private Resource libraryForWindow;

    @PostConstruct
    private void loadLibrary() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("linux")) {
            log.info("Loading Linux (.so) library...");
            loadLibraryFromResource(libraryForLinux);
        }
        else if (os.contains("mac")) {
            log.info("Loading macOS (.dylib) library...");
            loadLibraryFromResource(libraryForMac);
        }
        else if (os.contains("win")) {
            log.info("Loading Windows (.dll) library...");
            loadLibraryFromResource(libraryForWindow);
        }
        else {
            log.info("Not Support os for library");
        }
    }

    /**
     * resources에 있는 라이브러리 파일을 서버의 파일 시스템으로 복사하고 로드하기 위한 코드
     * @param resource resource 이름
     */
    private void loadLibraryFromResource(Resource resource) {
        // 리소스로부터 InputStream을 얻기
        try (InputStream inputStream = resource.getInputStream()) {

            // 임시 파일을 생성
            String[] parts = resource.getFilename().split("\\.");
            String prefix = parts[0];
            String suffix = "." + parts[1];
            File tempFile = File.createTempFile(prefix, suffix);

            // JVM 종료 시 임시 파일을 자동으로 삭제하도록 설정
            tempFile.deleteOnExit();

            // 리소스의 내용을 임시 파일에 복사
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 임시 파일의 절대 경로를 사용하여 네이티브 라이브러리를 로드
            System.load(tempFile.getAbsolutePath());

            log.info("Successfully loaded library from: " + tempFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
