# EntityJS 1.20.1 multiloader architecture

The Fabric and Forge jars are built from one Gradle project. Code in `common/src`
is compiled directly into each loader jar against that loader's native Minecraft,
KubeJS, Rhino, and GeckoLib dependencies. There is no separately loaded common jar.

## Source ownership

- `common`: the canonical API and behavior: callback machinery, builders,
  modification APIs, renderers/models, animation interfaces, assets, and
  implementation bases.
- `fabric`: Fabric entrypoints, registration, networking, access widening, mixins,
  and thin leaves for signatures that differ from Forge-patched Minecraft.
- `forge`: Forge entrypoints, events, networking, compatibility integrations, and
  thin leaves for Forge-patched Minecraft signatures.

Shared code must not import Fabric or Forge APIs. Equivalent vanilla APIs should be
preferred over platform hooks. `EntityJSPlatform` is intentionally implemented with
the same FQCN in each loader source tree, so common code is compiled and linked
against the native implementation without bytecode transformation.

Concrete entity implementations remain loader leaves when their Minecraft
superclasses have incompatible ABIs. The common causes are Forge lifecycle methods,
Forge multipart entities, patched step-sound signatures, and patched collision or
trample methods. These same-FQCN leaves are recorded in
`config/loader-source-allowlist.txt`; adding another duplicate without making that
decision explicit fails verification.

Fabric attribute customization is retained through
`EntityJSPlatform.applyAttributeBuilder`. Shared builders use Forge's canonical
attribute defaults and Fabric applies its `attributes(...)` callback before the
supplier is built. GeckoLib packet sends use the same pattern so the public
animation interfaces remain common while each loader calls its native network API.

Dynamic override method aliases use `EntityJSPlatform.dynamicOverrideMethodAlias`.
Fabric returns no alias because its runtime names already match the catalog keys.
Forge generates `entityjs/mappings/method_aliases.tsv` from Loom's SRG-to-named
mapping output and resolves production method names through Forge's mapping service.

## TameableMobJS method map

`TameableMobJSBase` proves the extraction pattern before it is applied to the other
entity implementations:

| Difference | Ownership |
| --- | --- |
| Sound/entity registry access | Shared vanilla `BuiltInRegistries` |
| Tame cancellation | `EntityJSPlatform.isAnimalTameCancelled` |
| Living jump hook | `EntityJSPlatform.onLivingJump` |
| Food properties and custom arrows | `EntityJSPlatform` compatibility hooks |
| Fabric lifecycle emulation | Shared base, enabled by platform capability |
| Native Forge lifecycle callbacks | Forge leaf |
| Multipart return type | Loader leaf |
| Forge-only patched entity methods | Forge leaf |
| Step-sound signature differences | Loader leaf; callback API remains common |
| Vanilla-compatible behavior and callback overrides | Shared base |

The combined common base and Forge leaf must retain every public/protected method
name from the previous Forge implementation. Both loader compilation and both
`remapJar` tasks are required before another implementation family is migrated.

## Intentional loader-only features

Forge biome-spawn and spawn-placement events and the CGM integration remain under
`forge`. They are capability differences, not duplicated common implementations.

## Verification

Run `./gradlew verifyMultiloader`. It enforces all of the following:

- common source contains no Fabric or Forge imports and is not shadowed by a loader;
- every remaining same-FQCN loader source is present in the explicit allowlist;
- Fabric and Forge register the same entity builder types except Forge-only
  `cgm:ammo`;
- both remapped jars contain their own loader descriptor, exclude the other
  descriptor, and include the common builder plus the native platform class;
- the Forge jar contains a non-empty production method alias table at
  `entityjs/mappings/method_aliases.tsv`.

Both loader compiles and both `remapJar` tasks are dependencies of this verification
path. A startup smoke test should also be run when loader dependencies or entrypoint
code changes.
