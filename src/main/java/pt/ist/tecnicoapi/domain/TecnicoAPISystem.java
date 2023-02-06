package pt.ist.tecnicoapi.domain;

import java.util.function.Supplier;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.Singleton;

public class TecnicoAPISystem extends TecnicoAPISystem_Base {

    private static final Supplier<TecnicoAPISystem> SYSTEM_GETTER = () -> Bennu.getInstance().getTecnicoAPISystem();
    private static final Supplier<TecnicoAPISystem> SYSTEM_CREATOR = TecnicoAPISystem::new;

    public TecnicoAPISystem() {
        setBennu(Bennu.getInstance());
    }

    public static TecnicoAPISystem getInstance() {
        return Singleton.getInstance(SYSTEM_GETTER, SYSTEM_CREATOR);
    }

}
