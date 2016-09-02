package us.kbase.auth2.service.kbase;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.ini4j.Ini;
import org.slf4j.LoggerFactory;

import us.kbase.auth2.service.AuthConfig;
import us.kbase.auth2.service.SLF4JAutoLogger;
import us.kbase.auth2.service.IdentityProvider;
import us.kbase.auth2.service.exceptions.AuthConfigurationException;
import us.kbase.common.service.JsonServerSyslog;

public class KBaseAuthConfig implements AuthConfig {
	
	//TODO JAVADOC
	//TODO TEST unittests
	
	private static final String KB_DEP = "KB_DEPLOYMENT_CONFIG";
	private static final String CFG_LOC ="authserv2";
	private static final String DEFAULT_LOG_NAME = "KBaseAuthService2";
	private static final String TEMP_KEY_CFG_FILE = "temp-key-config-file";
	
	private static final String KEY_LOG_NAME = "log-name";
	private static final String KEY_MONGO_HOST = "mongo-host";
	private static final String KEY_MONGO_DB = "mongo-db";
	private static final String KEY_MONGO_USER = "mongo-user";
	private static final String KEY_MONGO_PWD = "mongo-pwd";
	private static final String KEY_ID_PROV = "identity-providers";
	private static final String KEY_PREFIX_ID_PROVS = "identity-providers-";
	private static final String KEY_SUFFIX_ID_PROVS_IMG = "-image-url";
	private static final String KEY_SUFFIX_ID_PROVS_CLIENT_ID = "-client-id";
	private static final String KEY_SUFFIX_ID_PROVS_CLIENT_SEC =
			"-client-secret";
	private static final String KEY_SUFFIX_ID_PROVS_REDIRECT = "-redirect-uri";
	
	private final SLF4JAutoLogger logger;
	private final String mongoHost;
	private final String mongoDB;
	private final String mongoUser;
	private final char[] mongoPwd;
	private final Set<IdentityProvider> providers;

	public KBaseAuthConfig() throws AuthConfigurationException {
		final Map<String, String> cfg = getConfig();
		final String ln = getString(KEY_LOG_NAME, cfg);
		logger = new JsonServerSysLogAutoLogger(new JsonServerSyslog(
				ln == null ? DEFAULT_LOG_NAME : ln,
				//TODO KBASECOMMON allow null for the fake config prop arg
				"thisisafakekeythatshouldntexistihope",
				JsonServerSyslog.LOG_LEVEL_INFO, true));
		try {
			mongoHost = getString(KEY_MONGO_HOST, cfg, true);
			mongoDB = getString(KEY_MONGO_DB, cfg, true);
			mongoUser = getString(KEY_MONGO_USER, cfg);
			String mongop = getString(KEY_MONGO_PWD, cfg);
			if (mongoUser != null ^ mongop != null) {
				mongop = null; //gc
				throw new AuthConfigurationException(String.format(
						"Must provide both %s and %s params in config file " +
						"%s section %s if MongoDB authentication is to be " +
						"used",
						KEY_MONGO_USER, KEY_MONGO_PWD,
						cfg.get(TEMP_KEY_CFG_FILE), CFG_LOC));
			}
			mongoPwd = mongop == null ? null : mongop.toCharArray();
			mongop = null; //gc
		} catch (AuthConfigurationException e) {
			LoggerFactory.getLogger(getClass()).error(
					"Configuration error", e);
			throw e;
		}
		
		
		providers = null; //TODO NOW Providers immutable set
	}
	
	private static class JsonServerSysLogAutoLogger implements SLF4JAutoLogger {
		@SuppressWarnings("unused")
		private JsonServerSyslog logger; // keep a reference to avoid gc

		private JsonServerSysLogAutoLogger(JsonServerSyslog logger) {
			super();
			this.logger = logger;
		}
	}
	
	// returns null if no string
	private String getString(
			final String paramName,
			final Map<String, String> config)
			throws AuthConfigurationException {
		return getString(paramName, config, false);
	}
	
	private String getString(
			final String paramName,
			final Map<String, String> config,
			final boolean except)
			throws AuthConfigurationException {
		final String s = config.get(paramName);
		if (s != null && !s.trim().isEmpty()) {
			return s.trim();
		} else if (except) {
			throw new AuthConfigurationException(String.format(
					"Required parameter %s not provided in configuration " +
					"file %s, section %s",
					paramName, config.get(TEMP_KEY_CFG_FILE), CFG_LOC));
		} else {
			return null;
		}
	}

	private Map<String, String> getConfig() throws AuthConfigurationException {
		final String file = System.getProperty(KB_DEP) == null ?
				System.getenv(KB_DEP) : System.getProperty(KB_DEP);
		if (file == null) {
			throw new AuthConfigurationException(String.format(
					"Deployment configuration variable %s not in " +
					"environment or system properties", KB_DEP));
		}
		final File deploy = new File(file).getAbsoluteFile();
		final Ini ini;
		try {
			ini = new Ini(deploy);
		} catch (IOException ioe) {
			throw new AuthConfigurationException(String.format(
					"Could not read configuration file %s: %s",
					deploy, ioe.getMessage()), ioe);
		}
		final Map<String, String> config = ini.get(CFG_LOC);
		if (config == null) {
			throw new AuthConfigurationException(String.format(
					"No section %s in config file %s", CFG_LOC, deploy));
		}
		config.put(TEMP_KEY_CFG_FILE, deploy.getAbsolutePath());
		return config;
	}
	
	@Override
	public SLF4JAutoLogger getLogger() {
		return logger;
	}

	@Override
	public Set<IdentityProvider> getIdentityProviders() {
		return providers;
	}

	@Override
	public String getMongoHost() {
		return mongoHost;
	}

	@Override
	public String getMongoDatabase() {
		return mongoDB;
	}

	@Override
	public String getMongoUser() {
		return mongoUser;
	}

	@Override
	public char[] getMongoPwd() {
		return mongoPwd;
	}

}