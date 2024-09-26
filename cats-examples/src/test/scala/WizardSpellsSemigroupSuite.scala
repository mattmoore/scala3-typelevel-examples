import cats.implicits.*
import cats.Semigroup
import weaver.*

object WizardSpellSemigroupSuite extends SimpleIOSuite {
  enum Spell {
    case Absorb, Fire, Water
  }

  case class Wizard(name: String, spells: Map[Spell, Int])

  pureTest("A wizard absorbs another wizard's spells, accumulating the level as well") {
    val gandalf = Wizard(
      "Gandalf",
      Map(
        (Spell.Water  -> 1),
        (Spell.Absorb -> 1),
      ),
    )

    val saruman = Wizard(
      "Saruman",
      Map(
        (Spell.Water  -> 2),
        (Spell.Fire   -> 2),
        (Spell.Absorb -> 2),
      ),
    )

    extension (a: Wizard) {
      def absorb(b: Wizard): Wizard = {
        if (a.spells.contains(Spell.Absorb))
        then a.copy(
            spells = (a.spells ++ b.spells).map { (k, _) =>
              (k, a.spells.getOrElse(k, 0) + b.spells.getOrElse(k, 0))
            },
          )
        else a
      }
    }

    val result = gandalf.absorb(saruman)

    expect(
      result == Wizard(
        "Gandalf",
        Map(
          (Spell.Water  -> 3),
          (Spell.Absorb -> 3),
          (Spell.Fire   -> 2),
        ),
      ),
    )
  }

  pureTest("A wizard can't absorb another wizard's spells if they don't have the absorb spell") {
    val gandalf = Wizard(
      "Gandalf",
      Map(
        (Spell.Water -> 1),
      ),
    )

    val saruman = Wizard(
      "Saruman",
      Map(
        (Spell.Water  -> 2),
        (Spell.Fire   -> 2),
        (Spell.Absorb -> 2),
      ),
    )

    extension (a: Wizard) {
      def absorb(b: Wizard): Wizard = {
        if (a.spells.contains(Spell.Absorb)) {
          a.copy(
            spells = (a.spells ++ b.spells).map { (k, _) =>
              (k, a.spells.getOrElse(k, 0) + b.spells.getOrElse(k, 0))
            },
          )
        } else {
          a
        }
      }
    }

    val result = gandalf.absorb(saruman)

    expect(
      result == Wizard(
        "Gandalf",
        Map(
          (Spell.Water -> 1),
        ),
      ),
    )
  }

  pureTest("Semigroup variant: A wizard absorbs another wizard's spells, accumulating the level as well") {
    val gandalf = Wizard(
      "Gandalf",
      Map(
        (Spell.Water  -> 1),
        (Spell.Absorb -> 1),
      ),
    )

    val saruman = Wizard(
      "Saruman",
      Map(
        (Spell.Water  -> 2),
        (Spell.Fire   -> 2),
        (Spell.Absorb -> 2),
      ),
    )

    given Semigroup[Wizard] with {
      override def combine(a: Wizard, b: Wizard): Wizard =
        if (a.spells.contains(Spell.Absorb))
        then a.copy(spells = a.spells |+| b.spells)
        else a
    }

    val result = gandalf |+| saruman

    expect(
      result == Wizard(
        "Gandalf",
        Map(
          (Spell.Water  -> 3),
          (Spell.Absorb -> 3),
          (Spell.Fire   -> 2),
        ),
      ),
    )
  }

  pureTest("Semigroup variant: A wizard can't absorb another wizard's spells if they don't have the absorb spell") {
    val gandalf = Wizard(
      "Gandalf",
      Map(
        (Spell.Water -> 1),
      ),
    )

    val saruman = Wizard(
      "Saruman",
      Map(
        (Spell.Water  -> 2),
        (Spell.Fire   -> 2),
        (Spell.Absorb -> 2),
      ),
    )

    given Semigroup[Wizard] with {
      override def combine(a: Wizard, b: Wizard): Wizard =
        if (a.spells.contains(Spell.Absorb))
        then a.copy(spells = a.spells |+| b.spells)
        else a
    }

    val result = gandalf |+| saruman

    expect(
      result == Wizard(
        "Gandalf",
        Map(
          (Spell.Water -> 1),
        ),
      ),
    )
  }
}
