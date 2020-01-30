package cc.funkemunky.api.tinyprotocol.packet.out;

import cc.funkemunky.api.reflections.Reflections;
import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.reflections.types.WrappedField;
import cc.funkemunky.api.tinyprotocol.api.NMSObject;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.types.v1_13.WrappedSuggestions;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WrappedOutTabComplete extends NMSObject {

    private static String packet = Server.TAB_COMPLETE;
    private static WrappedClass tabClass = Reflections.getNMSClass(packet);
    private static WrappedClass suggestionsClass = WrappedSuggestions.suggestionsClass;

    public List<String> suggestions = new ArrayList<>();

    //1.13 only
    public int id = -1;

    private static WrappedField suggestionsAccessor;

    //1.13 and above
    private static WrappedField idAccessor;

    public WrappedOutTabComplete(Object object) {
        super(object);
    }

    public WrappedOutTabComplete(Object object, Player player) {
        super(object, player);
    }

    //For everything below 1.13.
    public WrappedOutTabComplete(String... result) {
        if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_13)) {
            setPacket(packet, (Object) result);
        }
    }

    //For 1.13 and above
    public WrappedOutTabComplete(int id, String input, int startChar, String... result) {
        if(ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_13)) {
            WrappedSuggestions.SuggestionsBuilder builder = new WrappedSuggestions.SuggestionsBuilder(input, startChar);

            for (String s : result) {
                builder = builder.suggest(s);
            }

            setPacket(packet,
                    id,
                    builder.build()
                            .getObject());
        }
    }

    @Override
    public void process(Player player, ProtocolVersion version) {
        if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_13)) {
            //Getting suggestions.
            Collections.addAll(suggestions, suggestionsAccessor.get(getObject()));
        } else {
            //Getting suggestions
            WrappedSuggestions suggestionsObject = new WrappedSuggestions(suggestionsAccessor.get(getObject()));

            suggestionsObject.suggestions.stream()
                    .map(suggestion -> suggestion.text)
                    .forEachOrdered(suggestions::add);

            id = idAccessor.get(getObject());
        }
    }

    static {
        if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_13)) {
            suggestionsAccessor = tabClass.getFieldByType(String[].class, 0);
        } else {
            suggestionsAccessor = tabClass.getFieldByType(suggestionsClass.getParent(), 0);
            idAccessor = tabClass.getFieldByType(int.class, 0);
        }
    }
}
