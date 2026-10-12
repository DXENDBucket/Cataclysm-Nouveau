package dev.cataclysmnouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("cataclysm_nouveau_ignis") @PrefixGameTestTemplate(false)
public final class IgnisActionMigrationTests {
    @GameTest(template="empty",batch="ignis_migration_0",timeoutTicks=100)
    public static void ignisLeapingSmashWaitsForRealLandingAndRestoresGravity(GameTestHelper h){NativeAbilityTests.ignisLeapingSmashWaitsForRealLandingAndRestoresGravity(h);}
    @GameTest(template="empty",batch="ignis_migration_1",timeoutTicks=100)
    public static void ignisReinforcedSmashIncludesAirAndBothLandingVariants(GameTestHelper h){NativeAbilityTests.ignisReinforcedSmashIncludesAirAndBothLandingVariants(h);}
    @GameTest(template="empty",batch="ignis_migration_2",timeoutTicks=100)
    public static void ignisEarthShuddersCompletesAllThreeStrikesAndProtectsAllies(GameTestHelper h){NativeAbilityTests.ignisEarthShuddersCompletesAllThreeStrikesAndProtectsAllies(h);}
    @GameTest(template="empty",batch="ignis_migration_3",timeoutTicks=100)
    public static void ignisUltimateRetainsOpeningRingEightFieldsAndTerrain(GameTestHelper h){NativeAbilityTests.ignisUltimateRetainsOpeningRingEightFieldsAndTerrain(h);}
    @GameTest(template="empty",batch="ignis_migration_4",timeoutTicks=100)
    public static void ignisBothSwordDancePhasesDamageBrandAndHealingOwnership(GameTestHelper h){NativeAbilityTests.ignisBothSwordDancePhasesDamageBrandAndHealingOwnership(h);}
    @GameTest(template="empty",batch="ignis_migration_5",timeoutTicks=100)
    public static void ignisSwordDanceVisualPacketsFollowBothPhasesAndCancellation(GameTestHelper h){NativeAbilityTests.ignisSwordDanceVisualPacketsFollowBothPhasesAndCancellation(h);}
    @GameTest(template="empty",batch="ignis_migration_6",timeoutTicks=100)
    public static void ignisPlayerSteeringLocksDuringStrikesAndUsesDamageHeading(GameTestHelper h){NativeAbilityTests.ignisPlayerSteeringLocksDuringStrikesAndUsesDamageHeading(h);}
    @GameTest(template="empty",batch="ignis_migration_7",timeoutTicks=100)
    public static void ignisPositionSwordDanceKeepsGeometryOwnershipAndIndependentActor(GameTestHelper h){NativeAbilityTests.ignisPositionSwordDanceKeepsGeometryOwnershipAndIndependentActor(h);}
    @GameTest(template="empty",batch="ignis_migration_8",timeoutTicks=100)
    public static void ignisBeneficialEntitySelectionRejectsEnemiesAndMobUsesAttackTarget(GameTestHelper h){NativeAbilityTests.ignisBeneficialEntitySelectionRejectsEnemiesAndMobUsesAttackTarget(h);}
    @GameTest(template="empty",batch="ignis_migration_9",timeoutTicks=100)
    public static void swordCoreClaimsAuthorizationAndLegacyActionConflict(GameTestHelper h){NativeAbilityTests.swordCoreClaimsAuthorizationAndLegacyActionConflict(h);}
    @GameTest(template="empty",batch="ignis_migration_10",timeoutTicks=100)
    public static void ignisActionsRejectHostileAndMissTargetsWithoutChangingStateOrCharging(GameTestHelper h){NativeAbilityTests.ignisActionsRejectHostileAndMissTargetsWithoutChangingStateOrCharging(h);}
    @GameTest(template="empty",batch="ignis_migration_11",timeoutTicks=100)
    public static void ignisFixedActionsRetainNativeDamageStagesPoseFeeAndRootMotion(GameTestHelper h){NativeAbilityTests.ignisFixedActionsRetainNativeDamageStagesPoseFeeAndRootMotion(h);}
    @GameTest(template="empty",batch="ignis_migration_12",timeoutTicks=100)
    public static void ignisEarthPlayerTurnsDuringPreparationAndLocksDamageAndVisualTogether(GameTestHelper h){NativeAbilityTests.ignisEarthPlayerTurnsDuringPreparationAndLocksDamageAndVisualTogether(h);}
    @GameTest(template="empty",batch="ignis_migration_13",timeoutTicks=100)
    public static void ignisActionsRevokeAuthorityReleaseOwnClaimAndCancelOnlyOwnChildren(GameTestHelper h){NativeAbilityTests.ignisActionsRevokeAuthorityReleaseOwnClaimAndCancelOnlyOwnChildren(h);}
}
