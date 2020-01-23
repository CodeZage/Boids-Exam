# Boids Exam
My Exam Project for the Intro To Programming course at Aalborg University

The basic functionality of the program is based on the flocking behaviour described first by Craig Reynolds in 1986.

This implementation makes use the same basic ideas that he pioneered. It simulates a flocking behaviour as seen in birds or fish. The "Boids" as they are called will
attempt to approach other boids in there vicinity, avoid hitting them, and align their direction of movement to the flock. They will also attempt to avoid obstacles
and if the boid impacts the obstacle it will be pushed back.

-------CONTROLS-------
Mouse click: Add object of chosen class to the sketch. Can be either boid or obstacle 
Spacebar: Changes the object-type to be added by mouseclick


-------VARIABLES------- (Alter these to change starting conditions)
initialboids (number of boids created at the start of the sketch)
