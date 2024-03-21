package com.github.xcykrix.dynamiclights.module;


import com.github.xcykrix.dynamiclights.module.events.PlayerHandlerEvent;
import com.github.xcykrix.dynamiclights.util.LightManager;
import com.github.xcykrix.plugincommon.PluginCommon;
import com.github.xcykrix.plugincommon.api.helper.configuration.LanguageFile;
import com.github.xcykrix.plugincommon.extendables.implement.Initialize;
import com.shaded._100.aikar.commands.BaseCommand;
import com.shaded._100.aikar.commands.CommandHelp;
import com.shaded._100.aikar.commands.annotation.*;
import de.tr7zw.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("dynamiclights|dynamiclight|dl")
public class DynamicLightCommand extends BaseCommand implements Initialize {
    private final PluginCommon pluginCommon;
    private final LightManager lightManager;
    private final LanguageFile languageFile;

    public DynamicLightCommand(PluginCommon pluginCommon, LightManager lightManager) {
        this.pluginCommon = pluginCommon;
        this.lightManager = lightManager;
        this.languageFile = this.pluginCommon.configurationAPI.getLanguageFile();
    }

    @Override
    public void initialize() {
        pluginCommon.getServer().getPluginManager().registerEvents(new PlayerHandlerEvent(pluginCommon, this.lightManager), pluginCommon);
    }

    @Subcommand("lock")
    @CommandPermission("dynamiclights.lock")
    @Description("Toggle placing light sources from your Off Hand.")
    public void lock(Player player) {
        boolean status = this.lightManager.lightLockStatus.getOrDefault(player.getUniqueId().toString(), true);
        if (!status) {
            pluginCommon.adventureAPI.getAudiences().player(player).sendMessage(
                this.languageFile.getComponentFromID("enable-lock", true)
            );
            this.lightManager.lightLockStatus.put(player.getUniqueId().toString(), true);
        } else {
            pluginCommon.adventureAPI.getAudiences().player(player).sendMessage(
                this.languageFile.getComponentFromID("disable-lock", true)
            );
            this.lightManager.lightLockStatus.put(player.getUniqueId().toString(), false);
        }
    }

    @Subcommand("light")
    @CommandPermission("dynamiclights.lock")
    public void setItemLight(Player player , @Flags("光照等级") Integer lightLevel, @Flags("可照明分钟") Double lightTime) {
        if (lightLevel > 15){
            player.sendMessage("光照等级最大为15");
        }
        if (!(player.getInventory().getItemInMainHand().getType().equals(Material.AIR)))
        {
            ItemStack item = player.getInventory().getItemInMainHand();
            NBT.modify(item, nbt -> {
                nbt.setInteger("lightLevel", lightLevel);
                nbt.setDouble("lightTime", lightTime);
            });
            player.sendMessage(String.format("设置成功：物品光照等级%s ,可照明%s秒 !", lightLevel, lightTime));
        }
        else {
            player.sendMessage("您必须手持物品以设置光照等级和耐久度!");
        }
    }

    @HelpCommand
    public void help(CommandSender sender, CommandHelp helpCommand) {
        helpCommand.showHelp();
    }
}
