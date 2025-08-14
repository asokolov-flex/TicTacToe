# Tic Tac Toe Game

A classic implementation of the Tic Tac Toe game with an intelligent computer opponent. This game features a graphical interface where players can compete against an AI that employs strategic decision-making.

## Features

- Interactive 3x3 game board
- Player vs Computer gameplay
- Intelligent AI opponent with strategic moves
- Visual feedback with colored symbols (X in red, O in blue)
- Game state messages (Win, Draw, Game Over)
- Easy restart functionality

## How to Play

1. The game starts with an empty 3x3 grid
2. Player uses X (red) and Computer uses O (blue)
3. Click on any empty cell to place your mark
4. The computer will automatically make its move after yours
5. The game ends when:
    - A player gets three marks in a row (horizontally, vertically, or diagonally)
    - The board is full (Draw)

## Controls

- **Left Mouse Click**: Make a move in an empty cell
- **Space**: Restart the game when it's finished
- **Escape**: Restart the game at any time

## Computer AI Strategy

The computer opponent uses a prioritized strategy to make decisions:

1. Takes the center cell if available
2. Looks for winning moves
3. Blocks player's winning moves
4. Takes the first available empty cell if no strategic moves are possible

## Game Messages

- "You Win!" (green) - When the player wins
- "Game Over" (red) - When the computer wins
- "Draw!" (blue) - When no more moves are possible and no winner is determined

## Technical Requirements

The game is built in Java and requires:
- Java SDK 17 or higher
- Graphics support for the game interface

## Project Structure

The game is implemented as a Java class that extends a base Game class, providing:
- Game board management
- Player interaction handling
- Win condition verification
- Computer AI logic
- Visual interface updates
