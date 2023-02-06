package pt.ist.tecnicoapi.serializer;

import org.fenixedu.academic.domain.degreeStructure.BibliographicReferences.BibliographicReference;
import org.fenixedu.academic.domain.degreeStructure.BibliographicReferences.BibliographicReferenceType;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class BibliographicReferenceSerializer extends DomainObjectSerializer {

    protected BibliographicReferenceSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull BibliographicReference bibliographicReference) {
        return JsonUtils.toJson(data -> {
            addIfAndFormat(data, "authors", bibliographicReference, BibliographicReference::getAuthors);
            addIfAndFormat(data, "title", bibliographicReference, BibliographicReference::getTitle);
            addIfAndFormat(data, "publisherReference", bibliographicReference, BibliographicReference::getReference);
            addIfAndFormat(data, "type", bibliographicReference.getType(), BibliographicReferenceType::getName);
            addIfAndFormat(data, "url", bibliographicReference, BibliographicReference::getUrl);
            addIfAndFormat(data, "year", bibliographicReference, BibliographicReference::getYear);
        });
    }

}
