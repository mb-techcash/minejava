/*
 * Created by MBtech on 6/20/2018 6:00 PM
 * Copyright (c) 2018.

 */
package com.heirteir.autoeye;

import com.heirteir.autoeye.api.CheckRegister;
import com.heirteir.autoeye.packets.ChannelInjector;
import com.heirteir.autoeye.permissions.PermissionsManager;
import com.heirteir.autoeye.player.AutoEyePlayerList;
import com.heirteir.autoeye.util.MathUtil;
import com.heirteir.autoeye.util.logger.Logger;
import com.heirteir.autoeye.util.server.Version;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter public final class Autoeye extends JavaPlugin {
    @Getter private static final Version version = Version.getVersion(Bukkit.getBukkitVersion());
    private final Logger pluginLogger;
    private final ChannelInjector channelInjector;
    private final AutoEyePlayerList autoEyePlayerList;
    private final MathUtil mathUtil;
    private final PermissionsManager permissionsManager;
    private final CheckRegister checkRegister;
    private boolean running = false;

    public Autoeye() {
        this.pluginLogger = new Logger(this);
        if (!version.equals(Version.NONE)) {
            this.autoEyePlayerList = new AutoEyePlayerList(this);
            this.channelInjector = new ChannelInjector();
            this.mathUtil = new MathUtil();
            this.permissionsManager = new PermissionsManager();
            this.checkRegister = new CheckRegister(this);
        } else {
            this.channelInjector = null;
            this.autoEyePlayerList = null;
            this.mathUtil = null;
            this.permissionsManager = null;
            this.checkRegister = null;
        }
    }

    @Override public void onEnable() {
        if (version.equals(Version.NONE)) {
            this.pluginLogger.sendConsoleMessageWithPrefix(this.pluginLogger.getPluginName() + "&c does not support the version of your Minecraft Server. " + this.pluginLogger.getPluginName() + " &conly supports &7[&e1.7-1.12&7]&c.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.running = true;
        this.channelInjector.inject(this);
        this.autoEyePlayerList.createListener();
        this.checkRegister.registerDefaultChecks();
    }

    @Override public void onDisable() {
        if (this.autoEyePlayerList != null) {
            this.autoEyePlayerList.getPlayers().clear();
            this.autoEyePlayerList.unregister();
            this.checkRegister.unregisterChecks();
        }
        this.running = false;
    }
}
