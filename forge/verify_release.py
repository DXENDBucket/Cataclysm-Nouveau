"""Check the reobfuscated Forge binary and sources: runtime references only."""
from pathlib import Path
import hashlib,json,re,zipfile
root=Path(__file__).resolve().parent
version=(root.parent/'VERSION').read_text().strip()
binary=root/f'build/libs/cataclysm-nouveau-{version}-mc1.20.1-forge.jar'
sources=root/f'build/libs/cataclysm-nouveau-{version}-sources.jar'
report=[]
for file in [binary,sources]:
    with zipfile.ZipFile(file) as jar:
        for name in jar.namelist():
            if name.endswith('/'):continue
            if name=='assets/minecraft/atlases/blocks.json':
                assert json.loads(jar.read(name))=={'sources':[{'type':'minecraft:single','resource':'cataclysm:entity/deepling/deepling_warlock_mark1'}]}
            elif name.startswith('assets/') and not name.startswith('assets/cataclysm_nouveau/'):
                raise SystemExit('Foreign asset namespace: '+name)
            if name.endswith('.class') and not name.startswith('dev/cataclysmnouveau/'):
                raise SystemExit('Foreign class: '+name)
            if name.startswith(('com/github/','libs/','.upstream/')) or re.search(r'(?i)\.(jar|png|ogg|wav|obj|bbmodel|jpeg|jpg|webp|gif)$',name):
                raise SystemExit('Embedded native code/media/dependency: '+name)
        if not {'LICENSE','THIRD_PARTY_NOTICES.md'} <= set(jar.namelist()):raise SystemExit('Missing license or notices')
        if file==binary:
            assert json.loads(jar.read('cataclysm_nouveau.mixins.json'))['compatibilityLevel']=='JAVA_17'
            assert jar.read('cataclysm_nouveau.refmap.json')
            recipes=[n for n in jar.namelist() if n.startswith('data/cataclysm_nouveau/recipes/') and n.endswith('.json')]
            glyphs=[n for n in recipes if json.loads(jar.read(n)).get('type')=='ars_nouveau:glyph']
            assert len(glyphs)==102,len(glyphs)
            assert b'neoforge' not in jar.read('META-INF/mods.toml').lower()
            for n in glyphs:
                r=json.loads(jar.read(n));assert isinstance(r['output'],str) and r['inputItems']
        report.append({'file':file.name,'sha256':hashlib.sha256(file.read_bytes()).hexdigest(),'entries':len(jar.namelist())})
(root/'build/distribution-audit.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report,indent=2))
