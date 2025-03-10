package br.com.erudio.mapper;

import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;

import java.util.ArrayList;
import java.util.List;

public class ObjectMapper {

    // Criando uma instância do Mapper do Dozer para mapear objetos entre diferentes classes
    private static Mapper mapper = DozerBeanMapperBuilder.buildDefault();

    // Converte um objeto de um tipo para outro.
    public static <O, D> D parseObject(O origin, Class<D> destination) {
        return mapper.map(origin, destination);
    }

    // Converte uma lista de objetos de um tipo para outro.
    public static <O, D> List<D> parseListObjects(List<O> origin, Class<D> destination) {

        // Criando uma nova lista para armazenar os objetos convertidos
        List<D> destinationObjects = new ArrayList<>();

        // Iterando sobre a lista de origem e convertendo cada objeto
        for (O o : origin) {
            destinationObjects.add(mapper.map(o, destination));
        }

        return destinationObjects;
    }

}
