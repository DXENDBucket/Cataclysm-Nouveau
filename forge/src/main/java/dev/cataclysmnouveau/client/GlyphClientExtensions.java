package dev.cataclysmnouveau.client;
import com.github.L_Ender.cataclysm.init.ModItems;
import dev.cataclysmnouveau.GlyphCatalog;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.*;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import java.util.*;
import java.util.function.Supplier;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="cataclysm_nouveau",value=net.minecraftforge.api.distmarker.Dist.CLIENT,bus=net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD)
public final class GlyphClientExtensions {
 @net.minecraftforge.eventbus.api.SubscribeEvent
 public static void setup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event){
  event.enqueueWork(()->dev.arsconflux.api.glyph.GlyphVisuals.register("cataclysm_nouveau",part->{
   var stack=nativeStack(part.getGlyph().getDefaultInstance());
   return stack.isEmpty()?null:dev.arsconflux.api.glyph.GlyphIcon.item(stack);
  }));
 }

 private static final Map<Item,Supplier<Item>> SOURCES=new IdentityHashMap<>();
 private static boolean initialized;
 private static void put(Supplier<Item> source,Item...glyphs){for(var g:glyphs)SOURCES.put(g,source);}
 public static ItemStack nativeStack(ItemStack glyph){
  if(!initialized){initialized=true;init();}
  var source=SOURCES.get(glyph.getItem());return source==null?ItemStack.EMPTY:source.get().getDefaultInstance();
 }
 private static void init(){
        put(() -> ModItems.VOID_ASSULT_SHOULDER_WEAPON.get(),GlyphCatalog.VOID_HOWITZER.getGlyph());
        put(() -> ModItems.WRATH_OF_THE_DESERT.get(),GlyphCatalog.CURSED_SANDSTORM.getGlyph());
        put(() -> ModItems.TIDAL_CLAWS.get(),GlyphCatalog.ABYSS_BLAST.getGlyph(),GlyphCatalog.TIDAL_TENTACLE.getGlyph(),GlyphCatalog.ABYSS_TRIPLE_BLAST.getGlyph());
        put(() -> com.github.L_Ender.cataclysm.init.ModBlocks.ABYSSAL_EGG.get().asItem(),GlyphCatalog.ABYSS_BOMBARDMENT.getGlyph(),GlyphCatalog.ABYSS_BOMBARDMENT_ARRAY.getGlyph());
        put(() -> ModItems.CURSED_BOW.get(),GlyphCatalog.PHANTOM_ARROW.getGlyph(),GlyphCatalog.PHANTOM_AERIAL_VOLLEY.getGlyph());
        put(() -> ModItems.SOUL_RENDER.get(),GlyphCatalog.PHANTOM_HALBERD.getGlyph(),GlyphCatalog.PHANTOM_SPIRAL.getGlyph(),GlyphCatalog.PHANTOM_RINGS.getGlyph(),GlyphCatalog.HALBERD_CLEAVE.getGlyph(),GlyphCatalog.MALEDICTUS_AERIAL_SMASH.getGlyph(),GlyphCatalog.MALEDICTUS_RUSH.getGlyph(),GlyphCatalog.MALEDICTUS_SHOCKWAVE.getGlyph(),GlyphCatalog.MALEDICTUS_UPPERCUT.getGlyph(),GlyphCatalog.MALEDICTUS_MELEE_COMBO.getGlyph(),GlyphCatalog.MALEDICTUS_SPIN_SLASHES.getGlyph());
        put(() -> ModItems.CERAUNUS.get(),GlyphCatalog.ANCHOR.getGlyph(),GlyphCatalog.WAVE.getGlyph(),GlyphCatalog.SPIN.getGlyph(),GlyphCatalog.LEAP.getGlyph());
        put(() -> ModItems.LASER_GATLING.get(),GlyphCatalog.LASER_BOLT.getGlyph(),GlyphCatalog.DEATH_LASER.getGlyph());
        put(() -> ModItems.WITHER_ASSULT_SHOULDER_WEAPON.get(),GlyphCatalog.MISSILE.getGlyph(),GlyphCatalog.HOMING.getGlyph(),GlyphCatalog.HOWITZER.getGlyph(),GlyphCatalog.HOWITZER_VOLLEY.getGlyph(),GlyphCatalog.MISSILE_SALVO.getGlyph());
        put(() -> ModItems.THE_INCINERATOR.get(),GlyphCatalog.IGNIS_BLADE_FIREBALLS.getGlyph(),GlyphCatalog.IGNIS_SWORD_COMBO.getGlyph(),GlyphCatalog.IGNIS_ULTIMATE.getGlyph(),GlyphCatalog.IGNIS_FLAME_STRIKE.getGlyph());
        put(() -> ModItems.BULWARK_OF_THE_FLAME.get(),GlyphCatalog.IGNIS_AIR_SMASH.getGlyph(),GlyphCatalog.IGNIS_EARTH_SHUDDERS.getGlyph(),GlyphCatalog.IGNIS_REINFORCED_SMASH.getGlyph());
        put(() -> ModItems.INFERNAL_FORGE.get(),GlyphCatalog.QUAKE.getGlyph());
        put(() -> ModItems.GAUNTLET_OF_GUARD.get(),GlyphCatalog.GRAVITY_PUNCH.getGlyph(),
            GlyphCatalog.GUARDIAN_UPPERCUT.getGlyph(),GlyphCatalog.GUARDIAN_ROCKET_PUNCH.getGlyph());
 }
 public static IClientMobEffectExtensions status(){return new IClientMobEffectExtensions(){
  private net.minecraft.client.renderer.texture.TextureAtlasSprite sprite(MobEffectInstance e){
   var effect=e.getEffect();
   var icon=effect==dev.cataclysmnouveau.CataclysmEffects.CURSED_RAGE.get()?MobEffects.DAMAGE_BOOST:
    effect==dev.cataclysmnouveau.CataclysmEffects.WATER_WALKING.get()?MobEffects.DOLPHINS_GRACE:MobEffects.FIRE_RESISTANCE;
   return Minecraft.getInstance().getMobEffectTextures().get(icon);
  }
  public boolean renderInventoryIcon(MobEffectInstance e,net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen<?> screen,net.minecraft.client.gui.GuiGraphics g,int x,int y,int z){g.blit(x,y+7,z,18,18,sprite(e));return true;}
  public boolean renderGuiIcon(MobEffectInstance e,net.minecraft.client.gui.Gui gui,net.minecraft.client.gui.GuiGraphics g,int x,int y,float z,float alpha){
   com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1,1,1,alpha);
   try{g.blit(x+3,y+3,(int)z,18,18,sprite(e));}finally{com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1,1,1,1);}return true;
  }
 };}
 private GlyphClientExtensions(){}
}
