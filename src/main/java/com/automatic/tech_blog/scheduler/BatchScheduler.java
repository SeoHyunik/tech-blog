package com.automatic.tech_blog.scheduler;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class BatchScheduler {

  private final JobLauncher jobLauncher;
  private final Job techBlogJob;

  @Scheduled(cron = "0 50 * * * *") // Every hour at 50 minutes
  public void runTechBlogJob() {
    try {
      String uuid = UUID.randomUUID().toString();
      MDC.put("uuid", uuid);
      JobParameters jobParameters = new JobParametersBuilder()
          .addLong("startAt", System.currentTimeMillis())
          .toJobParameters();

      jobLauncher.run(techBlogJob, jobParameters);
      log.info("TechBlogJob executed successfully.");
    } catch (Exception e) {
      log.error("Error while running TechBlogJob", e);
    } finally {
      MDC.clear();
    }
  }
}
