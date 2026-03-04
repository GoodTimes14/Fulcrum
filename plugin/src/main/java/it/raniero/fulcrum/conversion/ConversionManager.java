package it.raniero.fulcrum.conversion;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.api.command.context.ICommandContext;
import it.raniero.fulcrum.api.command.context.source.FulcrumSource;
import it.raniero.fulcrum.api.conversion.IConversionManager;
import it.raniero.fulcrum.api.conversion.IConverter;
import it.raniero.fulcrum.conversion.impl.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConversionManager implements IConversionManager {

    private final Fulcrum fulcrum;

    private final Map<String, IConverter<?>> converters = new HashMap<>();

    @Override
    public void init() {
        registerConverter(Double.class, new DoubleConverter());
        registerConverter(Integer.class, new IntegerConverter());
        registerConverter(
                FulcrumSource.class, new PlayerConverter(fulcrum.getPlugin().getFulcrumServer()));
        registerConverter(String.class, new StringConverter());
        registerConverter(UUID.class, new UUIDConverter());
        registerConverter(Long.class, new LongConverter());
    }

    @Override
    public <T> void registerConverter(Class<T> type, IConverter<T> converter) {
        converters.put(type.getSimpleName(), converter);
    }

    @Override
    public boolean convertAndAddArgument(Class<?> type, String parameter, ICommandContext context) {

        if (converters.containsKey(type.getSimpleName())) {

            IConverter<?> converter = converters.get(type.getSimpleName());
            if (!converter.canConvert(parameter)) {
                return false;
            }

            Object converted = converter.convert(parameter);
            context.addArgument(converted);

            return true;

        } else if (type == fulcrum.getPlugin().getFulcrumServer().getPlayerClass()) {
            IConverter<?> converter = converters.get(FulcrumSource.class.getSimpleName());

            FulcrumSource source = (FulcrumSource) converter.convert(parameter);
            if (source == null) {
                return false;
            }

            context.addArgument(source.getSourceObject());
            return true;
        }

        return false;
    }

    @Override
    public Object convertArgument(Class<?> type, String parameter) {

        if (converters.containsKey(type.getSimpleName())) {

            IConverter<?> converter = converters.get(type.getSimpleName());
            if (!converter.canConvert(parameter)) {
                return false;
            }

            return converter.convert(parameter);

        } else if (type == fulcrum.getPlugin().getFulcrumServer().getPlayerClass()) {
            IConverter<?> converter = converters.get(FulcrumSource.class.getSimpleName());

            FulcrumSource source = (FulcrumSource) converter.convert(parameter);
            if (source == null) {
                return null;
            }

            return null;
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> IConverter<T> getConverterForArgumentType(Class<T> type) {

        if (converters.containsKey(type.getSimpleName())) {
            return (IConverter<T>) converters.get(type.getSimpleName());
        }

        return null;
    }
}
