package org.fenixedu.api.domain;

import java.util.function.Supplier;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.Singleton;

public class FenixEduAPISystem extends FenixEduAPISystem_Base {

    private static final Supplier<FenixEduAPISystem> SYSTEM_GETTER = Bennu.getInstance()::getFenixEduAPISystem;
    private static final Supplier<FenixEduAPISystem> SYSTEM_CREATOR = FenixEduAPISystem::new;

    public FenixEduAPISystem() {
        setBennu(Bennu.getInstance());
    }

    public static FenixEduAPISystem getInstance() {
        return Singleton.getInstance(SYSTEM_GETTER, SYSTEM_CREATOR);
    }

}
