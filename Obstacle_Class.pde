class Obstacle 
{   
    PVector position;

    color col; 

    Obstacle (float x, float y)
    {
        position = new PVector(0, 0);
        col = color(#bf0a22);
        position.x = x;
        position.y = y;
    }

    void drawObstacle()
    {   
        fill(col);
        rect(position.x, position.y, 10, 10);
    }
}