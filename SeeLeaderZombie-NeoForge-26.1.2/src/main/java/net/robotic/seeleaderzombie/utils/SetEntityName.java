package net.robotic.seeleaderzombie.utils;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import java.lang.reflect.Method;
import org.slf4j.Logger;

public class SetEntityName {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void setEntityName(LivingEntity entity, String entityName) {
        if (entityName == null) {
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
            return;
        }
        Component name = createLiteralComponent(entityName);
        if (name != null) {
            entity.setCustomName(name);
        }
        entity.setCustomNameVisible(true);
    }

    private static Component createLiteralComponent(String text) {
        try {
            Method literal = Component.class.getMethod("literal", String.class);
            Object result = literal.invoke(null, text);
            if (result instanceof Component component) {
                return component;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        try {
            Class<?> serializerClass = Class.forName("net.minecraft.network.chat.Component$Serializer");
            Method fromJson = serializerClass.getMethod("fromJson", String.class);
            Object result = fromJson.invoke(null, "{\"text\":" + quoteJson(text) + "}");
            if (result instanceof Component component) {
                return component;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        LOGGER.error("Could not name entity Leader Zombie. Please create a GitHub issue.");
        return null;
    }

    private static String quoteJson(String s) {
        StringBuilder out = new StringBuilder(s.length() + 2);
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
        return out.toString();
    }
}
