package pt.ist.tecnicoapi.util;

import com.google.common.io.BaseEncoding;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.photograph.PictureMode;
import org.fenixedu.academic.ui.spring.controller.PhotographController;
import org.fenixedu.bennu.core.domain.Avatar;
import org.fenixedu.bennu.core.domain.User;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class PhotoUtils {

    public static @NotNull String toBase64Png(final @NotNull Person person, final boolean checkAccess) {
        final User user = person.getUser();
        final Avatar.PhotoProvider photoProvider = user == null ? null : Avatar.photoProvider.apply(user);
        final byte[] content = photoProvider == null || (checkAccess && !person.isPhotoAvailableToCurrentUser())
                ? mysteryMan()
                : photoProvider.getCustomAvatar(100, 100, PictureMode.FIT.name());
        return BaseEncoding.base64().encode(content);
    }

    private static byte @NotNull [] mysteryMan() {
        try (final InputStream mm = PhotographController.class.getClassLoader()
                .getResourceAsStream("META-INF/resources/img/mysteryman.png")) {
            return Avatar.process(mm, "image/png", 100);
        } catch (final IOException ex) {
            throw new Error(ex);
        }
    }

}
