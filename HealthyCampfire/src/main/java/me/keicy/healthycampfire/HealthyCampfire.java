package me.keicy.healthycampfire;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HealthyCampfire extends JavaPlugin implements Listener {

    private final Map<UUID, Integer> playerTasks = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        startHealingTask(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        boolean isNearCampfire = isNearCampfire(to, 3);

        // Vérifier si le joueur est sorti du rayon du feu de camp
        if (!isNearCampfire) {
            // Si le joueur n'est plus dans la zone, annuler la tâche de régénération
            stopHealingTask(player);
        } else if (!playerTasks.containsKey(player.getUniqueId())) {
            // Si le joueur vient d'entrer dans la zone, démarrer la tâche de régénération
            startHealingTask(player);
        }
    }

    // Méthode pour démarrer la tâche de régénération pour un joueur
    private void startHealingTask(Player player) {
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            double newHealth = Math.min(player.getHealth() + 2.0, maxHealth); // 1 coeur = 2 points de vie

            if (player.getHealth() < maxHealth) {
                player.setHealth(newHealth);
                player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
            }
        }, 40L, 40L); // Tâche se répète toutes les 40 ticks (2 secondes)

        playerTasks.put(player.getUniqueId(), taskId);
        player.sendMessage("Vous vous sentez réchauffé par le feu de camp !");
    }

    // Méthode pour arrêter la tâche de régénération pour un joueur
    private void stopHealingTask(Player player) {
        Integer taskId = playerTasks.remove(player.getUniqueId());
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
            player.sendMessage("Vous n'êtes plus réchauffé par le feu de camp !");
        }
    }

    // Méthode pour vérifier si un joueur est autour d'un feu de camp dans un rayon donné
    private boolean isNearCampfire(Location location, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = location.getWorld().getBlockAt(location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ() + z);
                    if (block.getType() == Material.CAMPFIRE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}