import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.sound.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Boids extends PApplet {

//Original idea created by Craig Reynolds
//Inspiration for this implementation: Daniel Shiffmann, youtube user: mick maus, youtube user: Sebastian Lague

 //Imports sound library

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

public void setup() 
{
    
    

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

public void draw() 
{   
    mouseVector = new PVector(mouseX, mouseY);

    background(0xff454359);

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

public void mouseClicked()
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

public void keyPressed()
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

public void createBoid()
{
    flock.add(new Boid(mouseX, mouseY));
}

public String currentTool()
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

public void createObstacle()
{   
    rectMode(CENTER);
    obstacleList.add(new Obstacle(mouseX, mouseY));
    rectMode(CORNER);
}

class Boid 
{
    PVector position;
    PVector velocity;
    PVector acceleration;
    
    int col;
    float accelerationType; 
    ArrayList<Boid> neighbors;
    ArrayList<Obstacle> obstacles;

    Boid (float x, float y) 
    {
        position = new PVector(0, 0);
        velocity = new PVector(random(-2, 2), random(-2, 2));
        acceleration = new PVector(0, 0);
        position.x = x;
        position.y = y;
        col = color(random(0, 255), random(0, 255), random(0, 255));
        neighbors = new ArrayList<Boid>();
        obstacles = new ArrayList<Obstacle>();
    }

    int directionIndicatorLenght = 15;

    //Draws the boid with it's current position to the screen
    public void drawBoid() 
    {   
        //Draws the boids as arrows with a color 
        pushMatrix(); 
        
        translate(position.x, position.y);     
        rotate(velocity.heading());
        
        fill(col);

        beginShape();
        vertex(8, 0);
        vertex(0 - 16, 0 + 6);
        vertex(-10, 0);
        vertex(0 - 16, 0 - 6);  
        endShape(CLOSE);
        
        popMatrix(); 

        //Shows the neighborRadius of the Boids
        // noFill();
        // ellipse(position.x, position.y, neighborRadius, neighborRadius);
    }

    public void update()
    {   
        findNeighbors();
        findObstacles();

        PVector seperation = getSeperation();
        PVector alignment = getAlignment();
        PVector cohesion = getCohesion();
        PVector obstacleAvoidance = avoidObstacles();
        PVector obstacleHit = hitObstacle();

        col = getColor();

        acceleration.set(0, 0);
        acceleration.add(alignment);
        acceleration.add(seperation);
        acceleration.add(cohesion);
        acceleration.add(obstacleAvoidance);

        velocity.add(acceleration);
        velocity.limit(velocityLimit);
        velocity.add(obstacleHit);
       
        position.add(velocity);
    }

    //Moves boids to opposite side of screen if they escape the boundaries
    public void edges() 
    {
        if (position.x > width) 
        {
            position.x = 0;
        }
        if (position.x < 0) 
        {
            position.x = width;
        }
        if (position.y > height) 
        {
            position.y = 0;
        }
        if (position.y < 0) 
        {
            position.y = height;
        }
    }


    //Attempts to steer the boids towards their target with the magnitude of their desired
    public void findNeighbors()
    {
        ArrayList<Boid> nearby = new ArrayList<Boid>();
        for (int i = 0; i < flock.size(); i++) 
        {
            Boid possibleNeigbor = flock.get(i);
            if(possibleNeigbor == this) continue;
            if(abs(possibleNeigbor.position.x - this.position.x) < neighborRadius && abs(possibleNeigbor.position.y - this.position.y) < neighborRadius)
            {
                nearby.add(possibleNeigbor);        
            }
        }
        neighbors = nearby; 
    }

    public void findObstacles()
    {
        ArrayList<Obstacle> nearby = new ArrayList<Obstacle>();
        for (int i = 0; i < obstacleList.size(); i++)
        {
            Obstacle possibleObstacle = obstacleList.get(i);
            if(abs(possibleObstacle.position.x - this.position.x) < obstacleRadius && abs(possibleObstacle.position.y - this.position.y) < obstacleRadius)
            {
                nearby.add(possibleObstacle);
            }
        }
        obstacles = nearby;
    }

    public PVector avoidObstacles()
    {
        PVector steering = new PVector(0, 0);
        int obstacleCount = 0;

        for (Obstacle obstacle : obstacles)
        {
            float distance = PVector.dist(this.position, obstacle.position);
            if (distance < obstacleRadius)
            {
                PVector seperate = PVector.sub(this.position, obstacle.position);
                seperate.setMag(10);
                seperate.div(distance);
                steering.add(seperate);
                obstacleCount++;
            }
        }
        return steering;
    }

    public PVector hitObstacle()
    {
        PVector steering = new PVector(0, 0);
        int obstacleCount = 0;

        for (Obstacle obstacle : obstacles)
        {
            float distance = PVector.dist(this.position, obstacle.position);
            if (distance < hitRadius)
            {
                hit.play();
                PVector bounce = PVector.sub(this.position, obstacle.position);
                bounce.setMag(10);
                steering.add(bounce);
                obstacleCount++;
            }
        }
        return steering;
    }

    public int getColor()
    {
        int neighborAverage = col;
        int neighborCount = 0;

        for (Boid other : neighbors)
        {
            neighborAverage = lerpColor(other.col, col, 0.5f);
            neighborCount++;
        }

        if (neighborCount == 0)
        {
            return col; 
        }
        return  neighborAverage;
    }

    public PVector getSeperation()
    {
        PVector steering = new PVector(0, 0);
        int neighborCount = 0;

        for (Boid other : neighbors)
        {
            float distance = PVector.dist(this.position, other.position);
            if (distance > 0 && distance < seperationRadius)
            {
                PVector seperate = PVector.sub(this.position, other.position);
                seperate.setMag(4);
                seperate.div(distance);
                steering.add(seperate);
                neighborCount++;
            }
        }
        return steering;
    }

    public PVector getAlignment()
    {
        PVector steering = new PVector(0, 0);
        int neighborCount = 0;
        
        for (Boid other : neighbors)
        {
            float distance = PVector.dist(this.position, other.position);
            if (distance > 0 && distance < alignmentRadius)
            {
                PVector mimic = other.velocity.copy();
                mimic.setMag(velocityLimit * neighborCount);
                mimic.div(distance);
                steering.add(mimic);
                neighborCount++;
            }
        }
        return steering;
    }

    public PVector getCohesion()
    {
        PVector accumulator = new PVector(0, 0); //Vessel for accumulated neighbor positions
        int neighborCount = 0; 
        for (Boid other : neighbors)
        {
            float distance = PVector.dist(this.position, other.position);
            if (distance > 0 && distance < cohesionRadius)
            {
                accumulator.add(other.position);
                neighborCount++;
            }
        }
        if (neighborCount > 0)
        {
            accumulator.div(neighborCount);

            PVector cohesionDesired = PVector.sub(accumulator, position);
            return cohesionDesired.setMag(0.1f);
        }
        else 
        {
            return new PVector(0, 0);         
        }
    }
}
class Obstacle 
{   
    PVector position;
    boolean isActive;    
    int col; 

    Obstacle (float x, float y)
    {
        position = new PVector(0, 0);
        col = color(0xffbf0a22);
        position.x = x;
        position.y = y;
        isActive = true;
    }

    public void drawObstacle()
    {   
        if (isActive == true) 
        {
            fill(col);
            rect(position.x, position.y, 10, 10);
        }    
    }
}
  public void settings() {  size(1920, 1080, P2D);  smooth(8); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Boids" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
