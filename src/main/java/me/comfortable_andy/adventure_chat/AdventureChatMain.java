package me.comfortable_andy.adventure_chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Method;

public final class AdventureChatMain extends JavaPlugin implements Listener {

    private Method DEFAULT_CONVERT;
    private Object DEFAULT_GSON;
    private Class<?> DEFAULT_COMPONENT;
    private Method DEFAULT_TEXT_CONTENT;

    @Override
    public void onEnable() {
        saveResource("dont_touch_me.yml", true);
        getServer().getPluginManager().registerEvents(this, this);

        final FileConfiguration file = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "dont_touch_me.yml"));
        try {
            final Class<?> clazz = Class.forName(String.valueOf(file.getString("serializerClass")));
            DEFAULT_GSON = clazz.getMethod("gson").invoke(null);
            DEFAULT_CONVERT = clazz.getMethod("deserialize", Object.class);
            DEFAULT_COMPONENT = Class.forName(file.getString("componentClass"));
            DEFAULT_TEXT_CONTENT = Class.forName(file.getString("textComponentClass")).getMethod("content");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        getLogger().info("Loaded " + DEFAULT_GSON + ", " + DEFAULT_CONVERT + ", " + DEFAULT_COMPONENT + ", " + DEFAULT_TEXT_CONTENT);
    }

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @EventHandler
    public void onChat(AsyncChatEvent event) throws ReflectiveOperationException {
        AsyncChatEvent.class.getMethod("message", DEFAULT_COMPONENT)
                .invoke(
                        event,
                        DEFAULT_CONVERT
                                .invoke(
                                        DEFAULT_GSON,
                                        GsonComponentSerializer
                                                .gson()
                                                .serialize(
                                                        this.miniMessage.deserialize(
                                                                (String) DEFAULT_TEXT_CONTENT.invoke(AsyncChatEvent.class.getMethod("message").invoke(event))
                                                        )
                                                )
                                )
                );
    }

}
