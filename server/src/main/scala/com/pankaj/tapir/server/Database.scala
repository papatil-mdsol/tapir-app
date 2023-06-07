package com.pankaj.tapir.server

import com.pankaj.tapir.endpoints.Kitten

object Database {
  var kittens: List[Kitten] = List(
    Kitten(1L, "mew", "male", 20),
    Kitten(2L, "mews", "female", 25),
    Kitten(3L, "smews", "female", 29)
  )
}
