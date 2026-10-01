package core.interfaces;

public sealed interface Movement permits NormalMove, Castle, EnPassant, Promotion {}
