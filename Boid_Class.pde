class Boid 
{
    PVector position;
    PVector velocity;
    PVector acceleration;
    
    color col;
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
    void drawBoid() 
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

    void update()
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
    void edges() 
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
    void findNeighbors()
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

    void findObstacles()
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

    PVector avoidObstacles()
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

    PVector hitObstacle()
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

    color getColor()
    {
        color neighborAverage = col;
        int neighborCount = 0;

        for (Boid other : neighbors)
        {
            neighborAverage = lerpColor(other.col, col, 0.5);
            neighborCount++;
        }

        if (neighborCount == 0)
        {
            return col; 
        }
        return  neighborAverage;
    }

    PVector getSeperation()
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

    PVector getAlignment()
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

    PVector getCohesion()
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
            return cohesionDesired.setMag(0.1);
        }
        else 
        {
            return new PVector(0, 0);         
        }
    }
}
