package org.fenixedu.bennu;

import org.fenixedu.bennu.spring.BennuSpringModule;
import org.fenixedu.commons.configuration.ConfigurationInvocationHandler;
import org.fenixedu.commons.configuration.ConfigurationManager;

@BennuSpringModule(basePackages = "org.fenixedu.api.ui", bundles = "FenixEduAPIResources")
public class FenixEduAPIConfiguration {
    public final static String BUNDLE = "resources.FenixEduAPIResources";

    @ConfigurationManager(description = "FenixEdu API Configuration")
    public interface ConfigurationProperties {
    }

    public static ConfigurationProperties getConfiguration() {
        return ConfigurationInvocationHandler.getConfiguration(ConfigurationProperties.class);
    }

}
