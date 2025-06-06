package it.raniero.fulcrum.conversion;

import it.raniero.fulcrum.Fulcrum;
import it.raniero.fulcrum.command.context.ICommandContext;
import it.raniero.fulcrum.command.context.source.FulcrumSource;
import it.raniero.fulcrum.conversion.impl.*;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class ConversionManager implements IConversionManager {

    private final Fulcrum fulcrum;

    private final Map<Class<?>,IConverter<?>> converters = new HashMap<>();


    @Override
    public void init() {
        registerConverter(Double.class,new DoubleConverter());
        registerConverter(Integer.class,new IntegerConverter());
        registerConverter(FulcrumSource.class,new PlayerConverter(fulcrum.getPlugin().getServer()));
        registerConverter(String.class,new StringConverter());
        registerConverter(UUID.class,new UUIDConverter());
        registerConverter(Long.class,new LongConverter());
    }

    @Override
    public <T> void registerConverter(Class<T> type, IConverter<T> converter) {
        converters.put(type,converter);
    }

    @Override
    public boolean convertAndAddArgument(Class<?> type, String parameter, ICommandContext context) {

        if (converters.containsKey(type)) {

            IConverter<?> converter = converters.get(type);
            if(!converter.canConvert(parameter)) {
                return false;
            }

            Object converted = converter.convert(parameter);
            context.addArgument(converted);

        } else if (type == fulcrum.getPlugin().getServer().getPlayerClass()) {
            IConverter<?> converter = converters.get(FulcrumSource.class);

            FulcrumSource source = (FulcrumSource) converter.convert(parameter);
            if (source == null) {
                return false;
            }

            context.addArgument(null);
        }

        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> IConverter<T> getConverterForArgumentType(Class<T> type) {

        if (converters.containsKey(type)) {
            return (IConverter<T>) converters.get(type);
        }

        return null;
    }
}
