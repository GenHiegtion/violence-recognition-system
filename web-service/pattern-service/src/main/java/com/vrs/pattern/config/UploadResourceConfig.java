package com.vrs.pattern.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    private final Path resourceRoot;

    public UploadResourceConfig(@Value("${app.upload-dir:${user.dir}/../../uploads/patterns}") String uploadDir) {
        Path uploadRoot = Paths.get(uploadDir).normalize().toAbsolutePath();
        Path parent = uploadRoot.getParent();
        this.resourceRoot = parent != null ? parent : uploadRoot;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = resourceRoot.toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
