//Original idea created by Craig Reynolds
//Inspiration for this implementation: Daniel Shiffmann, youtube user: mick maus, youtube user: Sebastian Lague

import processing.sound.*; //Imports sound library

//Designates an array list to contain all instances of the classes 
ArrayList<Boid> flock;
ArrayList<Obstacle> obstacleList; 


PVector mouseVector;

boolean boidTool = true; //True = Boid tool | False = obstacle tool 


int initialBoids = 300; //Determines number of boids created at startup
int flockLimit = 500; //Determines max number of boids

//Collection of boid radii (can be understood as perception range)
float neighborRadius = 100;
float cohesionRadius = 100;
float alignmentRadius = 85;
float seperationRadius = 75;
float obstacleRadius = 150;

float hitRadius = 20; //If a boid comes in contact with an Obstacle it will be pushed back

SoundFile hit;

//Max boid movespeed
float velocityLimit = 2;

void setup() 
{
    size(1920, 1080, P2D);
    smooth(8);

    hit = new SoundFile(this, "hit.wav");

    flock = new ArrayList<Boid>();
    obstacleList = new ArrayList<Obstacle>();

    for (int i = 0; i < initialBoids; i++) 
    {
        flock.add(new Boid(random(60, width - 60), random(60, height - 60)));
    }
    
    //Drawborders
    for (int i = 0; i < width; i += 10) 
    {
        obstacleList.add(new Obstacle(0 + i, 0));
    }

    for (int i = 0; i < height; i += 10) 
    {
        obstacleList.add(new Obstacle(0, 0 + i));
    }

    for (int i = 0; i < width; i += 10) 
    {
        obstacleList.add(new Obstacle(0 + i, height - 10));
    }

    for (int i = 0; i < height; i += 10) 
    {
        obstacleList.add(new Obstacle(width - 10, 0 + i));
    }
    
}

void draw() 
{   
    mouseVector = new PVector(mouseX, mouseY);

    background(#454359);

    for (int i = 0; i < flock.size(); i++) 
    {   
        Boid initial = flock.get(i);
        initial.drawBoid();
        initial.edges();
        initial.update();
    }

    for (int i = 0; i < obstacleList.size(); i++)
    {   
        Obstacle initial = obstacleList.get(i);
        initial.drawObstacle();
    }

    fill(255);
    textSize(25);
    text("Boids: " + flock.size(), 30, 50);
    text("Current Tool: " + currentTool(), 30, 100);
}

void mouseClicked()
{   
    if (boidTool == true)
    {
        if (flock.size() < flockLimit) 
        {
            createBoid();
        }
        else 
        {
            return;    
        }
    }
    if (boidTool == false)
    {
        createObstacle();
    }
}

void keyPressed()
{
    if (key == ' ')
    {
        if (boidTool == true)
        {
            boidTool = false;
        }
        else if (boidTool == false)
        {
            boidTool = true;
        }
    }
}

void createBoid()
{
    flock.add(new Boid(mouseX, mouseY));
}

String currentTool()
{
    if (boidTool == true)
    {
        return "Boids";
    }
    
    else if (boidTool == false)
    {
        return "Obstacles";
    }

    else return "NULL";
}

void createObstacle()
{   
    rectMode(CENTER);
    obstacleList.add(new Obstacle(mouseX, mouseY));
    rectMode(CORNER);
}

