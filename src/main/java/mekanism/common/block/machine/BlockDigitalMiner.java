package mekanism.common.block.machine;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Upgrade;
import mekanism.api.block.IBlockElectric;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasModel;
import mekanism.api.block.IHasSecurity;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsRedstone;
import mekanism.api.block.ISupportsUpgrades;
import mekanism.common.MekanismLang;
import mekanism.common.base.IActiveState;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.block.states.IStateWaterLogged;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.DigitalMinerContainer;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.VoxelShapeUtils;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BlockDigitalMiner extends BlockMekanism implements IBlockElectric, ISupportsUpgrades, IHasModel, IHasGui<TileEntityDigitalMiner>, IStateFacing, IStateActive,
      IHasInventory, IHasSecurity, ISupportsRedstone, IHasTileEntity<TileEntityDigitalMiner>, IStateWaterLogged, IHasDescription {

    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShape miner = VoxelShapeUtils.combine(
              makeCuboidShape(5, 9, -14, 6, 10, -13),
              makeCuboidShape(10, 9, -14, 11, 10, -13),
              makeCuboidShape(10, 9, -13, 11, 11, -9),
              makeCuboidShape(5, 9, -13, 6, 11, -9),
              makeCuboidShape(10, 20, -11, 12, 22, -9),
              makeCuboidShape(4, 20, -11, 6, 22, -9),
              makeCuboidShape(-8, 3, -9, 24, 32, 3),
              makeCuboidShape(-8, 3, 20, 24, 32, 32),
              makeCuboidShape(-8, 3, 4, 24, 8, 19),
              makeCuboidShape(24, 24, -8, 29, 29, -6),
              makeCuboidShape(24, 24, 0, 29, 29, 2),
              makeCuboidShape(24, 24, 21, 29, 29, 23),
              makeCuboidShape(24, 24, 29, 29, 29, 31),
              makeCuboidShape(-13, 24, -8, -8, 29, -6),
              makeCuboidShape(-13, 24, 0, -8, 29, 2),
              makeCuboidShape(-13, 24, 21, -8, 29, 23),
              makeCuboidShape(-13, 24, 29, -8, 29, 31),
              makeCuboidShape(24, 24, -6, 25, 29, 0),
              makeCuboidShape(24, 24, 23, 25, 29, 29),
              makeCuboidShape(-9, 24, -6, -8, 29, 0),
              makeCuboidShape(-9, 24, 23, -8, 29, 29),
              makeCuboidShape(26, 2, -7, 30, 30, 1),
              makeCuboidShape(26, 2, 22, 30, 30, 30),
              makeCuboidShape(-14, 2, -7, -10, 30, 1),
              makeCuboidShape(-14, 2, 22, -10, 30, 30),
              makeCuboidShape(24, 0, -8, 31, 2, 2),
              makeCuboidShape(24, 0, 21, 31, 2, 31),
              makeCuboidShape(-15, 0, 21, -8, 2, 31),
              makeCuboidShape(-15, 0, -8, -8, 2, 2),
              makeCuboidShape(-7, 4, 3, 23, 31, 20),
              makeCuboidShape(5, 2, -6, 11, 4, 5),
              makeCuboidShape(5, 1, 5, 11, 4, 11),
              makeCuboidShape(-15, 5, 5, -6, 11, 11),
              makeCuboidShape(22, 5, 5, 31, 11, 11),
              makeCuboidShape(4, 0, 4, 12, 1, 12),
              makeCuboidShape(-16, 4, 4, -15, 12, 12),
              makeCuboidShape(-9, 4, 4, -8, 12, 12),
              makeCuboidShape(31, 4, 4, 32, 12, 12),
              makeCuboidShape(24, 4, 4, 25, 12, 12),
              makeCuboidShape(-8, 27, 4, 24, 32, 19),
              makeCuboidShape(-8, 21, 4, 24, 26, 19),
              makeCuboidShape(-8, 15, 4, 24, 20, 19),
              makeCuboidShape(-8, 9, 4, 24, 14, 19),
              //Keyboard
              makeCuboidShape(3, 11, -10.5, 13, 12.5, -11.75),
              makeCuboidShape(3, 10, -11.75, 13, 11.5, -13),
              makeCuboidShape(3, 9.5, -13, 13, 11, -14.25),
              makeCuboidShape(3, 9, -14.25, 13, 10.5, -15.25),
              makeCuboidShape(4, 9.5, -12, 12, 10, -13),
              makeCuboidShape(4, 8.5, -13, 12, 9.5, -14.25),
              //Center monitor
              makeCuboidShape(2, 18, -10.5, 14, 24, -11.5),
              makeCuboidShape(1, 16, -11.5, 15, 26, -13.5),
              //Left monitor
              makeCuboidShape(17, 17.75, -10, 18.5, 24.25, -11.5),
              makeCuboidShape(18.5, 17.75, -10.5, 22, 24.25, -12),
              makeCuboidShape(22, 17.75, -11.5, 25.5, 24.25, -13),
              makeCuboidShape(25.5, 17.75, -12.5, 29, 24.25, -14),
              makeCuboidShape(15.5, 16, -11.5, 19.5, 26, -13.5),
              makeCuboidShape(18.5, 16, -12, 23, 26, -14),
              makeCuboidShape(22, 16, -13, 26.5, 26, -15),
              makeCuboidShape(25.5, 16, -14, 30, 26, -16),
              //Right Monitor
              makeCuboidShape(-3 + 2.5, 17.75, -10, -6.5 + 2.5, 24.25, -11.5),
              makeCuboidShape(-6.5 + 2.5, 17.75, -10.5, -10 + 2.5, 24.25, -12),
              makeCuboidShape(-10 + 2.5, 17.75, -11.5, -13.5 + 2.5, 24.25, -13),
              makeCuboidShape(-13.5 + 2.5, 17.75, -12.5, -15 + 2.5, 24.25, -14),
              makeCuboidShape(-6.5 + 2.5, 16, -11.5, -2 + 2.5, 26, -13.5),
              makeCuboidShape(-10 + 2.5, 16, -12, -5.5 + 2.5, 26, -14),
              makeCuboidShape(-13.5 + 2.5, 16, -13, -9 + 2.5, 26, -15),
              makeCuboidShape(-16.5 + 2.5, 16, -14, -12.5 + 2.5, 26, -16)
        );
        for (Direction side : EnumUtils.HORIZONTAL_DIRECTIONS) {
            bounds[side.ordinal() - 2] = VoxelShapeUtils.rotateHorizontal(miner, side);
        }
    }

    public BlockDigitalMiner() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 16F));
    }

    /**
     * @inheritDoc
     * @apiNote Only called on the client side
     */
    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile != null && MekanismUtils.isActive(world, pos) && ((IActiveState) tile).renderUpdate() && MekanismConfig.client.machineEffects.get()) {
            float xRandom = (float) pos.getX() + 0.5F;
            float yRandom = (float) pos.getY() + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float zRandom = (float) pos.getZ() + 0.5F;
            float iRandom = 0.52F;
            float jRandom = random.nextFloat() * 0.6F - 0.3F;
            Direction side = tile.getDirection();

            switch (side) {
                case WEST:
                    world.addParticle(ParticleTypes.SMOKE, xRandom - iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    world.addParticle(RedstoneParticleData.REDSTONE_DUST, xRandom - iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    break;
                case EAST:
                    world.addParticle(ParticleTypes.SMOKE, xRandom + iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    world.addParticle(RedstoneParticleData.REDSTONE_DUST, xRandom + iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    break;
                case NORTH:
                    world.addParticle(ParticleTypes.SMOKE, xRandom + jRandom, yRandom, zRandom - iRandom, 0.0D, 0.0D, 0.0D);
                    world.addParticle(RedstoneParticleData.REDSTONE_DUST, xRandom + jRandom, yRandom, zRandom - iRandom, 0.0D, 0.0D, 0.0D);
                    break;
                case SOUTH:
                    world.addParticle(ParticleTypes.SMOKE, xRandom + jRandom, yRandom, zRandom + iRandom, 0.0D, 0.0D, 0.0D);
                    world.addParticle(RedstoneParticleData.REDSTONE_DUST, xRandom + jRandom, yRandom, zRandom + iRandom, 0.0D, 0.0D, 0.0D);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        if (MekanismConfig.client.enableAmbientLighting.get()) {
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            if (tile instanceof IActiveState && ((IActiveState) tile).lightUpdate() && ((IActiveState) tile).wasActiveRecently()) {
                return MekanismConfig.client.ambientLightingLevel.get();
            }
        }
        return 0;
    }

    @Nonnull
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isRemote) {
            return ActionResultType.SUCCESS;
        }
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return ActionResultType.PASS;
        }
        if (tile.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return ActionResultType.SUCCESS;
        }
        return tile.openGui(player);
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(BlockState state, @Nonnull PlayerEntity player, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return SecurityUtils.canAccess(player, MekanismUtils.getTileEntity(world, pos)) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
    }

    @Override
    public float getExplosionResistance(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        //TODO: This is how it was before, but should it be divided by 5 like in Block.java
        return blockResistance;
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (tile != null) {
                tile.onNeighborChange(neighborBlock);
            }
        }
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return bounds[getDirection(state).ordinal() - 2];
    }

    @Override
    public double getUsage() {
        return MekanismConfig.usage.digitalMiner.get();
    }

    @Override
    public double getConfigStorage() {
        return MekanismConfig.storage.digitalMiner.get();
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityDigitalMiner tile) {
        return new ContainerProvider(TextComponentUtil.translate(getTranslationKey()), (i, inv, player) -> new DigitalMinerContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityDigitalMiner> getTileType() {
        return MekanismTileEntityTypes.DIGITAL_MINER.getTileEntityType();
    }

    @Nonnull
    @Override
    public Set<Upgrade> getSupportedUpgrade() {
        return EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.ANCHOR);
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_DIGITAL_MINER;
    }
}