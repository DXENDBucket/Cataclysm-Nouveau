"""Generate mechanical Forge/Ars-4 API bridges from the maintained shared sources.

Behavioral differences live in forge/src/main/java, which takes precedence.
No original Cataclysm source or media is copied by this generator.
"""
from pathlib import Path
import json
import re

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
OUT = HERE / 'build/generated/forge'


def closing(text, start, left='{', right='}'):
    depth = 0
    quote = None
    escaped = False
    line_comment = block_comment = False
    i = start
    while i < len(text):
        c = text[i]
        nxt = text[i:i+2]
        if line_comment:
            if c == '\n': line_comment = False
        elif block_comment:
            if nxt == '*/': block_comment = False; i += 1
        elif quote:
            if escaped: escaped = False
            elif c == '\\': escaped = True
            elif c == quote: quote = None
        elif nxt == '//': line_comment = True; i += 1
        elif nxt == '/*': block_comment = True; i += 1
        elif c in '\"\'': quote = c
        elif c == left: depth += 1
        elif c == right:
            depth -= 1
            if depth == 0: return i
        i += 1
    raise ValueError('Unbalanced Java input')


def pattern_switches(text):
    """Java-21 pattern statements become equivalent Java-17 instanceof chains."""
    edits = []
    for m in re.finditer(r'switch\s*\((\w+)\)\s*\{', text):
        end = closing(text, m.end()-1)
        body = text[m.end():end]
        first = re.match(r'\s*case\s+([\w.$]+)\s+(\w+)\s*->', body)
        if not first: continue
        cursor = 0; branches = []
        while cursor < len(body):
            match = re.match(r'\s*(?:case\s+([\w.$]+)\s+(\w+)|(?P<default>default))\s*->\s*', body[cursor:])
            if not match: break
            cursor += match.end()
            if body[cursor] == '{':
                stop = closing(body, cursor); statement = body[cursor:stop+1]; cursor = stop+1
            else:
                stop = body.index(';', cursor); statement = '{' + body[cursor:stop+1] + '}'; cursor = stop+1
            if match.group('default'): prefix = 'else '
            else: prefix = ('if' if not branches else 'else if') + f' ({m.group(1)} instanceof {match.group(1)} {match.group(2)}) '
            branches.append(prefix + statement)
        if body[cursor:].strip(): raise ValueError('Unsupported pattern switch tail: '+body[cursor:cursor+100])
        edits.append((m.start(), end+1, '\n'.join(branches)))
    for start,end,replacement in reversed(edits): text=text[:start]+replacement+text[end:]
    return text


MC_METHODS = {'tick','aiStep','hurt','defineSynchedData','readAdditionalSaveData','addAdditionalSaveData','onHit','onHitEntity','onHitBlock','canHitEntity','getAddEntityPacket','getBoundingBoxForCulling','shouldRenderAtSqrDistance','remove','setDeltaMovement','setPos','getYRot','getXRot'}


def split_mixed_selectors(text):
    edits=[]
    for m in re.finditer(r'@(ModifyArg|ModifyExpressionValue|WrapOperation|Inject)\s*\(',text):
        end=closing(text,m.end()-1,'(',')')
        annotation=text[m.start():end+1]
        selector=re.search(r'method\s*=\s*\{([^}]+)\}',annotation)
        if not selector:continue
        names=re.findall(r'"([^"]+)"',selector.group(1))
        mc=[x for x in names if x.split('(')[0] in MC_METHODS]
        native=[x for x in names if x not in mc]
        if not mc or not native:continue
        declaration=re.search(r'private [\w.<>]+ ([\w$]+)\(',text[end:])
        if not declaration:raise ValueError('Expected callback')
        body=text.index('{',end+declaration.end())
        stop=closing(text,body)+1
        whole=text[m.start():stop]
        name=declaration.group(1)
        variants=[]
        for group,suffix in [(mc,'Mc'),(native,'Native')]:
            value=whole[:selector.start()]+ 'method={'+','.join('"'+x+'"' for x in group)+'}'+whole[selector.end():]
            value=re.sub(r'\b'+re.escape(name)+r'\(',name+suffix+'(',value)
            variants.append(value)
        edits.append((m.start(),stop,'\n    '.join(variants)))
    for a,b,value in reversed(edits):text=text[:a]+value+text[b:]
    return text


