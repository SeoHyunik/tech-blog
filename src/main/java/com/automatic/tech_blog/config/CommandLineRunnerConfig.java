package com.automatic.tech_blog.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CommandLineRunnerConfig {

  private final JobLauncher jobLauncher;
  private final Job techBlogJob;

  @Bean
  public CommandLineRunner runJob() {
    return args -> {
      try {
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("startAt", System.currentTimeMillis())
            .toJobParameters();
        jobLauncher.run(techBlogJob, jobParameters);
      } catch (Exception e) {
        log.error("Error while running batch job", e);
      }
    };
  }
}