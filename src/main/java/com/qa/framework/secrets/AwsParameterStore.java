package com.qa.framework.secrets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.qa.framework.base.ConstantTest.*;

/**
 * Reads (SecureString) parameters from AWS Systems Manager Parameter Store.
 *
 * <p>Credentials come from the AWS default provider chain: env vars
 * (AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY), ~/.aws/credentials profile (AWS_PROFILE),
 * or the IAM role of the Jenkins agent (EC2 instance profile / ECS task role / IRSA).
 *
 * <p>Region: {@code -Daws.region}, env {@code AWS_REGION}, or {@code aws.region} in the config file
 * (default ap-south-1). Values are cached for the JVM lifetime so parallel threads hit AWS once.
 */
public final class AwsParameterStore {

    private static final Logger LOG = LogManager.getLogger(AwsParameterStore.class);
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();
    private static volatile SsmClient client;

    private AwsParameterStore() {
    }

    public static String getParameter(String name) {
        return CACHE.computeIfAbsent(name, AwsParameterStore::fetch);
    }

    private static String fetch(String name) {
        LOG.info("Fetching secret from AWS Parameter Store: {}", name); // never log the value
        try {
            return client().getParameter(GetParameterRequest.builder()
                            .name(name)
                            .withDecryption(true)
                            .build())
                    .parameter()
                    .value();
        } catch (SdkException e) {
            throw new IllegalStateException("Could not read SSM parameter '" + name + "'. Check AWS credentials, "
                    + "region and that the IAM identity has ssm:GetParameter (+ kms:Decrypt for SecureString). Cause: "
                    + e.getMessage(), e);
        }
    }

    private static SsmClient client() {
        if (client == null) {
            synchronized (AwsParameterStore.class) {
                if (client == null) {
                    client = SsmClient.builder().region(Region.of(region())).build();
                }
            }
        }
        return client;
    }

    private static String region() {
        // Deliberately not using ConfigManager.get() here to avoid a circular ssm: lookup.
        String r = System.getProperty(KEY_AWS_REGION);
        if (r == null || r.isBlank()) {
            r = System.getenv(KEY_AWS_REGION_ENV_VAR);
        }
        if (r == null || r.isBlank()) {
            r = com.qa.framework.config.ConfigManager.raw(KEY_AWS_REGION);
        }
        return (r == null || r.isBlank()) ? DEFAULT_AWS_REGION : r.trim();
    }
}