def remap_selectors(text):
    edits=[]
    for m in re.finditer(r'@(Inject|Redirect|ModifyArg|ModifyArgs|ModifyConstant|ModifyVariable|ModifyExpressionValue|ModifyReturnValue|WrapOperation|WrapMethod)\s*\(', text):
        end=closing(text,m.end()-1,'(',')')
        annotation=text[m.start():end+1]
        selector=re.search(r'method\s*=\s*(\{[^}]*\}|\"[^\"]*\")',annotation)
        if not selector or not any(name.split('(')[0] in MC_METHODS for name in re.findall(r'\"([^\"]+)\"',selector.group(1))):continue
        if re.search(r'\bremap\s*=\s*false', annotation.split('at=')[0]):continue
        # Add only the annotation's own flag. Nested @At flags remain independent.
        annotation=annotation[:m.end()-m.start()]+'remap=true,'+annotation[m.end()-m.start():]
        edits.append((m.start(),end+1,annotation))
    for a,b,value in reversed(edits):text=text[:a]+value+text[b:]
    edits=[]
    for m in re.finditer(r'@At\s*\(',text):
        end=closing(text,m.end()-1,'(',')');a=text[m.start():end+1]
        target=re.search(r'target="([^";]+(?:;[^" ]+)?)"',a)
        if target and 'remap=' not in a:
            member=re.search(r';([^(:]+)[(:]',target.group(1))
            flag='true' if target.group(1).startswith('Lnet/minecraft/') or member and member.group(1) in MC_METHODS else 'false'
            a=a[:-1]+',remap='+flag+')';edits.append((m.start(),end+1,a))
    for a,b,value in reversed(edits):text=text[:a]+value+text[b:]
    return text


def packet(text, name):
    text=text.replace('RegistryFriendlyByteBuf','FriendlyByteBuf').replace(' implements CustomPacketPayload','')
    text=re.sub(r'import net\.minecraft\.network\.(codec\.StreamCodec|protocol\.common\.custom\.CustomPacketPayload);\n','',text)
    text=re.sub(r'import net\.neoforged\.[^;]+;\n','',text)
    start=text.index('    public static final Type<')
    codec=text.index('CODEC=StreamCodec.of(',start)
    opening=text.index('(',codec)
    end=closing(text,opening,'(',')')
    body=text[opening+1:end]
    write_end=closing(body,body.index('{'))
    encoder=body[body.index('{')+1:write_end]
    decoder=body[write_end+1:].strip()
    assert decoder.startswith(',b->')
    decoder=decoder[len(',b->'):]
    replacement=f'    public static void encode({name} p, FriendlyByteBuf b) {{{encoder}}}\n    public static {name} decode(FriendlyByteBuf b) {{return {decoder};}}'
    text=text[:start]+replacement+text[end+2:]
    text=re.sub(r'    @Override public Type<[^\n]+\n','',text)
    start=text.index('    public static void register(')
    end=closing(text,text.index('{',start))
    receiver='BodyPhantoms' if name=='BodyPhantomPayload' else 'IgnisPhantoms'
    text=text[:start]+f'''    public static void handle({name} p, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> context) {{
        context.get().enqueueWork(()->dev.cataclysmnouveau.client.{receiver}.accept(p));
        context.get().setPacketHandled(true);
    }}'''+text[end+1:]
    return text


