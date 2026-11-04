package game;

import game.view.TwoPlayerSetupValidation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class TwoPlayerSetupValidationTest {
    static Stream<Arguments> effectiveNames() {
        return Stream.of(Arguments.of(null,"Người chơi 1","Người chơi 1"),
                Arguments.of("","Người chơi 1","Người chơi 1"),Arguments.of("  ","Người chơi 2","Người chơi 2"),
                Arguments.of("\t\n","Player 2","Player 2"),Arguments.of(" Messi ","Player 1","Messi"),
                Arguments.of("  Ronaldo\t","Player 2","Ronaldo"),Arguments.of("A B","Player 1","A B"));
    }
    @ParameterizedTest @MethodSource("effectiveNames")
    void resolvesBlankAndTrimmedNames(String input,String fallback,String expected) {
        assertEquals(expected,TwoPlayerSetupValidation.effectiveName(input,fallback));
    }
    static Stream<Arguments> namePairs() {
        return Stream.of(Arguments.of("","",false),Arguments.of("  ","\t",false),
                Arguments.of(" Messi ","mEsSi",true),Arguments.of("ronaldo","RONALDO",true),
                Arguments.of("Người chơi 2","",true),Arguments.of(""," người chơi 1 ",true),
                Arguments.of("messi","ronaldo",false),Arguments.of("AB","A B",false));
    }
    @ParameterizedTest @MethodSource("namePairs")
    void comparesEffectiveNamesAfterTrimIgnoringCase(String first,String second,boolean duplicate) {
        var result=TwoPlayerSetupValidation.validate(first,second,"Người chơi 1","Người chơi 2",0,1);
        assertEquals(duplicate,result.duplicateNames()); assertEquals(!duplicate,result.canStart());
    }
    @Test void defaultsAreExplicitAndLocaleIndependent() {
        assertTrue(TwoPlayerSetupValidation.validate("Player 2","","Player 1","Player 2",0,1).duplicateNames());
        assertTrue(TwoPlayerSetupValidation.validate("","jogador 1","Jogador 1","Jogador 2",0,1).duplicateNames());
    }
    @Test void locksTheOtherPlayersAvatarInBothDirections() {
        assertTrue(TwoPlayerSetupValidation.isAvatarLocked(3,3));
        assertFalse(TwoPlayerSetupValidation.isAvatarLocked(4,3));
        assertTrue(TwoPlayerSetupValidation.isAvatarLocked(4,4));
        assertFalse(TwoPlayerSetupValidation.isAvatarLocked(3,4));
    }
    @Test void keepsAnAvailableSecondAvatar() {
        assertEquals(4,TwoPlayerSetupValidation.initialSecondAvatar(0,4));
    }
    @Test void resolvesOnlyTheInitialSecondAvatarConflict() {
        assertEquals(1,TwoPlayerSetupValidation.initialSecondAvatar(0,0));
        assertEquals(0,TwoPlayerSetupValidation.initialSecondAvatar(7,7));
    }
    @Test void duplicateAvatarsCannotStartEvenWithDistinctNames() {
        var conflict=TwoPlayerSetupValidation.validate("Messi","Ronaldo","Player 1","Player 2",3,3);
        assertFalse(conflict.duplicateNames()); assertTrue(conflict.duplicateAvatars()); assertFalse(conflict.canStart());
        assertTrue(TwoPlayerSetupValidation.validate("Messi","Ronaldo","Player 1","Player 2",3,4).canStart());
    }
}
