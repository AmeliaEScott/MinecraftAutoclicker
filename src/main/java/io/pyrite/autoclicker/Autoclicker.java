package io.pyrite.autoclicker;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import com.mojang.brigadier.CommandDispatcher;
import io.github.cottonmc.clientcommands.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.command.CommandSource;
import net.minecraft.text.LiteralText;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class Autoclicker implements ClientCommandPlugin, ClientModInitializer, ClientTickEvents.EndWorldTick {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "io_pyrite_autoclicker";
    public static final String MOD_NAME = "Autoclicker";

    private int leftClickPeriod = -1;
    private int rightClickPeriod = -1;
    private long currentTick = 0;

    public Autoclicker(){
        super();
        LOGGER.log(Level.INFO, "################################### CONSTRUCTING AUTOCLICKER ###################################");
    }

    @Override
    public void registerCommands(CommandDispatcher<CottonClientCommandSource> dispatcher) {
        log(Level.DEBUG, "Registering commands.");

        LiteralCommandNode<CottonClientCommandSource> rootNode = ArgumentBuilders
                .literal("autoclicker")
                .build();

        LiteralCommandNode<CottonClientCommandSource> leftClickNode = ArgumentBuilders
                .literal("left")
                .executes(context -> {
                    leftClickPeriod = 0;
                    context.getSource().sendFeedback(new LiteralText("Left click hold"));
                    return 1;
                })
                .build();

        ArgumentCommandNode<CottonClientCommandSource, Integer> leftClick = ArgumentBuilders
                .argument("period", IntegerArgumentType.integer())
                .executes(context -> {
                    leftClickPeriod = IntegerArgumentType.getInteger(context, "period");
                    context.getSource().sendFeedback(new LiteralText("Left click every " + leftClickPeriod + " ticks."));
                    return 1;
                })
                .build();

        LiteralCommandNode<CottonClientCommandSource> rightClickNode = ArgumentBuilders
                .literal("right")
                .executes(context -> {
                    rightClickPeriod = 0;
                    context.getSource().sendFeedback(new LiteralText("Right click hold"));
                    return 1;
                })
                .build();

        ArgumentCommandNode<CottonClientCommandSource, Integer> rightClick = ArgumentBuilders
                .argument("period", IntegerArgumentType.integer())
                .executes(context -> {
                    rightClickPeriod = IntegerArgumentType.getInteger(context, "period");
                    context.getSource().sendFeedback(new LiteralText("Right click every " + rightClickPeriod + " ticks."));
                    return 1;
                })
                .build();

        LiteralCommandNode<CottonClientCommandSource> stopNode = ArgumentBuilders
                .literal("stop")
                .executes(context -> {
                    rightClickPeriod = -1;
                    leftClickPeriod = -1;
                    context.getSource().sendFeedback(new LiteralText("Stopping all autoclicking."));
                    return 1;
                })
                .build();

        dispatcher.getRoot().addChild(rootNode);
        rootNode.addChild(leftClickNode);
        leftClickNode.addChild(leftClick);
        rootNode.addChild(rightClickNode);
        rightClickNode.addChild(rightClick);
        rootNode.addChild(stopNode);

        ClientTickEvents.END_WORLD_TICK.register(this);
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_WORLD_TICK.register(this);

    }

    @Override
    public void onEndTick(ClientWorld world) {
        this.currentTick += 1;
        MinecraftClient client = MinecraftClient.getInstance();

        io.pyrite.autoclicker.Autoclicker.LOGGER.log(Level.INFO, "Current tick: " + currentTick + ", left: " + leftClickPeriod + ", right: " + rightClickPeriod);

        if(this.rightClickPeriod > 0) {
            if (this.currentTick % this.rightClickPeriod == 2) {
                client.options.keyUse.setPressed(false);
            } else if (this.currentTick % this.rightClickPeriod == 0) {
                client.options.keyUse.setPressed(true);
            }
        }else if(this.rightClickPeriod == 0){
            client.options.keyUse.setPressed(true);
        }

        if(this.leftClickPeriod > 0) {
            if (this.currentTick % this.leftClickPeriod == 2) {
                client.options.keyAttack.setPressed(false);
            } else if (this.currentTick % this.leftClickPeriod == 0) {
                client.options.keyAttack.setPressed(true);
            }
        }else if(this.leftClickPeriod == 0){
            client.options.keyAttack.setPressed(true);
        }
    }

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }


}
