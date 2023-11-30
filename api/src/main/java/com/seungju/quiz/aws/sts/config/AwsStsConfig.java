package com.seungju.quiz.aws.sts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;

public class AwsStsConfig {
    @Value("${cloud.aws.sts.region}")
    private Region region;

    @Bean
    public StsClient stsClient() {
        return StsClient.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
