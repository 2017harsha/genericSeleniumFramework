package com.qa.framework.config;

import com.qa.framework.secrets.AwsParameterStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import static com.qa.framework.base.ConstantTest.*;

/**
 * Central configuration lookup.
 *
 * <p>Files loaded (later overrides earlier):
 * <ol>
 *   <li>{@code config/common.properties}</li>
 *   <li>{@code config/<env>.properties} where env comes from {@code -Denv=qa|dev|prod} (default qa)</li>
 * </ol>
 *
 * <p>Resolution order for any key, highest priority first:
 * <ol>
 *   <li>JVM system property ({@code -Dapp.url=...})</li>
 *   <li>OS environment variable (key upper-cased, dots/dashes to underscores: {@code APP_URL})</li>
 *   <li>Properties files above</li>
 * </ol>
 *
 * <p>Any resolved value that starts with {@code ssm:} is treated as an AWS Systems Manager
 * Parameter Store path and fetched (decrypted) at runtime, e.g.
 * {@code app.password=ssm:/selenium-framework/prod/app.password}.
 */
public final class ConfigManager {

    private static final Logger LOG = LogManager.getLogger(ConfigManager.class);
    private static final String ENV = resolveEnv();
    private static final Properties PROPS = loadProperties(ENV);

    private ConfigManager() {
    }

    /** Active environment name: qa, dev or prod. */
    public static String env() {
        return ENV;
    }

    /** Value for key, or {@code null} if not set anywhere. Resolves ssm: references. */
    public static String get(String key) {
        String raw = raw(key);
        if (raw != null && raw.startsWith(SSM_PREFIX)) {
            return AwsParameterStore.getParameter(raw.substring(SSM_PREFIX.length()).trim());
        }
        return raw;
    }

    public static String get(String key, String defaultValue) {
        String v = get(key);
        return isBlank(v) ? defaultValue : v;
    }

    /** Value for key; fails fast with a clear message when it is missing. */
    public static String getRequired(String key) {
        String v = get(key);
        if (isBlank(v)) {
            throw new IllegalStateException("Missing required config '" + key + "' for env '" + ENV
                    + "'. Set it in config/" + ENV + ".properties, as -D" + key + "=..., or env var " + toEnvVar(key));
        }
        return v;
    }

    public static int getInt(String key, int defaultValue) {
        String v = get(key);
        return isBlank(v) ? defaultValue : Integer.parseInt(v.trim());
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String v = get(key);
        return isBlank(v) ? defaultValue : Boolean.parseBoolean(v.trim());
    }

    /** Raw value without resolving secrets (safe for logging whether a key is an ssm reference). */
    public static String raw(String key) {
        String v = System.getProperty(key);
        if (isBlank(v)) {
            v = System.getenv(toEnvVar(key));
        }
        if (isBlank(v)) {
            v = PROPS.getProperty(key);
        }
        return isBlank(v) ? null : v.trim();
    }

    // ------------------------------------------------------------------------------------------

    private static String resolveEnv() {
        String env = System.getProperty(KEY_ENV);
        if (isBlank(env)) {
            env = System.getenv(KEY_ENV_VAR);
        }
        return isBlank(env) ? DEFAULT_ENV : env.trim().toLowerCase(Locale.ROOT);
    }

    private static Properties loadProperties(String env) {
        Properties props = new Properties();
        loadInto(props, COMMON_CONFIG_FILE, false);
        loadInto(props, CONFIG_DIR + env + PROPERTIES_EXT, true);
        LOG.info("Loaded configuration for environment '{}'", env);
        return props;
    }

    private static void loadInto(Properties props, String resource, boolean required) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                if (required) {
                    throw new IllegalStateException("Config file not found on classpath: " + resource
                            + " (valid envs are the files under src/test/resources/config)");
                }
                return;
            }
            Properties p = new Properties();
            p.load(in);
            props.putAll(p);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read " + resource, e);
        }
    }

    private static String toEnvVar(String key) {
        return key.toUpperCase(Locale.ROOT).replace('.', '_').replace('-', '_');
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
