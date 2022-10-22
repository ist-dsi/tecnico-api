package org.fenixedu.bennu;

import org.fenixedu.api.serializer.FenixEduAPISerializer;
import org.fenixedu.bennu.spring.BennuSpringModule;
import org.fenixedu.commons.configuration.ConfigurationInvocationHandler;
import org.fenixedu.commons.configuration.ConfigurationManager;

@BennuSpringModule(basePackages = "org.fenixedu.api.ui", bundles = "FenixEduAPIResources")
public class FenixEduAPIConfiguration {
    public final static String BUNDLE = "resources.FenixEduAPIResources";
    private final static FenixEduAPISerializer serializer = new FenixEduAPISerializer();

    @ConfigurationManager(description = "FenixEdu API Configuration")
    public interface ConfigurationProperties {
    }

    public static ConfigurationProperties getConfiguration() {
        return ConfigurationInvocationHandler.getConfiguration(ConfigurationProperties.class);
    }

    public static FenixEduAPISerializer getSerializer() {
        return serializer;
    }

}
