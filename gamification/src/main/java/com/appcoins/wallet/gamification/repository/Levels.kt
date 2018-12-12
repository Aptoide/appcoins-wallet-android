package com.appcoins.wallet.gamification.repository

import java.math.BigDecimal

data class Levels(val status: Status, val list: List<Level>) {
  constructor(status: Status) : this(status, emptyList())

  data class Level(val amount: BigDecimal, val bonus: Double, val level: Int)
  enum class Status {
    OK, NO_NETWORK, UNKNOWN_ERROR
  }
}