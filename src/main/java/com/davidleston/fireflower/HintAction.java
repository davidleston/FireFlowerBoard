package com.davidleston.fireflower;

abstract class HintAction extends Action {
  final int playerReceivingHint;

  HintAction(int playerReceivingHint) {
    this.playerReceivingHint = playerReceivingHint;
  }
}