def transform(text, relative):
    if relative.startswith('network/') and relative.endswith('Payload.java'):
        return packet(text,Path(relative).stem)
    text=pattern_switches(text)
    replacements={
        'ResourceLocation.fromNamespaceAndPath(':'new ResourceLocation(',
        'ResourceLocation.parse(':'new ResourceLocation(',
        '.getFirst()':'.get(0)',
        'com.github.L_Ender.cataclysm.entity.projectile.CMAbstractHurtingProjectile':'net.minecraft.world.entity.projectile.AbstractHurtingProjectile',
        'CMAbstractHurtingProjectile':'AbstractHurtingProjectile',
        'com.github.L_Ender.cataclysm.entity.projectile.CustomAbstractHurtingProjectile':'com.github.L_Ender.cataclysm.entity.projectile.Elemental_Spear_Entity',
        'CustomAbstractHurtingProjectile':'Elemental_Spear_Entity',
        'net.neoforged.neoforge.common.ModConfigSpec':'net.minecraftforge.common.ForgeConfigSpec',
        'ModConfigSpec':'ForgeConfigSpec',
        'net.neoforged.api.distmarker':'net.minecraftforge.api.distmarker',
        'net.neoforged.bus.api.SubscribeEvent':'net.minecraftforge.eventbus.api.SubscribeEvent',
        'net.neoforged.fml.common.EventBusSubscriber':'net.minecraftforge.fml.common.Mod',
        'net.neoforged.neoforge.client.event.ClientTickEvent':'net.minecraftforge.event.TickEvent',
        'net.neoforged.neoforge.client.event':'net.minecraftforge.client.event',
        '@EventBusSubscriber(':'@Mod.EventBusSubscriber(',
        'bus=EventBusSubscriber.Bus.MOD':'bus=Mod.EventBusSubscriber.Bus.MOD',
        'ClientTickEvent.Post':'net.minecraftforge.event.TickEvent.ClientTickEvent',
        'net.neoforged.neoforge.event.EventHooks':'net.minecraftforge.event.ForgeEventFactory',
        'EventHooks.onEnderTeleport':'ForgeEventFactory.onEnderTeleport',
        'EventHooks.canEntityGrief':'ForgeEventFactory.getMobGriefingEvent',
        'EventHooks.onEntityDestroyBlock':'ForgeEventFactory.onEntityDestroyBlock',
        'Lnet/neoforged/neoforge/event/EventHooks;canEntityGrief':'Lnet/minecraftforge/event/ForgeEventFactory;getMobGriefingEvent',
        'net.neoforged.neoforge.network.PacketDistributor':'dev.cataclysmnouveau.network.ForgePackets',
        'PacketDistributor.send':'ForgePackets.send',
        'getPartialTick().getGameTimeDeltaPartialTick(false)':'getPartialTick()',
    }
    for old,new in replacements.items():text=text.replace(old,new)
    for param in (['builder','b'] if 'SynchedEntityData.Builder' in text else []):
        text=text.replace('SynchedEntityData.Builder '+param+',CallbackInfo ci','CallbackInfo ci')
        text=text.replace('defineSynchedData(SynchedEntityData.Builder '+param+')','defineSynchedData()')
        text=text.replace('super.defineSynchedData('+param+')','super.defineSynchedData()')
        text=text.replace(param+'.define(', '((net.minecraft.world.entity.Entity)(Object)this).getEntityData().define(')
    if 'AbstractHurtingProjectile' in text and 'import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;' not in text:
        text=re.sub(r'(package [^;]+;)',r'\1\nimport net.minecraft.world.entity.projectile.AbstractHurtingProjectile;',text,count=1)
    text=re.sub(r'owner\.getData\(com\.github\.L_Ender\.cataclysm\.init\.ModDataAttachments\.TIDAL_TENTACLE_ATTACHMENT\)', 'dev.cataclysmnouveau.ForgeNative.tentacles(owner)',text)
    text=text.replace('cast.caster().getData(com.github.L_Ender.cataclysm.init.ModDataAttachments.TIDAL_TENTACLE_ATTACHMENT)', 'dev.cataclysmnouveau.ForgeNative.tentacles(cast.caster())')
    text=text.replace('pool.maxLavaTime=CataclysmConfig.lavaLifetime()', 'pool.setMaxLavaTime(CataclysmConfig.lavaLifetime())').replace('pool.maxLavaTime+2','pool.getMaxLavaTime()+2')
    text=text.replace('storm.setPower(storm.getPower().scale(acceleration))','dev.cataclysmnouveau.ForgeNative.accelerateStorm(storm,acceleration)')
    if relative=='mixin/CursedStormHitMixin.java':text=text.replace('@Mixin(Projectile.class)','@Mixin(Cursed_Sandstorm_Entity.class)')
    if relative=='mixin/FlareDamageMixin.java':text=text.replace('spawnJet','spawnjet')
    if relative=='mixin/IgnisExplosionMixin.java':
        text=text.replace(',com.github.L_Ender.cataclysm.entity.effect.Flame_Strike_Entity.class','')
    if relative=='mixin/CursedStormMixin.java':
        text=text.replace('lifeTicks','lifetick').replace('lifetick=290','lifetick=0').replace('method="tickHoming"','method="tick"').replace('storm.setHit(2);','')
    if relative in {'mixin/IgnisMotionMixin.java','mixin/CursedStormMotionMixin.java'}:
        if relative.endswith('CursedStormMotionMixin.java'):text=text.replace('Elemental_Spear_Entity','Cursed_Sandstorm_Entity')
        text=text.replace(';Lnet/minecraft/world/level/ClipContext$Block;', ';')
        text=text.replace(',ClipContext.Block type','').replace(',ClipContext.Block clip','').replace(',filter,type)',',filter)').replace(',filter,clip)',',filter)')
    for effect in ['LAVA_WALKING','WATER_WALKING','CURSED_RAGE']:
        text=re.sub(r'CataclysmEffects\.'+effect+r'(?![\w.])','CataclysmEffects.'+effect+'.get()',text)
    text=re.sub(r'ModEffect\.(EFFECT[A-Z_]+)(?![\w.])',r'ModEffect.\1.get()',text)
    if relative=='casting/HarbingerCasting.java':
        text=text.replace('if(payload instanceof AbstractHurtingProjectile fire)IgnisCasting.accelerate', 'if(payload instanceof AbstractHurtingProjectile fire && payload instanceof IgnisProjectile)IgnisCasting.accelerate')
    if relative=='casting/IgnisCasting.java':
        text=text.replace('if(!(child instanceof AbstractHurtingProjectile shot))', 'if(!(child instanceof AbstractHurtingProjectile shot) || !(child instanceof IgnisProjectile))')
    text=text.replace('new ExplosionDamageCalculator()', 'new dev.cataclysmnouveau.ForgeExplosionCalculator()')
    text=text.replace('new Ender_Guardian_Bullet_Entity(world,owner,direction)', 'new Ender_Guardian_Bullet_Entity(world,owner,direction.x,direction.y,direction.z)')
    text=text.replace('ParticleTypes.DUST_PILLAR','ParticleTypes.BLOCK')
    text=text.replace('new MobEffectInstance(CataclysmEffects.CURSED_RAGE.get(),interval*(stacks-level+1),\n                    level-1,false,true,true,chain)', 'dev.cataclysmnouveau.ForgeNative.rageLayer(interval*(stacks-level+1),level-1,chain)')
    # Ars 4 has no per-parent augment description hook. Retain the actual compatibility sets.
    text=re.sub(r'(?:for \(var form : PROJECTILES\) )?(?:profile\.glyph\(\)\.)?augmentDescriptions\.(?:putIfAbsent|put|remove)\([^;]+;', '',text)
    if relative.startswith('mixin/'):
        # Axe Blade declares its own contact methods on Entity, not Projectile overrides.
        if relative=='mixin/AxeBladeMixin.java':
            text=text.replace('method="onHit"','method="onHit(Lnet/minecraft/world/phys/HitResult;)V",remap=false')
        if relative in {'mixin/HarbingerNativeMixin.java','mixin/IgnisExplosionMixin.java'}:
            text=text.replace('"onHitBlock"','"onHitBlock(Lnet/minecraft/world/phys/BlockHitResult;)V"').replace('"onHitEntity"','"onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V"')
        text=split_mixed_selectors(text)
        text=remap_selectors(text)

    return text


