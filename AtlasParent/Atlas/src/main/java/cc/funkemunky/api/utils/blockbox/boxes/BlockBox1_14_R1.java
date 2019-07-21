package cc.funkemunky.api.utils.blockbox.boxes;

import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.ReflectionsUtil;
import cc.funkemunky.api.utils.blockbox.BlockBox;
import lombok.val;
import net.minecraft.server.v1_14_R1.AxisAlignedBB;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockBox1_14_R1 implements BlockBox {
    @Override
    public List<BoundingBox> getCollidingBoxes(World world, BoundingBox box) {
        List<AxisAlignedBB> aabbs = new ArrayList<>();
        List<BoundingBox> boxes = new ArrayList<>();

        double minX = box.minX;
        double maxX = box.maxX;
        double minY = box.minY;
        double maxY = box.maxY;
        double minZ = box.minZ;
        double maxZ = box.maxZ;


        for (double x = minX; x < maxX; x++) {
            for (double z = minZ; z < maxZ; z++) {
                for (double y = minY; y < maxY; y++) {
                    org.bukkit.block.Block block = BlockUtils.getBlock(new Location(world, x, y, z));
                    if (!block.getType().equals(Material.AIR)) {
                        if (BlockUtils.collisionBoundingBoxes.containsKey(block.getType())) {
                            aabbs.add((AxisAlignedBB) BlockUtils.collisionBoundingBoxes.get(block.getType()).add(block.getLocation().toVector()).toAxisAlignedBB());
                        } else {
                            net.minecraft.server.v1_14_R1.BlockPosition pos = new net.minecraft.server.v1_14_R1.BlockPosition(x, y, z);
                            net.minecraft.server.v1_14_R1.World nmsWorld = ((org.bukkit.craftbukkit.v1_14_R1.CraftWorld) world).getHandle();
                            net.minecraft.server.v1_14_R1.IBlockData nmsiBlockData = ((org.bukkit.craftbukkit.v1_14_R1.CraftWorld) world).getHandle().getType(pos);
                            net.minecraft.server.v1_14_R1.Block nmsBlock = nmsiBlockData.getBlock();

                            net.minecraft.server.v1_14_R1.VoxelShape shape = nmsiBlockData.getCollisionShape(nmsWorld, pos);
                            if (shape.toString().equals("EMPTY")) {
                                aabbs.add(new net.minecraft.server.v1_14_R1.AxisAlignedBB(block.getLocation().getX(), block.getLocation().getY(), block.getLocation().getZ(), block.getLocation().getX() + 1, block.getLocation().getY() + 1, block.getLocation().getZ() + 1));
                            } else {
                                aabbs.addAll(shape.d());
                            }

                            if (nmsBlock instanceof net.minecraft.server.v1_14_R1.BlockShulkerBox) {
                                net.minecraft.server.v1_14_R1.TileEntity tileentity = nmsWorld.getTileEntity(pos);
                                net.minecraft.server.v1_14_R1.BlockShulkerBox shulker = (net.minecraft.server.v1_14_R1.BlockShulkerBox) nmsBlock;

                                if (tileentity instanceof net.minecraft.server.v1_14_R1.TileEntityShulkerBox) {
                                    net.minecraft.server.v1_14_R1.TileEntityShulkerBox entity = (net.minecraft.server.v1_14_R1.TileEntityShulkerBox) tileentity;
                                    //Bukkit.broadcastMessage("entity");
                                    aabbs.add(entity.a(nmsiBlockData));

                                    val loc = block.getLocation();
                                    if (entity.s().toString().contains("OPEN") || entity.s().toString().contains("CLOSING")) {
                                        aabbs.add(new net.minecraft.server.v1_14_R1.AxisAlignedBB(loc.getX(), loc.getY(), loc.getZ(), loc.getX() + 1, loc.getY() + 1.5, loc.getZ() + 1));
                                    }
                                }
                            }
                        }
                        /*
                        else {
                            BoundingBox blockBox = new BoundingBox((float) nmsBlock.B(), (float) nmsBlock.D(), (float) nmsBlock.F(), (float) nmsBlock.C(), (float) nmsBlock.E(), (float) nmsBlock.G());
                        }*/

                    }
                }
            }
        }

        for (AxisAlignedBB aabb : aabbs) {
            if(aabb == null) continue;

            boxes.add(new BoundingBox((float)aabb.minX, (float)aabb.minY, (float)aabb.minZ, (float)aabb.maxX, (float)aabb.maxY, (float)aabb.maxZ));
        }
        return boxes;
    }

    @Override
    public List<BoundingBox> getSpecificBox(Location loc) {
        return getCollidingBoxes(loc.getWorld(), new BoundingBox(loc.clone().toVector(), loc.clone().toVector()));
    }

    @Override
    public boolean isChunkLoaded(Location loc) {
        net.minecraft.server.v1_14_R1.World world = ((org.bukkit.craftbukkit.v1_14_R1.CraftWorld) loc.getWorld()).getHandle();

        return !world.isClientSide && world.isLoaded(new net.minecraft.server.v1_14_R1.BlockPosition(loc.getBlockX(), 0, loc.getBlockZ())) && world.getChunkAtWorldCoords(new net.minecraft.server.v1_14_R1.BlockPosition(loc.getBlockX(), 0, loc.getBlockZ())).r();
    }

    @Override
    public boolean isRiptiding(LivingEntity entity) {
        return ((CraftLivingEntity) entity).getHandle().isRiptiding();
    }

    @Override
    public boolean isUsingItem(Player player) {
        net.minecraft.server.v1_14_R1.EntityLiving entity = ((org.bukkit.craftbukkit.v1_14_R1.entity.CraftLivingEntity) player).getHandle();
        return entity.dl() != null && entity.dl().l() != net.minecraft.server.v1_14_R1.EnumAnimation.NONE;
    }

    @Override
    public float getMovementFactor(Player player) {
        return (float) ((CraftPlayer) player).getHandle().getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue();
    }

    @Override
    public int getTrackerId(Player player) {
        return player.getEntityId();
    }

    @Override
    public float getAiSpeed(Player player) {
        return ((CraftPlayer) player).getHandle().da();
    }
}
