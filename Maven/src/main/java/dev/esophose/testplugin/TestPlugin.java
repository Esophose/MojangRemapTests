package dev.esophose.testplugin;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class TestPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Run each test in a separate scheduler task since they will be throwing uncatchable exceptions
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(this, () -> this.testTextComponent(player));
        Bukkit.getScheduler().runTask(this, () -> this.testRegistry(player));
        Bukkit.getScheduler().runTask(this, () -> this.testPacketSend(player));
        Bukkit.getScheduler().runTask(this, () -> this.testPacketSendWorking(player));
    }

    public void testTextComponent(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();

        // Split on different lines so it's easier to see what errored
        // Obviously you should use Spigot's chat component API for this, but this is just an example
        serverPlayer.sendMessage(
                new TextComponent("This message will not appear due to a mapping error")
                        .withStyle(style ->
                                style.withColor(
                                        ChatFormatting.RED)),
                ChatType.SYSTEM,
                Util.NIL_UUID
        );
    }

    private void testRegistry(Player player) {
        // For an example, get number of entries in the entity registry
        player.sendMessage("Registered entity types: " + Registry.ENTITY_TYPE.stream().count());
    }

    private void testPacketSend(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();

        // Obviously the Spigot particle API should be used for this, but this is just to create an example
        Packet<?> packet = new ClientboundLevelParticlesPacket(
                ParticleTypes.NAUTILUS,
                true,
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ(),
                1.0F,
                1.0F,
                1.0F,
                0.1F,
                30
        );

        // Likely fails due to having an overload method
        serverPlayer.connection.send(packet);
    }

    private void testPacketSendWorking(Player player) {
        // This test is to prove that most of the mappings actually do work
        // Obviously the Spigot particle API should be used for this, but this is just to create an example
        Packet<?> packet = new ClientboundLevelParticlesPacket(
                ParticleTypes.FLAME,
                true,
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ(),
                1.0F,
                1.0F,
                1.0F,
                0.01F,
                30
        );

        // Unlike the ServerGamePacketListenerImpl#send(Packet) method above, this one actually does map correctly
        ((CraftPlayer) player).getHandle().connection.send(packet, null);
    }

}
