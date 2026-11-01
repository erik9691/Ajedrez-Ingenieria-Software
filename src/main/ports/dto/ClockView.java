package main.ports.dto;

// Estado de los relojes para el display: tiempos restantes y de quién es el turno.
public record ClockView(long whiteMillis, long blackMillis, boolean whiteTurn) {}
