class Obstacle 
{   
    PVector position;
    boolean isActive;    
    color col; 

    Obstacle (float x, float y)
    {
        position = new PVector(0, 0);
        col = color(#bf0a22);
        position.x = x;
        position.y = y;
        isActive = true;
    }

    void drawObstacle()
    {   
        if (isActive == true) 
        {
            fill(col);
            rect(position.x, position.y, 10, 10);
        }    
    }
}