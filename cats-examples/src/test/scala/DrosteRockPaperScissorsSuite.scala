// import cats.effect.*
// import weaver.*
// import higherkindness.droste.*
// import higherkindness.droste.data.*
// 
// object DrosteRockPaperScissorsSuite extends SimpleIOSuite {
//   pureTest("Rock, Paper, Scissors game") {
//     // Actions the player can perform
//     sealed trait Action
//     case object Rock     extends Action
//     case object Paper    extends Action
//     case object Scissors extends Action
// 
//     // A strategy, or how the player decides the next action based on opponent's previous actions:
//     type Strategy = PartialFunction[List[Action], Action]
//     val strategy: PartialFunction[List[Action], Action] = {
//       case Nil                 => Rock  // First move
//       case Rock :: Rock :: Nil => Paper // Two Rocks in a row
//     }
// 
//     // The definition of the game:
//     case class Player(wins: Int = 0, strategy: Strategy, history: List[Action] = Nil)
//     case class Game(player1: Player, player2: Player, round: Int = 0)
// 
//     // We’re going to create a new recursive structure that represents the development of the game:
//     sealed trait GameF[+T]
//     case class StepF[T](action1: Action, action2: Action, next: T) extends GameF[T]
//     case class EndF[T](player1: Player, player2: Player)           extends GameF[T]
// 
//     // And now the game rules as a Coalgebra:
//     //   We calculate what the next move is for each player
//     //   We update the status of the player based on these moves (win count)
//     //   We move to the next round
//     val coalgebra: Coalgebra[GameF, Game] = Coalgebra {
//       case Game(player1, player2, 3) => EndF(player1, player2)
//       case g @ Game(player1, player2, round) =>
//         val (nextAction1, nextAction2) =
//           (player1.strategy(player2.history), player2.strategy(player1.history))
//         StepF(
//           nextAction1,
//           nextAction2,
//           g.copy(
//             player1 = updatePlayer(player1, nextAction1, nextAction2),
//             player2 = updatePlayer(player2, nextAction2, nextAction1),
//             round = round + 1,
//           ),
//         )
//     }
// 
//     // And now we define an algebra to extract the game result in different ways:
// 
//     // Returning a string with the winner:
//     val algebraResult: Algebra[GameF, String] = Algebra {
//       case StepF(_, _, result) => result
//       case EndF(player1, player2) =>
//         if (player1.wins > player2.wins) "Player1 wins"
//         else if (player2.wins > player1.wins) "Player2 wins"
//         else "Nobody wins"
//     }
// 
//     // Returning an IO that will print the result for every round:
//     val algebraStepByStep: Algebra[GameF, IO[Unit]] = Algebra {
//       case StepF(a1, a2, io) => IO(println(s"Player1: $a1 | Player2: $a2")) *> io
//       case EndF(_, _)        => IO.unit
//     }
// 
//     // And finally, we run the game to return both things:
//     val runGame: Game => (IO[Unit], String) = scheme.ghylo(
//       (algebraStepByStep zip algebraResult).gather(Gather.cata),
//       coalgebra.scatter(Scatter.ana),
//     )
// 
//     expect(
//       true,
//     )
//   }
// }
