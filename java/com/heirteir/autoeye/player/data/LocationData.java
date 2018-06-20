/*
 * Created by Justin Heflin on 4/19/18 8:21 PM
 * Copyright (c) 2018.
 *
 * Can be redistributed non commercially as long as credit is given to original copyright owner.
 *
 * last modified: 4/19/18 7:22 PM
 */
package com.heirteir.autoeye.player.data;

import com.heirteir.autoeye.Autoeye;
import com.heirteir.autoeye.packets.wrappers.PacketPlayInFlying;
import com.heirteir.autoeye.player.AutoEyePlayer;
import com.heirteir.autoeye.util.BlockSet;
import com.heirteir.autoeye.util.reflections.wrappers.WrappedAxisAlignedBB;
import com.heirteir.autoeye.util.vector.Vector2D;
import com.heirteir.autoeye.util.vector.Vector3D;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.NumberConversions;

@Getter public class LocationData {
    private boolean teleported;
    private boolean inWater;
    private Vector3D teleportLocation;
    private WrappedAxisAlignedBB axisAlignedBB;
    private BlockSet solidBlocks;
    private BlockSet groundBlocks;
    private boolean onStairs;
    private boolean onSlime;
    private boolean inWeb;
    private boolean onLadder;
    private boolean onPiston;
    private Vector3D location;
    private Vector3D previousLocation;
    private Vector2D direction;
    private Vector2D previousDirection;
    private boolean changedPos;
    private boolean changedLook;
    private boolean clientOnGround;
    private boolean previousClientOnGround;
    private boolean serverOnGround;
    private boolean previousServerOnGround;
    private boolean hasSolidAbove;
    private boolean onIce;
    private boolean currentlyOnSlime;
    private Vector3D groundLocation;

    public LocationData(Autoeye autoeye, AutoEyePlayer player) {
        this.reset(autoeye, player);
    }

    public void setTeleported(AutoEyePlayer player, boolean teleported) {
        this.teleported = teleported;
        player.getTimeData().getLastTeleport().update();
    }

    public void reset(Autoeye autoeye, AutoEyePlayer player) {
        this.location = this.previousLocation = new Vector3D((float) player.getPlayer().getLocation().getX(), (float) player.getPlayer().getLocation().getY(), (float) player.getPlayer().getLocation().getZ());
        this.direction = this.previousDirection = new Vector2D(player.getPlayer().getLocation().getYaw(), player.getPlayer().getLocation().getPitch());
        this.axisAlignedBB = new WrappedAxisAlignedBB(autoeye, player.getPlayer().getWorld(), this.location.getX(), this.location.getY(), this.location.getZ(), player.getWrappedEntity().getWidth(), player.getWrappedEntity().getLength());
        teleportLocation = new Vector3D(this.location.getX(), (float) (this.location.getY() % 1 >= .5 ? Math.ceil(this.location.getY()) : Math.floor(this.location.getY())), this.getLocation().getZ());
        this.solidBlocks = new BlockSet(this.axisAlignedBB.offset(0, -0.0001F, 0, 0, 0, 0).getSolidBlocks());
        this.groundLocation = this.location;
    }

    public void setLocation(Vector3D location) {
        this.previousLocation = this.location;
        this.location = location;
    }

    public void update(Autoeye autoeye, AutoEyePlayer player, PacketPlayInFlying flying) {
        this.previousClientOnGround = this.clientOnGround;
        this.clientOnGround = flying.isOnGround();
        if (this.changedPos = flying.isHasPos()) {
            this.axisAlignedBB = new WrappedAxisAlignedBB(autoeye, player.getPlayer().getWorld(), this.location.getX(), this.location.getY(), this.location.getZ(), player.getWrappedEntity().getWidth(), player.getWrappedEntity().getLength());
            WrappedAxisAlignedBB offset = this.axisAlignedBB.offset(0F, -0.001F, 0F, 0F, 0, 0F);
            this.solidBlocks = new BlockSet(offset.getSolidBlocks());
            this.previousServerOnGround = this.serverOnGround;
            this.serverOnGround = this.solidBlocks.getBlocks().size() > 0;
            if (this.solidBlocks.getBlocks().size() != 0) {
                this.groundLocation = new Vector3D(flying.getX(), flying.getY(), flying.getZ());
                this.groundBlocks = this.solidBlocks;
                this.onSlime = this.groundBlocks.containsString("SLIME");
                this.onIce = this.groundBlocks.containsString("ICE");
            }
            this.currentlyOnSlime = this.onSlime && new BlockSet(this.axisAlignedBB.offset(0F, -1F, 0F, 0F, -player.getWrappedEntity().getLength(), 0F).getSolidBlocks()).containsString("SLIME");
            if (this.onStairs = this.solidBlocks.containsString("STEP", "STAIR")) {
                player.getTimeData().getLastOnStairs().update();
            } else {
                this.onStairs = player.getTimeData().getLastOnStairs().getAmount() > 0;
            }
            if (this.inWeb = offset.containsMaterial("WEB")) {
                player.getTimeData().getLastInWeb().update();
            } else {
                this.inWeb = player.getTimeData().getLastInWeb().getAmount() > 0;
            }
            if (this.onPiston = offset.containsMaterial("PISTON")) {
                player.getTimeData().getLastOnPiston().update();
            } else {
                this.onPiston = player.getTimeData().getLastOnPiston().getAmount() > 0;
            }
            if (flying.getX() != 0 && flying.getZ() != 0) {
                Block block = player.getPlayer().getWorld().getBlockAt(NumberConversions.floor(flying.getX()), NumberConversions.floor(this.axisAlignedBB.getMin().getY()), NumberConversions.floor(flying.getZ()));
                if (this.onLadder = (block.getType().equals(Material.LADDER) || block.getType().equals(Material.VINE)) && !player.getPlayer().getGameMode().name().equals("SPECTATOR")) {
                    player.getTimeData().getLastOnLadder().update();
                } else {
                    this.onLadder = player.getTimeData().getLastOnLadder().getAmount() > 0;
                }
            }
            if (this.hasSolidAbove = this.axisAlignedBB.offset(0, player.getWrappedEntity().getLength(), 0, 0, 0.2F, 0).getSolidBlocks().size() > 0) {
                player.getTimeData().getLastSolidAbove().update();
            } else {
                this.hasSolidAbove = player.getTimeData().getLastSolidAbove().getAmount() > 0;
            }
            if (this.inWater = this.axisAlignedBB.containsLiquid()) {
                player.getTimeData().getLastInWater().update();
            } else {
                this.inWater = player.getTimeData().getLastInWater().getAmount() > 0;
            }
            this.teleported = player.getTimeData().getLastTeleport().getAmount() > 0;
            if (player.getTimeData().getSecondTick().getDifference() >= 950 && !this.teleported) {
                this.teleportLocation = this.groundLocation;
            }
        }
        if (this.changedLook = flying.isHasLook()) {
            this.previousDirection = this.direction;
            this.direction = new Vector2D(flying.getYaw(), flying.getPitch());
        }
    }
}
