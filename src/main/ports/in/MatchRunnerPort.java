package main.ports.in;

// Los puertos 'in' tienen que ser implementados por las clases del nucleo, no por los adaptadores.
// Los adaptadores de entrada llaman a estos puertos, pero ellos mismos no implementan ninguno.

public interface MatchRunnerPort {
    Boolean runGame();
}
