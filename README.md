# caliban-http4s-ce3

A template project for building GraphQL applications with the following stack:

- http4s,
- Caliban,
- Cats Effect 3
- Scala 3.


## What does it have?
- exposing a resolver under `/api/graphql`
- error logging: errors in the GraphQL interpreter will be logged using slf4j
- source separation: the `core` module is defined without any dependencies on Caliban, so unless you need customization you won't even be able to access ZIO types in there.