def generate():
    expected=set()
    for source_root in [ROOT/'common/src/main/java',ROOT/'src/main/java']:
        for source in source_root.rglob('*.java'):
            rel=source.relative_to(source_root)
            if (HERE/'src/main/java'/rel).exists() or rel.name=='SpearVisualAccess.java':continue
            out=OUT/'java'/rel
            out.parent.mkdir(parents=True,exist_ok=True)
            short=str(rel).replace('\\','/').removeprefix('dev/cataclysmnouveau/')
            out.write_text(transform(source.read_text(encoding='utf-8'),short),encoding='utf-8')
            expected.add(out)
            if short=='mixin/IgnisExplosionMixin.java':
                flame=out.with_name('ForgeFlameExplosionMixin.java')
                value=transform(source.read_text(encoding='utf-8'),short).replace('public abstract class IgnisExplosionMixin','public abstract class ForgeFlameExplosionMixin')
                value=value.replace('Ignis_Fireball_Entity.class,Ignis_Abyss_Fireball_Entity.class','com.github.L_Ender.cataclysm/entity/effect/Flame_Strike_Entity.class'.replace('/','.'))
                value=re.sub(r'method=\{[^}]+\}', 'method="tick"',value)
                flame.write_text(value,encoding='utf-8');expected.add(flame)
    # Only generated files in this task's known build directory can be removed.
    for old in (OUT/'java').rglob('*.java'):
        if old not in expected:old.unlink()
    for root in [ROOT/'common/src/main/resources',ROOT/'src/main/resources']:
        for source in root.rglob('*'):
            if not source.is_file():continue
            rel=source.relative_to(root).as_posix()
            if rel=='META-INF/neoforge.mods.toml':continue
            rel=rel.replace('/recipe/','/recipes/').replace('/tags/item/','/tags/items/')
            out=OUT/'resources'/rel;out.parent.mkdir(parents=True,exist_ok=True)
            if source.suffix not in {'.json','.mcmeta'}:out.write_bytes(source.read_bytes());continue
            data=json.loads(source.read_text(encoding='utf-8'))
            if rel=='cataclysm_nouveau.mixins.json':
                data['compatibilityLevel']='JAVA_17';data['refmap']='cataclysm_nouveau.refmap.json'
                data['client'].append('client.ForgeGlyphItemRendererMixin');data['mixins'] += ['ForgeExplosionAccess','ForgeExplosionMixin','ForgeHiddenEffectAccess','ForgeFlameExplosionMixin'];data['mixins'].remove('SpearVisualAccess')
            elif rel.startswith('data/cataclysm_nouveau/recipes/'):
                if data.get('type')=='ars_nouveau:glyph':
                    data={'type':data['type'],'exp':data['exp'],'output':data['output']['id'],'count':data['output'].get('count',1),'inputItems':[{'item':x} for x in data['inputs']]}
                elif isinstance(data.get('result'),dict) and 'id' in data['result']:
                    data['result']['item']=data['result'].pop('id')
            elif rel=='pack.mcmeta':data['pack']['pack_format']=15
            out.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('Generated Forge Java bridges:',len(expected))


if __name__=='__main__':generate()
