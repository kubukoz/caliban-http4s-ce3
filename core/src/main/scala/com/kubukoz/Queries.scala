package com.kubukoz

import cats.implicits._
import cats.Applicative
import cats.ApplicativeThrow

trait Characters[F[_]] {
  def characters: F[List[Character]]
  def character(name: CharacterName): F[Option[Character]]
}

object Characters {
  def apply[F[_]](using F: Characters[F]): Characters[F] = F

  def instance[F[_]: ApplicativeThrow]: Characters[F] =
    new Characters[F] {

      override def characters: F[List[Character]] = List(Character("foo", 42), Character("bar", 50))
        .pure[F]

      override def character(
        name: CharacterName
      ): F[Option[Character]] =
        if (name.name == "foo")
          Some(
            Character("foo", 42)
          ).pure[F]
        else
          new Throwable("foo!").raiseError

    }

  extension [F[_]](F: Characters[F]) {

    def toQueries: CharacterQueries[F] = CharacterQueries(
      F.characters,
      F.character,
    )

  }

  case class CharacterQueries[F[_]](
    characters: F[List[Character]],
    character: CharacterName => F[Option[Character]],
  )

}

case class Character(name: String, age: Int)

case class CharacterName(name: String)

//resolver
case class Queries[F[_]](
  characters: Characters.CharacterQueries[F]
)

object Queries {

  def instance[F[_]: Characters] = Queries[F](
    Characters[F].toQueries
  )

}
